package net.flowgrammer.flowsmssender.jobdetail;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.flowgrammer.flowsmssender.LoginActivity;
import net.flowgrammer.flowsmssender.R;
import net.flowgrammer.flowsmssender.jobs.JobsAdapter;
import net.flowgrammer.flowsmssender.util.Const;
import net.flowgrammer.flowsmssender.util.Setting;
import net.flowgrammer.flowsmssender.util.Util;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.List;

/**
 * Created by neox on 5/17/16.
 */
public class JobDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = JobDetailActivity.class.getSimpleName();
    public static final String ACTION_SMS_SENT = "net.flowgrammer.flowsmssender.SMS_SENT_ACTION";


    Button mSendButton;
    DetailAdapter mDetailAdapter;
    Integer mTotalPage;
    Integer mCurrentPage;
    Integer mItemPerPage;
    String mJobID;

    ProgressDialog mDialog;
    Boolean isSendingSms;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        mCurrentPage = 1;
        isSendingSms = false;

        mJobID = getIntent().getStringExtra("jobID");
        Log.i(LOG_TAG, "job id : " + mJobID);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(true);

        EditText editContent = (EditText)findViewById(R.id.edit_content);
//        editContent.setEnabled(false);
//        editContent.setInputType(InputType.TYPE_NULL);
//        editContent.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });

        ListView listView = (ListView)findViewById(R.id.listview);
        mDetailAdapter = new DetailAdapter(this, getLayoutInflater());
        listView.setAdapter(mDetailAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        mSendButton = (Button)findViewById(R.id.btn_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSendingSms) {
                    isSendingSms = true;
                    mSendButton.setText("Stop");
                    startSmsSending();
                } else {
                    isSendingSms = false;
                    mSendButton.setText("Start");
                    stopSmsSending();
                }
            }
        });

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = null;
                boolean error = true;
                Integer seq = intent.getIntExtra("seq", -1);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        message = "Message sent!";
                        error = false;
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        message = "Error.";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        message = "Error: No service.";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        message = "Error: Null PDU.";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        message = "Error: Radio off.";
                        break;
                }


            }
        }, new IntentFilter(ACTION_SMS_SENT));

        loadJobDetail();
    }

    private void stopSmsSending() {
    }

    private void startSmsSending() {
    }

    private void sendSms(String recipient, String smsBody, Integer seq) {
//        String strSMSBody = "";
        //sms recipient added by user from the activity screen
//        String strReceipentsList = "01034567890";
        SmsManager sms = SmsManager.getDefault();
        List<String> messages = sms.divideMessage(smsBody);
        for (String message : messages) {
            Intent intent = new Intent(ACTION_SMS_SENT);
            intent.putExtra("seq", seq);
            sms.sendTextMessage(recipient, null, message, PendingIntent.getBroadcast(
                    this, 0, intent, 0), null);
        }
    }

    private void loadJobDetail() {

        mDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Cookie", "connect.sid=" + Setting.cookie(getApplicationContext()));
        client.addHeader("Accept", "application/json");

        RequestParams params = new RequestParams();
//        params.put("session", mSetting.authKey());

        String requestUrl = Const.QUERY_URL + "/jobs/" + mJobID + "/page/" + mCurrentPage;

        Log.e(LOG_TAG, "request url : " + requestUrl);

        client.get(requestUrl, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                mDialog.dismiss();
                Util.saveCookie(getApplicationContext(), headers);
                Log.d(LOG_TAG, response.toString());
                String result = response.optString("result");
                if (!result.equalsIgnoreCase("success")) {
                    String message = response.optString("message");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(JobDetailActivity.this, LoginActivity.class);
                    startActivityForResult(intent, 1);
                    return;
                }
                JSONObject job = response.optJSONObject("job");
                JSONArray smsList  = job.optJSONArray("smslist");
                JSONObject author = job.optJSONObject("author");
                String created = job.optString("created");
                String title = job.optString("name");
                String content = job.optString("description");

                TextView textTitle = (TextView)findViewById(R.id.text_title);
                textTitle.setText(title);
                EditText editContent = (EditText)findViewById(R.id.edit_content);
                editContent.setText(content);

                View view = JobDetailActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

//                mTotalPage = Integer.valueOf(response.optString("totalJobCount"));
//                mCurrentPage = Integer.valueOf(response.optString("currentPage"));
//                mItemPerPage = Integer.valueOf(response.optString("itemPerPage"));
//                Log.i(LOG_TAG, "totalPage : " + mTotalPage
//                        + ", currentPage : " + mCurrentPage
//                        + ", itemPerPage ; " + mItemPerPage);

//                content = content.replace("\\r", "\r");
//                content = content.replace("\\n", "\n");

                Log.e(LOG_TAG, "author : " + author.toString()
                                + ", created : " + created
                                + ", title ; " + title);
                mDetailAdapter.updateData(smsList);
            }

            @Override
            public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                mDialog.dismiss();
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
                loadJobDetail();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
            }
        }

    }
}
