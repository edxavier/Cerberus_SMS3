package com.edxavier.cerberus_sms.fragments.callLog.di;

import android.content.Context;

import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogPresenter;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogService;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogView;
import com.edxavier.cerberus_sms.fragments.callLog.impl.CalllogPresenterImpl;
import com.edxavier.cerberus_sms.fragments.callLog.impl.CalllogServiceImpl;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsView;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Eder Xavier Rojas on 18/07/2016.
 */
@Module
public class CallLogModule {
    private CallLogView view;

    public CallLogModule(CallLogView view) {
        this.view = view;
    }

    @Provides
    @Singleton
    CallLogView provideCheckOperatorView(){
        return this.view;
    }


    @Provides
    @Singleton
    CallLogPresenter provideCallLogPresenter(EventBusIface eventBus, CallLogView view, CallLogService service){
        return new CalllogPresenterImpl(eventBus, service, view);
    }

    @Provides
    @Singleton
    CallLogService provideCallLogService(EventBusIface bus, Context context){
        return new CalllogServiceImpl(bus, context);
    }

}
