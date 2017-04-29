package com.edxavier.cerberus_sms.fragments.checkOperator.di;

import android.content.Context;

import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorPresenter;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorService;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorView;
import com.edxavier.cerberus_sms.fragments.checkOperator.implemts.CheckOperatorPresenterImpl;
import com.edxavier.cerberus_sms.fragments.checkOperator.implemts.CheckOperatorServiceImpl;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;
import com.edxavier.cerberus_sms.mLibs.GrobotEventbus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Eder Xavier Rojas on 08/07/2016.
 */
@Module
public class CheckOperatorModule {
    private CheckOperatorView view;

    public CheckOperatorModule(CheckOperatorView view) {
        this.view = view;
    }

    @Provides @Singleton
    CheckOperatorView provideCheckOperatorView(){
        return this.view;
    }

    @Provides @Singleton
    CheckOperatorPresenter provideCheckOperatorPresenter(EventBusIface eventBus, CheckOperatorView view, CheckOperatorService service){
        return new CheckOperatorPresenterImpl(eventBus, view, service);
    }

    @Provides @Singleton
    CheckOperatorService provideCheckOperatorService(EventBusIface eventBus, Context context){
        return new CheckOperatorServiceImpl(eventBus, context);
    }


}
