package com.edxavier.cerberus_sms.fragments.callLog.di;

import com.edxavier.cerberus_sms.fragments.callLog.CallLogFragment;
import com.edxavier.cerberus_sms.fragments.contacts.ContactFragment;
import com.edxavier.cerberus_sms.fragments.contacts.di.ContactsModule;
import com.edxavier.cerberus_sms.mLibs.di.LibsModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Eder Xavier Rojas on 18/07/2016.
 */
@Singleton
@Component(modules = {LibsModule.class, CallLogModule.class})
public interface CallLogComponent {
    void inject(CallLogFragment fragment);
}
