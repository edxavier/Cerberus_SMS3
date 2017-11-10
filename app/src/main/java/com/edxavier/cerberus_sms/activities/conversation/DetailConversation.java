package com.edxavier.cerberus_sms.activities.conversation;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.edxavier.cerberus_sms.DialerActivityV2;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.activities.conversation.contracts.ConversationPresenter;
import com.edxavier.cerberus_sms.activities.conversation.contracts.ConversationPresenterImp;
import com.edxavier.cerberus_sms.activities.conversation.contracts.ConversationView;
import com.edxavier.cerberus_sms.db.realm.BlackList;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.db.realm.Notifications;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import jp.wasabeef.recyclerview.animators.FlipInBottomXAnimator;

import static com.edxavier.cerberus_sms.helpers.Constans.BLOCK_NONE;
import static com.edxavier.cerberus_sms.helpers.Constans.CLARO;
import static com.edxavier.cerberus_sms.helpers.Constans.CONVENCIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.COOTEL;
import static com.edxavier.cerberus_sms.helpers.Constans.DESCONOCIDO;
import static com.edxavier.cerberus_sms.helpers.Constans.INTERNACIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.MOVISTAR;

//import com.afollestad.materialdialogs.MaterialDialog;

public class DetailConversation extends AppCompatActivity implements ConversationView, View.OnClickListener {


    String nombre, numero, operador;
    ConversationPresenter presenter;
    @BindView(R.id._recycler_conversation)
    RecyclerView recyclerMessages;
    @BindView(R.id.conversation_toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab_sent_message)
    FloatingActionButton fab_sent_message;
    @BindView(R.id.txt_message_body)
    TextView txt_message_body;

    boolean isActivityVisible = false;
    @BindView(R.id.adView)
    AdView adView;

    private ConversationAdapterRealm adapter;
    String tmp_msg = "";

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        setTheme(R.style.NewAppTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        operador = intent.getStringExtra("operador");
        setAppTheme(operador);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_conversation);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(intent.getStringExtra("nombre"));
        nombre = intent.getStringExtra("nombre");
        numero = intent.getStringExtra("numero");

        presenter = new ConversationPresenterImp(this);
        presenter.getMessages(numero);
        presenter.setMessagesAsReaded(numero);

        fab_sent_message.setOnClickListener(this);

