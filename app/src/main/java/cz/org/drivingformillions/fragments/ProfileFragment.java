package cz.org.drivingformillions.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import cz.org.drivingformillions.MainActivity;
import cz.org.drivingformillions.MyApp;
import cz.org.drivingformillions.R;
import cz.org.drivingformillions.services.NetworkUtils;
import cz.org.drivingformillions.services.UrlCollection;


public class ProfileFragment extends Fragment{


    private TextInputEditText tiet_firstname, tiet_lastname, tiet_email;
    private Button btn_update, btn_buyplan;
    private TextView tv_saved, tv_remaining;
    private View rootView;

    public ProfileFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        tiet_email = (TextInputEditText) rootView.findViewById(R.id.profile_email_edit_text);
        tiet_firstname = (TextInputEditText) rootView.findViewById(R.id.profile_first_name_edit_text);
        tiet_lastname = (TextInputEditText) rootView.findViewById(R.id.profile_last_name_edit_text);
        btn_update = (Button) rootView.findViewById(R.id.update_profile_button);
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tiet_firstname.getText().length() == 0){
                    Snackbar.make(v, "Please enter your firstname.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    return ;
                }
                if(tiet_lastname.getText().length() == 0){
                    Snackbar.make(v, "Please enter your lastname.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    return;
                }
                if(tiet_email.getText().length()==0){
                    Snackbar.make(v, "Please enter your email.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    return ;
                }
                updateProfile();
            }
        });
        btn_buyplan = (Button) rootView.findViewById(R.id.open_store_button);
        btn_buyplan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).buySubscribe();
            }
        });
        tv_saved = (TextView) rootView.findViewById(R.id.properties_saved);
        tv_remaining = (TextView) rootView.findViewById(R.id.properties_remaining);
        loadData();

        if(!BillingProcessor.isIabServiceAvailable(getContext())) {
            Snackbar.make(rootView, "In-app billing service is unavailable, please upgrade Android Market/Play to version >= 3.9.16", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
        return rootView;
    }

    private void loadData(){
        String email = MyApp.getInstance().myProfile.getEmail();
        if(email != null) tiet_email.setText(email);
        String firstname = MyApp.getInstance().myProfile.getFirstname();
        if(firstname != null) tiet_firstname.setText(firstname);
        String lastname = MyApp.getInstance().myProfile.getLastname();
        if(lastname != null) tiet_lastname.setText(lastname);

        tv_remaining.setText("5");

        //==navigation setting========================
        ((MainActivity)getActivity()).nav_tv_email.setText(MyApp.getInstance().myProfile.getEmail());
        ((MainActivity)getActivity()).nav_tv_request.setText("Requests Remaining: 5");
        if(firstname != null && lastname != null){
            ((MainActivity)getActivity()).nav_tv_username.setText(firstname + " " + lastname);
        }else{
            ((MainActivity)getActivity()).nav_tv_username.setText(MyApp.getInstance().myProfile.getUsername());
        }

        if(((MainActivity)getActivity()).bAppActived){
            btn_buyplan.setEnabled(false);
            tv_saved.setText("Enabled");
        } else {
            btn_buyplan.setEnabled(true);
            tv_saved.setText("Expired");
        }
    }

    private void updateProfile(){
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

        final String first = tiet_firstname.getText().toString();
        final String last = tiet_lastname.getText().toString();
        final String email = tiet_email.getText().toString();
        String uid = String.valueOf(MyApp.getInstance().myProfile.getId());
        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.updateprofile_url)
                .addBodyParameter("firstname", first)
                .addBodyParameter("lastname", last)
                .addBodyParameter("email", email)
                .addBodyParameter("uid", uid)
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
                                MyApp.getInstance().myProfile.setFirstname(first);
                                MyApp.getInstance().myProfile.setLastname(last);
                                MyApp.getInstance().myProfile.setEmail(email);
                                Snackbar.make(rootView, "Profile was updated successfully.", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                                loadData();
                            } else{
                                Snackbar.make(rootView, "Profile update was failed.", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                        } catch (JSONException ex){
                            Snackbar.make(rootView, ex.getMessage(), Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        // handle error
                        Snackbar.make(rootView, error.getMessage(), Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    }
                });
    }

}
