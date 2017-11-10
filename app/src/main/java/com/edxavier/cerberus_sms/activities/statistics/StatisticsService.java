package com.edxavier.cerberus_sms.activities.statistics;

import com.edxavier.cerberus_sms.db.realm.CallsHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 12/04/2017.
 */

public interface StatisticsService {
    void onResume();
    void onDestroy();
    RealmResults<CallsHistoryRealm> getCallsOperators();
    RealmResults<CallsHistoryRealm> getOutgoingCallsOperators();
    RealmResults<CallsHistoryRealm> getIncomingCallsOperators();

    RealmResults<ContactRealm> getContactsOperators();
    RealmResults<MessagesHistoryRealm> getSmsOperators();


    ArrayList<PieEntry>getCallEntries();
    ArrayList<PieEntry>getOutgoingCallsEntries();
    ArrayList<PieEntry>getIncomingCallsEntries();
    ArrayList<PieEntry>getOutgoingCallsTimeEntries();
    ArrayList<PieEntry>getIncomingCallsTimeEntries();

    ArrayList<PieEntry>getContactsEntries();
    ArrayList<PieEntry>getCallTimeEntries();
    ArrayList<PieEntry>getSmsEntries();


}
