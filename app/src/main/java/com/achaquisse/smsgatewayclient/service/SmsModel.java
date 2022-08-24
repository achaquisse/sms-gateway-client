package com.achaquisse.smsgatewayclient.service;

public class SmsModel {

    private String pk;
    private String sk;
    private long to;
    private String message;
    private long statusAt;

    public SmsModel(String pk, String sk, long to, String message, long statusAt) {
        this.pk = pk;
        this.sk = sk;
        this.to = to;
        this.message = message;
        this.statusAt = statusAt;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getStatusAt() {
        return statusAt;
    }

    public void setStatusAt(long statusAt) {
        this.statusAt = statusAt;
    }
}
