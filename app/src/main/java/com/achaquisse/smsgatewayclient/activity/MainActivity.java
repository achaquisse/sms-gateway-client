package com.achaquisse.smsgatewayclient.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.achaquisse.smsgatewayclient.sender.ServerListenerService;
import com.achaquisse.smsgatewayclient.util.Constant;
import com.achaquisse.smsgatewayclient.R;
import com.achaquisse.smsgatewayclient.sender.ServiceReceiver;

public class MainActivity extends AppCompatActivity {

    private ServiceReceiver serviceReceiver;

    private TextView statusTextView;
    private TextView lastSentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        lastSentTextView = findViewById(R.id.lastSentTextView);
        TextView serverTextView = findViewById(R.id.serverTextView);

        serverTextView.setText(Constant.serverName());

        requestReadAndSendSmsPermission();
        setupServiceReceiver();

        Intent serverListener = new Intent(this, ServerListenerService.class);
        serverListener.putExtra("receiver", serviceReceiver);
        startService(serverListener);
    }

    private void requestReadAndSendSmsPermission() {
        if (!isSmsPermissionGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS,
                    Manifest.permission.INTERNET}, 1);
        }
    }

    private boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public void setupServiceReceiver() {
        serviceReceiver = new ServiceReceiver(new Handler());

        serviceReceiver.setReceiver((resultCode, resultData) -> {
            if (resultCode == Constant.LAST_SENT) {
                String lastSentDate = resultData.getString("lastSentDate");
                lastSentTextView.setText(lastSentDate);
            }
            if (resultCode == Constant.STATUS_ONLINE) {
                statusTextView.setText(R.string.connected);
            }
            if (resultCode == Constant.STATUS_OFFLINE) {
                statusTextView.setText(R.string.disconnected);
            }
        });
    }

}