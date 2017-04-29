package com.edxavier.cerberus_sms.db.realm;

import com.edxavier.cerberus_sms.helpers.UUIDGenerator;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

// Created by Eder Xavier Rojas on 12/11/2016.

public class CallsRealm extends RealmObject {
    @PrimaryKey
    String id;
    public ContactRealm contact;
    @Index
    public String call_phone_number;
    @Index
    public String call_operator;
    public int call_duration;
    public int call_direction;
    public Date call_date;
    public int calls_count;


    public CallsRealm() {
        this.id = UUIDGenerator.nextUUID();
    }

    public CallsRealm(ContactRealm contact, String call_phone_number, int call_duration,
                      int call_direction, Date call_date, int calls_count, String call_operator) {
        this.id = UUIDGenerator.nextUUID();
        this.contact = contact;
        this.call_phone_number = call_phone_number;
        this.call_duration = call_duration;
        this.call_direction = call_direction;
        this.call_date = call_date;
        this.calls_count = calls_count;
        this.call_operator = call_operator;
    }

    public static void removeCall(String[] call_numbers){
        //eliminar los cotnactos que estan en realm y no en el directorio telefonico
        Realm realm = Realm.getDefaultInstance();
        RealmResults<CallsRealm> res = realm.where(CallsRealm.class).not()
                .in("call_phone_number", call_numbers)
                .findAll();
        //Log.e("EDER", String.valueOf(res.size()) + " contacts removed from realm");
        realm.executeTransaction(realm1 -> {
            res.deleteAllFromRealm();
        });
        realm.close();
    }

    public static long getCallsCount(String call_phone_number){
        //eliminar los cotnactos que estan en realm y no en el directorio telefonico
        Realm realm = Realm.getDefaultInstance();
        long callsRealm = realm.where(CallsRealm.class)
                .equalTo("call_phone_number", call_phone_number).count();
        realm.close();
        return callsRealm;
    }


}
