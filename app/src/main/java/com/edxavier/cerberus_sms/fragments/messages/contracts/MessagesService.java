package com.edxavier.cerberus_sms.fragments.messages.contracts;

import com.edxavier.cerberus_sms.db.realm.MessagesRealm;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 20/07/2016.
 */
public interface MessagesService {

    boolean hasReadSMSPermission();

    void syncMessagesToRealm();
    RealmResults<MessagesRealm> getMessagesFromRealm();
    void onDestroy();
    int getUnreadedMsgs();
    int getUnreadedMsgs(String number);
    int getFailedMsgs(String number);

    void sendToBlackList(int options, String number);

    void clearRecords();
    int clearPhoneReacords();
    int clearContactReacords(String number);
    void markAllAsRead();

}
