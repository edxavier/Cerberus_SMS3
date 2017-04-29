package com.edxavier.cerberus_sms.db.realm;

import android.util.Log;

import com.edxavier.cerberus_sms.helpers.UUIDGenerator;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

// Created by Eder Xavier Rojas on 12/11/2016.

public class ContactRealm extends RealmObject {
    @PrimaryKey
    String id;
    public String contact_name;
    @Index
    public String contact_phone_number;
    @Index
    public String contact_operator;
    public String contact_photo_uri;
    public String contact_last_update;


    public ContactRealm() {
        this.id = UUIDGenerator.nextUUID();
    }

    public static void removeContact(String[] phone_contacts){
        //eliminar los cotnactos que estan en realm y no en el directorio telefonico
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ContactRealm> res = realm.where(ContactRealm.class).not()
                .in("contact_phone_number", phone_contacts)
                .findAll();
        //Log.e("EDER", String.valueOf(res.size()) + " contacts removed from realm");
        realm.executeTransaction(realm1 -> {
            res.deleteAllFromRealm();
        });
        realm.close();
    }

    public static boolean existContact(ContactRealm contact_){
        //eliminar los cotnactos que estan en realm y no en el directorio telefonico
        Realm realm = Realm.getDefaultInstance();
        ContactRealm contact = realm.where(ContactRealm.class)
                .equalTo("contact_phone_number", contact_.contact_phone_number)
                .findFirst();
        if(contact!=null){
            //si el contacto has sido modificado actualizar la info
            if(contact.contact_last_update != null && contact_.contact_last_update !=null) {
                if (!contact.contact_last_update.equals(contact_.contact_last_update)) {
                    realm.executeTransaction(realm1 -> {
                        contact.contact_last_update = contact_.contact_last_update;
                        contact.contact_name = contact_.contact_name;
                        contact.contact_photo_uri = contact_.contact_photo_uri;
                        contact.contact_operator = contact_.contact_operator;
                    });
                }
            }else {
                if(contact_.contact_last_update !=null) {
                    realm.executeTransaction(realm1 -> {
                        contact.contact_last_update = contact_.contact_last_update;
                        contact.contact_name = contact_.contact_name;
                        contact.contact_operator = contact_.contact_operator;
                        contact.contact_photo_uri = contact_.contact_photo_uri;
                    });
                }
            }
        }
        realm.close();
        //Log.e("EDER_exist", String.valueOf(exist));
        return contact != null;
    }


}
