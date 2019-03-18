package com.ekdorn.stealapeak.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static final String keys_notifications = "NOTIFICATIONS";
    private static final String settings_prefs = "SETTINGS";

    private static PrefManager prefManager;
    private SharedPreferences notificationsPrefs;
    private SharedPreferences settingsPrefs;

    private PrefManager(Context context) {
        notificationsPrefs = context.getSharedPreferences(keys_notifications, Context.MODE_PRIVATE);
        settingsPrefs = context.getSharedPreferences(settings_prefs, Context.MODE_PRIVATE);
    }

    public static PrefManager get(Context context) {
        if (prefManager != null) {
            return prefManager;
        } else {
            prefManager = new PrefManager(context);
            return prefManager;
        }
    }



    public int getNotifications(String phone) {
        return notificationsPrefs.getInt(phone, 0);
    }

    public void incrementNotifications(String phone) {
        int not = notificationsPrefs.getInt(phone, 0) + 1;
        notificationsPrefs.edit().putInt(phone, not).apply();
    }

    public void nullNotifications(String phone) {
        notificationsPrefs.edit().putInt(phone, 0).apply();
    }



    public void logOut() {
        settingsPrefs.edit().clear().apply();
        notificationsPrefs.edit().clear().apply();
    }
}
