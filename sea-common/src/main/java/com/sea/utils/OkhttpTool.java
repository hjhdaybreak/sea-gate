package com.sea.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sea.exception.SeaException;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkhttpTool {

    private static final String HTTP_JSON = "application/json;charset=utf-8";

    private static final Gson gson = new GsonBuilder().create();

    private static final OkHttpClient client;

    static {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public static <T> void doPost(String url, T t) {
        RequestBody requestBody = RequestBody.create(gson.toJson(t), MediaType.parse(HTTP_JSON));
        Request request = new Request.Builder()
                .post(requestBody)
                .url(url)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.code() < 200 || response.code() >= 300) {
                throw new SeaException("request " + url + " fail,http code:" + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SeaException("request " + url + " fail");
        }
    }

    // TODO 这里为啥不用 JSON
    public static String doPut(String url, Map<String, Object> queryParamMap, String body) {
        String requestUrl = null;
        if (queryParamMap == null) {
            requestUrl = url;
        } else {
            StringBuilder sb = new StringBuilder(url);
            sb.append("?");
            for (Map.Entry<String, Object> entry : queryParamMap.entrySet()) {
                sb.append(entry.getKey() + "=" + entry.getValue());
                sb.append("&");
            }
            requestUrl = sb.toString();
        }
        // MediaType.parse(HTTP_JSON) 获取  MediaType
        RequestBody requestBody = RequestBody.create(body, MediaType.parse(HTTP_JSON));
        Request request = new Request.Builder()
                .put(requestBody)
                .url(requestUrl)
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.code() < 200 || response.code() >= 300) {
                throw new SeaException("request " + requestUrl + " fail,http code:" + response.code());
            }
            return response.body().string();
        } catch (IOException e) {
            throw new SeaException("request " + requestUrl + " fail");
        }
    }
}
