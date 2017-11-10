package com.edxavier.cerberus_sms.fragments.contacts.impl;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsService;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.Utils;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;

import java.util.ArrayList;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 09/07/2016.
 */
public class ContactsServiceImpl implements ContactsService {
    private EventBusIface eventBus;
    private Context context;
    Realm realmG;


    public ContactsServiceImpl(EventBusIface eventBus, Context context) {
        this.eventBus = eventBus;
        this.context = context;
        this.realmG = Realm.getDefaultInstance();
    }

    @Override
    public void syncContactsToRealm() {
        Flowable.fromCallable(() -> {
            Realm realm = Realm.getDefaultInstance();
            String temp_number="";
            ArrayList<ContactRealm> contact_list = new ArrayList<>();
            ArrayList<String> numbers = new ArrayList<>();

            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    String haveNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER));
                    if (Integer.parseInt(haveNumber) > 0) {
                        ContactRealm contacto = new ContactRealm();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            contacto.contact_last_update = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP));
                        }
                        contacto.contact_name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        contacto.contact_photo_uri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                        contacto.contact_phone_number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contacto.contact_phone_number = Utils.formatPhoneNumber(contacto.contact_phone_number);

                        AreaCodeRealm areaCodeRealm = Utils.getOperadoraV4(contacto.contact_phone_number, context);
                        String operator = "";
                        if(areaCodeRealm != null)
                            operator = areaCodeRealm.area_operator;
                        else
                            operator = Constans.DESCONOCIDO;
                        contacto.contact_operator = operator;
                        if(!contacto.contact_phone_number.equals(temp_number)) {
                            numbers.add(contacto.contact_phone_number);
                            if(!ContactRealm.existContact(contacto))
                                contact_list.add(contacto);
                            temp_number=contacto.contact_phone_number;
                        }
                    }
                }
                cursor.close();
                realm.executeTransactionAsync(realm1 -> {
                    realm1.copyToRealm(contact_list);
                });
                if(!numbers.isEmpty()) {
                    String[] nums = new String[numbers.size()];
                    nums = numbers.toArray(nums);
                    ContactRealm.removeContact(nums);
                }
            }
            realm.close();
            return "";
        }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(t -> {},
                        throwable -> {Log.e("EDER", "CONTACT_SYNC ERROR " + throwable.getMessage());});

    }

    @Override
    public RealmResults<ContactRealm> getContactsFromRealm() {
        return realmG.where(ContactRealm.class)
                .distinct("contact_phone_number").sort("contact_name", Sort.ASCENDING);
    }

    @Override
    public RealmResults<ContactRealm> getFilterContactsFromRealm(String query) {
        return realmG.where(ContactRealm.class)
                .beginGroup()
                    .contains("contact_phone_number", query, Case.INSENSITIVE)
                    .or()
                    .contains("contact_name", query, Case.INSENSITIVE)
                .endGroup()
                .distinct("contact_phone_number");
    }


    @Override
    public boolean canReadContacts() {
        int hasPerm = context.getPackageManager().checkPermission(Manifest.permission.READ_CONTACTS,
                        context.getPackageName());
        return hasPerm == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onDestroy() {
        realmG.close();
    }

    @Override
    public void sendToBlackList(int options, String number) {
        Utils.sendToBlackList(options, number);
    }

}

