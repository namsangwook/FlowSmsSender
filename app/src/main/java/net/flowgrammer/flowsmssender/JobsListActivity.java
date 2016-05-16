package net.flowgrammer.flowsmssender;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.flowgrammer.flowsmssender.util.Const;
import net.flowgrammer.flowsmssender.util.Setting;
import net.flowgrammer.flowsmssender.util.Util;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by neox on 5/15/16.
 */
public class JobsListActivity extends AppCompatActivity {

    private static final String LOG_TAG = JobsListActivity.class.getSimpleName();

//    Setting mSetting;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        loadJobsList();
    }

    private void loadJobsList() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Cookie", "connect.sid=" + Setting.cookie(getApplicationContext()));

        RequestParams params = new RequestParams();
//        params.put("session", mSetting.authKey());

        client.get(Const.QUERY_URL + "/jobs", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Util.saveCookie(getApplicationContext(), headers);
//                onSuccess(statusCode, response);
//            }
//            public void onSuccess(JSONObject response) {
                Log.d("TEST", "success");
//                        Log.d("TEST", response.toString());
                String result = response.optString("result");
                if (result.equalsIgnoreCase("fail")) {
                    String message = response.optString("message");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(JobsListActivity.this, LoginActivity.class);
                    startActivityForResult(intent, 1);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                loadJobsList();
//                String result=data.getStringExtra("result");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Cookie", "connect.sid=" + Setting.cookie(getApplicationContext()));

            RequestParams params = new RequestParams();
//        params.put("session", mSetting.authKey());

            client.get(Const.QUERY_URL + "/logout", new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(LOG_TAG, "logout");
                    Setting.setCookie(getApplicationContext(), "");
                    Toast.makeText(getApplicationContext(), "Logout Success", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                    super.onFailure(statusCode, e, errorResponse);
                    Toast.makeText(getApplicationContext(), errorResponse == null ? "" : errorResponse.toString(), Toast.LENGTH_LONG).show();

                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
