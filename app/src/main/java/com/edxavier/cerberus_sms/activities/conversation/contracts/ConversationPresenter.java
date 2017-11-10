package com.edxavier.cerberus_sms.activities.conversation.contracts;

/**
 * Created by Eder Xavier Rojas on 04/09/2017.
 */

public interface ConversationPresenter {
    void getMessages(String number);
    void setMessagesAsReaded(String number);
    void disableNotifications(boolean disable, String number);
    void sendToBlackList(int options, String number);
    void onDestroy();
}
