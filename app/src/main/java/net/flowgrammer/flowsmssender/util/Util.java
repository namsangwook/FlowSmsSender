package net.flowgrammer.flowsmssender.util;

import android.content.Context;
import android.util.Log;

import org.apache.http.Header;

/**
 * Created by neox on 5/16/16.
 */
public class Util {
    private static final String LOG_TAG = Util.class.getSimpleName();

    public static void saveCookie(Context context, Header[] headers) {
        for (Header header : headers) {
//            Log.i(LOG_TAG, header.getName() + " : " + header.getValue());
            if (header.getName().equalsIgnoreCase("set-cookie")) {
                String cookie = header.getValue();
                String [] cookieValues = cookie.split(";");
                for (String cookieValue : cookieValues) {
                    String [] keyValues = cookieValue.split("=");
                    String key = keyValues[0];
                    if (key.equalsIgnoreCase("connect.sid")) {
                        Log.e(LOG_TAG, "save cookie : " + keyValues[1]);
                        Setting.setCookie(context, keyValues[1]);
                        return;
                    }
                }
            }
        }
    }
}
