package com.example.photoapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;


//keystore을 통해 암호화해야지만 보안가능

public class LoginInfoProvider {
    static final String PREF_USER_NAME = "userName";
    static final String PREF_USER_EMAIL = "userEmail";
    static final String PREF_USER_UID = "userUID";
    static final String PREF_USER_GOOGLEPHOTO_TOKEN= "token";

    static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    // 계정 정보 저장
    public static void setUserName(Context context, String userName) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.apply();
    }

    public static void setUserEmail(Context context, String userEmail) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_EMAIL, userEmail);
        editor.apply();
    }

    public static void setUserUID(Context context, String userUID) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_UID, userUID);
        editor.apply();
    }


    // 저장된 정보 가져오기
    public static String getUserName(Context context) {
        return getSharedPreferences(context).getString(PREF_USER_NAME, "");
    }

    public static String getUserEmail(Context context){
        return getSharedPreferences(context).getString(PREF_USER_EMAIL, "");
    }

    public static String getUserUID(Context context){
        return getSharedPreferences(context).getString(PREF_USER_UID , "");
    }

    // 로그아웃
    public static void clearUserData(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }

    // Data가 전부 저장되어 있는지 확인
    public static boolean checkPreference(Context context){
        if(getUserName(context).length()==0){
            return false;
        }

        if(getUserEmail(context).length()==0){
            return false;
        }

        return true;
    }

    public static Map<String, Object> getUserInfoMap (Context context){
        Map<String, Object> userinfo = new HashMap<>();
        userinfo.put("userName", LoginInfoProvider.getUserName(context));
        userinfo.put("userEmail", LoginInfoProvider.getUserEmail(context));
        userinfo.put("userUID", LoginInfoProvider.getUserUID(context));
        return  userinfo;
    }
}

// Logout
// Login정보 바뀌었을떄 생각해야됌