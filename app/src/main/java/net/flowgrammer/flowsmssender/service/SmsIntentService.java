package net.flowgrammer.flowsmssender.service;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.flowgrammer.flowsmssender.util.Const;
import net.flowgrammer.flowsmssender.util.Setting;
import net.flowgrammer.flowsmssender.util.Util;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by neox on 5/18/16.
 */
public class SmsIntentService extends IntentService {

    private static final String LOG_TAG = SmsIntentService.class.getSimpleName();
    public static final String ACTION_SMS_SENT = "net.flowgrammer.flowsmssender.SMS_SENT_ACTION";


    int mCurrentSeq = -1;

    BroadcastReceiver mReceiver;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SmsIntentService() {
        super(SmsIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String name = intent.getStringExtra("name");
        String phonenumber = intent.getStringExtra("phonenumber");
        String content = intent.getStringExtra("content");
        int seq = intent.getIntExtra("seq", -1);
        Log.e(LOG_TAG, "name : " + name + ", phonenumber : " + phonenumber
                + ", content : " + content + ", seq : " + seq);

        sendSms(phonenumber, content, seq);

//            Thread.sleep(5 * 1000);
//            Intent broadcastIntent = new Intent();
//            broadcastIntent.setAction("net.flowgrammer.intent.action.MESSAGE_PROCESSED");
//            broadcastIntent.putExtra("message", "Hello, BroadCast!");
//            getBaseContext().sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(LOG_TAG, "service onBind");
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(LOG_TAG, "service onCreate");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = "unknown";
                Integer seq = intent.getIntExtra("seq", -1);
                Log.e(LOG_TAG, "broadcast receive, seq : " + seq);
                Log.e(LOG_TAG, "broadcast receive, current seq : " + mCurrentSeq);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("net.flowgrammer.intent.action.MESSAGE_PROCESSED");
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        message = "Message sent!";
                        Log.e(LOG_TAG, "send sms success");
                        broadcastIntent.putExtra("result", "success");
                        broadcastIntent.putExtra("seq", mCurrentSeq);
                        getBaseContext().sendBroadcast(broadcastIntent);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        message = "Error.";
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        message = "Error: No service.";
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        message = "Error: Null PDU.";
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        message = "Error: Radio off.";
                    default:
                        Log.e(LOG_TAG, "send sms fail, reason : " + message);

                        broadcastIntent.putExtra("result", "fail");
                        broadcastIntent.putExtra("message", message);
                        broadcastIntent.putExtra("seq", mCurrentSeq);
                        getBaseContext().sendBroadcast(broadcastIntent);
                        break;
                }
            }
        };
        registerReceiver(mReceiver, new IntentFilter(ACTION_SMS_SENT));
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
        Log.e(LOG_TAG, "service onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(LOG_TAG, "service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendSms(String recipient, String smsBody, Integer seq) {
//        String strSMSBody = "";
        //sms recipient added by user from the activity screen
//        String strReceipentsList = "01034567890";
//        try {
//            Thread.sleep(5 * 1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction("net.flowgrammer.intent.action.MESSAGE_PROCESSED");
//        broadcastIntent.putExtra("message", "Hello, BroadCast!");
//        getBaseContext().sendBroadcast(broadcastIntent);



        SmsManager sms = SmsManager.getDefault();
        List<String> messages = sms.divideMessage(smsBody);
        for (String message : messages) {
            Intent intent = new Intent(ACTION_SMS_SENT);
            intent.putExtra("seq", seq);
            mCurrentSeq = seq;
            sms.sendTextMessage(recipient, null, message, PendingIntent.getBroadcast(
                    this, 0, intent, 0), null);
        }
    }
}
