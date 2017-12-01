package cz.org.drivingformillions.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import cz.org.drivingformillions.MyApp;
import cz.org.drivingformillions.R;
import cz.org.drivingformillions.services.NetworkUtils;
import cz.org.drivingformillions.services.UrlCollection;


public class ChangePassword extends Fragment {
    private EditText et_curpassword;
    private EditText et_newpassword;
    private EditText et_confirm;
    private Button btn_update;
    private View mainView;

    public ChangePassword() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview =inflater.inflate(R.layout.fragment_change_password, container, false);
        et_curpassword = (EditText) rootview.findViewById(R.id.change_current_password);
        et_newpassword = (EditText) rootview.findViewById(R.id.change_new_password);
        et_confirm = (EditText) rootview.findViewById(R.id.change_new_password_confirm);
        btn_update = (Button) rootview.findViewById(R.id.update_password);
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_curpassword.getText().length() == 0){
                    Snackbar.make(v, "Please enter your password", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    return ;
                }
                if(et_newpassword.getText().length() == 0){
                    Snackbar.make(v, "Please enter your new password", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    return;
                }
                if(et_confirm.getText().length()==0){
                    Snackbar.make(v, "Please confirm your new password.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    return ;
                }
                if(et_newpassword.getText().toString().equals(et_confirm.getText().toString())){
                    updatePassword();
                } else{
                    Snackbar.make(v, "Please confirm your new password.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    return ;
                }
            }
        });
        mainView = rootview;
        return rootview;
    }

    private void updatePassword(){
        //internet detect
        if(!NetworkUtils.isNetworkConnected(getActivity())){
            Toast.makeText(getActivity(),getString(R.string.ERROR_NETWORK),Toast.LENGTH_SHORT).show();
            return;
        }
        final KProgressHUD hud = KProgressHUD.create(getActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary))
                .setLabel(getString(R.string.waitstring))
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        final String curpass = et_curpassword.getText().toString();
        final String newpass = et_newpassword.getText().toString();
        final String email = MyApp.getInstance().myProfile.getEmail();
        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.changepass_url)
                .addBodyParameter("new_pass", newpass)
                .addBodyParameter("old_pass", curpass)
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
                                Snackbar.make(mainView, "Password was updated successfully.", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            } else{
                                Snackbar.make(mainView, "Password changing was failed.", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                        } catch (JSONException ex){
                            Snackbar.make(mainView, ex.getMessage(), Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        // handle error
                        Snackbar.make(mainView, error.getMessage(), Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    }
                });
    }

}
