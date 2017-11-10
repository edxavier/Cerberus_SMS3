package com.edxavier.cerberus_sms.activities.statistics;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.edxavier.cerberus_sms.DialerActivityV2;
import com.edxavier.cerberus_sms.R;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatisticsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_sts)
    Toolbar toolbarSts;

    StatisticsPresenter presenter;

    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.chart_detail)
    PieChart mChart;
    @BindView(R.id.chart)
    PieChart chart;
    @BindView(R.id.chart_contacts)
    PieChart chartContacts;
    @BindView(R.id.chart_messages)
    PieChart chartMessages;
    @BindView(R.id.incomingCallChart)
    PieChart incomingCallChart;
    @BindView(R.id.outgoingCallChart)
    PieChart outgoingCallChart;
    @BindView(R.id.incomingCallTimeChart)
    PieChart incomingCallTimeChart;
    @BindView(R.id.outgoingCallTimeChart)
    PieChart outgoingCallTimeChart;
    @BindView(R.id.adView)
    AdView adView;

    FirebaseAnalytics analytics;
    @BindView(R.id.adView2)
    AdView adView2;
    @BindView(R.id.adView3)
    AdView adView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ButterKnife.bind(this);
        analytics = FirebaseAnalytics.getInstance(this);
        analytics.logEvent("activity_estadisticas", null);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Estadisticas")
                .putContentId("p06")
                .putContentType("Pantalla"));
        presenter = new StatisticsPresenterImpl(this);
        setSupportActionBar(toolbarSts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.statistics));

        mChart = presenter.setupPieChart(mChart);
        mChart.setData(presenter.setCallEntriesData());

        chart = presenter.setupPieChart(chart);
        chart.setData(presenter.setCallTimesEntriesData());

        chartContacts = presenter.setupPieChart(chartContacts);
        chartContacts.setData(presenter.setContactsEntriesData());

        chartMessages = presenter.setupPieChart(chartMessages);
        chartMessages.setData(presenter.setMessagesEntriesData());

        incomingCallChart = presenter.setupPieChart(incomingCallChart);
        incomingCallChart.setData(presenter.setIncomingCallEntriesData());

        incomingCallTimeChart = presenter.setupPieChart(incomingCallTimeChart);
        incomingCallTimeChart.setData(presenter.setIncomingCallTimeEntriesData());

        outgoingCallChart = presenter.setupPieChart(outgoingCallChart);
        outgoingCallChart.setData(presenter.setOutgoingCallEntriesData());

        outgoingCallTimeChart = presenter.setupPieChart(outgoingCallTimeChart);
        outgoingCallTimeChart.setData(presenter.setOutgoingCallTimeEntriesData());

        mChart.setCenterText(generateCenterSpannableText(getString(R.string.drawer_op_call_log)));
        chart.setCenterText(generateCenterSpannableText(getString(R.string.drawer_op_call_log)));
        chartContacts.setCenterText(generateCenterSpannableText(getString(R.string.drawer_op_contactos)));
        chartMessages.setCenterText(generateCenterSpannableText(getString(R.string.drawer_op_inbox)));
        incomingCallChart.setCenterText(generateCenterSpannableText(getString(R.string.incoming_call_chart)));
        outgoingCallChart.setCenterText(generateCenterSpannableText(getString(R.string.outgoing_call_chart)));
        incomingCallTimeChart.setCenterText(generateCenterSpannableText(getString(R.string.incoming_call_chart)));
        outgoingCallTimeChart.setCenterText(generateCenterSpannableText(getString(R.string.outgoing_call_chart)));
        adView.setVisibility(View.GONE);
        adView2.setVisibility(View.GONE);
        adView3.setVisibility(View.GONE);
        if (!Prefs.getBoolean("ads_removed", false)) {
            setupAds();
            DialerActivityV2.requestAds(this);
        }
    }

    private void setupAds() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });
        adView2.loadAd(adRequest);
        adView2.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView2.setVisibility(View.VISIBLE);
            }
        });

        adView3.loadAd(adRequest);
        adView3.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView3.setVisibility(View.VISIBLE);
            }
        });


    }

    private SpannableString generateCenterSpannableText(String text) {

        SpannableString s = new SpannableString(text);
        s.setSpan(new RelativeSizeSpan(1.5f), 0, text.length(), 0);
        /*s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);*/
        return s;
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
}
