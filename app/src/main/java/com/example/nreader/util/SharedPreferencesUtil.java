package com.example.nreader.util;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SharedPreferencesUtil {

    public static final String KEY_NAMES = "KEY_NAMES";
    public static final String KEY_NUMBER_LENGTH = "KEY_NUMBER_LENGTH";

    private static SharedPreferences sharedPreferences = null;

    public static void init(SharedPreferences sps) {
        sharedPreferences = sps;
    }

    public static boolean putArray(String key, ArrayList<String> list) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray array = new JSONArray(list);
        String json = array.toString();
        Log.i("SharedPreferencesUtil", String.format("putArray: %s, %s", key, json));
        editor.putString(key, json);
        return editor.commit();
    }

    public static ArrayList<String> getArray(String key) {
        ArrayList<String> ret = new ArrayList<>();
        String json = sharedPreferences.getString(key, null);
        Log.i("SharedPreferencesUtil", String.format("getArray: %s, %s", key, json));
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    String str = array.optString(i);
                    ret.add(str);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static boolean putInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.i("SharedPreferencesUtil", String.format("putInt: %d", value));
        editor.putInt(key, value);
        return editor.commit();
    }

    public static int getInt(String key, int defValue) {
        int value = sharedPreferences.getInt(key, defValue);
        Log.i("SharedPreferencesUtil", String.format("getInt: %d", value));
        return value;
    }
}
