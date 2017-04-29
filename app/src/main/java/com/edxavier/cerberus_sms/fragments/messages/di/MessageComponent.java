package com.edxavier.cerberus_sms.fragments.messages.di;

import com.edxavier.cerberus_sms.fragments.callLog.CallLogFragment;
import com.edxavier.cerberus_sms.fragments.callLog.di.CallLogModule;
import com.edxavier.cerberus_sms.fragments.messages.MessagesFragment;
import com.edxavier.cerberus_sms.mLibs.di.LibsModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Eder Xavier Rojas on 18/07/2016.
 */
@Singleton
@Component(modules = {LibsModule.class, MessageModule.class})
public interface MessageComponent {
    void inject(MessagesFragment fragment);
}