        //registerReceiver(sentReceiver, new IntentFilter("SMS_SENT"));
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Detalle mensajes")
                .putContentId("p05")
                .putContentType("Pantalla"));
        if(!Prefs.getBoolean("ads_removed", false)) {
            setupAds();
        }
        try {
            NotificationManagerCompat notificationManager2 = NotificationManagerCompat.from(getApplicationContext());
            notificationManager2.cancelAll();
        }catch (Exception ignored){}

    }

    public void setupAds() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                .build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if(recyclerMessages!=null)
                    recyclerMessages.setPadding(0,95,0,0);
            }
        });
        adView.loadAd(adRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.setMessagesAsReaded(numero);
        presenter.onDestroy();
        //unregisterReceiver(sentReceiver);
    }

    private void setAppTheme(String operador) {
        try {
            switch (operador) {
                case CLARO:
                    setTheme(R.style.ClaroTheme);
                    break;
                case MOVISTAR:
                    setTheme(R.style.MovistarTheme);
                    break;
                case COOTEL:
                    setTheme(R.style.CootelTheme);
                    break;
                case CONVENCIONAL:
                    setTheme(R.style.NewAppTheme);
                    break;
                case INTERNACIONAL:
                    setTheme(R.style.InternationalTheme);
                    break;
                case DESCONOCIDO:
                    setTheme(R.style.UnkonwTheme);
                    break;
                default:
                    setTheme(R.style.NewAppTheme);
                    break;
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.msgs_detail, menu);
        if (!Utils.isDefaultSmsApp(this)) {
            MenuItem item = menu.findItem(R.id.action_notifications);
            if (item != null)
                item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_call:
                String uri = "tel:" + numero.replaceAll("\\s+", "");
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                try {
                    startActivity(intent);
                } catch (Exception ignored) {
                }
                break;
            case R.id.action_notifications:
                Realm r = Realm.getDefaultInstance();
                int opt = 0;
                Notifications enabled = r.where(Notifications.class).equalTo("phone_number", numero).findFirst();
                if (enabled != null)
                    opt = 1;
                r.close();

                new MaterialDialog.Builder(this)
                        .title(R.string.ac_notifications)
                        .items(R.array.notifications_options)
                        .itemsCallbackSingleChoice(opt, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (which == 0) {
                                    Answers.getInstance().logCustom(new CustomEvent("Notificaciones de numero")
                                            .putCustomAttribute("Tipo", "Habilitadas"));
                                    presenter.disableNotifications(false, numero);
                                } else {
                                    Answers.getInstance().logCustom(new CustomEvent("Notificaciones de numero")
                                            .putCustomAttribute("Tipo", "Desabilitadas"));
                                    presenter.disableNotifications(true, numero);
                                }
                                return true;
                            }
                        })
                        .positiveText(R.string.accept)
                        .show();
                return true;
            case R.id.action_block:
                Realm realm = Realm.getDefaultInstance();
                int op = BLOCK_NONE;
                BlackList entry = realm.where(BlackList.class).equalTo("phone_number", numero).findFirst();
                if (entry != null) {
                    if (entry.block_incoming_call && entry.block_incoming_sms)
                        op = Constans.BLOCK_BOTH;
                    else if (entry.block_incoming_call && !entry.block_incoming_sms)
                        op = Constans.BLOCK_CALLS;
                    else if (!entry.block_incoming_call && entry.block_incoming_sms)
                        op = Constans.BLOCK_MESSAGES;

                }
                realm.close();
                new MaterialDialog.Builder(this)
                        .title(R.string.ac_block)
                        .items(R.array.block_options)
                        .itemsCallbackSingleChoice(op, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                presenter.sendToBlackList(which, numero);
                                return true;
                            }
                        })
                        .positiveText(R.string.accept)
                        .negativeText(R.string.cancelar)
                        .show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {

        if (txt_message_body.getText().length() > 0) {
            Realm realm = Realm.getDefaultInstance();
            final MessagesHistoryRealm[] lastSMS = new MessagesHistoryRealm[1];
            realm.executeTransaction(realm_trans -> {
                tmp_msg = txt_message_body.getText().toString();

                Date fecha = new Date();
                lastSMS[0] = realm_trans.copyToRealm(new MessagesHistoryRealm(Utils.getContact(numero),
                        numero, operador, tmp_msg, Constans.MESSAGE_TYPE_QUEUED, Constans.MESSAGE_READ, fecha));

                MessagesRealm mResume = realm_trans.where(MessagesRealm.class)
                        .equalTo("sms_phone_number", numero).findFirst();
                if (mResume != null) {
                    mResume.entries.add(lastSMS[0]);
                    mResume.last_update = fecha;
                    mResume.contact = Utils.getContact(numero);
                }

                String SENT = "SMS_SENT";
                final Context cntx = getApplicationContext();
                Intent _intent = new Intent(SENT);
                _intent.putExtra("numero", numero);
                _intent.putExtra("message", tmp_msg);
                _intent.putExtra("fecha", fecha.getTime());
                _intent.putExtra("msg_id", lastSMS[0].id);

                PendingIntent _pendingIntent = PendingIntent.getBroadcast(cntx, 0,
                        _intent, PendingIntent.FLAG_ONE_SHOT);

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(numero.replaceAll("\\s+", ""), null, tmp_msg, _pendingIntent, null);
                txt_message_body.setText("");
            });

            realm.close();
        } else {
            Toast.makeText(this, getString(R.string.messages_hint), Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public void showEmptyMsg(boolean show) {

    }

    @Override
    public void setMessages(RealmResults<MessagesHistoryRealm> messages) {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(mLayoutManager);
        //recyclerMessages.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerMessages.setHasFixedSize(true);
        adapter = new ConversationAdapterRealm(messages, this, presenter);
        recyclerMessages.setItemAnimator(new FlipInBottomXAnimator());
        recyclerMessages.getItemAnimator().setAddDuration(300);
        recyclerMessages.setAdapter(adapter);
        recyclerMessages.scrollToPosition(0);
    }

    @Override
    public void scrollTo(int pos) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isActivityVisible) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();

        }
        try {
            recyclerMessages.scrollToPosition(0);
        } catch (Exception ignored) {
        }
    }

    public void checkSMSsettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Utils.thereAreBlockedSmsNumbers() && !Utils.isDefaultSmsApp(this)) {
                //recyclerMessages.setPadding(0, 100, 0, 0);
                //warning.setVisibility(View.VISIBLE);
            } else {
                //recyclerMessages.setPadding(0, 0, 0, 0);
                //warning.setVisibility(View.GONE);
            }
        }

        /*warning.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (!Telephony.Sms.getDefaultSmsPackage(getContext()).equals(getContext().getPackageName())) {
                    //Store default sms package name
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                            getContext().getPackageName());
                    //startActivity(intent);
                    startActivityForResult(intent, REQUEST_CODE_SMS_DEFAULT_DIALOG);
                }
            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false;

    }

}
