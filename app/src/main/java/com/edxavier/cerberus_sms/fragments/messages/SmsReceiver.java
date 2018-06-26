package com.edxavier.cerberus_sms.fragments.messages;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.activities.conversation.DetailConversation;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.BlackList;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.NotificationHelper;
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
        Bundle bundle = intent.getExtras();
        Date fecha = new Date();
        try {
            Realm realm = Realm.getDefaultInstance();
            if (MSG_TYPE != null) {
                switch (MSG_TYPE) {
                    case "android.provider.Telephony.SMS_RECEIVED":
                        Object messages[] = (Object[]) bundle.get("pdus");
                        SmsMessage smsMessage[] = new SmsMessage[0];
                        if (messages != null) {
                            smsMessage = new SmsMessage[messages.length];
                        }
                        for (int n = 0; n < messages.length; n++) {
                            smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
                        }
                        String incomingNum;
                        try {
                            incomingNum = Utils.formatPhoneNumber(smsMessage[0].getOriginatingAddress());
                        } catch (Exception e) {
                            incomingNum = "";
                        }
                        String incomingMsg;
                        try {
                            incomingMsg = smsMessage[0].getMessageBody();
                        } catch (Exception e) {
                            incomingMsg = "-----ERROR al leer el mensaje----";
                        }
                        //boolean isNumber = Utils.isPhoneNumber(incomingNum);
                        //boolean isMarketNum = !incomingNum.startsWith("+505") && incomingNum.length() <= 4;
                        //boolean isMarketNum2 = incomingNum.startsWith("+505") && incomingNum.replaceAll("\\s+","").trim().length() <= 8;
                        //Ignorar lo que no paresca un numero telefonico
                        //if(!(isMarketNum || isMarketNum2) && isNumber) {

                        BlackList entry = realm.where(BlackList.class)
                                .equalTo("phone_number", incomingNum)
                                .equalTo("block_incoming_sms", true).findFirst();
                        if (entry != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                if (Utils.isDefaultSmsApp(context)) {
                                    Toast.makeText(context, "SMS de << " + incomingNum + " >> Bloqueado!", Toast.LENGTH_LONG).show();
                                    abortBroadcast();
                                    realm.beginTransaction();
                                    entry.block_sms_count += 1;
                                    realm.commitTransaction();
                                    realm.close();
                                    return;
                                }
                            } else {
                                Toast.makeText(context, "SMS de << " + incomingNum + " >> Bloqueado!", Toast.LENGTH_LONG).show();
                                abortBroadcast();
                                realm.beginTransaction();
                                entry.block_sms_count += 1;
                                realm.commitTransaction();
                                realm.close();
                                return;
                            }

                        }

                        ContactRealm contact;
                        contact = Utils.getContact(incomingNum);
                        AreaCodeRealm areaCodeRealm = Utils.getOperadoraV4(incomingNum, context, realm);
                        String operator = "";
                        if (areaCodeRealm != null)
                            operator = areaCodeRealm.area_operator;
                        else
                            operator = Constans.DESCONOCIDO;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (Utils.isDefaultSmsApp(context) && Utils.isNotificationEnabled(incomingNum)) {
                                NotificationHelper notificationHelper = new NotificationHelper(context);
                                notificationHelper.sendNotification(incomingMsg, incomingNum, contact, operator);
                            }
                        }

                        String finalIncomingNum = incomingNum;
                        String finalOperator = operator;
                        String finalIncomingMsg = incomingMsg;
                        realm.executeTransaction(realm_trans -> {
                            //Cargar el historial para el numero en cuestion
                            RealmResults<MessagesHistoryRealm> msgs = realm.where(MessagesHistoryRealm.class)
                                    .equalTo("sms_phone_number", finalIncomingNum).findAll().sort("sms_date", Sort.DESCENDING);
                            MessagesHistoryRealm lastSMS = null;
                            if (msgs.isEmpty()) {
                                //+++++++++++++++++++++++++++++++++++++++++++++++PRIMER REGISTRO PARA UN NUMERO+++++++++++++++++++++++++++++++++++++++++++++++++++
                                //Ya que no hay ingresar el primer registro al historial
                                lastSMS = realm_trans.copyToRealm(new MessagesHistoryRealm(contact, finalIncomingNum,
                                        finalOperator, finalIncomingMsg, Constans.MESSAGE_TYPE_INBOX, Constans.MESSAGE_UNREAD, fecha));
                                //Ya que no hay ingresar el registro al resumen
                                MessagesRealm msgsResume = realm_trans.createObject(MessagesRealm.class, MessagesRealm.getId());
                                msgsResume.contact = contact;
                                msgsResume.sms_phone_number = finalIncomingNum;
                                if (lastSMS != null) {
                                    msgsResume.entries.add(lastSMS);
                                    msgsResume.last_update = fecha;
                                }
                                //realm_trans.copyToRealm(msgsResume);
                                //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                            } else {
                                lastSMS = msgs.first();
                                if (lastSMS.sms_date.before(fecha)) {
                                    lastSMS = realm_trans.copyToRealm(new MessagesHistoryRealm(contact, finalIncomingNum,
                                            finalOperator, finalIncomingMsg, Constans.MESSAGE_TYPE_INBOX, Constans.MESSAGE_UNREAD, fecha));

                                    MessagesRealm mResume = realm_trans.where(MessagesRealm.class).equalTo("sms_phone_number", finalIncomingNum).findFirst();
                                    if (mResume != null) {
                                        mResume.entries.add(lastSMS);
                                        mResume.last_update = fecha;
                                        mResume.contact = contact;
                                    }
                                }
                            }
                        });
                        realm.close();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Utils.saveSms(incomingNum, incomingMsg, fecha.getTime(), Constans.MESSAGE_TYPE_INBOX, Constans.MESSAGE_READ, "inbox", context);
                        }
                        //}
                        //abortBroadcast();
                        break;
                    //SMS_SENT es un intent action custom que se definio en la actividad detail conversation
                    case "SMS_SENT":
                        try {
                            String numero = bundle.getString("numero");
                            String message = bundle.getString("message");
                            long _fecha = bundle.getLong("fecha");
                            String message_id = bundle.getString("msg_id");

                            int type = -1;
                            switch (getResultCode()) {
                                case Activity.RESULT_OK:
                                    type = Constans.MESSAGE_TYPE_SENT;
                                    break;
                                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                    type = Constans.MESSAGE_TYPE_FAILED;
                                    break;
                                case SmsManager.RESULT_ERROR_NO_SERVICE:
                                    Toast.makeText(context, "No service",
                                            Toast.LENGTH_SHORT).show();
                                    type = Constans.MESSAGE_TYPE_FAILED;

                                    break;
                                case SmsManager.RESULT_ERROR_NULL_PDU:
                                    type = Constans.MESSAGE_TYPE_FAILED;

                                    break;
                                case SmsManager.RESULT_ERROR_RADIO_OFF:
                                    type = Constans.MESSAGE_TYPE_FAILED;
                                    break;
                            }
                            if (type == Constans.MESSAGE_TYPE_SENT)
                                Utils.saveSms(numero, message, _fecha, type, Constans.MESSAGE_READ, "sent", context);
                            else
                                Utils.saveSms(numero, message, _fecha, type, Constans.MESSAGE_READ, "failed", context);

                            int finalType = type;
                            realm.executeTransaction(realm_trans -> {

                                MessagesHistoryRealm lastSMS = realm_trans
                                        .where(MessagesHistoryRealm.class)
                                        .equalTo("id", message_id).findFirst();

                                if (lastSMS != null) {
                                    lastSMS.sms_type = finalType;
                                }
                            });
                            realm.close();
                        } catch (Exception ignored) {
                            if (ignored.getMessage() != null)
                                Answers.getInstance().logCustom(new CustomEvent("Error: " + ignored.getMessage())
                                        .putCustomAttribute("location", "SMS_SENT"));
                        }
                        break;
                    default:
                        abortBroadcast();
                        break;
                }
            }
        }catch (Exception ignored){}

    }
}

