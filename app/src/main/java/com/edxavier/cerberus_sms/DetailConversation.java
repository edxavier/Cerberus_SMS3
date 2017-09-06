package com.edxavier.cerberus_sms;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

//import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Date;

import jp.wasabeef.recyclerview.animators.LandingAnimator;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class DetailConversation extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
//    private ArrayList<InboxSms> sms_list;
  //  private AdapterInbox adapter;
    ScaleInAnimationAdapter scaleInAnimationAdapter;
    //MaterialDialog pgd;
    String nombre, numero;
    NewsmsBroadcastReceiver receiver;
    IntentFilter filter;


    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

            Transition exitTrans = new Explode();
            getWindow().setExitTransition(exitTrans);

            Transition reenterTrans = new Fade();
            getWindow().setReenterTransition(reenterTrans);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(10);
            notificationManager.cancelAll();

        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail_conversation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.conversation_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        receiver = new NewsmsBroadcastReceiver();
        filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction("android.provider.Telephony.SMS_DELIVER");

        Intent intent = getIntent();
        getSupportActionBar().setTitle(intent.getStringExtra("nombre"));
        nombre = intent.getStringExtra("nombre");
        String thread_id = intent.getStringExtra("thread");
        numero = intent.getStringExtra("numero");


        mRecyclerView = (RecyclerView) findViewById(R.id._recycler_conversation_detail);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        LandingAnimator animator = new LandingAnimator();
        animator.setAddDuration(800);
        animator.setMoveDuration(500);
        mRecyclerView.setItemAnimator(animator);

        try {
            Uri inboxURI = Uri.parse("content://sms/");
            ContentResolver cr = getContentResolver();
            //Cursor cursor = cr.query(inboxURI, null, "thread_id='" + thread_id + "'", null, "date desc");
            Cursor cursor = cr.query(inboxURI, null, "address='"+numero.replaceAll("\\s+", "")+"'", null, "date desc");
            while (cursor.moveToNext()){
                String service_center = cursor.getString(cursor.getColumnIndex("service_center"));
                String num = cursor.getString(cursor.getColumnIndex("address"));
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                String read = cursor.getString(cursor.getColumnIndex("read"));
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                if(read.equals("0")) {
                    ContentValues values = new ContentValues();
                    values.put("read", true);
                    getContentResolver().update(Uri.parse("content://sms/"), values, "_id=" + id, null);
                }
            }
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

//        adapter = new AdapterInbox(sms_list, R.layout.row_conversation_detail);
  //      scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapter);
        //scaleInAnimationAdapter.setDuration(800);
        //scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        //mRecyclerView.setAdapter(scaleInAnimationAdapter);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_inbox, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                finish();
                return true;
            /*case R.id.action_add_bl:
                Sms_Lock lock = new Sms_Lock(nombre, numero);
                if (lock.save() >= 1) {
                    Toast.makeText(this, nombre + " " + getResources().getString(R.string.add_blacklist), Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(this, nombre + " " + getResources().getString(R.string.added_blacklist), Toast.LENGTH_LONG).show();
                return true;*/
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
            /*new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.para) + nombre)
                .content(getResources().getString(R.string.mensaje))
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(getResources().getString(R.string.new_sms_body), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        final String textMessage = materialDialog.getInputEditText().getText().toString();
                        String SENT = "SMS_SENT";
                        final Context cntx = getApplicationContext();

                        PendingIntent sentPI = PendingIntent.getBroadcast(cntx, 0,
                                new Intent(SENT), 0);
                        //---when the SMS has been sent---
                        registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context arg0, Intent intent) {
                                InboxSms sms;
                                String MSG_TYPE = intent.getAction();
                                int type = -1;
                                ContentValues values = new ContentValues();
                                switch (getResultCode()) {
                                    case Activity.RESULT_OK:
                                        Toast.makeText(getBaseContext(), "Mensaje enviado",
                                                Toast.LENGTH_SHORT).show();
                                        type = Constans.MESSAGE_TYPE_SENT;
                                        values.put("address", numero.replaceAll("\\s+", ""));
                                        values.put("body", textMessage);
                                        values.put("type", String.valueOf(type));
                                        values.put("date", System.currentTimeMillis());
                                        getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                                        break;
                                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                        Toast.makeText(getBaseContext(), "Fallo envío",
                                                Toast.LENGTH_SHORT).show();
                                        type = Constans.MESSAGE_TYPE_FAILED;
                                        values.put("address", numero.replaceAll("\\s+", ""));
                                        values.put("body", textMessage);
                                        values.put("type", String.valueOf(type));
                                        values.put("date", System.currentTimeMillis());
                                        getContentResolver().insert(Uri.parse("content://sms/failed"), values);
                                        break;
                                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                                        Toast.makeText(getBaseContext(), "No service",
                                                Toast.LENGTH_SHORT).show();
                                        type = Constans.MESSAGE_TYPE_FAILED;
                                        values.put("address", numero.replaceAll("\\s+", ""));
                                        values.put("body", textMessage);
                                        values.put("type", String.valueOf(type));
                                        values.put("date", System.currentTimeMillis());
                                        getContentResolver().insert(Uri.parse("content://sms/failed"), values);
                                        break;
                                    case SmsManager.RESULT_ERROR_NULL_PDU:
                                        Toast.makeText(getBaseContext(), "Fallo envío",
                                                Toast.LENGTH_SHORT).show();
                                        type = Constans.MESSAGE_TYPE_FAILED;
                                        values.put("address", numero.replaceAll("\\s+", ""));
                                        values.put("body", textMessage);
                                        values.put("type", String.valueOf(type));
                                        values.put("date", System.currentTimeMillis());
                                        getContentResolver().insert(Uri.parse("content://sms/failed"), values);
                                        break;
                                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                                        Toast.makeText(getBaseContext(), "Fallo envío",
                                                Toast.LENGTH_SHORT).show();
                                        type = Constans.MESSAGE_TYPE_FAILED;
                                        values.put("address", numero.replaceAll("\\s+", ""));
                                        values.put("body", textMessage);
                                        values.put("type", String.valueOf(type));
                                        values.put("date", System.currentTimeMillis());
                                        getContentResolver().insert(Uri.parse("content://sms/failed"), values);
                                        break;
                                }

                                sms = new InboxSms(nombre, numero, textMessage, new Date(), type, Constans.MESSAGE_READ);
                                if (!adapter.itemExist(sms)) {
                                    adapter.addItem(sms);
                                    scaleInAnimationAdapter.notifyItemInserted(0);
                                    mRecyclerView.scrollToPosition(0);
                                }

                            }
                        }, new IntentFilter(SENT));

                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(numero.replaceAll("\\s+", ""), null, textMessage, sentPI, null);
                    }
                })
                .inputRange(0, 160)
                .positiveText(getResources().getString(R.string.enviar))
                .negativeText(getResources().getString(R.string.cancelar))
                .show();        */


        Uri uri = Uri.parse("smsto:" + numero);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        startActivity(intent);

    }

    class NewsmsBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String MSG_TYPE=intent.getAction();
            if(MSG_TYPE.equals("android.provider.Telephony.SMS_RECEIVED"))
            {
                Bundle bundle = intent.getExtras();

                Object messages[] = (Object[]) bundle.get("pdus");
                SmsMessage smsMessage[] = new SmsMessage[messages.length];
                for (int n = 0; n < messages.length; n++)
                {
                    smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
                }

                if(smsMessage[0].getOriginatingAddress().equals(numero.replaceAll("\\s+",""))) {
                    String msg = smsMessage[0].getMessageBody();
                    Date date = new Date(System.currentTimeMillis());
                }
            }
        }
    }

}
