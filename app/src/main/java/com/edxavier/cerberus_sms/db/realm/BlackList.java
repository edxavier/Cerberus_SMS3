package com.edxavier.cerberus_sms.db.realm;

import com.edxavier.cerberus_sms.helpers.UUIDGenerator;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Eder Xavier Rojas on 04/09/2017.
 */

public class BlackList extends RealmObject {
    @PrimaryKey
    private String id;
    @Index
    public String phone_number;
    public boolean block_incoming_sms;
    public boolean block_incoming_call;
    public int block_sms_count = 0;
    public int block_calls_count = 0;
    public boolean notification_disabled = false;


    public BlackList() {
        this.id = UUIDGenerator.nextUUID();
    }

}
