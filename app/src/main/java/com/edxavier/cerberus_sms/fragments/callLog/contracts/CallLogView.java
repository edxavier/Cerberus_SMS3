package com.edxavier.cerberus_sms.fragments.callLog.contracts;

import com.edxavier.cerberus_sms.db.realm.CallsRealm;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 18/07/2016.
 */
public interface CallLogView {
    void setupInjection();
    void loadCalllog();
    void setupAds();
    void showEmptyMsg(boolean show);
    void showProgress(boolean show);

    void setCallsData(RealmResults<CallsRealm> callsData);

    void requestPermission(String [] perms);

}
