package com.edxavier.cerberus_sms.fragments.messages.impl;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.CallsHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.CallsRealm;
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

import static android.R.attr.duration;

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
                int id = Integer.valueOf(cursor.getString(cursor.getColumnIndex("_id")));
                int read = Integer.valueOf(cursor.getString(cursor.getColumnIndex("read")));
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                Date fecha = new Date(date);
                String body = cursor.getString(cursor.getColumnIndex("body"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                number = Utils.formatPhoneNumber(number);
                boolean isNumber = Utils.isPhoneNumber(number);
                boolean isMarketNum = !number.startsWith("+505") && number.length() <= 5;
                boolean isMarketNum2 = number.startsWith("+505") && number.replaceAll("\\s+","").trim().length() <= 8;
                AreaCodeRealm areaCodeRealm = Utils.getOperadoraV4(number, context);
                String operator = "";
                if (areaCodeRealm != null)
                    operator = areaCodeRealm.area_operator;
                else
                    operator = Constans.DESCONOCIDO;
                numbers.add(number);
                ContactRealm contact = Utils.getContact(number);
                //Ignorar lo que no paresca un numero telefonico
                //!(isMarketNum || isMarketNum2) && isNumber
                if(true) {
                    String finalNumber = number;
                    String finalOperator = operator;
                    realm.executeTransaction(realm_trans -> {
                        //Cargar el historial para el numero en cuestion
                        RealmResults<MessagesHistoryRealm> msgs = realm.where(MessagesHistoryRealm.class)
                                .equalTo("sms_phone_number", finalNumber).findAllSorted("sms_date", Sort.DESCENDING);
                        MessagesHistoryRealm lastSMS = null;
                        if(msgs.isEmpty()) {
                            //+++++++++++++++++++++++++++++++++++++++++++++++PRIMER REGISTRO PARA UN NUMERO+++++++++++++++++++++++++++++++++++++++++++++++++++
                            //Ya que no hay ingresar el primer registro al historial
                            lastSMS = realm_trans.copyToRealm(new MessagesHistoryRealm(contact, finalNumber,
                                    finalOperator, body, type, read, fecha));
                            //Ya que no hay ingresar el registro al resumen
                            MessagesRealm msgsResume = realm_trans.createObject(MessagesRealm.class, MessagesRealm.getId());
                            msgsResume.contact =  Utils.getContact(finalNumber);
                            msgsResume.sms_phone_number = finalNumber;
                            if (lastSMS != null) {
                                msgsResume.entries.add(lastSMS);
                                msgsResume.last_update = fecha;
                            }
                            //realm_trans.copyToRealm(msgsResume);
                            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        }else {
                            lastSMS = msgs.first();
                            if (lastSMS.sms_date.before(fecha)) {
                                lastSMS = realm_trans.copyToRealm(new MessagesHistoryRealm(contact, finalNumber,
                                        finalOperator, body, type, read, fecha));

                                MessagesRealm mResume = realm_trans.where(MessagesRealm.class).equalTo("sms_phone_number", finalNumber).findFirst();
                                if(mResume!=null) {
                                    mResume.entries.add(lastSMS);
                                    mResume.last_update = fecha;
                                }
                            }
                        }
                    });

                }
            }
            if (cursor != null) {
                cursor.close();
            }
            /*
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
            }*/
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
        return realmG.where(MessagesRealm.class).findAllSorted("last_update", Sort.DESCENDING);
    }

    @Override
    public void onDestroy() {
        realmG.close();
    }

    @Override
    public int getUnreadedMsgs() {
        return (int) realmG.where(MessagesHistoryRealm.class)
                .equalTo("sms_read", Constans.MESSAGE_UNREAD).count();
    }
    @Override
    public int getUnreadedMsgs(String number) {
        return (int) realmG.where(MessagesHistoryRealm.class)
                .equalTo("sms_phone_number", number)
                .equalTo("sms_read", Constans.MESSAGE_UNREAD).count();
    }

    @Override
    public int getFailedMsgs(String number) {
        return (int) realmG.where(MessagesHistoryRealm.class)
                .equalTo("sms_phone_number", number)
                .equalTo("sms_type", Constans.MESSAGE_TYPE_FAILED).count();
    }

    @Override
    public void sendToBlackList(int options, String number) {
        Utils.sendToBlackList(options, number);
    }

    @Override
    public void clearRecords() {
        realmG.executeTransaction(realm -> {
            realm.delete(MessagesHistoryRealm.class);
            realm.delete(MessagesRealm.class);
        });
    }

    @Override
    public int clearPhoneReacords() {
        //delete all sms
        Uri inboxUri = Uri.parse("content://sms/");
        return context.getContentResolver().delete(inboxUri, Telephony.Sms._ID + "!=?", new String[]{"0"});
    }

    @Override
    public int clearContactReacords(String number) {

            Uri inboxUri = Uri.parse("content://sms/");
            int tot = context.getContentResolver().delete(inboxUri,  "address=?", new String[]{number});
            if(tot<=0){
                tot = context.getContentResolver().delete(inboxUri,
                        "address=?", new String[]{ number.replaceAll("\\s+","")});
            }
            if(tot>0) {
                realmG.executeTransaction(realm1 -> {
                    RealmResults<MessagesHistoryRealm> hist = realm1.where(MessagesHistoryRealm.class).equalTo("sms_phone_number", number).findAll();
                    RealmResults<MessagesRealm> resume = realm1.where(MessagesRealm.class).equalTo("sms_phone_number", number).findAll();
                    hist.deleteAllFromRealm();
                    resume.deleteAllFromRealm();
                });
            }

        return tot;
    }

    @Override
    public void markAllAsRead() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<MessagesHistoryRealm> notificationOrders = realm
                    .where(MessagesHistoryRealm.class)
                    .equalTo("sms_read", Constans.MESSAGE_UNREAD)
                    .findAll();
            for(MessagesHistoryRealm order : notificationOrders) {
                order.sms_read = Constans.MESSAGE_READ;
            }
        });
        realm.close();

    }
}
