package cz.org.drivingformillions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import cz.org.drivingformillions.models.UserModel;
import cz.org.drivingformillions.services.NetworkUtils;
import cz.org.drivingformillions.services.SaveSharedPrefrence;
import cz.org.drivingformillions.services.UrlCollection;

public class LoginActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST= 123;

    private boolean bSignInStatus = true;
    private TextInputEditText tiet_username;
    private TextInputLayout til_username;
    private TextInputEditText tiet_password;
    private TextInputEditText tiet_email;
    private LinearLayout ll_emailcontainer;
    private TextView tv_login_register;
    private Button btn_submit, btn_fb;
    private TextView tv_forgot;
    private CallbackManager callbackManager;
    private SaveSharedPrefrence sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        onSuccessFacebookLogin(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this, "Facebook Login cancel.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(LoginActivity.this, "Facebook Login error.", Toast.LENGTH_SHORT).show();
                    }
                });


        setContentView(R.layout.activity_login);
        sharedPreferences = new SaveSharedPrefrence();

        checkPermission(this);
        initLayout();
    }

    private void initLayout(){
        tiet_username = (TextInputEditText) findViewById(R.id.sign_in_username);
        tiet_password = (TextInputEditText) findViewById(R.id.sign_in_password);
        tiet_email = (TextInputEditText) findViewById(R.id.sign_in_email);
        ll_emailcontainer = (LinearLayout) findViewById(R.id.sign_in_email_container);
        til_username = (TextInputLayout) findViewById(R.id.sign_in_username_text_input_layout);
        tv_forgot = (TextView) findViewById(R.id.sign_in_forgot_pass);
        tv_login_register = (TextView) findViewById(R.id.login_or_register);
        btn_submit = (Button) findViewById(R.id.login_or_register_button);
        btn_fb = (Button) findViewById(R.id.login_fb_button);

        tv_login_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bSignInStatus){
                    bSignInStatus = false;
                } else{
                    bSignInStatus = true;
                }
                setInitData();
            }
        });

        tv_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkingFields()){
                    if(bSignInStatus){
                        loginUserProfit();
                    } else{
                        signupUserProfit();
                    }
                }
            }
        });

        btn_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //facebook logout
                if (AccessToken.getCurrentAccessToken() != null) {
                    LoginManager.getInstance().logOut();
                }
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile, email"));

            }
        });
        setInitData();
    }

    public void onSuccessFacebookLogin(final AccessToken token) {
        final AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        if (object != null) {
                            try {
                                String fbId = object.getString("id");
                                String fbEmail = object.getString("email");
                                String name = object.getString("first_name");
                                String surname = object.getString("last_name");
                                String photoUrl = "https://graph.facebook.com/" + object.getString("id") + "/picture?type=large";
                                loginWithFacebook(fbId, fbEmail, name, surname);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            showToast("GraphRequest is failed.");
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,gender,email,birthday,location,locale,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void loginWithFacebook(String fbId, String fbEmail, String name, String surname){
        //internet detect
        if(!NetworkUtils.isNetworkConnected(getApplicationContext())){
            Toast.makeText(getApplicationContext(),getString(R.string.ERROR_NETWORK),Toast.LENGTH_SHORT).show();
            return;
        }
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel(getString(R.string.waitstring))
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        final String username = name;
        final String last_name = surname;
        final String email = fbEmail;
        final String fbID = fbId;
        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.login_fb_url)
                .addBodyParameter("username", username)
                .addBodyParameter("lastname", last_name)
                .addBodyParameter("email", email)
                .addBodyParameter("fbid", fbID)
                .setTag("Android")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        // do anything with response
                        try{
                            String status = response.getString("status");
                            if(status.equals("success")){
                                JSONObject object = response.getJSONObject("data");
                                Gson gson = new Gson();
                                UserModel one = new UserModel();
                                one = gson.fromJson(object.toString(), UserModel.class);
                                MyApp.getInstance().myProfile = one;
                                sharedPreferences.saveKeyRemember(getApplicationContext(), "true");
                                sharedPreferences.saveKeyUsername(getApplicationContext(),one.getUsername());
                                sharedPreferences.saveKeyEmail(getApplicationContext(),one.getEmail());
                                sharedPreferences.saveKeyUserID(getApplicationContext(),String.valueOf(one.getId()));

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else{
                                sharedPreferences.saveKeyRemember(getApplicationContext(), "false");
                                String err = response.getString("error");
                                showToast(err);
                            }
                        } catch (JSONException ex){
                            showToast(ex.getMessage());
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        // handle error
                        showToast(error.getMessage());
                    }
                });
    }

    private void setInitData(){
        if(bSignInStatus){
            btn_submit.setText(getResources().getString(R.string.signin));
            tv_login_register.setText(getString(R.string.dont_have_account));
            ll_emailcontainer.setVisibility(View.GONE);
            tiet_username.setHint(R.string.username_or_email_hint);
            til_username.setHint(getString(R.string.username_or_email_hint));
        } else{
            btn_submit.setText(getResources().getString(R.string.register));
            tv_login_register.setText(getString(R.string.already_have_account));
            ll_emailcontainer.setVisibility(View.VISIBLE);
            tiet_username.setHint(R.string.username);
            til_username.setHint(getString(R.string.username));
        }
    }

    private boolean checkingFields(){
        if(bSignInStatus){
            if(tiet_username.getText().length() == 0){
                showToast("Enter your username or email Address");
                return false;
            }
            if(tiet_password.getText().length() == 0){
                showToast("Enter your password and try again.");
                return false;
            }
        } else{
            if(tiet_username.getText().length() == 0){
                showToast("Enter your username");
                return false;
            }
            if(tiet_password.getText().length() == 0){
                showToast("Enter your password and try again.");
                return false;
            }
            if(tiet_email.getText().length()==0){
                showToast("Enter your email Address and try again.");
                return false;
            }
        }

        return true;
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

    private void signupUserProfit(){
        //internet detect
        if(!NetworkUtils.isNetworkConnected(getApplicationContext())){
            Toast.makeText(getApplicationContext(),getString(R.string.ERROR_NETWORK),Toast.LENGTH_SHORT).show();
            return;
        }
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel(getString(R.string.waitstring))
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        final String username = tiet_username.getText().toString();
        final String password = tiet_password.getText().toString();
        final String email = tiet_email.getText().toString();
        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.signup_url)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .addBodyParameter("email", email)
                .setTag("Android")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        // do anything with response
                        try{
                            String status = response.getString("status");
                            if(status.equals("success")){
                                JSONObject object = response.getJSONObject("data");
                                Gson gson = new Gson();
                                UserModel one = new UserModel();
                                one = gson.fromJson(object.toString(), UserModel.class);
                                MyApp.getInstance().myProfile = one;
                                sharedPreferences.saveKeyRemember(getApplicationContext(), "true");
                                sharedPreferences.saveKeyUsername(getApplicationContext(),one.getUsername());
                                sharedPreferences.saveKeyEmail(getApplicationContext(),one.getEmail());
                                sharedPreferences.saveKeyUserID(getApplicationContext(),String.valueOf(one.getId()));

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else{
                                sharedPreferences.saveKeyRemember(getApplicationContext(), "false");
                                String err = response.getString("error");
                                showToast(err);
                            }
                        } catch (JSONException ex){
                            showToast(ex.getMessage());
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        // handle error
                        showToast(error.getMessage());
                    }
                });

    }

    private void loginUserProfit(){
        //internet detect
        if(!NetworkUtils.isNetworkConnected(getApplicationContext())){
            Toast.makeText(getApplicationContext(),getString(R.string.ERROR_NETWORK),Toast.LENGTH_SHORT).show();
            return;
        }
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel(getString(R.string.waitstring))
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        final String username = tiet_username.getText().toString();
        final String password = tiet_password.getText().toString();

        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.login_url)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .setTag("Android")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        // do anything with response
                        try{
                            String status = response.getString("status");
                            if(status.equals("success")){
                                JSONObject object = response.getJSONObject("data");
                                Gson gson = new Gson();
                                UserModel one = new UserModel();
                                one = gson.fromJson(object.toString(), UserModel.class);
                                MyApp.getInstance().myProfile = one;
                                sharedPreferences.saveKeyRemember(getApplicationContext(), "true");
                                sharedPreferences.saveKeyUsername(getApplicationContext(),one.getUsername());
                                sharedPreferences.saveKeyEmail(getApplicationContext(),one.getEmail());
                                sharedPreferences.saveKeyUserID(getApplicationContext(),String.valueOf(one.getId()));

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else{
                                sharedPreferences.saveKeyRemember(getApplicationContext(), "false");
                                String err = response.getString("error");
                                showToast(err);
                            }
                        } catch (JSONException ex){
                            showToast(ex.getMessage());
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        // handle error
                        showToast(error.getMessage());
                    }
                });
    }

    private void forgotPassword(){

        if(tiet_username.getText().toString().length() == 0){
            showToast("Enter your username or email Address");
            return;
        }
        //internet detect
        if(!NetworkUtils.isNetworkConnected(getApplicationContext())){
            Toast.makeText(getApplicationContext(),getString(R.string.ERROR_NETWORK),Toast.LENGTH_SHORT).show();
            return;
        }
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel(getString(R.string.waitstring))
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        final String username = tiet_username.getText().toString();

        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.forgotpassword_url)
                .addBodyParameter("username", username)
                .setTag("Android")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        // do anything with response
                        try{
                            String status = response.getString("status");
                            if(status.equals("success")){
                                showToast("Check your email to complete password reset");
                            } else{
                                String err = response.getString("error");
                                showToast(err);
                            }
                        } catch (JSONException ex){
                            showToast(ex.getMessage());
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        // handle error
                        showToast(error.getMessage());
                    }
                });
    }

    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                try {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //}
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    //code for deny
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
