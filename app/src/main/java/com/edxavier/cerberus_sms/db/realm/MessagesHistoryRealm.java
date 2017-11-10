package com.edxavier.cerberus_sms.db.realm;

import com.edxavier.cerberus_sms.helpers.UUIDGenerator;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

// Created by Eder Xavier Rojas on 12/11/2016.

public class MessagesHistoryRealm extends RealmObject {
    @PrimaryKey
    public String id;
    public int sms_id;
    public ContactRealm contact;
    @Index
    public String sms_phone_number;
    public String sms_operator;
    public String sms_text;
    public int sms_type;
    public int sms_read;
    public Date sms_date;


    public MessagesHistoryRealm() {
        this.id = UUIDGenerator.nextUUID();
    }

    public MessagesHistoryRealm(ContactRealm contact, String sms_phone_number, String sms_operator, String sms_text, int sms_type, int sms_read, Date sms_date) {
        this.id = UUIDGenerator.nextUUID();
        this.contact = contact;
        this.sms_phone_number = sms_phone_number;
        this.sms_operator = sms_operator;
        this.sms_text = sms_text;
        this.sms_type = sms_type;
        this.sms_read = sms_read;
        this.sms_date = sms_date;
    }
}
