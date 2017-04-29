package com.edxavier.cerberus_sms.fragments.callLog.receivers;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.CallsHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.CallsRealm;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.TextViewHelper;
import com.edxavier.cerberus_sms.helpers.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.edxavier.cerberus_sms.helpers.Constans.CLARO;
import static com.edxavier.cerberus_sms.helpers.Constans.CONVENCIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.COOTEL;
import static com.edxavier.cerberus_sms.helpers.Constans.INTERNACIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.MOVISTAR;

/**
 * Created by Eder Xavier Rojas on 19/07/2016.
 */
public class CallHandler extends PhonecallReceiver {
    private static WindowManager manager;
    private static View view;

    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        super.onIncomingCallStarted(ctx, number, start);
        if(checkDrawPermission(ctx)) {
            if(CallHandler.manager==null)
                CallHandler.manager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            Realm realm = Realm.getDefaultInstance();
            WindowManager.LayoutParams layoutParams = setupWM(ctx);
            LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
            if(CallHandler.view !=null && CallHandler.view.isShown())
                CallHandler.manager.removeView(CallHandler.view);
            CallHandler.view = layoutInflater.inflate(R.layout.floating_window_v2, null);
            setTouchListener(layoutParams);
            AppCompatImageView close = (AppCompatImageView) CallHandler.view.findViewById(R.id.close_floating);
            AppCompatImageView bg = (AppCompatImageView) CallHandler.view.findViewById(R.id.imgBackground);
            TextViewHelper msgText = (TextViewHelper) CallHandler.view.findViewById(R.id.floating_msg);
            CardView cardView = (CardView) CallHandler.view.findViewById(R.id.flaoting_card);
            close.bringToFront();
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        CallHandler.manager.removeView(CallHandler.view);
                    }catch (Exception e){
                        CallHandler.manager.removeView(CallHandler.view);
                        FirebaseCrash.logcat(Log.ERROR, "onIncomingCallStarted", e.getMessage());
                    }
                }
            });
            if(number!=null){
                AreaCodeRealm areaCode = Utils.getOperadoraV4(number, ctx, realm);
                if(areaCode!=null) {
                    if (areaCode.area_operator.length() > 0) {
                        msgText.setRobotoMedium();
                        //msgText.setTextSize(14);
                        msgText.setText(areaCode.area_operator);
                        switch (areaCode.area_operator) {
                            case CLARO:
                                bg.setImageResource((R.drawable.ic_biohazard));
                                break;
                            case MOVISTAR:
                                bg.setImageResource((R.drawable.ic_radiation));
                                break;
                            case COOTEL:
                                bg.setImageResource((R.drawable.ic_star_circle));
                                break;
                            case CONVENCIONAL:
                                bg.setImageResource((R.drawable.ic_internet));
                                msgText.setText(areaCode.area_name);
                                break;
                            case INTERNACIONAL:
                                bg.setImageResource((R.drawable.ic_exterior));
                                if (areaCode.area_name.length()>0)
                                    msgText.setText(areaCode.area_name);
                                else if(areaCode.country_name.length()>0)
                                    msgText.setText(areaCode.country_name);
                                break;
                            default:
                                msgText.setText(areaCode.area_name);
                                break;
                        }
                        if (areaCode.area_name.length()>0 || areaCode.country_name.length()>0)
                            CallHandler.manager.addView(CallHandler.view, layoutParams);
                    } else if (areaCode.area_name.length()>0){
                        msgText.setRobotoMedium();
                        msgText.setText(areaCode.area_name);
                        bg.setImageResource((R.drawable.ic_internet));
                        CallHandler.manager.addView(CallHandler.view, layoutParams);
                    }
                }
            }
            realm.close();
        }//fin de checkDraw Perms

    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        super.onOutgoingCallStarted(ctx, number, start);
        if(checkDrawPermission(ctx)) {
            if(CallHandler.manager==null)
                CallHandler.manager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            Realm realm = Realm.getDefaultInstance();
            WindowManager.LayoutParams layoutParams = setupWM(ctx);
            LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
            if(CallHandler.view !=null && CallHandler.view.isShown())
                CallHandler.manager.removeView(CallHandler.view);
            CallHandler.view = layoutInflater.inflate(R.layout.floating_window_v2, null);
            //CallHandler.view = View.inflate(ctx.getApplicationContext(), R.layout.floating_window_v2, null);
            setTouchListener(layoutParams);
            AppCompatImageView close = (AppCompatImageView)  CallHandler.view.findViewById(R.id.close_floating);
            AppCompatImageView bg = (AppCompatImageView)  CallHandler.view.findViewById(R.id.imgBackground);

            TextViewHelper msgText = (TextViewHelper)  CallHandler.view.findViewById(R.id.floating_msg);
            CardView cardView = (CardView)  CallHandler.view.findViewById(R.id.flaoting_card);

            //close.bringToFront();
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        CallHandler.manager.removeView( CallHandler.view);
                    }catch (Exception e){
                        CallHandler.manager.removeView( CallHandler.view);
                        FirebaseCrash.logcat(Log.ERROR, "onOutgoingCallStarted", e.getMessage());
                    }
                }
            });

            if (number != null) {
                AreaCodeRealm areaCode = Utils.getOperadoraV4(number, ctx, realm);
                if (areaCode != null) {
                    if( areaCode.area_operator.length() > 0) {
                        msgText.setRobotoMedium();
                        msgText.setText(areaCode.area_operator);
                        switch (areaCode.area_operator) {
                            case CLARO:
                                bg.setImageResource((R.drawable.ic_biohazard));
                                break;
                            case MOVISTAR:
                                bg.setImageResource((R.drawable.ic_radiation));
                                break;
                            case COOTEL:
                                bg.setImageResource((R.drawable.ic_star_circle));
                                break;
                            case CONVENCIONAL:
                                bg.setImageResource((R.drawable.ic_internet));
                                msgText.setText(areaCode.area_name);
                                break;
                            case INTERNACIONAL:
                                bg.setImageResource((R.drawable.ic_exterior));
                                if (areaCode.area_name.length()>0)
                                    msgText.setText(areaCode.area_name);
                                else if(areaCode.country_name.length()>0)
                                    msgText.setText(areaCode.country_name);
                                break;
                            default:
                                msgText.setText(areaCode.area_name);
                                break;
                        }
                        if (areaCode.area_name.length()>0 || areaCode.country_name.length()>0) {
                            CallHandler.manager.addView(CallHandler.view, layoutParams);
                        }
                    } else if (areaCode.area_name.length()>0){
                        msgText.setText(areaCode.area_name);
                        msgText.setRobotoMedium();
                        //cardView.setCardBackgroundColor(ctx.getResources().getColor(R.color.md_blue_600));
                        bg.setImageResource(R.drawable.ic_internet);
                        CallHandler.manager.addView(CallHandler.view, layoutParams);
                    }
                }
            }
            realm.close();
            Log.e("EDER", " realm.close");
        }
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        super.onIncomingCallEnded(ctx, number, start, end);
        try {
            if(CallHandler.view !=null && CallHandler.view.isShown())
                CallHandler.manager.removeView(CallHandler.view);
        }catch (Exception ignored){
        }
        if(number!=null) {
            number = Utils.formatPhoneNumber(number);
            int duration = (int) ((end.getTime() - start.getTime()) / 1000 % 60);
            saveCall(number, start, duration, android.provider.CallLog.Calls.INCOMING_TYPE, ctx);
        }
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        super.onOutgoingCallEnded(ctx, number, start, end);
        try {
            if(CallHandler.view !=null && CallHandler.view.isShown())
                CallHandler.manager.removeView(CallHandler.view);
        }catch (Exception ignored){}
        if(number!=null) {
            number = Utils.formatPhoneNumber(number);
            int duration = (int) ((end.getTime() - start.getTime()) / 1000 % 60);
            saveCall(number, start, duration, android.provider.CallLog.Calls.OUTGOING_TYPE, ctx);
        }
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        super.onMissedCall(ctx, number, start);
        try {
            if(CallHandler.view !=null && CallHandler.view.isShown())
                CallHandler.manager.removeView(CallHandler.view);
        }catch (Exception ignored){}
        if(number!=null) {
            Date end = new Date();
            number = Utils.formatPhoneNumber(number);
            int duration = (int) ((end.getTime() - start.getTime()) / 1000 % 60);
            saveCall(number, start, duration, android.provider.CallLog.Calls.MISSED_TYPE, ctx);
        }
    }

    boolean checkDrawPermission(Context context) {
        boolean hasPerm = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             hasPerm = Settings.canDrawOverlays(context);
        }
        return hasPerm;
    }

    WindowManager.LayoutParams setupWM(Context ctx){
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(500, 200,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.CENTER;
        //layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.alpha = 0.95f;
        layoutParams.packageName = ctx.getPackageName();
        layoutParams.buttonBrightness = 1f;
        layoutParams.windowAnimations = android.R.style.Animation_Dialog;

        return layoutParams;
    }

    void setTouchListener(final WindowManager.LayoutParams layoutParams){
        CallHandler.view.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams updatedParameters = layoutParams;
            double x;
            double y;
            double pressedX;
            double pressedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        x = updatedParameters.x;
                        y = updatedParameters.y;

                        pressedX = event.getRawX();
                        pressedY = event.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (event.getRawX() - pressedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - pressedY));
                        try {
                            CallHandler.manager.updateViewLayout(CallHandler.view, updatedParameters);
                        }catch (Exception e){
                            FirebaseCrash.logcat(Log.ERROR, "ACTION_MOVE", "Error al mover la ventana");
                            CallHandler.manager.removeView(CallHandler.view);
                        }

                    default:
                        break;
                }

                return false;
            }
        });
    }

    void saveCall(String number, Date start, int duration, int type, Context context){
        Realm realm2 = Realm.getDefaultInstance();
        AreaCodeRealm areaCodeRealm = Utils.getOperadoraV4(number, context, realm2);
        String operator = "";
        if(areaCodeRealm != null)
            operator = areaCodeRealm.area_operator;
        else
            operator = Constans.DESCONOCIDO;

        //CallsRealm call = realm.where(CallsRealm.class).equalTo("call_phone_number", number).findFirst();
        RealmResults<CallsRealm> calls = realm2.where(CallsRealm.class)
                .equalTo("call_phone_number", number).findAllSorted("call_date", Sort.DESCENDING);
        CallsRealm call = null;
        if(!calls.isEmpty()) {
            call = calls.first();
           //Log.e("EDER_fecha", String.valueOf(call.call_date));
            //Log.e("EDER_start", String.valueOf(start));
        }
        if(call==null) {
            String finalOperator = operator;
            realm2.executeTransaction(realm1 -> {
                realm1.copyToRealm(new CallsRealm(Utils.getContact(number), number,
                        duration,  type, start, 1, finalOperator));
                realm1.copyToRealm(new CallsHistoryRealm(Utils.getContact(number), number,
                        duration, type, start, finalOperator));
            });
        }else {
            CallsRealm finalCall = call;
            //Log.e("EDER","NUEVO Segundo" + call.call_phone_number);
            String finalOperator1 = operator;
            realm2.executeTransaction(realm1 -> {
                realm1.copyToRealm(new CallsHistoryRealm(Utils.getContact(number), number,
                            duration, type, start, finalOperator1));

                finalCall.calls_count += 1;
                finalCall.contact = Utils.getContact(number);
                finalCall.call_direction = type;
                finalCall.call_date = start;
                finalCall.call_duration = duration;
                finalCall.call_operator = finalOperator1;
             });

        }
        realm2.close();
    }
    public void getads(Context ctx){

        InterstitialAd mInterstitialAd = new InterstitialAd(ctx);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }
        });

        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                .build();

        mInterstitialAd.loadAd(adRequest);

    }
}