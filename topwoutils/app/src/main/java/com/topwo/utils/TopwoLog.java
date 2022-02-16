package com.topwo.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public class TopwoLog {
    private static final String TAG = TopwoLog.class.getSimpleName();
    private static Context sContext = null;
    /*****消息类型****/
    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case 0:
                    Toast.makeText(sContext, (String)msg.obj, Toast.LENGTH_LONG).show();
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static void showLog(String msg){
        showTips(msg, false);
    }

    public static void showLog(String tag, String msg){
        showTips(tag + ":" + msg, false);
    }

    public static void showTips(String msg){
        showTips(msg, true);
    }

    public static void showTips(String tag, String msg){
        showTips(tag + ":" + msg, true);
    }

    public static void showTips(final String str, boolean is_user_visible){
        if(!BuildConfig.DEBUG && !is_user_visible){
            Log.d(TAG, str);
            return;
        }
        Message msg = handler.obtainMessage(0);
        msg.obj = str;
        msg.sendToTarget();
    }
}
