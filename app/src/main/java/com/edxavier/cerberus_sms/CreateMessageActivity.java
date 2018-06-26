package com.edxavier.cerberus_sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.edxavier.cerberus_sms.activities.conversation.DetailConversation;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.CallsHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.Utils;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class CreateMessageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.chips_input)
    ChipsInput chipsInput;
    @BindView(R.id.txt_message_body)
    EditText msgBody;

    String mensaje, destino;
    List<Chip> contactList = new ArrayList<>();
    String temp_num = "";
    @BindView(R.id.fab_sent_message)
    FloatingActionButton fabSentMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_message_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Crear nuevo mensaje")
                .putContentId("p04")
                .putContentType("Pantalla"));
        Intent intent = getIntent();

        try {
            String number = intent.getDataString().split(":")[1];
            String msg = intent.getStringExtra("sms_body");
            destino = number;
            mensaje = msg;
            msgBody.setText(msg);
            ContactRealm c = Utils.getContact(Utils.formatPhoneNumber(number));
            if (c != null) {
                if (c.contact_photo_uri != null) {
                    chipsInput.addChip(new Chip(Uri.parse(c.contact_photo_uri), c.contact_name, c.contact_phone_number));
                }else {
                    chipsInput.addChip(new Chip(c.contact_name, c.contact_phone_number));
                }
            } else {
                chipsInput.addChip(number, number);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ContactRealm> res = realm.where(ContactRealm.class).findAll();
        RealmResults<CallsHistoryRealm> calls = realm.where(CallsHistoryRealm.class)
                .distinct("call_phone_number").findAll();
        RealmResults<MessagesHistoryRealm> msgs = realm
                .where(MessagesHistoryRealm.class).distinct("sms_phone_number").findAll();

        for (ContactRealm re : res) {
            if (re.contact_photo_uri != null)
                contactList.add(new Chip(Uri.parse(re.contact_photo_uri), re.contact_name, re.contact_phone_number));
            else
                contactList.add(new Chip(re.contact_name, re.contact_phone_number));

        }
        for (CallsHistoryRealm call : calls) {
            if (call.contact == null)
                contactList.add(new Chip(call.call_phone_number, call.call_phone_number));
        }
        for (MessagesHistoryRealm msg : msgs) {
            if (msg.contact == null) {
                boolean exist = false;
                for (int i = 0; i < contactList.size(); i++) {
                    if (msg.sms_phone_number.equals(contactList.get(i).getInfo()))
                        exist = true;
                }
                if (!exist)
                    contactList.add(new Chip(msg.sms_phone_number, msg.sms_phone_number));
            }
        }
        realm.close();
        chipsInput.setFilterableList(contactList);

        chipsInput.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chipInterface, int i) {
                temp_num = "";
            }

            @Override
            public void onChipRemoved(ChipInterface chipInterface, int i) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence) {
                temp_num = String.valueOf(charSequence);
            }
        });
        msgBody.setOnFocusChangeListener((view, b) -> {
            if (!temp_num.isEmpty() && Utils.isPhoneNumber(temp_num)) {
                chipsInput.addChip(temp_num, temp_num);
            } else {
                temp_num = "";
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds  items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_delete_records, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @OnClick(R.id.fab_sent_message)
    public void onViewClicked() {
        List<? extends ChipInterface> destinos = chipsInput.getSelectedChipList();
        if(msgBody.getText().length()>0) {
            String tmp_msg = msgBody.getText().toString();
            int it = 0;
            for (ChipInterface destino : destinos) {
                it++;
                Realm realm = Realm.getDefaultInstance();
                int finalIt = it;
                realm.executeTransaction(realm1 -> {
                    Date fecha = new Date();
                    String numero = destino.getInfo();
                    AreaCodeRealm op = Utils.getOperadoraV4(destino.getInfo(), this);
                    String operador = "";
                    if(op!=null) {
                        if(op.area_operator!=null)
                            operador = op.area_operator;
                    }else
                        operador = Constans.DESCONOCIDO;

                    MessagesHistoryRealm last_sms = realm.copyToRealm(new MessagesHistoryRealm(Utils.getContact(numero),
                            numero, operador, tmp_msg, Constans.MESSAGE_TYPE_QUEUED, Constans.MESSAGE_READ, fecha));
                    MessagesRealm mResume = realm.where(MessagesRealm.class)
                            .equalTo("sms_phone_number", numero).findFirst();
                    if (mResume != null) {
                        mResume.entries.add(last_sms);
                        mResume.last_update = fecha;
                        mResume.contact = Utils.getContact(numero);
                    }
                    String SENT = "SMS_SENT";
                    final Context cntx = getApplicationContext();
                    Intent _intent = new Intent(SENT);
                    _intent.putExtra("numero", numero);
                    _intent.putExtra("message", tmp_msg);
                    _intent.putExtra("fecha", fecha.getTime());
                    _intent.putExtra("msg_id", last_sms.id);
                    int rCode = (int) (System.currentTimeMillis() / 1000) + finalIt;
                    PendingIntent _pendingIntent = PendingIntent.getBroadcast(cntx, rCode,
                            _intent, PendingIntent.FLAG_ONE_SHOT);

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(numero.replaceAll("\\s+", ""), null, tmp_msg, _pendingIntent, null);
                    msgBody.setText("");

                });
                realm.close();

            }
            if(destinos.isEmpty())
                Toast.makeText(this, R.string.sent_dest_required, Toast.LENGTH_LONG).show();
            else if(destinos.size()>1)
                finish();
            else if(destinos.size()==1){
                AreaCodeRealm op = Utils.getOperadoraV4(destinos.get(0).getInfo(), this);
                String operador = "";
                if(op!=null) {
                    if(op.area_operator!=null)
                        operador = op.area_operator;
                }else
                    operador = Constans.DESCONOCIDO;
                Intent myIntent = new Intent(this, DetailConversation.class);
                myIntent.putExtra("nombre", destinos.get(0).getLabel());
                myIntent.putExtra("numero", destinos.get(0).getInfo());
                myIntent.putExtra("operador", operador);
                startActivity(myIntent);
                finish();
            }

        }else {
            Toast.makeText(this, getString(R.string.messages_hint), Toast.LENGTH_LONG).show();
        }
    }
}
