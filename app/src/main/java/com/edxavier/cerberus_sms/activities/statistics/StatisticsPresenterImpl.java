package com.edxavier.cerberus_sms.activities.statistics;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.CallsHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;

import io.realm.RealmResults;

import static com.edxavier.cerberus_sms.helpers.Constans.CLARO;
import static com.edxavier.cerberus_sms.helpers.Constans.CONVENCIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.COOTEL;
import static com.edxavier.cerberus_sms.helpers.Constans.DESCONOCIDO;
import static com.edxavier.cerberus_sms.helpers.Constans.INTERNACIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.MOVISTAR;

/**
 * Created by Eder Xavier Rojas on 12/04/2017.
 */

public class StatisticsPresenterImpl implements StatisticsPresenter {

    Context context;
    StatisticsService service;
    ArrayList<Integer> callColors = new ArrayList<Integer>();
    ArrayList<Integer> incomingCallColors = new ArrayList<Integer>();
    ArrayList<Integer> outgoingCallColors = new ArrayList<Integer>();
    ArrayList<Integer> contactColors = new ArrayList<Integer>();
    ArrayList<Integer> smsColors = new ArrayList<Integer>();

    private static int CALLS = 0;
    private static int CONTACTS = 1;
    private static int MESSAGES = 2;
    private static int INCOMING_CALLS = 3;
    private static int OUTGOING_CALLS = 4;



    public StatisticsPresenterImpl(Context context) {
        this.context = context;
        this.service = new StatisticsServiceimpl();
        RealmResults<CallsHistoryRealm> operators = service.getCallsOperators();
        RealmResults<CallsHistoryRealm> incomingOperators = service.getIncomingCallsOperators();
        RealmResults<CallsHistoryRealm> outgoingOperators = service.getOutgoingCallsOperators();

        RealmResults<ContactRealm> contactOperators = service.getContactsOperators();
        RealmResults<MessagesHistoryRealm> smsOperators = service.getSmsOperators();

        for (CallsHistoryRealm re : operators) {
            callColors.add(getOperatorColor(re.call_operator));
        }
        for (CallsHistoryRealm re : incomingOperators) {
            incomingCallColors.add(getOperatorColor(re.call_operator));
        }
        for (CallsHistoryRealm re : outgoingOperators) {
            outgoingCallColors.add(getOperatorColor(re.call_operator));
        }
        for (ContactRealm contactOperator : contactOperators) {
            contactColors.add(getOperatorColor(contactOperator.contact_operator));
        }
        for (MessagesHistoryRealm smsOperator : smsOperators) {
            smsColors.add(getOperatorColor(smsOperator.sms_operator));
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public PieChart setupPieChart(PieChart mChart) {
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 5, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);


        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(false);
        mChart.setHighlightPerTapEnabled(true);
        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTextSize(12f);
        mChart.setDrawEntryLabels(false);
        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // undo all highlights
        mChart.highlightValues(null);


        return mChart;
    }


    public Integer getOperatorColor(String call_operator) {
        int color = 0;
        switch (call_operator) {
            case CLARO:
                color = ContextCompat.getColor(context, R.color.md_red_700);
                break;
            case COOTEL:
                color = ContextCompat.getColor(context, R.color.md_orange_700);
                break;
            case MOVISTAR:
                color = ContextCompat.getColor(context, R.color.md_green_700);
                break;
            case CONVENCIONAL:
                color = ContextCompat.getColor(context, R.color.md_blue_700);
                break;
            case INTERNACIONAL:
                color = ContextCompat.getColor(context, R.color.md_purple_700);
                break;
            case DESCONOCIDO:
                color = ContextCompat.getColor(context, R.color.md_grey_700);
                break;
        }
        return color;
    }

    @Override
    public PieData setCallEntriesData() {
        ArrayList<PieEntry> entries = service.getCallEntries();
        return setData(entries, CALLS);
    }

    @Override
    public PieData setIncomingCallEntriesData() {
        ArrayList<PieEntry> entries = service.getIncomingCallsEntries();
        return setData(entries, INCOMING_CALLS);
    }

    @Override
    public PieData setOutgoingCallEntriesData() {
        ArrayList<PieEntry> entries = service.getOutgoingCallsEntries();
        return setData(entries, OUTGOING_CALLS);
    }

    @Override
    public PieData setIncomingCallTimeEntriesData() {
        ArrayList<PieEntry> entries = service.getIncomingCallsTimeEntries();
        return setData(entries, INCOMING_CALLS);
    }

    @Override
    public PieData setOutgoingCallTimeEntriesData() {
        ArrayList<PieEntry> entries = service.getOutgoingCallsTimeEntries();
        return setData(entries, OUTGOING_CALLS);
    }


    @Override
    public PieData setCallTimesEntriesData() {
        ArrayList<PieEntry> entries = service.getCallTimeEntries();
        return setData(entries, CALLS);
    }

    @Override
    public PieData setContactsEntriesData() {
        ArrayList<PieEntry> entries = service.getContactsEntries();
        return setData(entries, CONTACTS);
    }

    @Override
    public PieData setMessagesEntriesData() {
        ArrayList<PieEntry> entries = service.getSmsEntries();
        return setData(entries, MESSAGES);
    }

    private PieData setData(ArrayList<PieEntry> entries, int category){
        PieDataSet dataSet = null;
        PieData data = null;
        if(!entries.isEmpty()) {
            dataSet = new PieDataSet(entries, "");
            dataSet.setSliceSpace(3f);
            dataSet.setIconsOffset(new MPPointF(0, 40));
            dataSet.setSelectionShift(5f);

            if(category == CALLS)
                dataSet.setColors(callColors);
            else if(category == CONTACTS)
                dataSet.setColors(contactColors);
            else if(category == MESSAGES)
                dataSet.setColors(smsColors);
            else if(category == INCOMING_CALLS)
                dataSet.setColors(incomingCallColors);
            else if(category == OUTGOING_CALLS)
                dataSet.setColors(outgoingCallColors);

            data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.GRAY);
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        }
        return data;
    }
}
