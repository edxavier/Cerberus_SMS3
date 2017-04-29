package com.edxavier.cerberus_sms.fragments.messages.contracts;

import com.edxavier.cerberus_sms.db.realm.MessagesRealm;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 20/07/2016.
 */
public interface MessagesView {
    void setupInjection();
    void loadSMS();
    void setupAds();
    void showEmptyMsg(boolean show);
    void showProgress(boolean show);

    void setMessages(RealmResults<MessagesRealm>messages);

}
