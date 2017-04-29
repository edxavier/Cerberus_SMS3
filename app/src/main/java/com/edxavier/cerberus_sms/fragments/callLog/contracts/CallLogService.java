package com.edxavier.cerberus_sms.fragments.callLog.contracts;

import com.edxavier.cerberus_sms.db.realm.CallsRealm;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 18/07/2016.
 */
public interface CallLogService {
    boolean hasReadCalllogPermission();

    void syncCallsToRealm();
    RealmResults<CallsRealm> getCallsFromRealm();
    void onDestroy();
}
