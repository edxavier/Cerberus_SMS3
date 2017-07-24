package com.edxavier.cerberus_sms.fragments.callLog.contracts;

/**
 * Created by Eder Xavier Rojas on 18/07/2016.
 */
public interface CallLogPresenter {
    void onResume();
    void onPause();
    void onDestroy();
    boolean hasReadCalllogPermission();

    void getCallsFromRealm();
    void syncCalls();
    void clearRecords();
    boolean clearPhoneReacords();
    boolean clearContactReacords(String number);


}
