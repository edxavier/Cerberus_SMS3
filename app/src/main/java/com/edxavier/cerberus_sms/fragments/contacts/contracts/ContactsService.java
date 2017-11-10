package com.edxavier.cerberus_sms.fragments.contacts.contracts;

import com.edxavier.cerberus_sms.db.realm.ContactRealm;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 07/07/2016.
 */
public interface ContactsService {

    void syncContactsToRealm();
    RealmResults<ContactRealm> getContactsFromRealm();
    RealmResults<ContactRealm> getFilterContactsFromRealm(String query);

    boolean canReadContacts();

    void onDestroy();
    void sendToBlackList(int options, String number);

}
