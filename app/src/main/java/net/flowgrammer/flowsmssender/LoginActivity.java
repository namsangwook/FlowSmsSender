package net.flowgrammer.flowsmssender;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.flowgrammer.flowsmssender.util.Setting;
import net.flowgrammer.flowsmssender.util.Util;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by neox on 5/15/16.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String QUERY_URL = "http://10.0.2.2:3003/api";


    Setting mSetting;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSetting = new Setting(this);

        Button loginButton = (Button)findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Cookie", "connect.sid=" + Setting.cookie(getApplicationContext()));

        RequestParams params = new RequestParams();

        EditText username = (EditText)findViewById(R.id.username_text);
        EditText password = (EditText)findViewById(R.id.password_text);
        params.put("username", username.getText().toString());
        params.put("password", password.getText().toString());

        client.post(QUERY_URL + "/login", params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Util.saveCookie(getApplicationContext(), headers);
//            public void onSuccess(JSONObject response) {
                Log.d("TEST", "success");
                Log.d("TEST", response.toString());
                String result = response.optString("result");
                String sessionId = response.optString("session_id");
//                mSetting.setAuthKey(sessionId);
                if (result.equalsIgnoreCase("success")) {
//                    Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
//                    finishActivity(0);
                    setResult(Activity.RESULT_OK);
                    finish();
                }
                else {
                    String message = response.optString("message");
                    if (message.length() < 1) {
                        message = "Login failed";
                    }
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
//                        Log.d("TEST", response.toString());
//                        super.onSuccess(response);
            }

            @Override
            public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, e, errorResponse);
                Toast.makeText(getApplicationContext(), errorResponse.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
