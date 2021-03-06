package com.edxavier.cerberus_sms.fragments.messages.contracts;

/**
 * Created by Eder Xavier Rojas on 20/07/2016.
 */
public interface MessagesPresenter {
    void onResume();
    void onPause();
    void onDestroy();
    boolean hasReadSMSPermission();

    void getMessagesFromRealm();
    void syncMessages();
    int getUnreadedMsgs();
    int getUnreadedMsgs(String number);
    int getFailedMsgs(String number);
    void sendToBlackList(int options, String number);

    void clearRecords();
    int clearPhoneReacords();
    int clearContactReacords(String number);

    void markAllAsRead();
}
