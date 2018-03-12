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
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Date;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.edxavier.cerberus_sms.helpers.Constans.CLARO;
import static com.edxavier.cerberus_sms.helpers.Constans.CONVENCIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.COOTEL;
import static com.edxavier.cerberus_sms.helpers.Constans.DESCONOCIDO;
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
        try {
            if (checkDrawPermission(ctx)) {
                if (CallHandler.manager == null)
                    CallHandler.manager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                Realm realm = Realm.getDefaultInstance();
                WindowManager.LayoutParams layoutParams = setupWM(ctx);
                LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
                //Verificar si existe una ventana flotante visible y removerla
                if (CallHandler.view != null && CallHandler.view.isShown()) {
                    CallHandler.manager.removeView(CallHandler.view);
                    try {
                        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(ctx);
                        analytics.logEvent("remove_floatW", null);
                    } catch (Exception ignored) {
                    }
                }
                CallHandler.view = layoutInflater.inflate(R.layout.floating_window_v2, null);
                setTouchListener(layoutParams);
                AppCompatImageView close = CallHandler.view.findViewById(R.id.close_floating);
                AppCompatImageView bg = CallHandler.view.findViewById(R.id.imgBackground);
                TextView msgText = CallHandler.view.findViewById(R.id.floating_msg);
                CardView cardView = CallHandler.view.findViewById(R.id.flaoting_card);
                close.bringToFront();
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (CallHandler.view != null && CallHandler.view.isShown())
                                CallHandler.manager.removeView(CallHandler.view);
                        } catch (Exception e) {
                            CallHandler.manager.removeView(CallHandler.view);
                            FirebaseCrash.logcat(Log.ERROR, "onIncomingCallStarted", e.getMessage());
                        }
                    }
                });
                if (number != null) {
                    AreaCodeRealm areaCode = Utils.getOperadoraV4(number, ctx, realm);
                    if (areaCode != null) {
                        if (areaCode.area_operator.length() > 0) {
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
                                    if (areaCode.area_name.length() > 0)
                                        msgText.setText(areaCode.area_name);
                                    else if (areaCode.country_name.length() > 0)
                                        msgText.setText(areaCode.country_name);
                                    break;
                                default:
                                    msgText.setText(areaCode.area_name);
                                    break;
                            }
                            if (areaCode.area_name.length() > 0 || areaCode.country_name.length() > 0) {
                                try {
                                    CallHandler.manager.addView(CallHandler.view, layoutParams);
                                } catch (Exception ignored) {
                                }
                                ;
                            }
                        } else if (areaCode.area_name.length() > 0) {
                            msgText.setText(areaCode.area_name);
                            bg.setImageResource((R.drawable.ic_internet));
                            try {
                                CallHandler.manager.addView(CallHandler.view, layoutParams);
                            } catch (Exception ignored) {
                            }
                            ;
                        }
                    }
                }
                realm.close();
                //fin de checkDraw Perms
            }
        }catch (Exception ignored){
            if(ignored.getMessage()!=null)
                Answers.getInstance().logCustom(new CustomEvent("Error: "+ignored.getMessage())
                        .putCustomAttribute("location", "onIncomingCallStarted"));
        }

    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        super.onOutgoingCallStarted(ctx, number, start);
        try {
            if (checkDrawPermission(ctx)) {
                if (CallHandler.manager == null)
                    CallHandler.manager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                Realm realm = Realm.getDefaultInstance();
                WindowManager.LayoutParams layoutParams = setupWM(ctx);
                LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
                if (CallHandler.view != null && CallHandler.view.isShown())
                    CallHandler.manager.removeView(CallHandler.view);
                if (layoutInflater != null) {
                    CallHandler.view = layoutInflater.inflate(R.layout.floating_window_v2, null);
                }
                //CallHandler.view = View.inflate(ctx.getApplicationContext(), R.layout.floating_window_v2, null);
                setTouchListener(layoutParams);
                AppCompatImageView close = (AppCompatImageView) CallHandler.view.findViewById(R.id.close_floating);
                AppCompatImageView bg = (AppCompatImageView) CallHandler.view.findViewById(R.id.imgBackground);

                TextView msgText = CallHandler.view.findViewById(R.id.floating_msg);
                CardView cardView = (CardView) CallHandler.view.findViewById(R.id.flaoting_card);

                //close.bringToFront();
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (CallHandler.view != null && CallHandler.view.isShown())
                                CallHandler.manager.removeView(CallHandler.view);
                        } catch (Exception e) {
                            CallHandler.manager.removeView(CallHandler.view);
                            FirebaseCrash.logcat(Log.ERROR, "onOutgoingCallStarted", e.getMessage());
                        }
                    }
                });

                if (number != null) {
                    AreaCodeRealm areaCode = Utils.getOperadoraV4(number, ctx, realm);
                    if (areaCode != null) {
                        String operator = DESCONOCIDO;
                        if(areaCode.area_operator!=null)
                            if(areaCode.area_operator.length()>0)
                            operator = areaCode.area_operator;
                        if (operator.length() > 0) {
                            msgText.setText(areaCode.area_operator);
                            switch (operator) {
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
                                    if (areaCode.area_name.length() > 0)
                                        msgText.setText(areaCode.area_name);
                                    else if (areaCode.country_name.length() > 0)
                                        msgText.setText(areaCode.country_name);
                                    break;
                                default:
                                    msgText.setText(areaCode.area_name);
                                    break;
                            }
                            if (areaCode.area_name.length() > 0 || areaCode.country_name.length() > 0) {
                                try {
                                    CallHandler.manager.addView(CallHandler.view, layoutParams);
                                } catch (Exception ignored) {
                                }
                                ;
                            }
                        } else if (areaCode.area_name.length() > 0) {
                            msgText.setText(areaCode.area_name);
                            //cardView.setCardBackgroundColor(ctx.getResources().getColor(R.color.md_blue_600));
                            bg.setImageResource(R.drawable.ic_internet);
                            try {
                                CallHandler.manager.addView(CallHandler.view, layoutParams);
                            } catch (Exception ignored) {
                            }
                            ;
                        }
                    }
                }
                realm.close();
            }
        }catch (Exception ignored){
            if(ignored.getMessage()!=null)
                Answers.getInstance().logCustom(new CustomEvent("Error: "+ignored.getMessage())
                .putCustomAttribute("location", "onOutgoingCallStarted"));
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
            //if(!Prefs.getBoolean("ads_removed", false))
                //getads(ctx);
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
        WindowManager.LayoutParams layoutParams = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutParams = new WindowManager.LayoutParams(500, 200,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    PixelFormat.TRANSLUCENT);
        }else {
            layoutParams = new WindowManager.LayoutParams(500, 200,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    PixelFormat.TRANSLUCENT);
        }
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
                            if(CallHandler.view !=null && CallHandler.view.isShown())
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
        try {
            Realm realm2 = Realm.getDefaultInstance();
            AreaCodeRealm areaCodeRealm = Utils.getOperadoraV4(number, context, realm2);
            String operator;
            if(areaCodeRealm != null)
                operator = areaCodeRealm.area_operator;
            else
                operator = Constans.DESCONOCIDO;
            realm2.executeTransaction(realm_trans -> {
                //Cargar el historial para el numero en cuestion
                RealmResults<CallsHistoryRealm> calls = realm_trans.where(CallsHistoryRealm.class)
                        .equalTo("call_phone_number", number).findAllSorted("call_date", Sort.DESCENDING);
                CallsHistoryRealm lastCall = null;
                if(calls.isEmpty()) {
                    //+++++++++++++++++++++++++++++++++++++++++++++++PRIMER REGISTRO PARA UN NUMERO+++++++++++++++++++++++++++++++++++++++++++++++++++
                    //Ya que no hay ingresar el primer registro al historial
                    lastCall = realm_trans.copyToRealm(new CallsHistoryRealm(  Utils.getContact(number), number, duration,
                            type, start, operator));
                    //Ya que no hay ingresar el registro al resumen
                    CallsRealm callResume = realm_trans.createObject(CallsRealm.class, CallsRealm.getId());
                    callResume.contact =  Utils.getContact(number);
                    callResume.call_phone_number = number;
                    if (lastCall != null) {
                        callResume.entries.add(lastCall);
                        callResume.last_update = start;
                    }
                    //realm_trans.copyToRealm(callResume);
                    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                }else {
                    lastCall = calls.first();
                    if (lastCall != null && lastCall.call_date.before(start)) {
                        lastCall = realm_trans.copyToRealm(new CallsHistoryRealm(Utils.getContact(number), number, duration,
                                type, start, operator));

                        CallsRealm cResume = realm_trans.where(CallsRealm.class).equalTo("call_phone_number", number).findFirst();
                        if (cResume != null) {
                            cResume.entries.add(lastCall);
                            cResume.last_update = start;
                            cResume.contact = Utils.getContact(number);
                        }
                    }
                }
            });

            realm2.close();
        }catch (Exception ignored){Log.e("EDER", "SAVE CALL ERROR");}
    }

}
