package com.ideafactory.client.business.weichat.weichatutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

public class WeiChatReceiver extends BroadcastReceiver {
    private static final String TAG = "WeiChatReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String notifiTitleString = intent.getStringExtra(com.ideafactory.client.xmpp.Constants.NOTIFICATION_TITLE);

        if (!TextUtils.isEmpty(notifiTitleString) && notifiTitleString.equals("weixin")) {
            String notificationMessage = intent.getStringExtra(com.ideafactory.client.xmpp.Constants.NOTIFICATION_MESSAGE);
            Log.e(TAG, "onReceive: " + notificationMessage);
            try {
                JSONObject jsonObject = new JSONObject(notificationMessage);
                if (!jsonObject.isNull("type")) {
                    if (weiChatReceived != null) {
                        weiChatReceived.receivedWCMsg(notificationMessage);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface weiChatReceived {
        void receivedWCMsg(String msg);
    }

    public static weiChatReceived weiChatReceived;

    public static void setWeiChatReceived(WeiChatReceiver.weiChatReceived weiChatReceived) {
        WeiChatReceiver.weiChatReceived = weiChatReceived;
    }


}

