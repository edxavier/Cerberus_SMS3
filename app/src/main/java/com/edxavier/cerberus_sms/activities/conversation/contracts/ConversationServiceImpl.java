package com.edxavier.cerberus_sms.activities.conversation.contracts;

import android.util.Log;

import com.edxavier.cerberus_sms.db.realm.BlackList;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.Notifications;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.Utils;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 05/09/2017.
 */

public class ConversationServiceImpl implements ConversationService {
    private Realm realm;

    ConversationServiceImpl() {
        this.realm =  Realm.getDefaultInstance();
    }

    @Override
    public RealmResults<MessagesHistoryRealm> getMessages(String number) {
        //Realm realm = Realm.getDefaultInstance();
        RealmResults<MessagesHistoryRealm> res = realm.where(MessagesHistoryRealm.class)
                .equalTo("sms_phone_number", number)
                .findAll()
                .sort("sms_date", Sort.DESCENDING);
        //realm.close();
        return res;
    }

    @Override
    public void setMessagesAsReaded(String number) {
        //Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<MessagesHistoryRealm> notificationOrders = realm
                    .where(MessagesHistoryRealm.class)
                    .equalTo("sms_phone_number", number)
                    .equalTo("sms_read", Constans.MESSAGE_UNREAD)
                    .findAll();
            for(MessagesHistoryRealm order : notificationOrders) {
                order.sms_read = Constans.MESSAGE_READ;
            }
        });
        //realm.close();
    }

    @Override
    public void disableNotifications(boolean disable, String number) {
        //Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<Notifications> entries = realm1.where(Notifications.class).equalTo("phone_number", number).findAll();
            if(disable){
                if(entries.isEmpty()) {
                    Notifications notifications = new Notifications();
                    notifications.phone_number = number;
                    realm1.copyToRealm(notifications);
                }
            }else {
                if(!entries.isEmpty())
                    entries.deleteAllFromRealm();
            }
        });
        //realm.close();

    }

    @Override
    public void sendToBlackList(int options, String number) {
        Utils.sendToBlackList(options, number);
    }

    @Override
    public void onDestroy() {
        if(!realm.isClosed())
            realm.close();
    }
}
