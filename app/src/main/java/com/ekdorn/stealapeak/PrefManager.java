package com.ekdorn.stealapeak;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class PrefManager {
    private static final String contacts_prefs = "CONTACTS";
    private static final String settings_prefs = "SETTINGS";

    private static PrefManager prefManager;
    private SharedPreferences namesPrefs;
    private SharedPreferences tokensPrefs;
    private SharedPreferences settingsPrefs;

    private PrefManager(Context context) {
        namesPrefs = context.getSharedPreferences(contacts_prefs, Context.MODE_PRIVATE);
        tokensPrefs = context.getSharedPreferences(contacts_prefs, Context.MODE_PRIVATE);
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
        Map<String, ?> tokensMap = tokensPrefs.getAll();
        Map<String, User> userMap = new HashMap<>();

        for (Map.Entry entry: namesMap.entrySet()) {
            userMap.put((String) entry.getKey(), new User((String) entry.getValue(), (String) tokensMap.get(entry.getKey())));
        }

        return userMap;
    }

    public User getUserByPhone(String phone) {
        String name = namesPrefs.getString(phone, null);
        String token = tokensPrefs.getString(phone, null);
        return new User(name, token);
    }



    public static final String MY_TOKEN = "token";

    public void setUser(String phone, User user) {
        namesPrefs.edit().putString(phone, user.getName()).apply();
        tokensPrefs.edit().putString(phone, user.getToken()).apply();
    }

    public void deleteUser(String phone) {
        namesPrefs.edit().remove(phone).apply();
        tokensPrefs.edit().remove(phone).apply();
    }

    public void logOut() {
        tokensPrefs.edit().clear().apply();
        namesPrefs.edit().clear().apply();

        String lastToken = settingsPrefs.getString(MY_TOKEN, "");
        settingsPrefs.edit().clear().putString(MY_TOKEN, lastToken).apply();
    }



    public void setToken(String token, String pref) {
        settingsPrefs.edit().putString(pref, token).apply();
    }

    public String getToken(String pref) {
        return settingsPrefs.getString(pref, null);
    }
}
