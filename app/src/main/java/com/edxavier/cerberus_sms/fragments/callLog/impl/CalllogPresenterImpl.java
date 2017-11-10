package com.edxavier.cerberus_sms.fragments.callLog.impl;

import android.Manifest;
import android.os.Build;
import android.util.Log;

import com.edxavier.cerberus_sms.db.realm.CallsRealm;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogPresenter;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogService;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogView;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 18/07/2016.
 */
public class CalllogPresenterImpl implements CallLogPresenter {
    private CallLogService service;
    private CallLogView view;

    public CalllogPresenterImpl(EventBusIface eventBus, CallLogService service, CallLogView view) {
        this.service = service;
        this.view = view;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {
        view = null;
        service.onDestroy();
    }

    @Override
    public boolean hasReadCalllogPermission() {
        return service.hasReadCalllogPermission();
    }

    @Override
    public boolean hasWriteCallLogPermission() {
        return service.hasWriteCallLogPermission();
    }


    @Override
    public void getCallsFromRealm() {
        RealmResults<CallsRealm> res = service.getCallsFromRealm();
        if(res.isEmpty())
            view.showEmptyMsg(true);
        else
            view.showEmptyMsg(false);
        view.setCallsData(res);
    }

    @Override
    public void syncCalls() {
        service.syncCallsToRealm();
    }

    @Override
    public void clearRecords() {
        service.clearRecords();
    }

    @Override
    public int clearPhoneReacords() {
       return service.clearPhoneReacords();
    }

    @Override
    public int clearContactReacords(String number) {
        return service.clearContactReacords(number);
    }



    @Override
    public void requestPermission(String[] perms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.requestPermission(new String[]{Manifest.permission.WRITE_CALL_LOG});
        }
    }

    @Override
    public void sendToBlackList(int options, String number) {
        service.sendToBlackList(options, number);
    }
}
