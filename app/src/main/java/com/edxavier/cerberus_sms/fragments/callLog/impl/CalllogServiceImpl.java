package com.edxavier.cerberus_sms.fragments.callLog.impl;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.BlackList;
import com.edxavier.cerberus_sms.db.realm.CallsHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.CallsRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogService;
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
 * Created by Eder Xavier Rojas on 18/07/2016.
 */
public class CalllogServiceImpl implements CallLogService {
    private Context context;
    private Realm realmG;

    public CalllogServiceImpl(EventBusIface eventBus, Context context) {
        this.context = context;
        this.realmG = Realm.getDefaultInstance();
    }

    @Override
    public boolean hasReadCalllogPermission() {
        int hasPerm = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            hasPerm = context.getPackageManager().checkPermission(Manifest.permission.READ_CALL_LOG,
                    context.getPackageName());
        }
        return hasPerm == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean hasWriteCallLogPermission() {
        int hasPerm = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            hasPerm = context.getPackageManager().checkPermission(Manifest.permission.WRITE_CALL_LOG,
                    context.getPackageName());
        }
        return hasPerm == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void syncCallsToRealm() {
        Flowable.fromCallable(() -> {
            Realm realm = Realm.getDefaultInstance();

            ArrayList<String> numbers = new ArrayList<>();

            ContentResolver cr = context.getContentResolver();
            String strOrder = CallLog.Calls.DATE + " ASC";
            Uri callUri = Uri.parse("content://call_log/calls");
            Cursor cur = cr.query(callUri, null, null, null, strOrder);
            // loop through cursor
            while (cur != null && cur.moveToNext()) {
                String callNumber = cur.getString(cur
                        .getColumnIndex(CallLog.Calls.NUMBER));
                long callDate = cur.getLong(cur
                        .getColumnIndex(CallLog.Calls.DATE));
                Date fecha = new Date(callDate);
                int callType = cur.getInt(cur
                        .getColumnIndex(CallLog.Calls.TYPE));
                int duration = cur.getInt(cur
                        .getColumnIndex(CallLog.Calls.DURATION));

                // dar formato al numero
                callNumber = Utils.formatPhoneNumber(callNumber);
                //Obtener operador
                AreaCodeRealm areaCodeRealm = Utils.getOperadoraV4(callNumber, context);
                String operator = "";
                if (areaCodeRealm != null)
                    operator = areaCodeRealm.area_operator;
                else
                    operator = Constans.DESCONOCIDO;
                //agregar numero a una lista
                numbers.add(callNumber);
                //verificar si existe un contacto con ese numero
                ContactRealm contact = Utils.getContact(callNumber);
                //obtener todas las llamadas de ese numero
                RealmResults<CallsRealm> callsR = realm.where(CallsRealm.class)
                        .equalTo("call_phone_number", callNumber).findAllSorted("call_date", Sort.DESCENDING);
                CallsRealm callLog = null;
                //si hay resultados obtener el mas reciente
                if (!callsR.isEmpty()) {
                    callLog = callsR.first();
                }
                // si no hubo resultado guardarlo
                if (callLog == null) {
                    String finalCallNumber = callNumber;
                    String finalOperator = operator;
                    realm.executeTransaction(realm1 -> {
                        realm1.copyToRealm(new CallsRealm(contact, finalCallNumber, duration,
                                callType, fecha, 1, finalOperator));
                        realm1.copyToRealm(new CallsHistoryRealm(contact, finalCallNumber, duration,
                                callType, fecha, finalOperator));
                    });
                } else {
                    //si hubo resultados verificar si es mas antigua que la entrada del registro del telefono
                    if (callLog.call_date.before(fecha)) {
                        String finalCallNumber1 = callNumber;
                        CallsRealm finalCallLog = callLog;
                        String finalOperator1 = operator;
                        //guardar el registro en el historico y actualizar el resumen
                        realm.executeTransaction(realm1 -> {
                            realm1.copyToRealm(new CallsHistoryRealm(contact, finalCallNumber1,
                                    duration, callType, fecha, finalOperator1));
                            finalCallLog.contact = contact;
                            finalCallLog.call_phone_number = finalCallNumber1;
                            finalCallLog.call_duration = duration;
                            finalCallLog.call_direction = callType;
                            finalCallLog.call_date = fecha;
                            finalCallLog.calls_count++;
                            finalCallLog.call_operator = finalOperator1;
                        });

                    } else {
                        //si no solo actualiza datos de contact y oeprador
                        CallsRealm finalCallLog1 = callLog;
                        String finalOperator2 = operator;
                        realm.executeTransaction(realm1 -> {
                            finalCallLog1.contact = contact;
                            finalCallLog1.call_operator = finalOperator2;
                        });
                    }
                }
            }
            if (cur != null) {
                cur.close();
            }
            //si habian registros de llamadas verificar si en realm tenemos alguno que ya no esten en el telefono
            if (!numbers.isEmpty()) {
                String[] nums = new String[numbers.size()];
                nums = numbers.toArray(nums);
                RealmResults<CallsRealm> res = realm.where(CallsRealm.class).not()
                        .in("call_phone_number", nums)
                        .findAll();
                RealmResults<CallsHistoryRealm> res2 = realm.where(CallsHistoryRealm.class).not()
                        .in("call_phone_number", nums)
                        .findAll();  //Log.e("EDER", String.valueOf(res.size()) + " contacts removed from realmG");
                //eliminar los numeros que estan en la app pero no en el telefono
                realm.executeTransaction(realm1 -> {
                    res.deleteAllFromRealm();
                    res2.deleteAllFromRealm();
                });
            } else {
                //si no hay nada el el reg del telefono limpiar los de la app
                realm.executeTransaction(realm1 -> {
                    realm1.delete(CallsHistoryRealm.class);
                    realm1.delete(CallsRealm.class);
                });
            }
            realm.close();
            return "";
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                            //Log.e("EDER", "CALLS_SYNC DONE ");
                        },
                        throwable -> {
                            Log.e("EDER", "CALLS_SYNC ERROR " + throwable.getMessage());
                        });

    }

