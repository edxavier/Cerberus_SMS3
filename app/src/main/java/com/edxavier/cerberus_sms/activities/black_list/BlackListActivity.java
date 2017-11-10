package com.edxavier.cerberus_sms.activities.black_list;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.edxavier.cerberus_sms.DialerActivityV2;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.activities.black_list.contracts.BlackListPresenter;
import com.edxavier.cerberus_sms.activities.black_list.contracts.BlackListPresenterImp;
import com.edxavier.cerberus_sms.activities.black_list.contracts.BlackListView;
import com.edxavier.cerberus_sms.db.realm.BlackList;
import com.edxavier.cerberus_sms.helpers.TextViewHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class BlackListActivity extends AppCompatActivity implements BlackListView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    FirebaseAnalytics analytics;
    @BindView(R.id._recycler)
    RecyclerView BlackListRecycler;
    @BindView(R.id.empty_message)
    TextViewHelper emptyMessage;
    @BindView(R.id.empty_list_layout)
    LinearLayout emptyListLayout;
    BlackListAdapter adapter;
    @BindView(R.id.adView)
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_list);
        ButterKnife.bind(this);
        analytics = FirebaseAnalytics.getInstance(this);
        analytics.logEvent("activity_black_list", null);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Lista negra")
                .putContentId("p03")
                .putContentType("Pantalla"));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.black_list));
        BlackListPresenter presenter = new BlackListPresenterImp(this);

        setupRecycler();
        presenter.getBlackList();

        if (!Prefs.getBoolean("ads_removed", false)) {
            setupAds();
            DialerActivityV2.requestAds(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecycler() {
        BlackListRecycler.setLayoutManager(new LinearLayoutManager(this));
        BlackListRecycler.setHasFixedSize(true);
    }

    @Override
    public void showEmptyMsg(boolean show) {
        if (show) {
            emptyListLayout.setVisibility(View.VISIBLE);
        } else {
            emptyListLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void setCallsData(RealmResults<BlackList> blackList) {
        adapter = new BlackListAdapter(blackList, this);
        AlphaInAnimationAdapter slideAdapter = new AlphaInAnimationAdapter(adapter);
        slideAdapter.setDuration(250);
        slideAdapter.setInterpolator(new OvershootInterpolator(1f));
        slideAdapter.setFirstOnly(false);
        BlackListRecycler.setItemAnimator(new LandingAnimator());
        BlackListRecycler.getItemAnimator().setAddDuration(200);
        BlackListRecycler.setAdapter(adapter);
    }

    public void setupAds() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                .build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                BlackListRecycler.setPadding(0,0,0,96);
            }
        });
        adView.loadAd(adRequest);
    }
}
