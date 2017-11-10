package com.edxavier.cerberus_sms;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragment;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;


public class IntroductionActivity extends MaterialIntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_intro);
        boolean isAndroid_L_or_above = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        isAndroid_L_or_above = true;

        SlideFragmentBuilder contacts = new SlideFragmentBuilder()
                .backgroundColor(R.color.md_cyan_500)
                .buttonsColor(R.color.md_cyan_A400)
                .neededPermissions(new String[]{Manifest.permission.READ_CONTACTS})
                .title(getString(R.string.drawer_op_contactos))
                .description(getString(R.string.intro_contacts));

        SlideFragmentBuilder calls = new SlideFragmentBuilder()
                .backgroundColor(R.color.md_teal_500)
                .buttonsColor(R.color.md_teal_A700)
                .neededPermissions(new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG})
                .title(getString(R.string.drawer_op_call_log))
                .description(getString(R.string.intro_calls));

        SlideFragmentBuilder inbox = new SlideFragmentBuilder()
                .backgroundColor(R.color.md_pink_500)
                .buttonsColor(R.color.md_pink_A700)
                .neededPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS})
                //.image(R.drawable.ic_inbox_smartphone)
                .title(getString(R.string.drawer_op_inbox))
                .description(getString(R.string.intro_sms));

        SlideFragmentBuilder dial = new SlideFragmentBuilder()
                .backgroundColor(R.color.md_amber_500)
                .buttonsColor(R.color.md_amber_A700)
                .neededPermissions(new String[]{Manifest.permission.CALL_PHONE})
                //.image(R.drawable.dial)
                .title(getString(R.string.drawer_op_dialer))
                .description(getString(R.string.intro_dialer));

        contacts.image(R.drawable.ic_contacts_smartphone);
        calls.image(R.drawable.ic_call_receiving_sign);
        inbox.image(R.drawable.ic_inbox_smartphone);
        dial.image(R.drawable.ic_dial);


        addSlide(contacts.build());
        addSlide(calls.build());
        addSlide(inbox.build());
        addSlide(dial.build());

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });
    }

    @Override
    public void onFinish() {
        super.onFinish();
        Prefs.putBoolean("show_intro", true);
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
