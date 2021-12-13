package com.topwo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class TopwoSharedPreferences {
    private static final String TAG = TopwoSharedPreferences.class.getSimpleName();

    // ===========================================================
    // Constants
    // ===========================================================
    private static final String PREFS_NAME = "TopwoPrefsFile";

    private static Context sContext = null;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static boolean getBoolForKey(String key, boolean defaultValue) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE); //MODE_PRIVATE（只能被自己的应用程序访问）、MODE_WORLD_READABLE（除了自己访问外还可以被其它应该程序读取）、MODE_WORLD_WRITEABLE（除了自己访问外还可以被其它应该程序读取和写入）
        try {
            return settings.getBoolean(key, defaultValue);
        }
        catch (Exception ex) {
            ex.printStackTrace();

            Map allValues = settings.getAll();
            Object value = allValues.get(key);
            if ( value instanceof String) {
                return  Boolean.parseBoolean(value.toString());
            }
            else if (value instanceof Integer) {
                int intValue = ((Integer) value).intValue();
                return (intValue !=  0) ;
            }
            else if (value instanceof Long) {
                long longValue = ((Long) value).intValue();
                return (longValue !=  0) ;
            }
            else if (value instanceof Float) {
                float floatValue = ((Float) value).floatValue();
                return (floatValue != 0.0f);
            }
        }

        return defaultValue;
    }

    public static int getIntegerForKey(String key, int defaultValue) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        try {
            return settings.getInt(key, defaultValue);
        }
        catch (Exception ex) {
            ex.printStackTrace();

            Map allValues = settings.getAll();
            Object value = allValues.get(key);
            if ( value instanceof String) {
                return  Integer.parseInt(value.toString());
            }
            else if (value instanceof Long) {
                return ((Long) value).intValue();
            }
            else if (value instanceof Float) {
                return ((Float) value).intValue();
            }
            else if (value instanceof Boolean) {
                boolean booleanValue = ((Boolean) value).booleanValue();
                if (booleanValue)
                    return 1;
            }
        }

        return defaultValue;
    }

    public static long getLongForKey(String key, long defaultValue) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        try {
            return settings.getLong(key, defaultValue);
        }
        catch (Exception ex) {
            ex.printStackTrace();

            Map allValues = settings.getAll();
            Object value = allValues.get(key);
            if ( value instanceof String) {
                return  Long.parseLong(value.toString());
            }
            else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            }
            else if (value instanceof Float) {
                return ((Float) value).longValue();
            }
            else if (value instanceof Boolean) {
                boolean booleanValue = ((Boolean) value).booleanValue();
                if (booleanValue)
                    return 1;
            }
        }

        return defaultValue;
    }

    public static float getFloatForKey(String key, float defaultValue) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        try {
            return settings.getFloat(key, defaultValue);
        }
        catch (Exception ex) {
            ex.printStackTrace();;

            Map allValues = settings.getAll();
            Object value = allValues.get(key);
            if ( value instanceof String) {
                return  Float.parseFloat(value.toString());
            }
            else if (value instanceof Integer) {
                return ((Integer) value).floatValue();
            }
            else if (value instanceof Long) {
                return ((Long) value).floatValue();
            }
            else if (value instanceof Boolean) {
                boolean booleanValue = ((Boolean) value).booleanValue();
                if (booleanValue)
                    return 1.0f;
            }
        }

        return defaultValue;
    }

    public static String getStringForKey(String key, String defaultValue) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        try {
            return settings.getString(key, defaultValue);
        }
        catch (Exception ex) {
            ex.printStackTrace();

            return settings.getAll().get(key).toString();
        }
    }

    public static double getDoubleForKey(String key, double defaultValue) {
        // SharedPreferences doesn't support saving double value
        return getFloatForKey(key, (float) defaultValue);
    }

    public static void setBoolForKey(String key, boolean value) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setIntegerForKey(String key, int value) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setLongForKey(String key, long value) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void setFloatForKey(String key, float value) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static void setStringForKey(String key, String value) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setDoubleForKey(String key, double value) {
        // SharedPreferences doesn't support recording double value
        setFloatForKey(key, (float)value);
    }

    public static boolean isContainKey(String key) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        return settings.contains(key);
    }

    public static void deleteValueForKey(String key) {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void clearAll() {
        SharedPreferences settings = sContext.getSharedPreferences(TopwoSharedPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }
}
