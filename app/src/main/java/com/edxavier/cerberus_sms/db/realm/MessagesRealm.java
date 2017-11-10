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

public class MessagesRealm extends RealmObject {
    @PrimaryKey
    String id;
    public ContactRealm contact;
    @Index
    public String sms_phone_number;
    public RealmList<MessagesHistoryRealm> entries;
    public Date last_update;



    public MessagesRealm() {
        this.id = UUIDGenerator.nextUUID();
    }

    public MessagesRealm(ContactRealm contact, String sms_phone_number) {
        this.contact = contact;
        this.sms_phone_number = sms_phone_number;
        this.id = UUIDGenerator.nextUUID();
    }

    public static String getId() {
        return UUIDGenerator.nextUUID();
    }


}
