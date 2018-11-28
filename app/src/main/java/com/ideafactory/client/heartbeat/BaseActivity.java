package com.ideafactory.client.heartbeat;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.ideafactory.client.business.localnetcall.CallNum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    private static List<Activity> activities = new ArrayList<Activity>();

    /**
     * 获得Activity
     */
    public static Activity getActivity() {
        return activities.get(activities.size() - 1);
    }

    /**
     * finish所有Activity
     */
    public static void finishAll() {
        finish(null);
    }

    /**
     * finish所有其它Activity
     */
    public static void finishOthers(Class<? extends Activity> activity) {
        finish(activity);
    }

    public static void finish(Class<? extends Activity> currentActivity) {
        for (Iterator<Activity> iterator = activities.iterator(); iterator.hasNext(); ) {
            Activity activity = iterator.next();
            if (activity.getClass() == currentActivity) {
                continue;
            }
            iterator.remove();
            activity.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activities.add(this);
        CallNum.callNumInstance();
    }

    @Override
    protected void onDestroy() {
//        CallNum.callNumInstance().releaseSourse();
        activities.remove(this);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        CallNum.callNumInstance().handKeyCode(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }
}
