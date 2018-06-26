package com.edxavier.cerberus_sms.db.realm;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by Eder Xavier Rojas on 28/03/2017.
 */

public class MigrationRealm implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 1) {
            //schema.get("ContactRealm")
                    //.addField("contact_last_update", String.class);
               //     .addIndex("contact_phone_number");
            //oldVersion++;
        }


    }
}
