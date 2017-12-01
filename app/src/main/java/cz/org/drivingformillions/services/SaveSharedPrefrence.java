package cz.org.drivingformillions.services;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 12/4/2017.
 */

public class SaveSharedPrefrence {

    Context context;
    SharedPreferences sharedPreferences;

    public static final String PREFS_NAME = "DrivingForMillions";
    public static final String KEY_USER_ID = "key_user_id";
    public static final String KEY_USER_NAME = "key_username";
    public static final String KEY_EMAIL = "key_email";
    public static final String KEY_REMEMBER = "key_remember";
    public static final String KEY_TOURGUIDE = "key_tour";

    public String getKeyTourguide(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(KEY_TOURGUIDE, "false");

        return status;
    }

    public void saveKeyTourguide(Context context, String keyTourguide) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOURGUIDE, keyTourguide);

        editor.commit();
    }

    public void saveKeyRemember(Context context, String remember){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_REMEMBER, remember);

        editor.commit();
    }

    public String getKeyRemember(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(KEY_REMEMBER, "false");

        return status;
    }

    public void saveKeyEmail(Context context, String password){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, password);

        editor.commit();
    }

    public String getKeyEmail(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(KEY_EMAIL, "");

        return status;
    }

    public void saveKeyUsername(Context context, String username){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, username);

        editor.commit();
    }

    public String getKeyUsername(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(KEY_USER_NAME, "");

        return status;
    }

    public void saveKeyUserID(Context context, String userid){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userid);

        editor.commit();
    }

    public String getKeyUserID(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(KEY_USER_ID, "0");

        return status;
    }

    public void DeletePrefrence(Context context) {

        sharedPreferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

    }
}
