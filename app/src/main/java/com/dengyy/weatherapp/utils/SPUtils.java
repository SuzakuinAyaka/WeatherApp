package com.dengyy.weatherapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dengyy.weatherapp.constants.Constants;

public final class SPUtils {

    private SPUtils() {
    }

    public static void saveLoginUser(Context context, long userId, String username) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit()
                .putLong(Constants.SP_KEY_LOGIN_USER_ID, userId)
                .putString(Constants.SP_KEY_LOGIN_USERNAME, username)
                .apply();
    }

    public static long getLoginUserId(Context context) {
        return getPreferences(context).getLong(Constants.SP_KEY_LOGIN_USER_ID, -1L);
    }

    public static void clearLoginUser(Context context) {
        getPreferences(context).edit().clear().apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
    }
}
