package com.edxavier.cerberus_sms.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.activities.conversation.DetailConversation;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.PendingNotifications;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.support.v4.app.NotificationCompat.CATEGORY_MESSAGE;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PRIVATE;
import static android.support.v4.app.NotificationCompat.VISIBILITY_SECRET;

/**
 * Created by Eder Xavier Rojas on 18/09/2017.
 */

public class NotificationHelper extends ContextWrapper {
    private NotificationManager mManager;
    public static final String CHANNEL_ID = "com.edxavier.cerberus_sms";
    public static final String CHANNEL_NAME = "Verifica Operador Nicaragua";

    public NotificationHelper(Context base) {
        super(base);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setShowBadge(true);
            notificationChannel.setSound(soundUri, null);
            notificationChannel.setLightColor(Color.CYAN);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            getManager().createNotificationChannel(notificationChannel);
        }
    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public void sendNotification(String incomingMsg, String incomingNum, ContactRealm contact, String operator){
        Bitmap bitmap = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bitmap = getAvatar(incomingNum);
        }
        Intent detail = new Intent(getApplicationContext(), DetailConversation.class);
        detail.putExtra("numero",incomingNum);
        String sender = "";
        if(contact!=null)
            sender =  contact.contact_name;
        else
            sender = incomingNum;
        detail.putExtra("nombre", incomingNum);
        detail.putExtra("operador", operator);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(detail);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent pIntent =
                stackBuilder.getPendingIntent((int) System.currentTimeMillis()/1000, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pIntent2 = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), detail, 0);

        int notification_id = 0;
        int unreaded = 0;
        notification_id = getNotificationID(incomingNum);
        unreaded = getUnreadedMsgsCount(incomingNum);
        android.support.v4.app.NotificationCompat.MessagingStyle msgs;
        if(notification_id == -1) {
            notification_id = (int) System.currentTimeMillis()/1000;
            saveNotificationID(incomingNum, notification_id);
        }
        if(unreaded == 0){
            msgs = new NotificationCompat.MessagingStyle(getString(R.string.title_activity_new_black_list_entry))
                    .setConversationTitle(getString(R.string.title_activity_new_black_list_entry))
                    .addMessage(incomingMsg, System.currentTimeMillis(), sender);
        }else {
            msgs = new NotificationCompat.MessagingStyle(getString(R.string.title_activity_new_black_list_entry))
                    .setConversationTitle(getString(R.string.title_activity_new_black_list_entry))
                    .addMessage(incomingMsg, System.currentTimeMillis(), sender);
            RealmResults<MessagesHistoryRealm> ur_msgs = getUnreadedMsgs(incomingNum);
            for (MessagesHistoryRealm ur_msg : ur_msgs) {
                msgs.addMessage(ur_msg.sms_text, ur_msg.sms_date.getTime(), sender);
            }
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder = new NotificationCompat.Builder(getApplicationContext())
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary_dark))
                    .setContentTitle(getString(R.string.title_activity_new_black_list_entry))
                    .setContentText(incomingMsg)
                    .setSmallIcon(R.drawable.ic_textsms)
                    .setCategory(CATEGORY_MESSAGE)
                    .setContentIntent(pIntent)
                    .setChannelId(CHANNEL_ID)
                    .setContentText(incomingMsg)
                    .setVisibility(VISIBILITY_SECRET)
                    //.addAction(R.drawable.ic_textsms, "Leer", pIntent)
                    .setSound(soundUri)
                    .setLights(Color.CYAN, 500, 500)
                    .setNumber(unreaded + 1)
                    .setStyle(msgs)
                    .setAutoCancel(true);
            if (bitmap != null) {
                builder.setLargeIcon(bitmap);
            }
            NotificationManagerCompat notificationManager2 = NotificationManagerCompat.from(getApplicationContext());
            if(builder!=null)
                notificationManager2.notify(incomingNum, notification_id, builder.build());
        }else {
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, detail, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());
            b.setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(getString(R.string.title_activity_new_black_list_entry))
                    .setContentTitle(sender)
                    .setContentText(incomingMsg)
                    .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                    .setContentIntent(contentIntent)
                    .setContentInfo("Info");

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, b.build());



        }

    }

    private Bitmap getAvatar(String number) {
        Bitmap bitmap = null;
        AreaCodeRealm areaCode = Utils.getOperadoraV4(number, getApplicationContext());
        if (areaCode != null) {
            switch (areaCode.area_operator) {
                case Constans.CLARO:
                    bitmap =  ResourceUtil.getBitmap(getApplicationContext(), R.drawable.ic_user_red);
                break;
                case Constans.MOVISTAR:
                    bitmap =  ResourceUtil.getBitmap(getApplicationContext(), R.drawable.ic_user_green);
                break;
                case Constans.COOTEL:
                    bitmap =  ResourceUtil.getBitmap(getApplicationContext(), R.drawable.ic_user_orange);
                break;
                case Constans.CONVENCIONAL:
                    bitmap =  ResourceUtil.getBitmap(getApplicationContext(), R.drawable.ic_user_blue);
                break;
                case Constans.INTERNACIONAL:
                    bitmap =  ResourceUtil.getBitmap(getApplicationContext(), R.drawable.ic_user_purple);
                break;
                default:
                    bitmap =  ResourceUtil.getBitmap(getApplicationContext(), R.drawable.ic_user_unkown);
                break;
            }
        }
        return bitmap;
    }

    private int getNotificationID(String number){
        Realm realm = Realm.getDefaultInstance();
        PendingNotifications pn = realm.where(PendingNotifications.class).equalTo("phone_number", number).findFirst();
        int id = -1;
        if(pn!=null)
            id = pn.id;
        realm.close();
        return id;
    }
    private void saveNotificationID(String number, int id){
        Realm realm = Realm.getDefaultInstance();
        PendingNotifications pn = realm.where(PendingNotifications.class).equalTo("phone_number", number).findFirst();
        if(pn==null){
            realm.executeTransaction(realm1 -> {
                realm.copyToRealm(new PendingNotifications(id, number));
            });
        }
        realm.close();
    }
    public int getUnreadedMsgsCount(String number) {
        Realm realm = Realm.getDefaultInstance();
        int res =  (int) realm.where(MessagesHistoryRealm.class)
                .equalTo("sms_phone_number", number)
                .equalTo("sms_read", Constans.MESSAGE_UNREAD).count();
        realm.close();
        return res;
    }

    public RealmResults<MessagesHistoryRealm>  getUnreadedMsgs(String number) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<MessagesHistoryRealm> res = realm.where(MessagesHistoryRealm.class)
                .equalTo("sms_phone_number", number)
                .equalTo("sms_read", Constans.MESSAGE_UNREAD).findAll().sort("sms_date", Sort.DESCENDING);
        realm.close();
        return res;
    }
}
