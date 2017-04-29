package com.edxavier.cerberus_sms.fragments.checkOperator.contracts;

import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;

/**
 * Created by Eder Xavier Rojas on 07/07/2016.
 */
public interface CheckOperatorService {
    void onDestroy();
    AreaCodeRealm checkOperator(String phoneNumber);
    boolean checkDBinitialization();
    void dbInitialization();
    public void InitializeRealmAreacodes();

    boolean isReadContactsPermsGranted();
    void syncronizeContacts();



}
