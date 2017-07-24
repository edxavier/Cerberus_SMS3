package com.edxavier.cerberus_sms;

import android.app.Activity;
import android.app.Application;
import android.content.ContextWrapper;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.edxavier.cerberus_sms.db.realm.MigrationRealm;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogView;
import com.edxavier.cerberus_sms.fragments.callLog.di.CallLogComponent;
import com.edxavier.cerberus_sms.fragments.callLog.di.CallLogModule;
import com.edxavier.cerberus_sms.fragments.callLog.di.DaggerCallLogComponent;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorView;
import com.edxavier.cerberus_sms.fragments.checkOperator.di.CheckOperatorModule;
import com.edxavier.cerberus_sms.fragments.checkOperator.di.CheckoperatorComponent;
import com.edxavier.cerberus_sms.fragments.checkOperator.di.DaggerCheckoperatorComponent;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsView;
import com.edxavier.cerberus_sms.fragments.contacts.di.ContactsComponent;
import com.edxavier.cerberus_sms.fragments.contacts.di.ContactsModule;
import com.edxavier.cerberus_sms.fragments.contacts.di.DaggerContactsComponent;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesView;
import com.edxavier.cerberus_sms.fragments.messages.di.DaggerMessageComponent;
import com.edxavier.cerberus_sms.fragments.messages.di.MessageComponent;
import com.edxavier.cerberus_sms.fragments.messages.di.MessageModule;
import com.edxavier.cerberus_sms.mLibs.di.LibsModule;
import com.google.android.gms.ads.MobileAds;
import com.pixplicity.easyprefs.library.Prefs;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Eder Xavier Rojas on 07/07/2016.
 */
public class AppOperator extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        //MobileAds.initialize(this, "ca-app-pub-9964109306515647~3887839019");
        MultiDex.install(this);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration
                .Builder()
                .schemaVersion(2) // Must be bumped when the schema changes
                .migration(new MigrationRealm()) // Migration to run instead of throwing an exception
                .build();
        try {
            Realm.compactRealm(realmConfiguration);
        }catch (Exception ignored){}
        Realm.setDefaultConfiguration(realmConfiguration);

        //startService(new Intent(this,MyOperatorService.class));

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //stopService(new Intent(this,MyOperatorService.class));
    }
    public CheckoperatorComponent getCheckoperatorComponent(Fragment fragment, CheckOperatorView view){
        return DaggerCheckoperatorComponent.builder()
                .libsModule(new LibsModule(fragment))
                .checkOperatorModule(new CheckOperatorModule(view))
                .build();
    }

    public CheckoperatorComponent getCheckoperatorComponent(Activity activity, CheckOperatorView view){
        return DaggerCheckoperatorComponent.builder()
                .libsModule(new LibsModule(activity))
                .checkOperatorModule(new CheckOperatorModule(view))
                .build();
    }

    public ContactsComponent getContactComponent(Fragment fragment, ContactsView view){
        return DaggerContactsComponent.builder()
                .libsModule(new LibsModule(fragment))
                .contactsModule(new ContactsModule(view))
                .build();
    }
    public CallLogComponent getCallLogComponent(Fragment fragment, CallLogView view){
        return DaggerCallLogComponent.builder()
                .libsModule(new LibsModule(fragment))
                .callLogModule(new CallLogModule(view)).build();
    }
    public MessageComponent getMessageComponent(Fragment fragment, MessagesView view){
        return DaggerMessageComponent.builder()
                .libsModule(new LibsModule(fragment))
                .messageModule(new MessageModule(view)).build();
    }

    /*
*/
}
