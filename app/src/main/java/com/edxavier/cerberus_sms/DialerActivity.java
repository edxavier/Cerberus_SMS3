package com.edxavier.cerberus_sms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.fragments.callLog.CallLogFragment;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorPresenter;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorView;
import com.edxavier.cerberus_sms.fragments.checkOperator.di.CheckoperatorComponent;
import com.edxavier.cerberus_sms.fragments.contacts.ContactFragment;
import com.edxavier.cerberus_sms.fragments.messages.MessagesFragment;
import com.edxavier.cerberus_sms.helpers.PagerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

public class DialerActivity extends AppCompatActivity implements CheckOperatorView, TextWatcher {

    @Inject
    CheckOperatorPresenter presenter;
    boolean outGoingCall = false;


    @BindView(R.id.linear_layout_bottom_sheet)
    LinearLayout linearLayoutBottomSheet;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.sliding_tabs)
    TabLayout slidingTabs;
    @BindView(R.id.pager_container)
    ViewPager pagerContainer;

    Fragment[] fragments;
    FragmentManager manager;
    @BindView(R.id.adViewNative)
    NativeExpressAdView adViewNative;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int REQUEST_SYSTEM_ALERT_WINDOW = 2;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 3;

    @Inject
    FirebaseAnalytics analytics;
    Bundle analitycParams;


    public BottomSheetBehavior mbottomSheetBehavior;
    @BindView(R.id.operator_name)
    TextView operatorName;
    @BindView(R.id.card_operator)
    CardView cardOperator;
    @BindView(R.id.country)
    TextView country;
    @BindView(R.id.number_entry)
    AutoCompleteTextView numberEntry;
    @BindView(R.id.backspace_arrow)
    MaterialRippleLayout backspaceArrow;
    @BindView(R.id.num_1)
    MaterialRippleLayout num1;
    @BindView(R.id.num_2)
    MaterialRippleLayout num2;
    @BindView(R.id.num_3)
    MaterialRippleLayout num3;
    @BindView(R.id.num_4)
    MaterialRippleLayout num4;
    @BindView(R.id.num_5)
    MaterialRippleLayout num5;
    @BindView(R.id.num_6)
    MaterialRippleLayout num6;
    @BindView(R.id.num_7)
    MaterialRippleLayout num7;
    @BindView(R.id.num_8)
    MaterialRippleLayout num8;
    @BindView(R.id.num_9)
    MaterialRippleLayout num9;
    @BindView(R.id.num_asterisk)
    MaterialRippleLayout numAsterisk;
    @BindView(R.id.num_0)
    MaterialRippleLayout num0;
    @BindView(R.id.num_numeral)
    MaterialRippleLayout numNumeral;
    String number = "";
    @BindView(R.id.main_coordinator)
    CoordinatorLayout mainCoordinator;
    @BindView(R.id.suggestions)
    ListView suggestions;

    @BindView(R.id.linear_operator_show)
    LinearLayout linearOperatorShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);
        ButterKnife.bind(this);


        setupInjection();
        analitycParams = new Bundle();
        if (!presenter.checkDBinitialization())
            presenter.dbInitialization();

        //presenter.InitializeRealmArecodes();
        //initContacts();

        setupAds();
        setupWidgets();
        //checkCallPerms();
        checkPermissions();

    }

    private void setupWidgets() {
        setSupportActionBar(toolbar);
        //get bottom sheet behavior from bottom sheet view
        mbottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBottomSheet);
        //to expand the bottom sheet
        mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mbottomSheetBehavior.setSkipCollapsed(true);

        String[] titles = new String[]{"Llamadas", "Contactos", "Mensajes"};
        manager = getSupportFragmentManager();

        fragments = new Fragment[]{new CallLogFragment(), new ContactFragment(), new MessagesFragment()};
        PagerAdapter pagerAdapter = new PagerAdapter(manager, titles, fragments);
        pagerContainer.setAdapter(pagerAdapter);
        slidingTabs.setupWithViewPager(pagerContainer);
        try {
            slidingTabs.getTabAt(0).setText("");
            slidingTabs.getTabAt(1).setText("");
            slidingTabs.getTabAt(2).setText("");
            slidingTabs.getTabAt(0).setIcon(R.drawable.ic_phone_white_24dp);
            slidingTabs.getTabAt(1).setIcon(R.drawable.ic_users);
            slidingTabs.getTabAt(2).setIcon(R.drawable.ic_chat_speech_bubbles);
        } catch (Exception ignored) {
        }

        backspaceArrow.setOnClickListener(view -> {
            if (!number.isEmpty()) {
                number = number.substring(0, number.length() - 1);
                numberEntry.setText(number);
                suggestions.setVisibility(View.GONE);
                linearOperatorShow.setVisibility(View.GONE);
            }
        });
        backspaceArrow.setOnLongClickListener(view -> {
            numberEntry.setText("");
            number = "";
            suggestions.setVisibility(View.GONE);
            linearOperatorShow.setVisibility(View.GONE);
            return true;
        });

        num1.setOnClickListener(view -> numberEntry.setText(number += "1"));
        num2.setOnClickListener(view -> numberEntry.setText(number += "2"));
        num3.setOnClickListener(view -> numberEntry.setText(number += "3"));
        num4.setOnClickListener(view -> numberEntry.setText(number += "4"));
        num5.setOnClickListener(view -> numberEntry.setText(number += "5"));
        num6.setOnClickListener(view -> numberEntry.setText(number += "6"));
        num7.setOnClickListener(view -> numberEntry.setText(number += "7"));
        num8.setOnClickListener(view -> numberEntry.setText(number += "8"));
        num9.setOnClickListener(view -> numberEntry.setText(number += "9"));
        num0.setOnClickListener(view -> numberEntry.setText(number += "0"));
        numAsterisk.setOnClickListener(view -> numberEntry.setText(number += "*"));
        numNumeral.setOnClickListener(view -> numberEntry.setText(number += "#"));
        num0.setOnLongClickListener(view -> {
            numberEntry.setText(number += "+");
            return true;
        });

        setupAutocomplete();

        suggestions.setOnItemClickListener((adapterView, view, i, l) -> {

        });
    }

    void checkCallPerms() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            outGoingCall = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                    PERMISSIONS_REQUEST_CALL_PHONE);
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            analitycParams.putString("perms", "Verificar permiso para Android M");
            analytics.logEvent("android_m_request_perm", analitycParams);
            if (!Settings.canDrawOverlays(this)) {
                new MaterialDialog.Builder(this)
                        .title(R.string.info)
                        .content(R.string.alert_info)
                        .positiveText(R.string.accept)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW);
                            }
                        })
                        .show();
            }
        }
    }

    private void setupAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adViewNative.loadAd(adRequest);
        adViewNative.setVisibility(View.GONE);
        adViewNative.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adViewNative.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupInjection() {
        AppOperator app = (AppOperator) getApplication();
        CheckoperatorComponent component = app.getCheckoperatorComponent(this, this);
        component.inject(this);
    }

    @Override
    public AreaCodeRealm checkOperator(String phoneNumber) {
        return presenter.checkOperator(phoneNumber);
    }

    @Override
    public void setNumber(String number) {

    }

    @Override
    public void showElements(boolean show) {
    }

    @Override
    public void setResult() {
    }

    private void initContacts() {
        if (presenter.isReadContactsPermsGranted()) {
            presenter.syncronizeContacts();
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.save_info)
                    .content(R.string.contact_perms_info)
                    .positiveText(R.string.accept)
                    .negativeText(R.string.cancelar)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ActivityCompat.requestPermissions(DialerActivity.this,
                                    new String[]{Manifest.permission.READ_CONTACTS},
                                    PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    })
                    .show();
        }
    }

    //#####################

    @Override
    public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        presenter.onPause();
        super.onPause();
    }


    private void setupAutocomplete() {



        //ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,result);
        //ContactAutocompleteAdapter adapter = new ContactAutocompleteAdapter(this, R.layout.create_message_activity, R.id.lbl_contact_name_auto_complete, result);
        //suggestions.setAdapter(adapter);
        numberEntry.setThreshold(2);
        //numberEntry.setAdapter(adapter);
        numberEntry.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        showSuggestions();

        AreaCodeRealm res = checkOperator(numberEntry.getText().toString());
        if (res != null) {
            linearOperatorShow.setVisibility(View.VISIBLE);
            cardOperator.setVisibility(View.VISIBLE);
            operatorName.setText(res.area_operator);
            country.setText(res.country_name);
            if (numberEntry.getText().toString().startsWith("+505") && numberEntry.getText().toString().length() > 7)
                country.setText(res.area_name + " " + res.country_name);
            if (numberEntry.getText().toString().length() > 3 && !numberEntry.getText().toString().startsWith("+505"))
                country.setText(res.area_name + " " + res.country_name);

            switch (res.area_operator) {
                case "Claro":
                    cardOperator.setCardBackgroundColor(getResources().getColor(R.color.md_red_600));
                    break;
                case "Movistar":
                    cardOperator.setCardBackgroundColor(getResources().getColor(R.color.md_green_700));
                    break;
                case "CooTel":
                    cardOperator.setCardBackgroundColor(getResources().getColor(R.color.md_orange_700));
                    break;
                default:
                    cardOperator.setVisibility(View.GONE);
            }

        } else {
            cardOperator.setVisibility(View.GONE);
            operatorName.setText("");
            country.setText("");
        }
    }

    private void showSuggestions() {
        if (!numberEntry.getText().toString().isEmpty()) {

            //ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,result);
            //adapter = new ContactAutocompleteAdapter(this, R.layout.create_message_activity, R.id.lbl_contact_name_auto_complete, result);

        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(this, getResources().getString(R.string.sync_contacts_msg), LENGTH_SHORT).show();
                    presenter.syncronizeContacts();
                }
                break;
            }
            case PERMISSIONS_REQUEST_CALL_PHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (outGoingCall) {
                        if (numberEntry.getText().toString().length() >= 3) {
                            String uri = "tel:" + numberEntry.getText().toString().replaceAll("\\s+", "");
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse(uri));
                            //startActivity(intent);
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.call_msg_atempt), LENGTH_LONG).show();
                        }
                    }
                }
                break;
            }

        }
    }

}
