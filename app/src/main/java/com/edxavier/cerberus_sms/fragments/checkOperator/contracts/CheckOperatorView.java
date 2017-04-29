package com.edxavier.cerberus_sms.fragments.checkOperator.contracts;

import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;

/**
 * Created by Eder Xavier Rojas on 07/07/2016.
 */
public interface CheckOperatorView {
    AreaCodeRealm checkOperator(String phoneNumber);
    void setNumber(String number);
    void showElements(boolean show);
    void setResult();
}
