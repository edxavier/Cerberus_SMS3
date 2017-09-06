package com.edxavier.cerberus_sms.helpers;

import android.content.Context;
import android.util.Log;

import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;

import java.io.InputStream;
import java.util.ArrayList;

import io.realm.Realm;

//Created by Eder Xavier Rojas on 07/11/2015.

public class InitAreaCode {
    public static void initAreaCodes(Context cntx){

        InputStream inputStream = cntx.getResources().openRawResource(R.raw.usa_areas_codes);
        CSVreader csvReader = new CSVreader(inputStream);
        ArrayList<String[]> us_areas = csvReader.read();

        ArrayList<AreaCodeRealm> areaCodes = new ArrayList<AreaCodeRealm>();
        String us = cntx.getResources().getString(R.string.usa);

        for (String[] us_area : us_areas) {
            //codigoPais, CodigoArea, Pais, Area, Operador
            areaCodes.add(new AreaCodeRealm("+1",us_area[0], us,us_area[1],Constans.INTERNACIONAL));
        }

        areaCodes.add(new AreaCodeRealm("+501","","Belice","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+502","","Guatemala","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+503","","El Salvador","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+504","","Honduras","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+506","","Costa Rica","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+507","","Panama","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+509","","Haití","",Constans.INTERNACIONAL));

        areaCodes.add(new AreaCodeRealm("+51",""," Perú","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+52","","Mexico","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+53","","Cuba","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+54",""," Argentina","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+55","","Brasil","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+56","","Chile","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+57","","Colombia","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+58","","Venezuela","",Constans.INTERNACIONAL));


        areaCodes.add(new AreaCodeRealm("+32","","Belgica","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+33","","Francia","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+34","","España","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+39","","Italia","",Constans.INTERNACIONAL));

        areaCodes.add(new AreaCodeRealm("+40","","Rumania","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+41","","Suiza","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+43","","Austria","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+44","","Inglaterra","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+45","","Dinamarca","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+49","","Alemania","",Constans.INTERNACIONAL));

        areaCodes.add(new AreaCodeRealm("+7","","Rusia","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+80","","Japón","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+81","","Corea del Sur","",Constans.INTERNACIONAL));
        areaCodes.add(new AreaCodeRealm("+86","","China","",Constans.INTERNACIONAL));




        //FIJOS
        areaCodes.add(new AreaCodeRealm("+505","222","Nicaragua","Managua",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","223","Nicaragua","Managua",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","224","Nicaragua","Managua",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","225","Nicaragua","Managua",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","226","Nicaragua","Managua",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","227","Nicaragua","Managua",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","228","Nicaragua","Managua",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","229","Nicaragua","Managua",Constans.CONVENCIONAL));

        areaCodes.add(new AreaCodeRealm("+505","231","Nicaragua","Leon",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","234","Nicaragua","Chinandega",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","235","Nicaragua","Chinandega",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","251","Nicaragua","Chontales",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","252","Nicaragua","Masaya",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","253","Nicaragua","Carazo",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","254","Nicaragua","Boaco",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","255","Nicaragua","Granada",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","256","Nicaragua","Rivas",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","257","Nicaragua","RAAS",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","258","Nicaragua","Rio San Juan",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","271","Nicaragua","Esteli",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","272","Nicaragua","Madriz",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","273","Nicaragua","Nueva Segovia",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","277","Nicaragua","Matagalpa",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","278","Nicaragua","Jinotega",Constans.CONVENCIONAL));
        areaCodes.add(new AreaCodeRealm("+505","279","Nicaragua","RAAN",Constans.CONVENCIONAL));

        //MOVIL CLARO
        areaCodes.add(new AreaCodeRealm("+505","550","Nicaragua","","Claro"));

        for(int i = 570;i<=589;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Claro"));

        areaCodes.add(new AreaCodeRealm("+505","820","Nicaragua","","Claro"));
        areaCodes.add(new AreaCodeRealm("+505","821","Nicaragua","","Claro"));
        areaCodes.add(new AreaCodeRealm("+505","822","Nicaragua","","Claro"));
        areaCodes.add(new AreaCodeRealm("+505","823","Nicaragua","","Claro"));
        areaCodes.add(new AreaCodeRealm("+505","830","Nicaragua","","Claro"));
        areaCodes.add(new AreaCodeRealm("+505","831","Nicaragua","","Claro"));
        areaCodes.add(new AreaCodeRealm("+505","833","Nicaragua","","Claro"));
        areaCodes.add(new AreaCodeRealm("+505","835","Nicaragua","","Claro"));
        areaCodes.add(new AreaCodeRealm("+505","836","Nicaragua","","Claro"));

        for(int i = 840 ;i<=844;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Claro"));
        areaCodes.add(new AreaCodeRealm("+505","849","Nicaragua","","Claro"));

        for(int i = 850 ;i<=854;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Claro"));
        for(int i = 860 ;i<=866;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Claro"));
        areaCodes.add(new AreaCodeRealm("+505","869","Nicaragua","","Claro"));

        for(int i = 870 ;i<=874;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Claro"));
        for(int i = 882 ;i<=885;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Claro"));
        for(int i = 890 ;i<=894;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Claro"));

        //MOVISTAR
        for(int i = 750 ;i<=755;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Movistar"));

        for(int i = 760 ;i<=789;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Movistar"));

        for(int i = 810 ;i<=819;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Movistar"));

        for(int i = 824 ;i<=829;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Movistar"));
        areaCodes.add(new AreaCodeRealm("+505","832", "Nicaragua", "", "Movistar"));
        areaCodes.add(new AreaCodeRealm("+505","837", "Nicaragua", "", "Movistar"));
        areaCodes.add(new AreaCodeRealm("+505","838", "Nicaragua", "", "Movistar"));
        areaCodes.add(new AreaCodeRealm("+505","839", "Nicaragua", "", "Movistar"));
        for(int i = 845 ;i<=848;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Movistar"));

        for(int i = 855 ;i<=859;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Movistar"));

        areaCodes.add(new AreaCodeRealm("+505","867", "Nicaragua", "", "Movistar"));
        areaCodes.add(new AreaCodeRealm("+505","868", "Nicaragua", "", "Movistar"));
        for(int i = 875 ;i<=881;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Movistar"));
        for(int i = 886 ;i<=889;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Movistar"));
        for(int i = 895 ;i<=899;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","Movistar"));

        areaCodes.add(new AreaCodeRealm("+505","620", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","630", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","633", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","635", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","640", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","644", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","645", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","650", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","655", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","677", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","681", "Nicaragua", "", "CooTel"));
        for(int i = 681 ;i<=690;i++)
            areaCodes.add(new AreaCodeRealm("+505",String.valueOf(i),"Nicaragua","","CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","695", "Nicaragua", "", "CooTel"));
        areaCodes.add(new AreaCodeRealm("+505","699", "Nicaragua", "", "CooTel"));

        Realm realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(bg_realm -> {
            bg_realm.copyToRealm(areaCodes);
        }, () -> Log.e("EDER", "INIT AreaCodes Success"), error -> Log.e("EDER", "INIT AreaCodes Error"));

        realm.close();
    }

    public static boolean isAreacodeEmpty(){
        Realm realm = Realm.getDefaultInstance();
        boolean empty = realm.where(AreaCodeRealm.class).findAll().isEmpty();
        realm.close();
        return empty;
    }

}