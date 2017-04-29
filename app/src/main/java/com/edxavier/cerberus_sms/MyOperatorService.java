package com.edxavier.cerberus_sms;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import com.edxavier.cerberus_sms.fragments.callLog.receivers.CallHandler;
import com.edxavier.cerberus_sms.fragments.callLog.receivers.PhonecallReceiver;

public class MyOperatorService extends Service {
    PhonecallReceiver callHandler;
    UpdaterThread updaterThread ;
    private boolean isRunning = false;

    public MyOperatorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("EDER", "onCreate");
        callHandler = new CallHandler();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(callHandler, filter);
        updaterThread = new UpdaterThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e("EDER", "onStartCommand");
        isRunning = true;
        try {
            //updaterThread.start();
        }catch (Exception e){
            Log.e("EDER", "onStartCommand ERR");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(callHandler);
        Log.e("EDER", "onDestroy");
        isRunning = false;
    }

    //******************************************************
    private class UpdaterThread extends Thread {

        UpdaterThread() {
            super("KfcService-UpdaterThread");
        }


        @Override
        public void run() {
            super.run();
            MyOperatorService kfcService = MyOperatorService.this;
            int i= 0;
            while (kfcService.isRunning) {
                Log.e("EDER", "Thread " + i++);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}