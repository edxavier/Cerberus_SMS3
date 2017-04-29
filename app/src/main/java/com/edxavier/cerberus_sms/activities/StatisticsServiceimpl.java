package com.edxavier.cerberus_sms.activities;

import android.provider.CallLog;

import com.edxavier.cerberus_sms.db.realm.CallsHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 12/04/2017.
 */

public class StatisticsServiceimpl implements StatisticsService {
    private Realm realm;
    private RealmResults<CallsHistoryRealm> callsRes;
    public RealmResults<CallsHistoryRealm> outgoingCallsRes;
    public RealmResults<CallsHistoryRealm> incomingCallsRes;

    private RealmResults<ContactRealm> contactsRes;
    private RealmResults<MessagesRealm> messagesRes;



    public StatisticsServiceimpl() {
        realm = Realm.getDefaultInstance();
        this.callsRes = realm.where(CallsHistoryRealm.class)
                .distinct("call_operator").sort("call_operator", Sort.ASCENDING);
        this.outgoingCallsRes = realm.where(CallsHistoryRealm.class)
                .equalTo("call_direction", android.provider.CallLog.Calls.OUTGOING_TYPE)
                .distinct("call_operator").sort("call_operator", Sort.ASCENDING);
        this.incomingCallsRes = realm.where(CallsHistoryRealm.class)
                .equalTo("call_direction", android.provider.CallLog.Calls.INCOMING_TYPE)
                .distinct("call_operator").sort("call_operator", Sort.ASCENDING);

        this.contactsRes = realm.where(ContactRealm.class)
                .distinct("contact_operator").sort("contact_operator", Sort.ASCENDING);
        this.messagesRes = realm.where(MessagesRealm.class)
                .distinct("sms_operator").sort("sms_operator", Sort.ASCENDING);
    }

    @Override
    public void onResume() {}

    @Override
    public void onDestroy() {
        realm.close();
    }

    @Override
    public RealmResults<CallsHistoryRealm> getCallsOperators() {
        return callsRes;
    }

    @Override
    public RealmResults<CallsHistoryRealm> getOutgoingCallsOperators() {
        return outgoingCallsRes;
    }

    @Override
    public RealmResults<CallsHistoryRealm> getIncomingCallsOperators() {
        return incomingCallsRes;
    }

    @Override
    public RealmResults<ContactRealm> getContactsOperators() {
        return contactsRes;
    }

    @Override
    public RealmResults<MessagesRealm> getSmsOperators() {
        return messagesRes;
    }

    @Override
    public ArrayList<PieEntry> getCallEntries() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        for (CallsHistoryRealm re : callsRes) {
            long total = realm.where(CallsHistoryRealm.class).equalTo("call_operator",
                    re.call_operator).count();
            entries.add(new PieEntry(total, re.call_operator));
        }
        return entries;
    }

    @Override
    public ArrayList<PieEntry> getOutgoingCallsEntries() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        for (CallsHistoryRealm re : outgoingCallsRes) {
            long total = realm.where(CallsHistoryRealm.class)
                    .equalTo("call_operator", re.call_operator)
                    .equalTo("call_direction", android.provider.CallLog.Calls.OUTGOING_TYPE)
                    .count();
            entries.add(new PieEntry(total, re.call_operator));
        }
        return entries;
    }

    @Override
    public ArrayList<PieEntry> getIncomingCallsEntries() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        for (CallsHistoryRealm re : incomingCallsRes) {
            long total = realm.where(CallsHistoryRealm.class)
                    .equalTo("call_operator", re.call_operator)
                    .equalTo("call_direction", android.provider.CallLog.Calls.INCOMING_TYPE)
                    .count();
            entries.add(new PieEntry(total, re.call_operator));
        }
        return entries;
    }

    @Override
    public ArrayList<PieEntry> getOutgoingCallsTimeEntries() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        for (CallsHistoryRealm re : outgoingCallsRes) {
            Number total = realm.where(CallsHistoryRealm.class)
                    .equalTo("call_operator", re.call_operator)
                    .equalTo("call_direction", CallLog.Calls.OUTGOING_TYPE)
                    .sum("call_duration");
            entries.add(new PieEntry(total.longValue(), re.call_operator));
        }
        return entries;
    }

    @Override
    public ArrayList<PieEntry> getIncomingCallsTimeEntries() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        for (CallsHistoryRealm re : incomingCallsRes) {
            Number total = realm.where(CallsHistoryRealm.class)
                    .equalTo("call_operator", re.call_operator)
                    .equalTo("call_direction", CallLog.Calls.INCOMING_TYPE)
                    .sum("call_duration");
            entries.add(new PieEntry(total.longValue(), re.call_operator));
        }
        return entries;
    }

    @Override
    public ArrayList<PieEntry> getContactsEntries() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        for (ContactRealm re : contactsRes) {
            long total = realm.where(ContactRealm.class).equalTo("contact_operator",
                    re.contact_operator).count();
            entries.add(new PieEntry(total, re.contact_operator));
        }
        return entries;
    }

    @Override
    public ArrayList<PieEntry> getCallTimeEntries() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        for (CallsHistoryRealm re : callsRes) {
            Number time = realm.where(CallsHistoryRealm.class).equalTo("call_operator",
                    re.call_operator).sum("call_duration");
            entries.add(new PieEntry(time.longValue(), re.call_operator));
        }
        return entries;
    }

    @Override
    public ArrayList<PieEntry> getSmsEntries() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        for (MessagesRealm re : messagesRes) {
            long total = realm.where(MessagesHistoryRealm.class).equalTo("sms_operator",
                    re.sms_operator).count();
            entries.add(new PieEntry(total, re.sms_operator));
        }
        return entries;
    }
}
