package com.edxavier.cerberus_sms.fragments.checkOperator;


import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;
import com.edxavier.cerberus_sms.AppOperator;
import com.edxavier.cerberus_sms.BuildConfig;
import com.edxavier.cerberus_sms.DialerActivityV2;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorPresenter;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorView;
import com.edxavier.cerberus_sms.fragments.checkOperator.di.CheckoperatorComponent;
import com.edxavier.cerberus_sms.helpers.TextViewHelper;
import com.edxavier.cerberus_sms.helpers.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakewharton.rxbinding2.view.RxView;
import com.pixplicity.easyprefs.library.Prefs;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class CheckOperatorFrg extends Fragment implements CheckOperatorView, TextWatcher {

    @BindView(R.id.bottom_sheet)
    NestedScrollView bottomSheet;
    @BindView(R.id.fab_call_btn)
    FloatingActionButton fabCallBtn;
    @BindView(R.id.fab_sms_btn)
    FloatingActionButton fabSmsBtn;
    @BindView(R.id.numberContainer)
    LinearLayout numberContainer;
    @BindView(R.id.btnPaste)
    MaterialRippleLayout btnPaste;
    private AdapterContactSuggest adapter_realm;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int REQUEST_SYSTEM_ALERT_WINDOW = 2;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 3;
    @Inject
    CheckOperatorPresenter presenter;
    @Inject
    FirebaseAnalytics analytics;

    @BindView(R.id.CheckContainer)
    CoordinatorLayout CheckContainer;
    @BindView(R.id.suggestions)
    ListView suggestions;
    @BindView(R.id.operator_name)
    TextView operatorName;
    @BindView(R.id.card_operator)

    CardView cardOperator;
    @BindView(R.id.country)
    TextView country;
    @BindView(R.id.linear_operator_show)
    LinearLayout linearOperatorShow;
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
    @BindView(R.id.linear_layout_bottom_sheet)
    LinearLayout linearLayoutBottomSheet;

    String number = "";
    @BindView(R.id.recycler_suggestions)
    RecyclerView recyclerSuggestions;
    @BindView(R.id.adView)
    public AdView adView;

    public CheckOperatorFrg() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_dialer, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adView.setVisibility(View.GONE);
        if (!Prefs.getBoolean("ads_removed", false)) {
            setupAds();
        }
        setupInjection();
        checkCallPerms();
        setupWidgets();
    }

    void checkCallPerms() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                    PERMISSIONS_REQUEST_CALL_PHONE);
        }
    }


    private void setupInjection() {
        AppOperator app = (AppOperator) getActivity().getApplication();
        CheckoperatorComponent component = app.getCheckoperatorComponent(this, this);
        component.inject(this);
    }

    @Override
    public AreaCodeRealm checkOperator(String phoneNumber) {
        return presenter.checkOperator(phoneNumber);
    }

    @Override
    public void setNumber(String number) {
        numberEntry.setText(number);
    }

    @Override
    public void showElements(boolean show) {

    }

    @Override
    public void setResult() {

    }

    //#####################
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_delete_records, menu);
    }

    private void setupAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (!Prefs.getBoolean("ads_removed", false))
                    adView.setVisibility(View.VISIBLE);
                else
                    adView.setVisibility(View.GONE);
            }
        });
        adView.loadAd(adRequest);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void setupWidgets() {
        setupRecycler();
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

        RxView.clicks(fabCallBtn).subscribe(o -> {
            if (numberEntry.getText().toString().length() > 3) {
                String uri = "tel:" + numberEntry.getText().toString()
                        .replaceAll("\\s+", "").replace("#", "%23");
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                try {
                    getActivity().startActivity(intent);
                } catch (Exception ignored) {
                    Toast.makeText(getActivity(), "Error: " + ignored.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            } else
                Toast.makeText(getActivity(), getString(R.string.call_msg_atempt),
                        Toast.LENGTH_LONG).show();
        });
        RxView.clicks(fabSmsBtn).subscribe(o -> {
            if (numberEntry.getText().toString().length() > 3) {
                Intent intent2 = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("smsto:" + numberEntry.getText().toString().replaceAll("\\s+", "")));
                intent2.putExtra("sms_body", "");
                getActivity().startActivity(intent2);
            } else
                Toast.makeText(getActivity(), getString(R.string.sms_msg_atempt),
                        Toast.LENGTH_LONG).show();
        });

        final float[] offset = {0};
        final BottomSheetBehavior bsb = BottomSheetBehavior.from(bottomSheet);
        DialerActivityV2 activityV2 = (DialerActivityV2) getActivity();
        bsb.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset > offset[0]) {
                    activityV2.bottomBar.getShySettings().hideBar();
                } else if (slideOffset < offset[0]) {
                    activityV2.bottomBar.getShySettings().showBar();
                }
                offset[0] = slideOffset;
            }
        });
        RxView.clicks(btnPaste).subscribe(o -> {
            try {
                CharSequence pasteData = "";
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                pasteData = item.getText();
                if(Utils.isPhoneNumber(pasteData.toString()))
                    numberEntry.setText(pasteData);
                else
                    Toast.makeText(getActivity(),
                            pasteData + " No es valido", Toast.LENGTH_LONG).show();
            }catch (Exception e){
                Toast.makeText(getActivity(),
                        "ERROR: no se pudo pegar el contenido", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void setupAutocomplete() {

        Realm realm = Realm.getDefaultInstance();
        RealmResults<ContactRealm> result = realm.where(ContactRealm.class).findAllSorted("contact_name");
        //ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,result);

        numberEntry.setThreshold(2);
        //numberEntry.setAdapter(adapter);
        numberEntry.addTextChangedListener(this);
        realm.close();
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
        Realm realm = Realm.getDefaultInstance();
        if (!numberEntry.getText().toString().isEmpty()) {
            RealmResults<ContactRealm> result = realm.where(ContactRealm.class)
                    .contains("contact_phone_number", numberEntry.getText().toString())
                    .findAllSorted("contact_name");

            //ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,result);
            adapter_realm = new AdapterContactSuggest(result, getActivity(), this);
            recyclerSuggestions.setAdapter(adapter_realm);
            //if (result.isEmpty() || numberEntry.getText().toString().isEmpty())
            //suggestions.setVisibility(View.GONE);
            //else
            //suggestions.setVisibility(View.VISIBLE);
        } else {
            RealmResults<ContactRealm> result = realm.where(ContactRealm.class)
                    .contains("contact_phone_number", "---------")
                    .findAllSorted("contact_name");

            //ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,result);
            adapter_realm = new AdapterContactSuggest(result, getActivity(), this);
            recyclerSuggestions.setAdapter(adapter_realm);

        }
        realm.close();
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    private void setupRecycler() {
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerSuggestions.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            recyclerSuggestions.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            recyclerSuggestions.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        }
        recyclerSuggestions.setHasFixedSize(true);
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.drawer_help_menu)
                        .customView(R.layout.fragment_acercade, true)
                        .positiveText("OK")
                        .positiveColor(getContext().getResources().getColor(R.color.md_orange_700))
                        .build();
                TextViewHelper version = (TextViewHelper) dialog.getCustomView().findViewById(R.id.app_version);
                version.setText(BuildConfig.VERSION_NAME);
                dialog.show();
                break;


        }
        return super.onOptionsItemSelected(item);
    }


}
