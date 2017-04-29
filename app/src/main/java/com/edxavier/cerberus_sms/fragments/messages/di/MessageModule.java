package com.edxavier.cerberus_sms.fragments.messages.di;

import android.content.Context;

import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogPresenter;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogService;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogView;
import com.edxavier.cerberus_sms.fragments.callLog.impl.CalllogPresenterImpl;
import com.edxavier.cerberus_sms.fragments.callLog.impl.CalllogServiceImpl;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesPresenter;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesService;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesView;
import com.edxavier.cerberus_sms.fragments.messages.impl.MessagePresenterImpl;
import com.edxavier.cerberus_sms.fragments.messages.impl.MessagesServiceImpl;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Eder Xavier Rojas on 18/07/2016.
 */
@Module
public class MessageModule {
    private MessagesView view;

    public MessageModule(MessagesView view) {
        this.view = view;
    }

    @Provides
    @Singleton
    MessagesView provideMessagesView(){
        return this.view;
    }


    @Provides
    @Singleton
    MessagesPresenter provideMessagesPresenter(EventBusIface eventBus, MessagesView view, MessagesService service){
        return new MessagePresenterImpl(eventBus, service, view);
    }

    @Provides
    @Singleton
    MessagesService provideMessagesService(EventBusIface bus, Context context){
        return new MessagesServiceImpl(context, bus);
    }

}
