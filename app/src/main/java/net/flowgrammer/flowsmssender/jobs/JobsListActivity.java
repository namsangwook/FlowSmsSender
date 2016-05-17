package net.flowgrammer.flowsmssender.jobs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.flowgrammer.flowsmssender.LoginActivity;
import net.flowgrammer.flowsmssender.MainActivity;
import net.flowgrammer.flowsmssender.R;
import net.flowgrammer.flowsmssender.jobdetail.JobDetailActivity;
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

    JobsAdapter mJobsAdapter;
    Integer mTotalPage;
    Integer mCurrentPage;
    Integer mItemPerPage;

    ProgressDialog mDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);

        mCurrentPage = 1;

        ListView listView = (ListView)findViewById(R.id.listview);
        mJobsAdapter = new JobsAdapter(this, getLayoutInflater());
        listView.setAdapter(mJobsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject jsonObject = (JSONObject) mJobsAdapter.getItem(position);
                String jobID = jsonObject.optString("_id","");
                Intent detailIntent = new Intent(JobsListActivity.this, JobDetailActivity.class);
                detailIntent.putExtra("jobID", jobID);
                startActivity(detailIntent);
            }
        });

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(true);

        loadJobsList();
    }

    private void loadJobsList() {
        mDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Cookie", "connect.sid=" + Setting.cookie(getApplicationContext()));
        client.addHeader("Accept", "application/json");

        RequestParams params = new RequestParams();
//        params.put("session", mSetting.authKey());

        client.get(Const.QUERY_URL + "/jobs/page/" + mCurrentPage, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                mDialog.dismiss();
                Util.saveCookie(getApplicationContext(), headers);
                Log.d(LOG_TAG, response.toString());
                String result = response.optString("result");
                if (!result.equalsIgnoreCase("success")) {
                    String message = response.optString("message");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(JobsListActivity.this, LoginActivity.class);
                    startActivityForResult(intent, 1);
                    return;
                }
                JSONArray list = response.optJSONArray("jobs");
                mTotalPage = Integer.valueOf(response.optString("totalJobCount"));
                mCurrentPage = Integer.valueOf(response.optString("currentPage"));
                mItemPerPage = Integer.valueOf(response.optString("itemPerPage"));
                Log.i(LOG_TAG, "totalPage : " + mTotalPage
                        + ", currentPage : " + mCurrentPage
                        + ", itemPerPage ; " + mItemPerPage);
//                Log.d(LOG_TAG, list.toString());
//                if (result.equalsIgnoreCase("success")) {
//                    Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
//                }
                mJobsAdapter.updateData(list);
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
                loadJobsList();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
            }
        }

    }
}
