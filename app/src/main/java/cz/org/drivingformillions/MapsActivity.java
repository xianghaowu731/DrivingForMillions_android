package cz.org.drivingformillions;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.org.drivingformillions.models.AddressResultModel;
import cz.org.drivingformillions.models.ComponentModel;
import cz.org.drivingformillions.models.GeoModel;
import cz.org.drivingformillions.models.ListModel;
import cz.org.drivingformillions.models.PropertyModel;
import cz.org.drivingformillions.services.Constants;
import cz.org.drivingformillions.services.SaveSharedPrefrence;
import cz.org.drivingformillions.services.UrlCollection;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private FloatingActionButton btn_mylocation, btn_maptype, btn_savelist;
    private Button btn_Continue;
    private ImageButton btn_back;
    private EditText tv_name;
    private EditText et_locationsearch;
    private ArrayList<PropertyModel> list_property = null;
    private PropertyModel saveModel;
    private ListModel newList;
    private RelativeLayout rl_mapcontainer;
    private ScrollView sl_namecontainer;
    private Button btn_property_count;
    private Button btn_remaining;
    private Geocoder geoCoder;
    private boolean bEditMode=false;
    private boolean bRealLoc = false;
    private boolean bSaved = true;
    private boolean bLongClick = true;
    private final long mDurTime = 1000;
    GPSTracker gps;

    public TourGuide mTutorialHandler;
    private SaveSharedPrefrence sharedPreferences;
    private boolean bTour = true;
    ImageButton imageButton;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);


            }
        }

        initTour();
        btn_property_count = (Button) findViewById(R.id.property_count_button);
        btn_remaining = (Button) findViewById(R.id.properties_remaining_button);
        sl_namecontainer = (ScrollView) findViewById(R.id.list_name_container);
        rl_mapcontainer = (RelativeLayout) findViewById(R.id.map_view_container);
        initToolbar();
        setSupportMapFragment();
        initActionBar();
        initLocationButton();
        initMapTypeButton();
        initSaveListButton();
        initContinueButton();
        initBackButton();
        initMenuButton();

        newList = (ListModel) getIntent().getExtras().getSerializable("ListKey");
        if (newList.plist == null) {
            bEditMode = false;
            sl_namecontainer.setVisibility(View.VISIBLE);
            rl_mapcontainer.setVisibility(View.GONE);
        } else {
            list_property = newList.plist;
            bEditMode = true;
            bTour = true;
            //this.f1954x = new ArrayList();
            //this.f1955y = new ArrayList();
            sl_namecontainer.setVisibility(View.GONE);
            rl_mapcontainer.setVisibility(View.VISIBLE);
            switchToolbar();
        }
        initLocationSearchEditText();
        showCountText();

    }

    private void initTour(){
        sharedPreferences = new SaveSharedPrefrence();
        String tempStr = sharedPreferences.getKeyTourguide(getApplicationContext());
        if(tempStr.equals("true")) bTour = true;
        else bTour = false;

        //bTour = false;
        /* setup enter and exit animation */
        Animation enterAnimation = new AlphaAnimation(0f, 1f);
        enterAnimation.setDuration(600);
        enterAnimation.setFillAfter(true);

        Animation exitAnimation = new AlphaAnimation(1f, 0f);
        exitAnimation.setDuration(600);
        exitAnimation.setFillAfter(true);

        /* initialize TourGuide without playOn() */
        mTutorialHandler = TourGuide.init(this).with(TourGuide.Technique.CLICK)
                .setPointer(new Pointer())
                .setToolTip(new ToolTip()
                        .setTitle("Location Search")
                        .setDescription("Use this text field to search for specific locations.")
                        .setGravity(Gravity.BOTTOM|Gravity.RIGHT)
                )
                .setOverlay(new Overlay()
                        .setEnterAnimation(enterAnimation)
                        .setExitAnimation(exitAnimation)
                );
    }

    private void showCountText() {
        //int i = 0;

        if(list_property == null) list_property = new ArrayList<>();
        int size = list_property.size();
        int h = 10;
        CharSequence charSequence = size + " " + getString(R.string.properties);
        CharSequence charSequence2 = h + " " + getString(R.string.remaining);
        btn_property_count.setText(charSequence);
        btn_remaining.setText(charSequence2);
    }

    private void initLocationSearchEditText(){
        et_locationsearch = (EditText) findViewById(R.id.location_search_edit_text);
        et_locationsearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });
        et_locationsearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    v.clearFocus();
                    Address a = getSimilarAddress(v.getText().toString());
                    if (a == null) {
                        Toast.makeText(MapsActivity.this, getString(R.string.unable_to_get_address_for_location), Toast.LENGTH_SHORT).show();
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(a.getLatitude(), a.getLongitude()), 18.0f));
                        v.setText(null);
                    }
                    //InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    //imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return false;
            }
        });
        /*et_locationsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.charAt(s.length() - 1) == ' ') {
                    //et_locationsearch.clearFocus();
                    Address a = getSimilarAddress(et_locationsearch.getText().toString());
                    if (a == null) {
                        Toast.makeText(MapsActivity.this, getString(R.string.unable_to_get_address_for_location), Toast.LENGTH_SHORT).show();
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(a.getLatitude(), a.getLongitude()), 18.0f));
                        et_locationsearch.setText(null);
                    }
                }
            }
        });*/

        et_locationsearch.clearFocus();
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(MyApp.getInstance().bReturn){
            finish();
        }
        gps = new GPSTracker(this);

        if(gps.canGetLocation()){
            double m_currentLatitude = gps.getLatitude();
            double m_currentLongitude = gps.getLongitude();
        }else{
            gps.showSettingsAlert();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        gps.stopUsingGPS();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //finish();
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //Execute your code here
        finish();
    }

    private Address getSimilarAddress(String str) {
        if (geoCoder == null) {
            geoCoder = new Geocoder(this);
        }
        try {
            List fromLocationName = geoCoder.getFromLocationName(str, 1);
            if (!(fromLocationName == null || fromLocationName.isEmpty())) {
                return (Address) fromLocationName.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initMenuButton(){
        imageButton = (ImageButton) findViewById(R.id.menu_button);
        //imageButton.setImageResource(R.drawable.ic_more_vert_black_24dp);
        imageButton.setVisibility(View.GONE);
    }

    private void initBackButton(){
        btn_back = (ImageButton) findViewById(R.id.back_button);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bTour){
                    mTutorialHandler.cleanUp();
                    mTutorialHandler.setToolTip(new ToolTip().setTitle("Map Type").setDescription("Use this button to change map type.").setGravity(Gravity.BOTTOM|Gravity.LEFT)).playOn(btn_maptype);
                    return;
                }
                if (list_property.size() > 0 && !bSaved) {
                    ShowConfirmDlg(getString(R.string.are_you_sure), getString(R.string.list_data_will_be_lost));
                    return;
                }
                onBackPressed();
            }
        });

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void ShowConfirmDlg(String title_str, String content_str){
        TextView tv_content,tv_title;
        Button btn_yes, btn_no;
        final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_confirm_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_content  = (TextView) dialog.findViewById(R.id.tv_content);
        tv_title = (TextView) dialog.findViewById(R.id.tv_dlg_title);
        btn_yes = (Button) dialog.findViewById(R.id.btn_yes);
        btn_no = (Button) dialog.findViewById(R.id.btn_no);

        tv_content.setText(content_str);
        tv_title.setText(title_str);
        btn_no.setText("Cancel");
        btn_yes.setText("Continue");
        dialog.show();

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
                onBackPressed();
            }
        });

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
            }
        });
    }

    private void initContinueButton(){
        btn_Continue = (Button) findViewById(R.id.list_continue_button);
        tv_name = (EditText) findViewById(R.id.list_name_edit_text);
        btn_Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = tv_name.getText().toString();
                if (name.length() == 0) {
                    Snackbar.make(v, getString(R.string.enter_list_name), 0).show();
                    return;
                }
                newList = new ListModel();
                newList.setListname(name);
                list_property = new ArrayList<PropertyModel>();
                newList.setPlist(list_property);
                refreshMap();
                showMap();
                switchToolbar();
                //===set value

            }
        });
    }
    private void refreshMap(){
        if(mMap != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.09024d, -95.712891d), 3.0f));
        }
    }

    private void switchToolbar(){
        ((Toolbar) findViewById(R.id.map_toolbar)).bringToFront();
        getSupportActionBar().hide();
        if(!bTour) {
            mTutorialHandler.playOn(btn_back);
            sharedPreferences.saveKeyTourguide(getApplicationContext(),"true");
        }
    }

    private void showMap(){
        rl_mapcontainer.setAlpha(0.0f);
        rl_mapcontainer.setVisibility(View.VISIBLE);
        rl_mapcontainer.bringToFront();
        rl_mapcontainer.animate().alpha(1.0f).setDuration(mDurTime).setListener(null);
        sl_namecontainer.animate().alpha(0.0f).setDuration(mDurTime).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //super.onAnimationEnd(animation);
                sl_namecontainer.setVisibility(View.GONE);
            }
        });
    }

    private void initLocationButton() {
        if (btn_mylocation == null) {
            btn_mylocation = (FloatingActionButton) findViewById(R.id.my_location_button);
            btn_mylocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!bTour){
                        mTutorialHandler.cleanUp();
                        mTutorialHandler.setToolTip(new ToolTip().setTitle("Save Button").setDescription("Use this button to save edited properties.").setGravity(Gravity.BOTTOM|Gravity.LEFT)).playOn(btn_savelist);
                        return;
                    }
                    changeLocationStatus();
                    Location cur_loc = gps.getLocation();
                    LatLng latLng = new LatLng(cur_loc.getLatitude(), cur_loc.getLongitude());
                    if(bRealLoc){
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                        //mMap.animateCamera(CameraUpdateFactory.zoomTo(19));
                    }
                }
            });
            btn_mylocation.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.list_color_bg)));
        }
    }

    private void changeLocationStatus(){
        bRealLoc = !bRealLoc;
        if(bRealLoc){
            btn_mylocation.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
        } else{
            btn_mylocation.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.list_color_bg)));
        }
    }

    private void initMapTypeButton() {
        if (btn_maptype == null) {
            btn_maptype = (FloatingActionButton) findViewById(R.id.map_type_button);
            btn_maptype.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if(!bTour){
                        mTutorialHandler.cleanUp();
                        mTutorialHandler.setToolTip(new ToolTip().setTitle("Location Button").setDescription("Use this button to match map center on current location.").setGravity(Gravity.BOTTOM|Gravity.LEFT)).playOn(btn_mylocation);
                        return;
                    }
                    changeMapType();
                }
            });
            btn_maptype.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.list_color_bg)));
        }
    }

    private void changeMapType() {
        if (mMap != null) {
            if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                //C1400c.f2199a.m3122b(C1399b.f2195l.m3124a());notification
                btn_maptype.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_map_black_24dp));
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return;
            }
            btn_maptype.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_satellite_black_24dp));
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    private void initSaveListButton() {
        if (btn_savelist == null) {
            btn_savelist = (FloatingActionButton) findViewById(R.id.save_list_button);
            btn_savelist.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if(!bTour){
                        mTutorialHandler.cleanUp();
                        bTour = true;
                        btn_savelist.setEnabled(false);
                        btn_savelist.setBackgroundTintList(ColorStateList.valueOf( getResources().getColor(R.color.colorTextHint)));
                        et_locationsearch.clearFocus();
                        return;
                    }

                    if(!bSaved && !bEditMode && list_property.size() > 0 ){
                        newList.setPlist(list_property);
                        newList.setState("active");
                        String currentDateandTime = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm a").format(new Date());
                        newList.setUpdateDate(currentDateandTime);
                        //============================================
                        Intent intent = new Intent(MapsActivity.this, ListDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("list", newList);
                        intent.putExtras(bundle);
                        intent.putExtra("mode", Constants.CREATE_MODE);
                        startActivity(intent);
                        btn_savelist.setEnabled(false);
                        btn_savelist.setBackgroundTintList(ColorStateList.valueOf( getResources().getColor(R.color.colorTextHint)));
                    } else if(!bSaved && bEditMode && list_property.size() > 0){
                        newList.setPlist(list_property);
                        newList.setState("active");
                        String currentDateandTime = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm a").format(new Date());
                        newList.setUpdateDate(currentDateandTime);
                        //============================================
                        Intent intent = new Intent(MapsActivity.this, ListDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("list", newList);
                        intent.putExtras(bundle);
                        intent.putExtra("mode", Constants.CREATE_MODE);
                        startActivity(intent);
                        btn_savelist.setEnabled(false);
                        btn_savelist.setBackgroundTintList(ColorStateList.valueOf( getResources().getColor(R.color.colorTextHint)));
                    }
                }
            });
        }
        if(!bEditMode){
            if (list_property == null) {
                btn_savelist.setEnabled(false);
            } else {
                btn_savelist.setEnabled(list_property.size() > 0);
            }
        } else{
            if(bSaved){
                btn_savelist.setEnabled(false);
            } else{
                btn_savelist.setEnabled(true);
            }
        }
        if(!bTour){
            btn_savelist.setEnabled(true);
        }

        btn_savelist.setBackgroundTintList(ColorStateList.valueOf(btn_savelist.isEnabled() ? getResources().getColor(R.color.colorAccentGreen) : getResources().getColor(R.color.colorTextHint)));
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.list_map_toolbar);
        toolbar.bringToFront();
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        toolbar.setTitle(getTitle());
        setSupportActionBar(toolbar);
    }

    private void setSupportMapFragment(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);
    }

    private void initActionBar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private boolean isExistSameProperty(PropertyModel one){
        if(list_property != null && list_property.size()>0){
            for(int i = 0; i < list_property.size(); i++){
                String address = list_property.get(i).address;
                String city = list_property.get(i).city;
                String zip = list_property.get(i).zip;
                if(address.equals(one.address) && city.equals(one.city) && zip.equals(one.zip)) {
                    Snackbar.make( getWindow().getDecorView(), "A pin already exist for this location", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    return true; // radious 15m
                }
            }
        }
        return false;
    }

    private void addPropertyMaker(LatLng latlng){

        String fullurl = String.format(UrlCollection.getAddressFromLocation_url,latlng.latitude,latlng.longitude);
        AndroidNetworking.get(fullurl)
                .setTag("Android")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            String status = response.getString("status");
                            if(status.equals("OK")){
                                bLongClick = false;
                                JSONArray object = response.getJSONArray("results");
                                JSONObject first_obj = object.getJSONObject(0);
                                Gson gson = new Gson();
                                AddressResultModel one = new AddressResultModel();
                                PropertyModel pro_one = new PropertyModel();
                                pro_one.address = "";
                                one.components = new ArrayList<ComponentModel>();
                                JSONArray component_array = first_obj.getJSONArray("address_components");
                                for(int i = 0; i < component_array.length();i++){
                                    JSONObject com_one = component_array.getJSONObject(i);
                                    ComponentModel com = new ComponentModel();
                                    com.longName = com_one.getString("long_name");
                                    com.shortName = com_one.getString("short_name");
                                    com.types = com_one.getString("types");
                                    one.components.add(com);
                                    if(com.types.contains("street_number")){
                                        pro_one.address = com.shortName + " " + pro_one.address;
                                    } else if(com.types.contains("route")){
                                        pro_one.address = pro_one.address + " " + com.shortName;
                                    } else if(com.types.contains("locality")){
                                        pro_one.city = com.shortName;
                                    } else if(com.types.contains("administrative_area_level_1")){
                                        pro_one.state = com.shortName;
                                    } else if(com.types.contains("postal_code\"")){
                                        pro_one.zip = com.shortName;
                                    }
                                }
                                one.formatAddress = first_obj.getString("formatted_address");
                                one.geometry = gson.fromJson(first_obj.getJSONObject("geometry").toString(), GeoModel.class);
                                one.placeId = first_obj.getString("place_id");
                                one.types = first_obj.getString("types");

                                pro_one.setName(pro_one.address);
                                pro_one.setLatitude(one.geometry.location.lat);
                                pro_one.setLongitude(one.geometry.location.lng);
                                pro_one.fav = 0;
                                String currentDateandTime = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm a").format(new Date());
                                pro_one.setUpdateDate(currentDateandTime);
                                if(!isExistSameProperty(pro_one)){
                                    saveModel = pro_one;
                                    getOwnerInfo();
                                } else{
                                    bLongClick = true;
                                }
                            }

                        } catch (JSONException ex){
                            ex.printStackTrace();
                            bLongClick = true;
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        error.printStackTrace();
                        bLongClick = true;
                    }
                });
    }
    private void getOwnerInfo(){
        PropertyModel one = saveModel;
        //String fullurl = String.format(UrlCollection.getOwnerInfo_url,one.address, one.city+","+one.state+" "+one.zip);
        String fullurl = String.format(UrlCollection.getOwnerInfo2_url,Constants.ESTATED_API_KEY, one.address, one.city, one.state,one.zip);
        AndroidNetworking.get(fullurl)
                //.addHeaders("APIKey", "d27f7ede2b3781185c5a365a96353d62")
                .addHeaders("accept", "application/json")
                .setTag("Android")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            /*JSONObject statusObj = response.getJSONObject("status");
                            int status = statusObj.getInt("code");
                            if(status == 0){
                                JSONArray object = response.getJSONArray("property");
                                JSONObject first_obj = object.getJSONObject(0);
                                JSONObject owner_obj = first_obj.getJSONObject("owner");
                                JSONObject owner1 = owner_obj.getJSONObject("owner1");
                                JSONObject owner2 = owner_obj.getJSONObject("owner2");
                                String owner_name = owner1.getString("firstnameandmi") + "," + owner1.getString("lastname");
                                list_property.get(list_property.size()-1).ownername = owner_name;
                                String second_name = owner2.getString("firstnameandmi") + "," + owner2.getString("lastname");
                                list_property.get(list_property.size()-1).secondary = second_name;
                            }*/
                            //========Estated.com==================
                            String status = response.getString("status");
                            if(status.equals("success")){
                                JSONObject data = response.getJSONObject("data");
                                JSONObject property = data.getJSONObject("property");
                                JSONArray owners = property.getJSONArray("owners");
                                JSONObject owner0 = owners.getJSONObject(0);
                                String tempstr = owner0.getString("name");
                                if(!tempstr.equals("null")){
                                    saveModel.ownername = owner0.getString("name");
                                }else{
                                    String m_name = owner0.getString("middle_name");
                                    if(m_name.equals("null")){
                                        m_name = "";
                                    }
                                    String owner_name = owner0.getString("first_name")+ " " + m_name + " " + owner0.getString("last_name");
                                    saveModel.ownername = owner_name;
                                }

                                tempstr = owner0.getString("email");
                                if(tempstr.equals("null")){
                                    saveModel.owner_email = "";
                                } else{
                                    saveModel.owner_email = tempstr;
                                }
                                tempstr = owner0.getString("phone");
                                if(tempstr.equals("null")){
                                    saveModel.owner_phone = "";
                                } else{
                                    saveModel.owner_phone = tempstr;
                                }
                                if(owners.length() > 1) {
                                    JSONObject owner1 = owners.getJSONObject(1);
                                    tempstr = owner1.getString("name");
                                    if (!tempstr.equals("null")) {
                                        saveModel.secondary = owner1.getString("name");
                                    } else {
                                        String m_name1 = owner1.getString("middle_name");
                                        if (m_name1.equals("null")) {
                                            m_name1 = "";
                                        }
                                        String second_name = owner1.getString("first_name") + " " + m_name1 + " " + owner1.getString("last_name");
                                        saveModel.secondary = second_name;
                                    }
                                }
                                //==========tax, estimated value=============

                                JSONArray taxes = property.getJSONArray("taxes");
                                if(taxes.length()>0){
                                    JSONObject tax = taxes.getJSONObject(0);
                                    int tax_value = tax.getInt("tax_year");
                                    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
                                    format.setCurrency(Currency.getInstance("USD"));
                                    saveModel.tax_year = String.valueOf(tax_value);
                                    tax_value = tax.getInt("tax_amount");
                                    String result = format.format(tax_value);
                                    saveModel.tax_amount = result;
                                    tax_value = tax.getInt("land");
                                    result = format.format(tax_value);
                                    saveModel.tax_land = result;
                                    tax_value = tax.getInt("improvement");
                                    result = format.format(tax_value);
                                    saveModel.tax_improvement = result;
                                    tax_value = tax.getInt("total");
                                    result = format.format(tax_value);
                                    saveModel.tax_total = result;
                                } else{
                                    saveModel.tax_year = "";
                                    saveModel.tax_amount = "";
                                    saveModel.tax_land = "";
                                    saveModel.tax_improvement = "";
                                    saveModel.tax_total = "";
                                }
                                JSONObject valuation = property.getJSONObject("valuation");
                                String priceStr = valuation.getString("value");
                                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
                                format.setCurrency(Currency.getInstance("USD"));
                                String result_sale = "";
                                if(!priceStr.equals("null")){
                                    result_sale = format.format(Integer.valueOf(priceStr));
                                    saveModel.estimated_value = String.valueOf(result_sale);
                                } else {
                                    saveModel.estimated_value = "";
                                }
                                priceStr = valuation.getString("low");
                                if(!priceStr.equals("null")){
                                    result_sale = format.format(Integer.valueOf(priceStr));
                                    saveModel.estimated_low = String.valueOf(result_sale);
                                } else{
                                    saveModel.estimated_low = "";
                                }
                                priceStr = valuation.getString("high");
                                if(!priceStr.equals("null")) {
                                    result_sale = format.format(Integer.valueOf(priceStr));
                                    saveModel.estimated_high = String.valueOf(result_sale);
                                } else{
                                    saveModel.estimated_high = "";
                                }
                                //============================================

                                list_property.add(saveModel);
                                bSaved = false;
                                addMarkerOnMap(saveModel);
                                bLongClick = true;
                            }

                        } catch (JSONException ex){
                            ex.printStackTrace();
                            bLongClick = true;
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        error.printStackTrace();
                        bLongClick = true;
                    }
                });
    }

    private void addMarkerOnMap(PropertyModel one){
        if(mMap != null){
            double one_lat = Double.parseDouble(one.getLatitude());
            double one_lng = Double.parseDouble(one.getLongitude());
            LatLng one_loc = new LatLng(one_lat,one_lng);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(one_loc)
                    .title(one.getName())
                    .infoWindowAnchor(1.0f, 1.0f));
            marker.setTag(list_property.size() - 1);
        }
        showCountText();
        initSaveListButton();
    }

    public void showChooseDlg(Marker marker){
        Button btn_cancel, btn_remove, btn_viewimage;
        final Marker sel_marker = marker;
        final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_choose_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btn_remove = (Button) dialog.findViewById(R.id.btn_remove);
        btn_viewimage = (Button) dialog.findViewById(R.id.btn_image);

        dialog.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
            }
        });

        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
                int mark_ind = (int)sel_marker.getTag();
                sel_marker.remove();
                list_property.remove(mark_ind);
                showCountText();
                bSaved = false;
                initSaveListButton();
            }
        });

        btn_viewimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showStreetView(sel_marker.getPosition());
            }
        });
    }

    private void showStreetView(LatLng mlatlng){
        final ImageView iv_view;
        final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_image_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        iv_view = (ImageView) dialog.findViewById(R.id.iv_imageview);
        iv_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        String imageUrl = String.format(UrlCollection.getStreetView_url, mlatlng.latitude, mlatlng.longitude);

        Picasso.with(MapsActivity.this).load(imageUrl).into(iv_view);
        dialog.show();
        /*AndroidNetworking.get(imageUrl)
                .setTag("imageRequestTag")
                .setPriority(Priority.MEDIUM)
                .setBitmapMaxHeight(300)
                .setBitmapMaxWidth(300)
                .setBitmapConfig(Bitmap.Config.ARGB_8888)
                .build()
                .getAsBitmap(new BitmapRequestListener() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        // do anything with bitmap
                        iv_view.setImageBitmap(bitmap);
                        dialog.show();
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });*/
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                showChooseDlg(marker);
            }
        });
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(bLongClick) {
                    addPropertyMaker(latLng);
                }
            }
        });

        if(bEditMode){
            addListMarkerOnMap(list_property);
        }
        btn_savelist.bringToFront();
        btn_maptype.bringToFront();
        btn_mylocation.bringToFront();
    }

    private void addListMarkerOnMap(ArrayList<PropertyModel> mList){
        if(mMap != null){
            mMap.clear();
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            for (int i = 0 ; i < mList.size(); i++){
                PropertyModel one = mList.get(i);
                double one_lat = Double.parseDouble(one.getLatitude());
                double one_lng = Double.parseDouble(one.getLongitude());
                LatLng one_loc = new LatLng(one_lat,one_lng);

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(one_loc)
                        .title(one.getName())
                        .infoWindowAnchor(1.0f, 1.0f));
                marker.setTag(list_property.size() - 1);

                if(mList.size() == 1){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(one_loc, 18.0f));
                } else{
                    b.include(marker.getPosition());
                }
            }
            if(mList.size() > 1){
                LatLngBounds bounds = b.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,300,300,50);
                mMap.moveCamera(cu);
            }
        }
        showCountText();
        initSaveListButton();
    }

    public class GPSTracker extends Service implements LocationListener {

        private final Context mContext;

        // flag for GPS status
        public boolean isGPSEnabled = false;

        // flag for network status
        public boolean isNetworkEnabled = false;

        // flag for GPS status
        boolean canGetLocation = false;

        Location location; // location
        double latitude; // latitude
        double longitude; // longitude

        // The minimum distance to change Updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 100;//1000 * 60 * 1; // 1 minute

        // Declaring a Location Manager
        protected LocationManager locationManager;

        public GPSTracker(Context context) {
            this.mContext = context;
            getLocation();
        }

        public Location getLocation() {
            if ( Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return location;
            }

            try {
                locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

                // getting GPS status
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true;
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return location;
        }

        /**
         * Stop using GPS listener
         * Calling this function will stop using GPS in your app
         * */
        public void stopUsingGPS(){
            if(locationManager != null){
                locationManager.removeUpdates(GPSTracker.this);
            }
        }

        /**
         * Function to get latitude
         * */
        public double getLatitude(){
            if(location != null){
                latitude = location.getLatitude();
            }

            // return latitude
            return latitude;
        }

        /**
         * Function to get longitude
         * */
        public double getLongitude(){
            if(location != null){
                longitude = location.getLongitude();
            }

            // return longitude
            return longitude;
        }

        /**
         * Function to check GPS/wifi enabled
         * @return boolean
         * */
        public boolean canGetLocation() {
            return this.canGetLocation;
        }

        /**
         * Function to show settings alert dialog
         * On pressing Settings button will lauch Settings Options
         * */
        public void showSettingsAlert(){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

            // Setting Dialog Title
            alertDialog.setTitle("GPS is settings");

            // Setting Dialog Message
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

            // On pressing Settings button
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(intent);
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }

        @Override
        public void onLocationChanged(Location location) {
            this.location = location;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            //mMap.addMarker(new MarkerOptions().position(latLng));
            if(bRealLoc){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
            }
            //locationManager.removeUpdates(this);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View view;

        public CustomInfoWindowAdapter() {
            view = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            if (marker != null
                    && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            LatLng my_pos = marker.getPosition();
            String url = null;
            final ImageView image = ((ImageView) view.findViewById(R.id.badge));
            String imageUrl = String.format(UrlCollection.getStreetView_url, my_pos.latitude, my_pos.longitude);

            /*AndroidNetworking.get(imageUrl)
                    .setTag("imageRequestTag")
                    .setPriority(Priority.MEDIUM)
                    .setBitmapMaxHeight(200)
                    .setBitmapMaxWidth(200)
                    .setBitmapConfig(Bitmap.Config.ARGB_8888)
                    .build()
                    .getAsBitmap(new BitmapRequestListener() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            // do anything with bitmap
                            image.setImageBitmap(bitmap);
                        }
                        @Override
                        public void onError(ANError error) {
                            // handle error
                        }
                    });*/
            Picasso.with(MapsActivity.this).load(imageUrl).into(image);

            final String title = marker.getTitle();
            final TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }
            return view;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
