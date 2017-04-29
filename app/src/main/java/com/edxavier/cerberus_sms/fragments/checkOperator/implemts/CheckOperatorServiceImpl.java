package com.edxavier.cerberus_sms.fragments.checkOperator.implemts;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorService;
import com.edxavier.cerberus_sms.helpers.Utils;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;

/**
 * Created by Eder Xavier Rojas on 08/07/2016.
 */
public class CheckOperatorServiceImpl implements CheckOperatorService {
    private EventBusIface eventBus;
    private Context context;

    public CheckOperatorServiceImpl(EventBusIface eventBus, Context context) {
        this.eventBus = eventBus;
        this.context = context;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public AreaCodeRealm checkOperator(String phoneNumber) {
        return Utils.getOperadoraV4(phoneNumber, context);
    }


    @Override
    public boolean checkDBinitialization() {
        return false;
    }

    @Override
    public void dbInitialization() {

    }

    @Override
    public void InitializeRealmAreacodes() {
/*        if(InitAreaCode.AreacodeEmpty())
            InitAreaCode.initAreaCodes(context);
        else
            Log.e("EDER", "RealmAreacodes Initialized");
            */
    }

    @Override
    public boolean isReadContactsPermsGranted() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void syncronizeContacts() {
    }
}
