package com.edxavier.cerberus_sms.activities.conversation.contracts;

import com.edxavier.cerberus_sms.db.realm.BlackList;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 05/09/2017.
 */

public class ConversationPresenterImp implements ConversationPresenter {
    private ConversationView view;
    private ConversationService service;

    public ConversationPresenterImp(ConversationView view) {
        this.view = view;
        this.service = new ConversationServiceImpl();
    }

    @Override
    public void getMessages(String number) {
        RealmResults<MessagesHistoryRealm> res = this.service.getMessages(number);
        if(res.isEmpty())
            view.showEmptyMsg(true);
        else {
            view.showEmptyMsg(false);
            view.setMessages(res);
        }
    }

    @Override
    public void setMessagesAsReaded(String number) {
        service.setMessagesAsReaded(number);
    }

    @Override
    public void disableNotifications(boolean disable, String num) {
        service.disableNotifications(disable, num);
    }

    @Override
    public void sendToBlackList(int options, String number) {
        service.sendToBlackList(options, number);
    }

    @Override
    public void onDestroy() {
        service.onDestroy();
    }
}
