package com.edxavier.cerberus_sms.activities;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

/**
 * Created by Eder Xavier Rojas on 12/04/2017.
 */

public interface StatisticsPresenter {
    void onResume();
    void onPause();
    void onDestroy();

    PieChart setupPieChart(PieChart chart);

    Integer getOperatorColor(String call_operator);

    PieData setCallEntriesData();
    PieData setIncomingCallEntriesData();
    PieData setOutgoingCallEntriesData();

    PieData setIncomingCallTimeEntriesData();
    PieData setOutgoingCallTimeEntriesData();

    PieData setCallTimesEntriesData();

    PieData setContactsEntriesData();

    PieData setMessagesEntriesData();


}
