package com.wl.okhttpframe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OKHttpFrame";
    private final String url = "https://www.baidu.com/";
    private Button mSendUserIdReq;
    private MyInterceptor interceptor;
    private OkHttpClient client;
    private JSONObject userID;
    private JSONObject userIDData;
    public static final MediaType JSON= MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);

        interceptor = new MyInterceptor();

        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();

        initUserIdData();

        mSendUserIdReq = findViewById(R.id.user_id);
        mSendUserIdReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserIDRequest();
            }
        });

    }

    private void initUserIdData() {
        userIDData = new JSONObject();
        userID = new JSONObject();
        try {
            userIDData.put("appId", "xxxxxxxxxxxxxxxxx");
            updateEmployeeInfo(userIDData);
            userID.put("version", "V1");
            userID.put("data", userIDData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getUserIDRequest() {
        RequestBody requestBody = RequestBody.create(String.valueOf(userID), JSON);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Call call = client.newCall(request);
        Log.d(TAG, "send request");

        call.enqueue(new Callback() {  //异步请求
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d(TAG, "onResponse threadname: " + Thread.currentThread().getName());
                parseJsonWithJsonObject(response);
            }
        });

        // call.execute()，不能在主线程网络请求，需要开启线程进行同步请求
    }

    private void updateEmployeeInfo(JSONObject object) {
        try {
            object.put("userid", "xxxx");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseJsonWithJsonObject(Response response) throws IOException {
        String responseData = response.body().string();
        Log.d(TAG, "parseJsonWithJsonObject: " + responseData);
        try{
            JSONObject alldata = new JSONObject(responseData);
            if(!alldata.has("body")) {
                Log.d(TAG, "parseJsonWithJsonObject: alldata not contain body");
                return;
            }
            JSONObject body = new JSONObject(alldata.get("body").toString());
            if(!body.has("data")) {
                Log.d(TAG, "parseJsonWithJsonObject: body not contain data");
                return;
            }
            JSONObject data = new JSONObject(body.get("data").toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class MyInterceptor implements Interceptor {

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request request = chain.request();// 可以新建一个Request，用于重定向
            Response response = chain.proceed(request); // 可以修改Response内容或者新建Response
            return response;
        }
    }
}
