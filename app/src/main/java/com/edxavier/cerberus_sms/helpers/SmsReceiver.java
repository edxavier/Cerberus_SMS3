package com.edxavier.cerberus_sms.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.edxavier.cerberus_sms.DetailConversation;
import com.edxavier.cerberus_sms.R;

/**
 * Created by Eder Xavier Rojas on 24/10/2015.
 */
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String MSG_TYPE=intent.getAction();
        //Log.d("EDER", MSG_TYPE);
        if(MSG_TYPE.equals("android.provider.Telephony.SMS_RECEIVED"))
        {
            Bundle bundle = intent.getExtras();

            Object messages[] = (Object[]) bundle.get("pdus");
            SmsMessage smsMessage[] = new SmsMessage[messages.length];
            for (int n = 0; n < messages.length; n++)
            {
                smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
            }

            String incomingNum = Utils.formatPhoneNumber(smsMessage[0].getOriginatingAddress());
            final String myPackageName = context.getPackageName();
            //Sms_Lock res = new Select().from(Sms_Lock.class).where("numero = ?", incomingNum).executeSingle();
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String sender;
            //sender = Utils.getContactName(incomingNum);

            //if(res!=null) {
            if(true){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //Verificar si la aplicacion es la gestora de sms
                    if (Telephony.Sms.getDefaultSmsPackage(context).equals(myPackageName)) {
                        abortBroadcast();
                        //res.setConteo(res.getConteo() + 1);
                        //res.save();
                    } else {
                        Intent intent4 = new Intent(context, NotificationPendindIntentSetupDefaultSMS.class);
                        // use System.currentTimeMillis() to have a unique ID for the pending intent
                        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent4, 0);
                        Notification n = new Notification.Builder(context)
                                .setContentTitle("Advertencia de bloqueo")
                                //.setSmallIcon(R.drawable.error_filled_50)
                                .setSound(soundUri)
                                .setAutoCancel(true)
                                .addAction(R.drawable.ic_support_50, "Establecer", pIntent)
                                .setStyle(new Notification.BigTextStyle()
                                        .bigText(context.getResources().getString(R.string.notification) + " "))
                                .build();

                        // Will show lights and make the notification disappear when the presses it
                        n.flags |= Notification.FLAG_AUTO_CANCEL;

                        NotificationManager notificationManager =
                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, n);

                    }
                } else {
                        abortBroadcast();
                        //res.setConteo(res.getConteo() + 1);
                        //res.save();
                }
            }else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (Telephony.Sms.getDefaultSmsPackage(context).equals(myPackageName)) {
                        abortBroadcast();
                        ContentValues values = new ContentValues();
                        values.put("address", smsMessage[0].getOriginatingAddress());//sender name
                        values.put("body", smsMessage[0].getMessageBody());
                        values.put("read", 0);
                        values.put("date", System.currentTimeMillis());
                        context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);

                        Intent intent2 = new Intent(context, DetailConversation.class);
                        intent2.putExtra("numero",incomingNum);
                        intent2.putExtra("nombre", sender);
                        // use System.currentTimeMillis() to have a unique ID for the pending intent
                        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent2, 0);
                        //Define sound URI
                        Notification
                            n = new Notification.Builder(context)
                                    .setContentTitle("Nuevo mensaje")
                                    .setSmallIcon(R.drawable.sms_50)
                                    .setContentIntent(pIntent)
                                    .addAction(R.drawable.ic_new_post_100, "Leer", pIntent)
                                    .setSound(soundUri)
                                    .setAutoCancel(true)
                                    .setStyle(new Notification.BigTextStyle()
                                            .bigText(sender))
                                    .build();
                            //n.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
                            //n.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
                            NotificationManager notificationManager =
                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(10, n);
                        //n.flags |= Notification.FLAG_AUTO_CANCEL;
                        abortBroadcast();


                    }
                }

            }
             //abortBroadcast();
        }
        else if(MSG_TYPE.equals("android.provider.Telephony.SEND_SMS"))
        {
            Toast toast = Toast.makeText(context,"SMS SENT: "+MSG_TYPE, Toast.LENGTH_LONG);
            //toast.show();
            //abortBroadcast();
        }
        else
        {
            abortBroadcast();
        }

    }
}

