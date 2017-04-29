package com.edxavier.cerberus_sms.fragments.checkOperator.implemts;

import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorPresenter;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorService;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorView;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;

import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Eder Xavier Rojas on 08/07/2016.
 */
public class CheckOperatorPresenterImpl implements CheckOperatorPresenter {
    private EventBusIface eventBus;
    private CheckOperatorView view;
    private CheckOperatorService service;

    public CheckOperatorPresenterImpl(EventBusIface eventBus, CheckOperatorView view, CheckOperatorService service) {
        this.eventBus = eventBus;
        this.view = view;
        this.service = service;
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
    }



    @Override
    public AreaCodeRealm checkOperator(String phoneNumber) {
        return service.checkOperator(phoneNumber);
    }

    @Override
    public boolean checkDBinitialization() {
        return service.checkDBinitialization();
    }

    @Override
    public void dbInitialization() {
        service.dbInitialization();
    }


    @Override
    public boolean isReadContactsPermsGranted() {
        return service.isReadContactsPermsGranted();
    }


    @Override
    public void syncronizeContacts() {
        service.syncronizeContacts();
    }


}
