package com.edxavier.cerberus_sms.fragments.callLog.contracts;

/**
 * Created by Eder Xavier Rojas on 18/07/2016.
 */
public interface CallLogPresenter {
    void onResume();
    void onPause();
    void onDestroy();
    boolean hasReadCalllogPermission();
    boolean hasWriteCallLogPermission();

    void getCallsFromRealm();
    void syncCalls();
    void clearRecords();
    int clearPhoneReacords();
    int clearContactReacords(String number);


    void requestPermission(String [] perms);
    void sendToBlackList(int options, String number);
}
