package net.flowgrammer.flowsmssender;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.flowgrammer.flowsmssender.util.Setting;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by neox on 5/15/16.
 */
public class JobsListActivity extends AppCompatActivity {

    private static final String QUERY_URL = "http://10.0.2.2:3003/api";

    Setting mSetting;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        mSetting = new Setting(getApplicationContext());
        loadJobsList();

    }

    private void loadJobsList() {
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("session", mSetting.authKey());

        client.post(QUERY_URL + "/jobs", params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("TEST", "success");
//                        Log.d("TEST", response.toString());
                String result = response.optString("result");
                if (result.equalsIgnoreCase("fail")) {
                    String message = response.optString("message");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(JobsListActivity.this, LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                JSONArray list = response.optJSONArray("list");
                Log.d("TEST", list.toString());
                if (result.equalsIgnoreCase("success")) {
                    Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
                }
//                        Log.d("TEST", response.toString());
//                        super.onSuccess(response);
            }

            @Override
            public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, e, errorResponse);
                Toast.makeText(getApplicationContext(), errorResponse == null ? "" : errorResponse.toString(), Toast.LENGTH_LONG).show();

            }
        });
    }
}
