package com.edxavier.cerberus_sms.db.realm;

import com.edxavier.cerberus_sms.helpers.UUIDGenerator;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Eder Xavier Rojas on 04/09/2017.
 */

public class Notifications extends RealmObject {
    @PrimaryKey
    private String id;
    @Index
    public String phone_number;
    public boolean notification_disabled = false;


    public Notifications() {
        this.id = UUIDGenerator.nextUUID();
    }

}
