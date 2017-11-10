package com.edxavier.cerberus_sms.activities.conversation.contracts;

import com.edxavier.cerberus_sms.db.realm.BlackList;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 04/09/2017.
 */

public interface ConversationService {
    RealmResults<MessagesHistoryRealm> getMessages(String number);
    void setMessagesAsReaded(String number);
    void disableNotifications(boolean disable, String number);
    void sendToBlackList(int options, String number);
    void onDestroy();

}
