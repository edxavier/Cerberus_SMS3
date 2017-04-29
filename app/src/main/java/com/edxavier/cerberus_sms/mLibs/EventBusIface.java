package com.edxavier.cerberus_sms.mLibs;

/**
 * Created by Eder Xavier Rojas on 07/07/2016.
 */
public interface EventBusIface {
    void register(Object subscriber);
    void unregister(Object subscriber);
    void post(Object event);
}
