package com.achaquisse.smsgatewayclient.service;

import android.util.Log;

import com.achaquisse.smsgatewayclient.util.Constant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SmsRestClient {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static List<SmsModel> listPending() {
        try {
            Request request = new Request.Builder()
                    .url(String.format("%s/pending", Constant.serverName()))
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 200) {
                    JSONArray jsonArray = new JSONArray(Objects.requireNonNull(response.body()).string());
                    List<SmsModel> smsModelList = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        SmsModel smsModel = new SmsModel(jsonObject.getString("pk"),
                                jsonObject.getString("sk"),
                                jsonObject.getLong("to"),
                                jsonObject.getString("message"),
                                jsonObject.getLong("status_at")
                        );

                        smsModelList.add(smsModel);
                    }

                    Log.d("restClient.listPending", String.format("successfully list sms: %s", smsModelList.size()));
                    return smsModelList;

                } else {
                    Log.e("restClient.listPending", String.format("Error listing. cause: %s", Objects.requireNonNull(response.body()).string()));
                    throw new SmsClientException("Unable to connect to server. " + response.code());
                }
            }
        } catch (Exception ex) {
            Log.e("restClient.listPending", String.format("Failed listPending. cause: %s", ex.getMessage()));
            throw new SmsClientException("Unable to process sms listing request. ", ex);
        }
    }

    public static void updateStatus(String sk, String status) {
        executorService.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("status", status);

                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(String.format("%s/pending/%s", Constant.serverName(), sk))
                        .put(body)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.code() == 200) {
                        Log.d("restClient.updateStatus", String.format("successfully updateStatus sk: %s", status));

                    } else {
                        Log.e("restClient.updateStatus", String.format("Failed to updateStatus. sk: %s," +
                                " responseCode: %s", sk, response.code()));

                    }
                }
            } catch (Exception ex) {
                Log.e("restClient.forward", String.format("Failed updateStatus. sk: %s, " +
                        "cause: %s", sk, ex.getMessage()));
            }
        });
    }


}
