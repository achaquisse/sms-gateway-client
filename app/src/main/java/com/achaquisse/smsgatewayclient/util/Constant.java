package com.achaquisse.smsgatewayclient.util;

public class Constant {

    public static final int LAST_SENT = 1;
    public static final int STATUS_ONLINE = 3;
    public static final int STATUS_OFFLINE = 4;

    public static final String SmsStatusSuccess = "Success";
    public static final String SmsStatusFailed = "Failed";
    public static final String SmsStatusDelivered = "Delivered";

    public static String serverName() {
        return "https://ksmivptz2a.execute-api.us-east-1.amazonaws.com";
    }

    public static int listenerWaitTime() {
        return 1000 * 30; // 30s
    }
}
