package com.sea.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sea.exception.SeaException;
import okhttp3.*;

import java.io.IOException;
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

    public static <T> void post(String url, T t) {
        RequestBody requestBody = RequestBody.create(MediaType.parse(HTTP_JSON), gson.toJson(t));
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

}
