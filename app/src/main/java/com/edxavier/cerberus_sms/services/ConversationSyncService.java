package com.edxavier.cerberus_sms.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Eder Xavier Rojas on 11/11/2015.
 */
public class ConversationSyncService extends Service {
    static int DELAY = 5000;//5 seg
    private boolean isRunning = false;
    SyncThread syncThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("EDER", "SERVICE");
        syncThread = new SyncThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        isRunning = true;
        try {
            Log.e("EDER", "SERVICE START");
            syncThread.start();
        }catch (Exception e){
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        syncThread.interrupt();
        syncThread = null;
    }



    //******************************************************
    class SyncThread extends Thread{

        SyncThread() {
            super("Cerbero-SyncThread");
        }


        @Override
        public void run() {
            super.run();
            ConversationSyncService syncService = ConversationSyncService.this;

            while (syncService.isRunning){
                try {

                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    syncService.isRunning = false;
                }
            }

        }
    }


}
