package com.achaquisse.smsgatewayclient.sender;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.telephony.SmsManager;

import com.achaquisse.smsgatewayclient.util.Constant;
import com.achaquisse.smsgatewayclient.service.SmsRestClient;

import java.util.ArrayList;

public class SmsSender {

    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    private SmsSender() {
    }

    public static void send(Context context, String sk, long to, String message) {
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(DELIVERED), 0);


        //---when the SMS has been sent---
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if (getResultCode() == Activity.RESULT_OK) {
                    SmsRestClient.updateStatus(sk, Constant.SmsStatusSuccess);
                } else {
                    SmsRestClient.updateStatus(sk, Constant.SmsStatusFailed);
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if (getResultCode() == Activity.RESULT_OK) {
                    SmsRestClient.updateStatus(sk, Constant.SmsStatusDelivered);
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(message);

        ArrayList<PendingIntent> sentPIList = new ArrayList<>();
        sentPIList.add(sentPI);

        ArrayList<PendingIntent> deliveredPIList = new ArrayList<>();
        deliveredPIList.add(deliveredPI);

        sms.sendMultipartTextMessage("+" + to, null, parts, sentPIList, deliveredPIList);
    }

}
