package cz.org.drivingformillions;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

import cz.org.drivingformillions.models.UserModel;
import cz.org.drivingformillions.services.NetworkUtils;
import cz.org.drivingformillions.services.SaveSharedPrefrence;
import cz.org.drivingformillions.services.UrlCollection;

public class SplashActivity extends AppCompatActivity {

    private SaveSharedPrefrence sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPreferences = new SaveSharedPrefrence();
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                    String keyremember = sharedPreferences.getKeyRemember(getApplicationContext());
                    if(keyremember.equals("true")){
                        UserModel one = new UserModel();
                        one.setId(Integer.parseInt(sharedPreferences.getKeyUserID(getApplicationContext())));
                        one.setUsername(sharedPreferences.getKeyUsername(getApplicationContext()));
                        one.setEmail(sharedPreferences.getKeyEmail(getApplicationContext()));
                        MyApp.getInstance().myProfile = one;
                        getLoginUser();
                    } else{
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                }
            }
        };
        timerThread.start();

        // Add code to print out the key hash
        /*try {
            PackageInfo info = getPackageManager().getPackageInfo("cz.org.drivingformillions", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
        }*/

    }

    private void getLoginUser(){
        //internet detect
        if(!NetworkUtils.isNetworkConnected(getApplicationContext())){
            Toast.makeText(getApplicationContext(),getString(R.string.ERROR_NETWORK),Toast.LENGTH_SHORT).show();
            return;
        }

        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.getuserinfo_url)
                .addBodyParameter("uid", String.valueOf(MyApp.getInstance().myProfile.getId()))
                .setTag("Android")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        try{
                            String status = response.getString("status");
                            if(status.equals("success")){
                                JSONObject object = response.getJSONObject("data");
                                Gson gson = new Gson();
                                UserModel one = new UserModel();
                                one = gson.fromJson(object.toString(), UserModel.class);
                                MyApp.getInstance().myProfile = one;

                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                                finish();
                            } else{
                                sharedPreferences.saveKeyRemember(getApplicationContext(), "false");
                                String err = response.getString("error");
                                showToast(err);
                                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(i);
                                finish();
                            }
                        } catch (JSONException ex){
                            showToast(ex.getMessage());
                            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        showToast(error.getMessage());
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
    }

    private void showToast(String txt){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.mytoast,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(txt);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM|Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
