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
        SharedPreferences preferences = getPreferences(context);
        boolean followSystem = isThemeFollowSystem(context);
        int themeMode = getThemeMode(context);
        preferences.edit().clear().apply();
        setThemeFollowSystem(context, followSystem);
        setThemeMode(context, themeMode);
    }

    public static void setThemeFollowSystem(Context context, boolean followSystem) {
        getPreferences(context).edit()
                .putBoolean(Constants.SP_KEY_THEME_FOLLOW_SYSTEM, followSystem)
                .apply();
    }

    public static boolean isThemeFollowSystem(Context context) {
        return getPreferences(context).getBoolean(Constants.SP_KEY_THEME_FOLLOW_SYSTEM, true);
    }

    public static void setThemeMode(Context context, int themeMode) {
        getPreferences(context).edit()
                .putInt(Constants.SP_KEY_THEME_MODE, themeMode)
                .apply();
    }

    public static int getThemeMode(Context context) {
        return getPreferences(context).getInt(Constants.SP_KEY_THEME_MODE, 0);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
    }
}
