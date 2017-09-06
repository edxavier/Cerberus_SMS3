package com.edxavier.cerberus_sms.activities.black_list.contracts;

import com.edxavier.cerberus_sms.db.realm.BlackList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 05/09/2017.
 */

public class BlackListServiceImpl implements BlackListService {
    @Override
    public RealmResults<BlackList> getBlackList() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<BlackList> res = realm.where(BlackList.class).findAll();
        realm.close();
        return res;
    }
}
