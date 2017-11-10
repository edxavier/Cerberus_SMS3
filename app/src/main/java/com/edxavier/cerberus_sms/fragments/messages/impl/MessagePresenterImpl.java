package com.edxavier.cerberus_sms.fragments.messages.impl;

import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesPresenter;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesService;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesView;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 21/07/2016.
 */
public class MessagePresenterImpl implements MessagesPresenter {
    private MessagesService service;
    private MessagesView view;

    public MessagePresenterImpl(EventBusIface eventBus, MessagesService service, MessagesView view) {
        this.service = service;
        this.view = view;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {
        view = null;
        service.onDestroy();
    }

    @Override
    public boolean hasReadSMSPermission() {
        return service.hasReadSMSPermission();
    }


    @Override
    public void getMessagesFromRealm() {
        RealmResults<MessagesRealm> res = service.getMessagesFromRealm();
        view.setMessages(res);
        if(res.isEmpty())
            view.showEmptyMsg(true);
        else
            view.showEmptyMsg(false);
    }

    @Override
    public void syncMessages() {
        service.syncMessagesToRealm();
    }

    @Override
    public int getUnreadedMsgs() {
        return service.getUnreadedMsgs();
    }

    @Override
    public int getUnreadedMsgs(String number) {
        return service.getUnreadedMsgs(number);
    }

    @Override
    public int getFailedMsgs(String number) {
        return service.getFailedMsgs(number);
    }

    @Override
    public void sendToBlackList(int options, String number) {
        service.sendToBlackList(options, number);
    }

    @Override
    public void clearRecords() {
        service.clearRecords();
    }

    @Override
    public int clearPhoneReacords() {
        return service.clearPhoneReacords();
    }

    @Override
    public int clearContactReacords(String number) {
        return service.clearContactReacords(number);
    }

    @Override
    public void markAllAsRead() {
        service.markAllAsRead();
    }
}
