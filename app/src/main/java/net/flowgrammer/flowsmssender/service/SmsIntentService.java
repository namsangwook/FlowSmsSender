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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neox on 5/18/16.
 */
public class SmsIntentService extends IntentService {

    private static final String LOG_TAG = SmsIntentService.class.getSimpleName();
    public static final String ACTION_SMS_SENT = "net.flowgrammer.flowsmssender.SMS_SENT_ACTION";


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
    }

    @Override
    public IBinder onBind(Intent intent) {
//        Log.e(LOG_TAG, "service onBind");
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(LOG_TAG, "service onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.e(LOG_TAG, "service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendSms(String recipient, String smsBody, Integer seq) {
        SmsManager smsManager = SmsManager.getDefault();
//        int displaySeq = seq + 1;
//        smsBody += "\nseq : " + displaySeq;
        ArrayList<String> messages = smsManager.divideMessage(smsBody);
//        ArrayList<String> messages = smsManager.divideMessage("sldjflsdjflsjdflsjslkdjflsjdfljsdlkfjsldjflsjdfljsdlfjsldjflsdjflskjdflskjdflsjdflksjdflsjdlfkjsdlkfjsldkjfa;lkdslfjkasldjkfaljsdflajsdlfja;lsdjf;aldjsflajsdlfjalsdjflajsdflja;sldfjkal;jsdflajsdflajsdfljasldfjasldjflajsd;flajs;ldfjalkjsdflkajsdlfkjdlf");

        int messageCount = messages.size();
        Log.e(LOG_TAG, "Message Count: " + messageCount);

        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
        for (int i = 0; i < messages.size(); i++) {
            Intent intent = new Intent(ACTION_SMS_SENT);
            intent.putExtra("total", messageCount);
            intent.putExtra("seq", i + 1);
            PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), i, intent, PendingIntent.FLAG_ONE_SHOT);
            sentIntents.add(sentPI);
        }

        try {
            smsManager.sendMultipartTextMessage(recipient, null, messages, sentIntents, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("net.flowgrammer.intent.action.MESSAGE_PROCESSED");
            broadcastIntent.putExtra("result", "fail");
            broadcastIntent.putExtra("message", ex.getMessage());
            broadcastIntent.putExtra("seq", seq);
            getBaseContext().sendBroadcast(broadcastIntent);
        }
    }
}

