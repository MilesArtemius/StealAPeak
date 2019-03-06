package com.ekdorn.stealapeak;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class PrefManager {
    private static final String names_prefs = "NAMES";
    private static final String keys_prefs = "KEYS";
    private static final String keys_notifications = "NOTIFICATIONS";
    private static final String settings_prefs = "SETTINGS";

    private static PrefManager prefManager;
    private SharedPreferences namesPrefs;
    private SharedPreferences keysPrefs;
    private SharedPreferences notificationsPrefs;
    private SharedPreferences settingsPrefs;

    private PrefManager(Context context) {
        namesPrefs = context.getSharedPreferences(names_prefs, Context.MODE_PRIVATE);
        keysPrefs = context.getSharedPreferences(keys_prefs, Context.MODE_PRIVATE);
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



    public Map<String, User> getAllUsers() {
        Map<String, ?> namesMap = namesPrefs.getAll();
        Map<String, ?> tokensMap = keysPrefs.getAll();
        Map<String, ?> notificationsMap = notificationsPrefs.getAll();
        Map<String, User> userMap = new HashMap<>();

        for (Map.Entry entry: namesMap.entrySet()) {
            userMap.put((String) entry.getKey(),
                    new User((String) entry.getValue(), (String) tokensMap.get(entry.getKey()), (Boolean) notificationsMap.get(entry.getKey())));
        }

        return userMap;
    }

    public User getUser(String phone) {
        String name = namesPrefs.getString(phone, null);
        String token = keysPrefs.getString(phone, null);
        boolean isOpened = notificationsPrefs.getBoolean(phone, false);
        return new User(name, token, isOpened);
    }



    public void setUser(String phone, User user) {
        namesPrefs.edit().putString(phone, user.getName()).apply();
        keysPrefs.edit().putString(phone, user.getKey()).apply();
        notificationsPrefs.edit().putBoolean(phone, user.isNotificationOpened()).apply();
    }

    public void deleteUser(String phone) {
        namesPrefs.edit().remove(phone).apply();
        keysPrefs.edit().remove(phone).apply();
        notificationsPrefs.edit().remove(phone).apply();
    }

    public boolean isUser(String phone) {
        return keysPrefs.contains(phone);
    }

    public void logOut() {
        keysPrefs.edit().clear().apply();
        namesPrefs.edit().clear().apply();
        settingsPrefs.edit().clear().apply();
        notificationsPrefs.edit().clear().apply();
    }



    /*public void setToken(String token, String pref) {
        settingsPrefs.edit().putString(pref, token).apply();
    }

    public String getKey(String pref) {
        return settingsPrefs.getString(pref, null);
    }*/
}