    @Override
    public RealmResults<CallsRealm> getCallsFromRealm() {
        //Log.e("EDER_H", String.valueOf(realmG.where(CallsHistoryRealm.class).count()));
        return realmG.where(CallsRealm.class)
                .findAllSorted("call_date", Sort.DESCENDING);

    }

    @Override
    public void onDestroy() {
        if (this.realmG != null)
            this.realmG.close();
    }

    @Override
    public void clearRecords() {
        realmG.executeTransaction(realm -> {
            realm.delete(CallsHistoryRealm.class);
            realm.delete(CallsRealm.class);
        });
    }

    @Override
    public int clearPhoneReacords() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            int dlt = context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, null, null);
            clearRecords();
            return dlt;
        }else
            return -1;
    }

    @Override
    public int clearContactReacords(String number) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            int tot = context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls.NUMBER +"=?", new String[]{ number});
            if(tot<=0){
                tot = context.getContentResolver().delete(CallLog.Calls.CONTENT_URI,
                        CallLog.Calls.NUMBER +"=?", new String[]{ number.replaceAll("\\s+","")});
            }
            if(tot>0) {
                realmG.executeTransaction(realm -> {
                    RealmResults<CallsRealm> res = realm.where(CallsRealm.class)
                            .equalTo("call_phone_number", number)
                            .findAll();
                    RealmResults<CallsHistoryRealm> res2 = realm.where(CallsHistoryRealm.class)
                            .equalTo("call_phone_number", number)
                            .findAll();
                    res.deleteAllFromRealm();
                    res2.deleteAllFromRealm();
                });
            }
            return tot;
        }else
            return -1;
    }

    @Override
    public void addToBlackList(Integer[] which, String number) {
        realmG.executeTransaction(realm -> {
            BlackList entry = realm.where(BlackList.class).equalTo("phone_number", number).findFirst();
            if(entry == null)
                entry = new BlackList();
            entry.block_incoming_sms = false;
            entry.block_incoming_call = false;

            for (Integer integer : which) {
                if(integer==0)
                    entry.block_incoming_sms = true;
                else
                    entry.block_incoming_call = true;
            }
            entry.phone_number = number;
            realm.copyToRealmOrUpdate(entry);
        });
    }

}
