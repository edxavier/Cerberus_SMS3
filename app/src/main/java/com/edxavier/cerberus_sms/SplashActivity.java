package com.edxavier.cerberus_sms;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.edxavier.cerberus_sms.helpers.InitAreaCode;
import com.pixplicity.easyprefs.library.Prefs;

public class SplashActivity extends AppCompatActivity {

    int REQUEST_CODE_INTRO = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        if(InitAreaCode.isAreacodeEmpty())
            InitAreaCode.initAreaCodes(this);

        if(!Prefs.getBoolean("show_intro", false)) {
            Intent intent = new Intent(this, IntroductionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(intent, REQUEST_CODE_INTRO);
        }else{
            Intent intent = new Intent(this, DialerActivityV2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
            finish();

    }


}
