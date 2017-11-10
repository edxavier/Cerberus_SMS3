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
    public RealmList<CallsHistoryRealm> entries;
    public Date last_update;


    public CallsRealm() {
        this.id = UUIDGenerator.nextUUID();
    }

    public CallsRealm(ContactRealm contact, String call_phone_number) {
        this.id = UUIDGenerator.nextUUID();
        this.contact = contact;
        this.call_phone_number = call_phone_number;
    }

    public static String getId() {
        return UUIDGenerator.nextUUID();
    }



}
