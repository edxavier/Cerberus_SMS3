package com.edxavier.cerberus_sms.fragments.contacts.di;

import android.content.Context;

import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorPresenter;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorService;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorView;
import com.edxavier.cerberus_sms.fragments.checkOperator.implemts.CheckOperatorPresenterImpl;
import com.edxavier.cerberus_sms.fragments.checkOperator.implemts.CheckOperatorServiceImpl;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsPresenter;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsService;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsView;
import com.edxavier.cerberus_sms.fragments.contacts.impl.ContactsPresenterImpl;
import com.edxavier.cerberus_sms.fragments.contacts.impl.ContactsServiceImpl;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Eder Xavier Rojas on 08/07/2016.
 */
@Module
public class ContactsModule {
    private ContactsView view;

    public ContactsModule(ContactsView view) {
        this.view = view;
    }

    @Provides @Singleton
    ContactsView provideCheckOperatorView(){
        return this.view;
    }

    @Provides @Singleton
    ContactsPresenter provideContactsPresenter(EventBusIface eventBus, ContactsView view, ContactsService service){
        return new ContactsPresenterImpl(eventBus, view, service);
    }

    @Provides @Singleton
    ContactsService provideCheckOperatorService(EventBusIface eventBus, Context context){
        return new ContactsServiceImpl(eventBus, context);
    }


}
