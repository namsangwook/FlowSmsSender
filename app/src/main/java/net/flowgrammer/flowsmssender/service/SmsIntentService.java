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

import java.util.List;

/**
 * Created by neox on 5/18/16.
 */
public class SmsIntentService extends IntentService {

    private static final String LOG_TAG = SmsIntentService.class.getSimpleName();
    public static final String ACTION_SMS_SENT = "net.flowgrammer.flowsmssender.SMS_SENT_ACTION";
    public static final String ACTION_SMS_DELIVERED = "net.flowgrammer.flowsmssender.SMS_DELIVERED_ACTION";

    int mCurrentSeq = -1;

    BroadcastReceiver mReceiver;
    BroadcastReceiver mDeliveredReceiver;

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
//        Log.e(LOG_TAG, "service onCreate");

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
//                        broadcastIntent.putExtra("result", "success");
//                        broadcastIntent.putExtra("message", message);
//                        broadcastIntent.putExtra("seq", mCurrentSeq);
//                        getBaseContext().sendBroadcast(broadcastIntent);
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


        mDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent broadcastIntent) {
                switch (getResultCode()) {

                    case Activity.RESULT_OK: {
                        String message = "Message sent!";
//                        Toast.makeText(getBaseContext(), "SMS delivered",
//                                Toast.LENGTH_SHORT).show();
                        broadcastIntent.putExtra("result", "success");
                        broadcastIntent.putExtra("message", message);
                        broadcastIntent.putExtra("seq", mCurrentSeq);
                        getBaseContext().sendBroadcast(broadcastIntent);
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        Log.e(LOG_TAG, "delivered intent : canceled");
                        String message = "Message canceled!";
//                        Toast.makeText(getBaseContext(), "SMS not delivered",
//                                Toast.LENGTH_SHORT).show();
                        broadcastIntent.putExtra("result", "fail");
                        broadcastIntent.putExtra("message", message);
                        broadcastIntent.putExtra("seq", mCurrentSeq);
                        getBaseContext().sendBroadcast(broadcastIntent);
                        break;
                    }
                }
            }
        };
        registerReceiver(mDeliveredReceiver, new IntentFilter(ACTION_SMS_DELIVERED));
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (mDeliveredReceiver != null) {
            unregisterReceiver(mDeliveredReceiver);
        }
        super.onDestroy();
//        Log.e(LOG_TAG, "service onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.e(LOG_TAG, "service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendSms(String recipient, String smsBody, Integer seq) {
        SmsManager sms = SmsManager.getDefault();
        int displaySeq = seq + 1;
        smsBody += "\nseq : " + displaySeq;
        List<String> messages = sms.divideMessage(smsBody);

        int messageCount = messages.size();
        Log.e(LOG_TAG, "Message Count: " + messageCount);

//        ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
//        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
//
//        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_SMS_SENT), 0);
//        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_SMS_DELIVERED), 0);
//
//        for (int j = 0; j < messageCount; j++) {
//            sentIntents.add(sentPI);
//            deliveryIntents.add(deliveredPI);
//        }
        mCurrentSeq = seq;
//        sms.sendTextMessage(recipient, null, smsBody, sentPI, deliveredPI);
        for (String message : messages) {
//            Intent intent = new Intent(ACTION_SMS_SENT);
//            intent.putExtra("seq", seq);
            Log.e(LOG_TAG, "Send Message : " + message);
            mCurrentSeq = seq;
//            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_SMS_SENT), 0);
//            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_SMS_DELIVERED), 0);
//
//            sms.sendTextMessage(recipient, null, message, sentPI, deliveredPI);
            sms.sendTextMessage(recipient, null, message, null, null); // 일단 시간텀을 두고 sms 를 보내자

            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("net.flowgrammer.intent.action.MESSAGE_PROCESSED");

        broadcastIntent.putExtra("result", "success");
        broadcastIntent.putExtra("message", "success");
        broadcastIntent.putExtra("seq", mCurrentSeq);
        getBaseContext().sendBroadcast(broadcastIntent);
    }
}
