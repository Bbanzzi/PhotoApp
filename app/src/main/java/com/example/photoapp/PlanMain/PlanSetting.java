package com.example.photoapp.PlanMain;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PlanSetting {
    private static final String PREF_PLAN_PHOTO_UPLOADED = "photoUploaded";

    static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setPhotoUploaded(Context context, String planInfo, int photoUploaded) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_PLAN_PHOTO_UPLOADED + planInfo, String.valueOf(photoUploaded));
        editor.apply();
    }

    public static String getPhotoUploaded(Context context, String planInfo){
        return getSharedPreferences(context).getString(PREF_PLAN_PHOTO_UPLOADED+planInfo , null);
    }

    public static void removePhotoUploaded(Context context, String planInfo){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(PREF_PLAN_PHOTO_UPLOADED+planInfo).apply();
    }

}
