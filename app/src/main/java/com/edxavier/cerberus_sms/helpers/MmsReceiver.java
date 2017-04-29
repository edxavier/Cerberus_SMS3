package com.edxavier.cerberus_sms.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by Eder Xavier Rojas on 24/10/2015.
 */
public class MmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String MSG_TYPE=intent.getAction();
        if(MSG_TYPE.equals("android.provider.Telephony.SMS_RECEIVED"))
        {
//          Toast toast = Toast.makeText(context,"SMS Received: "+MSG_TYPE , Toast.LENGTH_LONG);
//          toast.show();

            Bundle bundle = intent.getExtras();

            if(bundle!=null){
                abortBroadcast();
            }
            Object messages[] = (Object[]) bundle.get("pdus");
            SmsMessage smsMessage[] = new SmsMessage[messages.length];
            for (int n = 0; n < messages.length; n++)
            {
                smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
            }

            // show first message
            Toast toast = Toast.makeText(context,"BLOCKED Received SMS: " + smsMessage[0].getMessageBody(), Toast.LENGTH_LONG);
            toast.show();
            abortBroadcast();
            for(int i=0;i<8;i++)
            {
                System.out.println("Blocking SMS **********************");
            }

        }
        else if(MSG_TYPE.equals("android.provider.Telephony.SEND_SMS"))
        {
            Toast toast = Toast.makeText(context,"SMS SENT: "+MSG_TYPE , Toast.LENGTH_LONG);
            toast.show();
            abortBroadcast();
            for(int i=0;i<8;i++)
            {
                System.out.println("Blocking SMS **********************");
            }

        }
        else
        {

            Toast toast = Toast.makeText(context,"SIN ELSE: "+MSG_TYPE , Toast.LENGTH_LONG);
            toast.show();
            abortBroadcast();
            for(int i=0;i<8;i++)
            {
                System.out.println("Blocking SMS **********************");
            }

        }

    }
}
