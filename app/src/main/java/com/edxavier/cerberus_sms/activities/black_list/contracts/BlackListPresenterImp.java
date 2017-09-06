package com.edxavier.cerberus_sms.activities.black_list.contracts;

import com.edxavier.cerberus_sms.db.realm.BlackList;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 05/09/2017.
 */

public class BlackListPresenterImp implements BlackListPresenter {
    private BlackListView view;
    private BlackListService service;

    public BlackListPresenterImp(BlackListView view) {
        this.view = view;
        this.service = new BlackListServiceImpl();
    }

    @Override
    public void getBlackList() {
        RealmResults<BlackList> res = this.service.getBlackList();
        if(res.isEmpty())
            view.showEmptyMsg(true);
        else {
            view.showEmptyMsg(false);
            view.setCallsData(res);
        }
    }
}
