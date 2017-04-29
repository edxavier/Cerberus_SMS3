package com.edxavier.cerberus_sms.fragments.contacts.contracts;

import com.edxavier.cerberus_sms.db.realm.ContactRealm;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 07/07/2016.
 */
public interface ContactsView {
    void showEmptyListMessage(boolean show);
    void setContacts(RealmResults<ContactRealm> contacts);

}
