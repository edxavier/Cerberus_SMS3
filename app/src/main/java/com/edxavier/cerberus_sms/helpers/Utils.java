package com.edxavier.cerberus_sms.helpers;


import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.google.firebase.crash.FirebaseCrash;

import io.realm.Realm;
import io.realm.exceptions.RealmError;

/**
 * Created by Eder Xavier Rojas on 13/10/2015.
 */
public class Utils {


    public static AreaCodeRealm getOperadoraV4(String numero, Context cntx){
        String codigoPais="";
        String codigoArea="";

        if (numero != null) {
            numero = numero.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll("\\s+", "");
            if (numero.startsWith("+1")) {
                codigoPais = "+1";
                if (numero.length() >= 5)
                    codigoArea = numero.substring(2, 5);
            }
            else if (numero.startsWith("+7")) {
                codigoPais = "+7";
            }
            else if (numero.startsWith("+50") && numero.length() >= 4) {
                codigoPais = numero.substring(0, 4);
                if(numero.length() >8 && numero.startsWith("+505"))
                    codigoArea = numero.substring(4, 7);
            }else if (numero.length() >= 3  && numero.startsWith("+3") || numero.startsWith("+4") || numero.startsWith("+8")) {
                try{
                    codigoPais = numero.substring(0, 3);
                }catch (Exception ignored){}
            }else if (numero.length() >= 3  && numero.startsWith("+5")) {
                if(!numero.startsWith("+50"))
                    codigoPais = numero.substring(0, 3);
            }else if ( numero.length() >= 4 && numero.length()<=8) {
                TelephonyManager tm = (TelephonyManager) cntx.getSystemService(Context.TELEPHONY_SERVICE);
                String countryCodeValue = tm.getNetworkCountryIso();
                codigoPais = "+505";
                codigoArea = numero.substring(0, 3);

                //Log.d("EDER_CV",tm.getNetworkCountryIso());
                //Log.d("EDER_CV",tm.getSimCountryIso());

                //Log.d("EDER_CV",countryCodeValue);
                /*if (countryCodeValue.equals("ni")) {
                    codigoPais = "+505";
                    codigoArea = numero.substring(0, 3);
                }else if(countryCodeValue.equals("us")){
                    codigoPais = "+1";
                    codigoArea = "";
                }*/
            }

        }
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            AreaCodeRealm areaCode;
            if(codigoArea.length()>0) {
                areaCode = realm.where(AreaCodeRealm.class).equalTo("country_code", codigoPais)
                        .equalTo("area_code", codigoArea).findFirst();
                //areaCode = SQLite.select().from(AreaCode.class).where(AreaCode_Table.country_code.eq(codigoPais))
                  //      .and(AreaCode_Table.area_code.eq(codigoArea)).querySingle();
            }else{
                //areaCode = SQLite.select().from(AreaCode.class).where(AreaCode_Table.country_code.eq(codigoPais))
                     //   .querySingle();
                areaCode = realm.where(AreaCodeRealm.class).equalTo("country_code", codigoPais).findFirst();
            }
            realm.close();
            return areaCode;

        }
        catch (RealmError err){
            Log.e("EDER_ex", "RealmError");
            if(realm != null)
                realm.close();
            if(err.getMessage()!=null) {
                Log.e("EDER_ex", err.getMessage());
                FirebaseCrash.logcat(Log.ERROR, "RealmError", err.getMessage());
            }
            return null;
        }
        catch (Exception e) {
            Log.e("EDER_ex", "Exception");
            Log.e("EDER_ex", e.getMessage());
            if(realm != null)
                realm.close();
            return null;
        }

    }

    public static AreaCodeRealm getOperadoraV4(String numero, Context cntx, Realm realm){
        String codigoPais="";
        String codigoArea="";

        if (numero != null) {
            numero = numero.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll("\\s+", "");
            if (numero.startsWith("+1")) {
                codigoPais = "+1";
                if (numero.length() >= 5)
                    codigoArea = numero.substring(2, 5);
            }
            else if (numero.startsWith("+7")) {
                codigoPais = "+7";
            }
            else if (numero.startsWith("+50") && numero.length() >= 4) {
                codigoPais = numero.substring(0, 4);
                if(numero.length() >8 && numero.startsWith("+505"))
                    codigoArea = numero.substring(4, 7);
            }else if (numero.length() >= 3  && numero.startsWith("+3") || numero.startsWith("+4") || numero.startsWith("+8")) {
                try{
                    codigoPais = numero.substring(0, 3);
                }catch (Exception ignored){}
            }else if (numero.length() >= 3  && numero.startsWith("+5")) {
                if(!numero.startsWith("+50"))
                    codigoPais = numero.substring(0, 3);
            }else if ( numero.length() >= 4 && numero.length()<=8) {
                TelephonyManager tm = (TelephonyManager) cntx.getSystemService(Context.TELEPHONY_SERVICE);
                String countryCodeValue = tm.getNetworkCountryIso();
                codigoPais = "+505";
                codigoArea = numero.substring(0, 3);
            }

        }
        try {
            AreaCodeRealm areaCode;
            if(codigoArea.length()>0) {
                areaCode = realm.where(AreaCodeRealm.class).equalTo("country_code", codigoPais)
                        .equalTo("area_code", codigoArea).findFirst();
            }else{
                areaCode = realm.where(AreaCodeRealm.class).equalTo("country_code", codigoPais).findFirst();
            }
            return areaCode;

        }
        catch (RealmError err){
            Log.e("EDER_ex", "RealmError");
            if(realm != null)
                realm.close();
            if(err.getMessage()!=null) {
                Log.e("EDER_ex", err.getMessage());
                FirebaseCrash.logcat(Log.ERROR, "RealmError", err.getMessage());
            }
            return null;
        }
        catch (Exception e) {
            Log.e("EDER_ex", "Exception");
            Log.e("EDER_ex", e.getMessage());
            if(realm != null)
                realm.close();
            return null;
        }

    }



    public static String formatPhoneNumber(String phoneNumber){
        if(phoneNumber!=null) {
            phoneNumber = phoneNumber.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll("\\s+", "");
            if (phoneNumber.length() == 12)
                phoneNumber = String.format("%s %s %s", phoneNumber.substring(0, 4), phoneNumber.substring(4, 8),
                        phoneNumber.substring(8, 12));
            else if (phoneNumber.length() == 8)
                phoneNumber = String.format("%s %s", phoneNumber.substring(0, 4), phoneNumber.substring(4, 8));
        }else
            phoneNumber = "";
        return phoneNumber;
    }



    public static String getDurationString(int seconds) {

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds);
    }

    public static String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }

    public static boolean isInteger(String str) {
        int num;
        try{
            num = Integer.parseInt(str.substring(0,3));
            return true;
        }catch (Exception e){
            return false;
        }
        //return str.matches("^-?[0-9]+(\\.[0-9]+)?$");
    }

    public static boolean isPhoneNumber(String number) {
        int num;
        number = number.replace("+","").replaceAll("\\(", "")
                .replaceAll("\\)", "").replaceAll("-", "")
                .replaceAll("\\s+","").trim();
        try{
            double res = Double.parseDouble(number);
            return true;
        }catch (Exception e){
            return false;
        }
        //return str.matches("^-?[0-9]+(\\.[0-9]+)?$");
    }


    public static ContactRealm getContact(String number){
        Realm realm = Realm.getDefaultInstance();
        ContactRealm contact = realm.where(ContactRealm.class)
                .equalTo("contact_phone_number", number)
                .findFirst();
        if(contact==null) {
            if (number.length() == 14)
                number = number.substring(5, 14);
            else if (number.length() == 9 && !number.startsWith("+")) {
                if(Utils.isPhoneNumber(number))
                    number = "+505 " + number;
            }
            contact = realm.where(ContactRealm.class)
                    .equalTo("contact_phone_number", number)
                    .findFirst();
        }
        realm.close();
        return contact;
    }

    public static TextDrawable getAvatar(String contact_name){
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        if (contact_name.length() > 1)
            contact_name = contact_name.substring(0, 2);
        else
            contact_name = contact_name.substring(0, 1);

        int color = generator.getColor(contact_name);
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(contact_name, color);

        return drawable;
    }

}

