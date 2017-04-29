package com.edxavier.cerberus_sms.fragments.checkOperator.di;

import com.edxavier.cerberus_sms.DialerActivity;
import com.edxavier.cerberus_sms.fragments.checkOperator.CheckOperatorFrg;
import com.edxavier.cerberus_sms.mLibs.di.LibsModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Eder Xavier Rojas on 08/07/2016.
 */
@Singleton @Component(modules = {LibsModule.class, CheckOperatorModule.class})
public interface CheckoperatorComponent {
    void inject(CheckOperatorFrg fragment);
    void inject(DialerActivity activity);
}
