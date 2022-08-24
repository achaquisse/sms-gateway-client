package com.achaquisse.smsgatewayclient.sender;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import com.achaquisse.smsgatewayclient.service.SmsClientException;
import com.achaquisse.smsgatewayclient.service.SmsModel;
import com.achaquisse.smsgatewayclient.service.SmsRestClient;
import com.achaquisse.smsgatewayclient.util.Constant;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ServerListenerService extends IntentService {

    private boolean isRunning;
    private boolean lastOnline;

    public ServerListenerService() {
        super("SmsGatewayClientListener");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("gateway.intent", "creating handle intent");
        isRunning = true;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("gateway.intent", "starting handle intent");

        ResultReceiver resultReceiver = intent.getParcelableExtra("receiver");

        while (isRunning) {
            try {
                List<SmsModel> smsModelList = SmsRestClient.listPending();

                if (!lastOnline) {
                    Bundle bundle = new Bundle();
                    bundle.putString("resultCode", currentDate());
                    resultReceiver.send(Constant.STATUS_ONLINE, bundle);
                    lastOnline = true;
                }

                if (!smsModelList.isEmpty()) {
                    for (SmsModel sms : smsModelList) {
                        SmsSender.send(getApplicationContext(), sms.getSk(), sms.getTo(), sms.getMessage());

                        Bundle bundle = new Bundle();
                        bundle.putString("lastSentDate", currentDate());
                        resultReceiver.send(Constant.LAST_SENT, bundle);
                    }
                }
            } catch (SmsClientException ex) {
                if (lastOnline) {
                    lastOnline = false;
                }

                Bundle bundle = new Bundle();
                resultReceiver.send(Constant.STATUS_OFFLINE, bundle);
            }

            sleep();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("gateway.intent", "destroying handle intent");
        isRunning = false;
        super.onDestroy();
    }

    private String currentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private void sleep() {
        try {
            Thread.sleep(Constant.listenerWaitTime());
        } catch (InterruptedException ignored) {
        }
    }
}
