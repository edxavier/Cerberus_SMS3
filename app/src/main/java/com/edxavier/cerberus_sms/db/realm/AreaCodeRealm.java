package com.edxavier.cerberus_sms.db.realm;


import com.edxavier.cerberus_sms.helpers.UUIDGenerator;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;


 //Created by Eder Xavier Rojas on 12/11/2016.

public class AreaCodeRealm extends RealmObject {
    @PrimaryKey
    public String id;

    @Index
    public String country_code;
    @Index
    public String area_code;
    public String country_name;
    public String area_name;
    public String area_operator;

    public AreaCodeRealm() {
        this.id = UUIDGenerator.nextUUID();
    }

    public AreaCodeRealm(String country_code, String area_code, String country_name, String area_name, String area_operator) {
        this.id = UUIDGenerator.nextUUID();
        this.country_code = country_code;
        this.area_code = area_code;
        this.country_name = country_name;
        this.area_name = area_name;
        this.area_operator = area_operator;
    }
}

