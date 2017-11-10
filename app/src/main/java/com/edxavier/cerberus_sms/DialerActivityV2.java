package com.edxavier.cerberus_sms;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.balysv.materialripple.MaterialRippleLayout;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.edxavier.cerberus_sms.activities.black_list.BlackListActivity;
import com.edxavier.cerberus_sms.activities.statistics.StatisticsActivity;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.fragments.callLog.CallLogFragment;
import com.edxavier.cerberus_sms.fragments.checkOperator.CheckOperatorFrg;
import com.edxavier.cerberus_sms.fragments.contacts.ContactFragment;
import com.edxavier.cerberus_sms.fragments.messages.MessagesFragment;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.pixplicity.easyprefs.library.Prefs;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.TabSelectionInterceptor;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class DialerActivityV2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TabSelectionInterceptor, BillingProcessor.IBillingHandler {

    private static InterstitialAd mInterstitialAd;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fragmentContainer)
    FrameLayout fragmentContainer;


    @BindView(R.id.bottomBar)
    public BottomBar bottomBar;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    FirebaseAnalytics analytics;
    FragmentManager manager;
    Fragment[] fragments = new Fragment[]{new ContactFragment(), new CallLogFragment(), new MessagesFragment(), new CheckOperatorFrg()};
    private static final int REQUEST_SYSTEM_ALERT_WINDOW = 2;
    public BillingProcessor bp;
    public static String PRODUCT = "remove_ads";
    @BindView(R.id.main_coordinator)
    CoordinatorLayout mainCoordinator;
    private RealmResults<MessagesHistoryRealm> messages = null;
    Realm realm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer_v2);
        ButterKnife.bind(this);

        bp = BillingProcessor.newBillingProcessor(this, BuildConfig.APP_BILLING_PUB_KEY, BuildConfig.MERCHANT_ID, this);
        bp.initialize();

        setupToolbar(savedInstanceState);
        checkDrawWindowPermission();
        ConstraintLayout headerNav = (ConstraintLayout) navView.getHeaderView(0);
        TextView version = (TextView) headerNav.findViewById(R.id.app_version);
        version.setText(BuildConfig.VERSION_NAME);
        if (Prefs.getBoolean("ads_removed", false))
            hideItem();

        Answers.getInstance().logCustom(new CustomEvent("Open app")
                .putCustomAttribute("SDK", Build.VERSION.SDK)
                .putCustomAttribute("SDK_NAME", Build.VERSION.RELEASE)
                .putCustomAttribute("MARCA", Build.BRAND)
                .putCustomAttribute("MODELO", Build.MODEL)
        );
    }

    private void checkDrawWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

    void setupToolbar(Bundle savedInstanceState) {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);


        manager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            manager.beginTransaction()
                    .add(R.id.fragmentContainer, fragments[1], "calls")
                    .add(R.id.fragmentContainer, fragments[0], "contacts")
                    .add(R.id.fragmentContainer, fragments[2], "messages")
                    .add(R.id.fragmentContainer, fragments[3], "dialer")
                    //.hide(fragments[0])
                    //.hide(fragments[2])
                    .commit();
        }


        bottomBar.setTabSelectionInterceptor(this);
        analytics = FirebaseAnalytics.getInstance(this);

            messages = realm.where(MessagesHistoryRealm.class)
                .equalTo("sms_read", Constans.MESSAGE_UNREAD).findAll();

        messages.addChangeListener((messagesHistoryRealms, changeSet) -> {
            MessagesFragment frg2 = (MessagesFragment) manager.findFragmentByTag("messages");
            int unrd = frg2.getUnreadedMessages();
            if(unrd>0)
                bottomBar.getTabWithId(R.id.tab_messages).setBadgeCount(unrd);
            else
                bottomBar.getTabWithId(R.id.tab_messages).removeBadge();
        });

        //esto es para ejecutar el evento tabselect ya que si nom aparecen superpuestos los fragments
        Observable.interval(300, TimeUnit.MILLISECONDS).take(1)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            //bottomBar.setDefaultTab(R.id.tab_messages);
            bottomBar.getTabWithId(R.id.tab_calls).callOnClick();
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.dialer_activity_v2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_charts:
                startActivity(new Intent(this, StatisticsActivity.class));
                break;
            case R.id.nav_black_list:
                startActivity(new Intent(this, BlackListActivity.class));
                break;
            case R.id.ac_drawer_app_info:
                analytics.logEvent("click_app_info", null);
                try {
                    //Open the specific App Info page:
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    //e.printStackTrace();
                    //Open the generic Apps page:
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);

                }
                break;
            case R.id.nav_remove_ads:
                analytics.logEvent("click_remove_ad", null);
                if (bp != null) {
                    try {
                        bp.purchase(this, PRODUCT);
                    }catch (Exception ignored){}
                }
                break;
            case R.id.nav_share:
                try {
                    analytics.logEvent("ac_share", null);
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                    String sAux = getResources().getString(R.string.share_app_msg);
                    sAux = sAux + "https://play.google.com/store/apps/details?id=" + getPackageName() + " \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, getResources().getString(R.string.share_using)));
                } catch (Exception ignored) {
                }
                break;

            case R.id.nav_rate:
                analytics.logEvent("ac_rate", null);
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                break;
            case R.id.ac_drawer_issues:
                //bp.consumePurchase(PRODUCT);
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "edxavier05@gmail.com", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                intent.putExtra(Intent.EXTRA_TEXT, "Escriba sus comentarios, sugerencias, reporte de error o peticiones");

                try {
                    startActivity(Intent.createChooser(intent, "Send mail..."));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                analytics.logEvent("ac_report_issue", null);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean shouldInterceptTabSelection(@IdRes int oldTabId, @IdRes int newTabId) {
        //if(oldTabId != newTabId) {
        try {
            MessagesFragment frg2 = (MessagesFragment) manager.findFragmentByTag("messages");
            int unrd = frg2.getUnreadedMessages();
            if(unrd>0)
                bottomBar.getTabWithId(R.id.tab_messages).setBadgeCount(unrd);
            else
                bottomBar.getTabWithId(R.id.tab_messages).removeBadge();
            switch (newTabId) {
                case R.id.tab_calls:
                    analytics.logEvent("frag_calls", null);
                    getSupportActionBar().setTitle(getString(R.string.drawer_op_call_log));
                    CallLogFragment frg = (CallLogFragment) manager.findFragmentByTag("calls");
                    frg.checkSMSsettings();
                    manager.beginTransaction()
                            .show(manager.findFragmentByTag("calls"))
                            .hide(manager.findFragmentByTag("contacts"))
                            .hide(manager.findFragmentByTag("messages"))
                            .hide(manager.findFragmentByTag("dialer"))
                            .commit();
                    if (!Prefs.getBoolean("ads_removed", false)) {
                        DialerActivityV2.requestAds(this);
                    } else
                        hideFragmentsAds();
                    break;
                case R.id.tab_contacts:
                    analytics.logEvent("frag_contacts", null);
                    getSupportActionBar().setTitle(getString(R.string.drawer_op_contactos));
                    manager.beginTransaction()
                            .hide(manager.findFragmentByTag("calls"))
                            .show(manager.findFragmentByTag("contacts"))
                            .hide(manager.findFragmentByTag("messages"))
                            .hide(manager.findFragmentByTag("dialer"))
                            .commit();
                    if (!Prefs.getBoolean("ads_removed", false)) {
                        DialerActivityV2.requestAds(this);
                    } else
                        hideFragmentsAds();
                    break;

                case R.id.tab_messages:
                    analytics.logEvent("frag_sms", null);
                    getSupportActionBar().setTitle(getString(R.string.drawer_op_inbox));
                    frg2.checkSMSsettings();
                    manager.beginTransaction()
                            .hide(manager.findFragmentByTag("calls"))
                            .hide(manager.findFragmentByTag("contacts"))
                            .show(manager.findFragmentByTag("messages"))
                            .hide(manager.findFragmentByTag("dialer"))
                            .commit();
                    if (!Prefs.getBoolean("ads_removed", false)) {
                        DialerActivityV2.requestAds(this);
                    } else
                        hideFragmentsAds();
                    break;
                case R.id.tab_chart:
                    analytics.logEvent("frag_dialer", null);
                    getSupportActionBar().setTitle(getString(R.string.drawer_op_dialer));
                    manager.beginTransaction()
                            .hide(manager.findFragmentByTag("calls"))
                            .hide(manager.findFragmentByTag("contacts"))
                            .hide(manager.findFragmentByTag("messages"))
                            .show(manager.findFragmentByTag("dialer"))
                            .commit();
                    if (!Prefs.getBoolean("ads_removed", false)) {
                        DialerActivityV2.requestAds(this);
                    } else
                        hideFragmentsAds();
                    break;
            }
        } catch (Exception e) {
            if(e.getMessage()!=null)
                Answers.getInstance().logCustom(new CustomEvent("Error")
                        .putCustomAttribute("location", e.getLocalizedMessage()));
        }
        //}

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        analytics.logEvent("ads_removed", null);
        Prefs.putBoolean("ads_removed", true);
        hideItem();
        hideFragmentsAds();
    }

    private void hideFragmentsAds() {
        try {
            CallLogFragment callFrg = (CallLogFragment) manager.findFragmentByTag("calls");
            ContactFragment contFrg = (ContactFragment) manager.findFragmentByTag("contacts");
            MessagesFragment smsFrg = (MessagesFragment) manager.findFragmentByTag("messages");
            CheckOperatorFrg dialFrg = (CheckOperatorFrg) manager.findFragmentByTag("dialer");

            callFrg.adView.setVisibility(View.GONE);
            dialFrg.adView.setVisibility(View.GONE);
            contFrg.adView.setVisibility(View.GONE);
            smsFrg.adView.setVisibility(View.GONE);
        }catch (Exception ignored){}
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.e("EDER", "onBillingError");
    }

    @Override
    public void onBillingInitialized() {
        loadInterstical();
        if (bp != null) {
            Prefs.putBoolean("ads_removed", bp.isPurchased(PRODUCT));
            //if(bp.isOneTimePurchaseSupported()) {
            if (Prefs.getBoolean("ads_removed", false)) {
                hideItem();
                hideFragmentsAds();
            }
        }
        //Log.e("EDER_ads_removed", String.valueOf(Prefs.getBoolean("ads_removed", false)));
        //}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();
        super.onDestroy();
        messages.removeAllChangeListeners();
        if(!realm.isClosed())
            realm.close();
    }

    private void hideItem() {
        Menu nav_Menu = navView.getMenu();
        nav_Menu.findItem(R.id.nav_remove_ads).setVisible(false);
    }

    public static void requestAds(Context context) {

        int ne = Prefs.getInt("num_show_interstical", 0);
        Prefs.putInt("num_show_interstical", ne + 1);
        if (Prefs.getInt("num_show_interstical", 0) >= Prefs.getInt("show_after", 8)) {
            Prefs.putInt("num_show_interstical", 0);
            Random r = new Random();
            int Low = 8;
            int High = 16;
            int rnd = r.nextInt(High - Low) + Low;
            Prefs.putInt("show_after", rnd);
            try {
                if(mInterstitialAd.isLoaded()) {
                    MaterialDialog dlg = new MaterialDialog.Builder(context)
                            .title(R.string.ads_notice)
                            .cancelable(false)
                            .progress(true, 0)
                            .progressIndeterminateStyle(true)
                            .build();
                    dlg.show();
                    Observable.interval(1, TimeUnit.MILLISECONDS).take(2400)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aLong -> {},
                                    throwable -> {}, () -> {
                                if(dlg.isShowing())
                                    dlg.dismiss();
                                mInterstitialAd.show();
                                Answers.getInstance().logContentView(new ContentViewEvent()
                                                .putContentName("Show Intersticall")
                                                .putContentId("adsInterstical")
                                                .putContentType("Ads"));
                            });
                }
            }catch (Exception ignored){}

        }
    }

    public  void loadInterstical(){

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                .build();
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(getApplicationContext().getResources().getString(R.string.id_banner_interstical));
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                loadInterstical();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                //SystemClock.sleep(5000);
                Observable.interval(1, TimeUnit.SECONDS).take(5)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {},
                        throwable -> {}, () -> {
                                    loadInterstical();
                        });
            }
        });
        if(!mInterstitialAd.isLoaded())
            mInterstitialAd.loadAd(adRequest);
    }

}
