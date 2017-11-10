package com.edxavier.cerberus_sms.activities.conversation.contracts;

import com.edxavier.cerberus_sms.db.realm.BlackList;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 04/09/2017.
 */

public interface ConversationView {
    void showEmptyMsg(boolean show);
    void setMessages(RealmResults<MessagesHistoryRealm> messages);
    void scrollTo(int pos);
}
