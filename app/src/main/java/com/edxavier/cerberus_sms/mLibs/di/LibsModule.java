package com.edxavier.cerberus_sms.mLibs.di;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;


import com.edxavier.cerberus_sms.mLibs.EventBusIface;
import com.edxavier.cerberus_sms.mLibs.GrobotEventbus;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Eder Xavier Rojas on 26/06/2016.
 */
@Module
public class LibsModule {

    Context context;
    //****** CONSTRUCTORES *******

    public LibsModule(Fragment fragment) {
        this.context = fragment.getContext();
    }

    public LibsModule(Activity activity) {
        this.context = activity;
    }
    //****** FIN CONSTRUCTORES *******

    @Provides
    @Singleton
    FirebaseAnalytics provideFirebaseAnalytics(Context context){
        return FirebaseAnalytics.getInstance(context);
    }

    @Provides
    @Singleton
    EventBusIface provideGrobotEventbus(){
        return new GrobotEventbus();
    }

    @Provides @Singleton
    Context provideContext(){
        return this.context;
    }
}
