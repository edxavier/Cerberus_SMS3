package com.edxavier.cerberus_sms.db.realm;

import com.edxavier.cerberus_sms.helpers.UUIDGenerator;

import java.util.Date;

import io.realm.Realm;
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
    @Index
    public String sms_operator;
    public String sms_text;
    public int sms_type;
    public String sms_read;
    public Date sms_date;
    public int sms_count;


    public MessagesRealm() {
        this.id = UUIDGenerator.nextUUID();
    }

    public MessagesRealm(ContactRealm contact, String sms_phone_number, String sms_operator, String sms_text, int sms_type, String sms_read, Date sms_date, int sms_count) {
        this.contact = contact;
        this.sms_phone_number = sms_phone_number;
        this.sms_operator = sms_operator;
        this.sms_text = sms_text;
        this.sms_type = sms_type;
        this.sms_read = sms_read;
        this.sms_date = sms_date;
        this.sms_count = sms_count;
        this.id = UUIDGenerator.nextUUID();
    }
}
