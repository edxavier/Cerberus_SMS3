package com.edxavier.cerberus_sms.db.realm;

import com.edxavier.cerberus_sms.helpers.UUIDGenerator;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Eder Xavier Rojas on 04/09/2017.
 */

public class PendingNotifications extends RealmObject {
    @PrimaryKey
    public int id;
    public String phone_number;

    public PendingNotifications() {
     }
    public PendingNotifications(int id, String phone_number) {
        this.id = id;
        this.phone_number = phone_number;
    }

}
