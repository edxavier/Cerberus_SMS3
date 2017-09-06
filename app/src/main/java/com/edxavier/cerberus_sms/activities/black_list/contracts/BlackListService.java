package com.edxavier.cerberus_sms.activities.black_list.contracts;

import com.edxavier.cerberus_sms.db.realm.BlackList;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 04/09/2017.
 */

public interface BlackListService {
    RealmResults<BlackList> getBlackList();
}
