package com.topwo.utils;

import android.app.Application;
import android.content.Context;

public class Topwo {
    private static final String TAG = Topwo.class.getSimpleName();

    private static Application sApplication = null;
    private static Context sContext = null;

    public static void init(Application application) {
        sApplication = application;
        sContext = sApplication.getApplicationContext();

        TopwoFile.init(sContext);
        TopwoLog.init(sContext);
        TopwoSharedPreferences.init(sContext);
        TopwoSystem.init(sContext);
        TopwoUri.init(sContext);
    }
}
