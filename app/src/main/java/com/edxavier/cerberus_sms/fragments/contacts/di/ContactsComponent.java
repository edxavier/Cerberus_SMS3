package com.edxavier.cerberus_sms.fragments.contacts.di;

import com.edxavier.cerberus_sms.fragments.checkOperator.CheckOperatorFrg;
import com.edxavier.cerberus_sms.fragments.contacts.ContactFragment;
import com.edxavier.cerberus_sms.mLibs.di.LibsModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Eder Xavier Rojas on 08/07/2016.
 */
@Singleton @Component(modules = {LibsModule.class, ContactsModule.class})
public interface ContactsComponent {
    void inject(ContactFragment fragment);
}
