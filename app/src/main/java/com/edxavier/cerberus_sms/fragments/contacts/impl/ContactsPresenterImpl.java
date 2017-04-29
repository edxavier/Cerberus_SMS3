package com.edxavier.cerberus_sms.fragments.contacts.impl;

import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsPresenter;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsService;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsView;
import com.edxavier.cerberus_sms.mLibs.EventBusIface;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 09/07/2016.
 */
public class ContactsPresenterImpl implements ContactsPresenter {
    private EventBusIface eventBus;
    private ContactsView view;
    private ContactsService service;

    public ContactsPresenterImpl(EventBusIface eventBus, ContactsView view, ContactsService service) {
        this.eventBus = eventBus;
        this.view = view;
        this.service = service;
    }

    @Override
    public void onResume() {
        //eventBus.register(this);
    }

    @Override
    public void onPause() {
        //eventBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        view = null;
        service.onDestroy();
    }

    @Override
    public void getFilterContactsFromRealm(String query) {
        RealmResults<ContactRealm> contacts = service.getFilterContactsFromRealm(query);
        if(contacts.isEmpty())
            view.showEmptyListMessage(true);
        else
            view.showEmptyListMessage(false);
        view.setContacts(contacts);
    }


    @Override
    public boolean canReadContacts() {
        return service.canReadContacts();
    }


    @Override
    public void getContactsFromRealm() {
        if(view!=null) {
            RealmResults<ContactRealm> contacts = service.getContactsFromRealm();
            if (contacts.isEmpty())
                view.showEmptyListMessage(true);
            else
                view.showEmptyListMessage(false);
            view.setContacts(contacts);
        }
    }

    @Override
    public void syncContacts() {
        service.syncContactsToRealm();
    }
}
