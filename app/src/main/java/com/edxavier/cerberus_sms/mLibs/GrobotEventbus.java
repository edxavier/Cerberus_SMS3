package com.edxavier.cerberus_sms.mLibs;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Eder Xavier Rojas on 26/06/2016.
 */
public class GrobotEventbus implements EventBusIface {
    //Intancia de Libreria EventBus de green robot
    EventBus eventBus;

    public GrobotEventbus() {
        this.eventBus = EventBus.getDefault();
    }

    public void register(Object subscriber){
        eventBus.register(subscriber);
    }

    public void unregister(Object subscriber){
        eventBus.unregister(subscriber);
    }

    public void post(Object event){
        eventBus.post(event);
    }

}
