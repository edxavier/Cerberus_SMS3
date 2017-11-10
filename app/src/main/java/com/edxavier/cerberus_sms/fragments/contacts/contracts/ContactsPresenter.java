package com.edxavier.cerberus_sms.fragments.contacts.contracts;

/**
 * Created by Eder Xavier Rojas on 07/07/2016.
 */
public interface ContactsPresenter {
    void onResume();
    void onPause();
    void onDestroy();

    void getFilterContactsFromRealm(String query);
    boolean canReadContacts();
    void getContactsFromRealm();
    void syncContacts();
    void sendToBlackList(int options, String number);
}
