package com.edxavier.cerberus_sms.fragments.messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.Utils;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * Created by Eder Xavier Rojas on 24/10/2015.
 */
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String MSG_TYPE=intent.getAction();
        Log.e("EDER", MSG_TYPE);
        switch (MSG_TYPE) {
            case "android.provider.Telephony.SMS_RECEIVED":
                Bundle bundle = intent.getExtras();

                Object messages[] = (Object[]) bundle.get("pdus");
                SmsMessage smsMessage[] = new SmsMessage[0];
                if (messages != null) {
                    smsMessage = new SmsMessage[messages.length];
                }
                for (int n = 0; n < messages.length; n++) {
                    smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
                }
                String incomingNum = Utils.formatPhoneNumber(smsMessage[0].getOriginatingAddress());
                boolean isNumber = Utils.isPhoneNumber(incomingNum);
                boolean isMarketNum = !incomingNum.startsWith("+505") && incomingNum.length() <= 4;
                boolean isMarketNum2 = incomingNum.startsWith("+505") && incomingNum.replaceAll("\\s+","").trim().length() <= 8;
                //Ignorar lo que no paresca un numero telefonico
                if(!(isMarketNum || isMarketNum2) && isNumber) {
                    Realm realm = Realm.getDefaultInstance();
                    ContactRealm contact;
                    contact = Utils.getContact(incomingNum);
                    AreaCodeRealm areaCodeRealm = Utils.getOperadoraV4(incomingNum, context, realm);
                    String operator = "";
                    if (areaCodeRealm != null)
                        operator = areaCodeRealm.area_operator;
                    else
                        operator = Constans.DESCONOCIDO;
                    RealmResults<MessagesRealm> messagesRealms = realm.where(MessagesRealm.class)
                            .equalTo("sms_phone_number", incomingNum).findAllSorted("sms_date", Sort.DESCENDING);
                    MessagesRealm theMessage = null;
                    String finalOperator = operator;
                    SmsMessage[] finalSmsMessage = smsMessage;
                    try {
                        if (!messagesRealms.isEmpty()) {
                            theMessage = messagesRealms.first();
                            MessagesRealm finalTheMessage = theMessage;
                            realm.executeTransaction(realm1 -> {
                                realm1.copyToRealm(new MessagesHistoryRealm(contact, incomingNum,
                                        finalOperator, finalSmsMessage[0].getMessageBody(), Constans.MESSAGE_TYPE_INBOX, "1", new Date()));
                                finalTheMessage.contact = contact;
                                finalTheMessage.sms_phone_number = incomingNum;
                                finalTheMessage.sms_text = finalSmsMessage[0].getMessageBody();
                                finalTheMessage.sms_date = new Date();
                                finalTheMessage.sms_operator = finalOperator;
                                finalTheMessage.sms_type = Constans.MESSAGE_TYPE_INBOX;
                                finalTheMessage.sms_read = "1";
                                finalTheMessage.sms_count++;
                            });
                        }else {
                            realm.executeTransaction(realm1 -> {
                                realm1.copyToRealm(new MessagesRealm(contact, incomingNum, finalOperator,
                                        finalSmsMessage[0].getMessageBody(), Constans.MESSAGE_TYPE_INBOX, "1", new Date(), 1));
                                realm1.copyToRealm(new MessagesHistoryRealm(contact, incomingNum,
                                        finalOperator, finalSmsMessage[0].getMessageBody(), Constans.MESSAGE_TYPE_INBOX, "1", new Date()));
                            });
                        }
                    }catch (Exception e){
                        FirebaseCrash.logcat(Log.ERROR, "SmsReceiver", e.getMessage());
                    }
                    realm.close();
                }
                //abortBroadcast();
                break;
            case "android.provider.Telephony.SEND_SMS":
                Toast toast = Toast.makeText(context, "SMS SENT: " + MSG_TYPE, Toast.LENGTH_LONG);
                toast.show();
                //abortBroadcast();
                break;
            default:
                abortBroadcast();
                break;
        }

    }
}

