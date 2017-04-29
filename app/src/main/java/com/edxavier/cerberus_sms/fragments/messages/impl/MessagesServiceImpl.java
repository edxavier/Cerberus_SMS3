package com.edxavier.cerberus_sms.fragments.messages.impl;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesService;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.Utils;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;

import java.util.ArrayList;
import java.util.Date;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 20/07/2016.
 */
public class MessagesServiceImpl implements MessagesService {
    private Context context;
    private EventBusIface eventBus;
    private Realm realmG;

    public MessagesServiceImpl(Context context, EventBusIface eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        this.realmG = Realm.getDefaultInstance();
    }

    @Override
    public boolean hasReadSMSPermission() {
        int hasPerm = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            hasPerm = context.getPackageManager().checkPermission(Manifest.permission.READ_SMS,
                    context.getPackageName());
        }
        return hasPerm == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void syncMessagesToRealm() {
        Flowable.fromCallable(() -> {
            Realm realm = Realm.getDefaultInstance();
            Uri allSmsURI = Uri.parse("content://sms/");
            ArrayList<String> numbers = new ArrayList<>();
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(allSmsURI, null, null, null, "date asc");
            while (cursor != null && cursor.moveToNext()){
                //String service_center = cursor.getString(cursor.getColumnIndex("service_center"));
                String number = cursor.getString(cursor.getColumnIndex("address"));
                //String id = cursor.getString(cursor.getColumnIndex("_id"));
                String read = cursor.getString(cursor.getColumnIndex("read"));
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                Date fecha = new Date(date);
                String body = cursor.getString(cursor.getColumnIndex("body"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                number = Utils.formatPhoneNumber(number);
                boolean isNumber = Utils.isPhoneNumber(number);
                boolean isMarketNum = !number.startsWith("+505") && number.length() <= 5;
                boolean isMarketNum2 = number.startsWith("+505") && number.replaceAll("\\s+","").trim().length() <= 8;
                //Ignorar lo que no paresca un numero telefonico
                if(!(isMarketNum || isMarketNum2) && isNumber) {
                    AreaCodeRealm areaCodeRealm = Utils.getOperadoraV4(number, context);
                    String operator = "";
                    if (areaCodeRealm != null)
                        operator = areaCodeRealm.area_operator;
                    else
                        operator = Constans.DESCONOCIDO;
                    numbers.add(number);
                    ContactRealm contact = Utils.getContact(number);
                    RealmResults<MessagesRealm> messagesRealms = realm.where(MessagesRealm.class)
                            .equalTo("sms_phone_number", number).findAllSorted("sms_date", Sort.DESCENDING);
                    MessagesRealm theMessage = null;
                    if (!messagesRealms.isEmpty()) {
                        theMessage = messagesRealms.first();
                    }
                    if (theMessage == null) {
                        String finalOperator = operator;
                        String finalNumber = number;
                        realm.executeTransaction(realm1 -> {
                            realm1.copyToRealm(new MessagesRealm(contact, finalNumber, finalOperator,
                                    body, type, read, fecha, 1));
                            realm1.copyToRealm(new MessagesHistoryRealm(contact, finalNumber,
                                    finalOperator, body, type, read, fecha));
                        });
                    } else {
                        if (theMessage.sms_date.before(fecha)) {
                            String finalOperator1 = operator;
                            String finalNumber1 = number;
                            MessagesRealm finalTheMessage = theMessage;
                            realm.executeTransaction(realm1 -> {
                                realm1.copyToRealm(new MessagesHistoryRealm(contact, finalNumber1,
                                        finalOperator1, body, type, read, fecha));
                                finalTheMessage.contact = contact;
                                finalTheMessage.sms_phone_number = finalNumber1;
                                finalTheMessage.sms_text = body;
                                finalTheMessage.sms_date = fecha;
                                finalTheMessage.sms_operator = finalOperator1;
                                finalTheMessage.sms_type = type;
                                finalTheMessage.sms_read = read;
                                finalTheMessage.sms_count++;
                            });
                        } else {
                            String finalOperator2 = operator;
                            MessagesRealm finalTheMessage1 = theMessage;
                            realm.executeTransaction(realm1 -> {
                                finalTheMessage1.contact = contact;
                                finalTheMessage1.sms_operator = finalOperator2;
                            });
                        }
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }

            if(!numbers.isEmpty()) {
                String[] nums = new String[numbers.size()];
                nums = numbers.toArray(nums);
                RealmResults<MessagesRealm> res = realm.where(MessagesRealm.class).not()
                        .in("sms_phone_number", nums)
                        .findAll();
                RealmResults<MessagesHistoryRealm> res2 = realm.where(MessagesHistoryRealm.class).not()
                        .in("sms_phone_number", nums)
                        .findAll();  //Log.e("EDER", String.valueOf(res.size()) + " contacts removed from realm");
                realm.executeTransaction(realm1 -> {
                    res.deleteAllFromRealm();
                    res2.deleteAllFromRealm();
                });
            }else {
                realm.executeTransaction(realm1 -> {
                    realm1.delete(MessagesRealm.class);
                    realm1.delete(MessagesHistoryRealm.class);
                });
            }
            realm.close();
            return "";
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                            },
                        throwable -> {Log.e("EDER", "SMS_SYNC ERROR " + throwable.getMessage());});

    }

    @Override
    public RealmResults<MessagesRealm> getMessagesFromRealm() {
        return realmG.where(MessagesRealm.class).findAllSorted("sms_date", Sort.DESCENDING);
    }

    @Override
    public void onDestroy() {
        Log.e("EDER", "Close sms real");
        realmG.close();
    }

}
