package com.calm_health.sports.share;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * save flags
 */
public class AppSharedPreferences {

    public static final String PREFS_NAME = "AOP_PREFS";

    public static final String SETTINGS_UNIT = "setting unit";

    public static final String AUTO_UPLOAD = "auto upload";

    public static final String BackgroundDataTransfer = "background";

    public static final String BLE_MAC = "ble_mac";


    public AppSharedPreferences() {
        super();
    }


    public static void saveUserIDPreference(Context ctx, String userID) {

        try {

            SharedPreferences.Editor editor = ctx.getSharedPreferences("AppPref", Context.MODE_PRIVATE).edit();
            editor.putString("UserID", userID);
            editor.commit();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public static String loadUserIDPreference(Context ctx) {
        String UserIDStr = "";
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
            UserIDStr = prefs.getString("UserID", "");
            Log.e("calm", "UserIDStr " + UserIDStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return UserIDStr;
    }

    // here 0 - metric 1 - imperial
    public static void storeSettingsUnits(Context context, int unit) {

        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.putInt(SETTINGS_UNIT, unit);

        editor.commit();
    }

    // here 0 - metric 1 - imperial
    public static int restoreSettingsUnits(Context context) {
        SharedPreferences settings;
        int unit;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        unit = settings.getInt(SETTINGS_UNIT, 0);

        return unit;
    }


    // here 0 - metric 1 - imperial
    public static void storeAutoUpload(Context context, int oNoff) {

        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.putInt(AUTO_UPLOAD, oNoff);

        editor.commit();
    }

    // here 0 - metric 1 - imperial
    public static int loadAutoUpload(Context context) {

        SharedPreferences settings;

        int unit;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        unit = settings.getInt(AUTO_UPLOAD, 1);

        return unit;
    }

    // here 0 - metric 1 - imperial
    public static void storeBackGroundTransfer(Context context, int oNoff) {

        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.putInt(BackgroundDataTransfer, oNoff);

        editor.commit();
    }

    // here 0 - metric 1 - imperial
    public static int loadBackGroundTransfer(Context context) {

        SharedPreferences settings;

        int unit;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        unit = settings.getInt(BackgroundDataTransfer, 0);

        return unit;
    }


    public static String getMac(Context context)
    {
        SharedPreferences bbSettings;
        SharedPreferences.Editor editor;
        bbSettings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String strMac = null;
        strMac = String.valueOf(bbSettings.getString(BLE_MAC, null));
        return strMac;
    }

    public static void setMac(Context context, String strMac)
    {
        SharedPreferences bbSettings;
        SharedPreferences.Editor editor;
        bbSettings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = bbSettings.edit();
        editor.putString(BLE_MAC, strMac);
        editor.commit();
    }


}