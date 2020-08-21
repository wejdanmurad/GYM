package com.uni.gym.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.uni.gym.R;


public class SharedPrefHelper {

    public static String getUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.key_userId), Context.MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.key_userId), null);
    }

    public static String getUserName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.key_userName), Context.MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.key_userName), null);
    }

    public static String getUserPic(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.key_userPic), Context.MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.key_userPic), null);
    }

    public static String getUserRole(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.key_userRole), Context.MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.key_userRole), null);
    }
}
