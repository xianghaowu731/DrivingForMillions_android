package cz.org.drivingformillions;

import android.*;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.baoyz.actionsheet.ActionSheet;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.org.drivingformillions.adapters.ListDetailAdapter;
import cz.org.drivingformillions.models.ListModel;
import cz.org.drivingformillions.models.PropertyModel;
import cz.org.drivingformillions.services.Constants;
import cz.org.drivingformillions.services.NetworkUtils;
import cz.org.drivingformillions.services.UrlCollection;

public class ListDetailActivity extends AppCompatActivity implements OnMapReadyCallback ,
        ActionSheet.ActionSheetListener{

    public static final int FILE_PERMISSIONS_REQUEST= 1024;

    private GoogleMap mMap;
    private ArrayList<PropertyModel> dmList;
    private ListModel curList;
    private ProgressBar mProgressBar;
    private FloatingActionButton edit_fab_btn, save_fab_btn, email_csv_btn;
    RecyclerView recyclerView;
    ListDetailAdapter mainAdapter;
    private int mode = 1;// editmode = 1, add/create = 2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_detail);

        curList = (ListModel) getIntent().getExtras().getSerializable("list");
        dmList = curList.plist;
        String title = curList.listname;
        mode = getIntent().getIntExtra("mode", Constants.EDIT_MODE);
        mProgressBar = (ProgressBar) findViewById(R.id.loading_properties_progress_bar);
        initActionButtons();
        setSupportMapFragment();

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!= null){
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initLayout();
        setFloatingActionButton();

    }

    protected void onResume(){
        super.onResume();

        if(MyApp.getInstance().bReturn){
            finish();
        }

        mainAdapter.setDataList(dmList);
        mainAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionButtons(){
        edit_fab_btn = (FloatingActionButton) findViewById(R.id.edit_list_fab);
        save_fab_btn = (FloatingActionButton) findViewById(R.id.save_list_fab);
        email_csv_btn = (FloatingActionButton) findViewById(R.id.email_csv_fab);

        edit_fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListDetailActivity.this, MapsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("ListKey", curList);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        save_fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveList();
            }
        });

        email_csv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActionSheet();
            }
        });

    }

    public void showActionSheet() {
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("Cancel")
                .setOtherButtonTitles("Email")//"Open Letter Marketing", "DropBox", "Email"
                .setCancelableOnTouchOutside(true).setListener(this).show();
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        checkPermission(this);
        switch (index){
            //case 0://letter marketing
             //   break;
            case 0://email

                File sendFile = makeCSVFile();
                if(sendFile == null){
                    showToast("Can't create CSV file.");
                    return;
                }
                //sendFile.setReadable(true,false);
                Uri path = Uri.fromFile(sendFile);
                Intent emailIntent = new Intent(Intent.ACTION_SEND);// set the type to 'email'
                emailIntent.setType("text/html");//"vnd.android.cursor.dir/email","plain/text"
                //emailIntent.setType("message/rfc822") ;
                String to[] = {MyApp.getInstance().myProfile.getEmail()};
                emailIntent.putExtra(Intent.EXTRA_EMAIL, to);// the attachment
                emailIntent.putExtra(Intent.EXTRA_STREAM, path);// the mail subject
                // Grant temporary read permission to the content URI
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                startActivity(Intent.createChooser(emailIntent , "Send email..."));
                /*Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Your subject");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Your message");

                ArrayList<Uri> uris = new ArrayList<Uri>();
                //String shareName = new String(pathToFile + filename);
                //File shareFile = new File(curList.listname + ".csv");
                Uri contentUri = FileProvider.getUriForFile(this, "cz.org.drivingformillions.fileprovider", sendFile);
                uris.add(contentUri);
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

                // Grant temporary read permission to the content URI
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                String msgStr = "Share...";
                startActivity(Intent.createChooser(shareIntent, msgStr));*/
                break;
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {

    }

    private void saveList(){
        if(!MyApp.getInstance().editmode){//if(mode == Constants.CREATE_MODE){
            addListToDB();
        } else {//if(mode == Constants.EDIT_MODE){
            updateList();
        }
    }

    private void updateList(){
        try{
            JSONObject saveObj = makeListToJsonObject();
            if(saveObj != null){
                updateData(saveObj);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void updateData(JSONObject saveData){
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

        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.updatelist_url)
                .addBodyParameter("list_id", String.valueOf(curList.id))
                .addBodyParameter("list", saveData.toString())
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
                                MyApp.getInstance().bReturn = true;
                                finish();
                            } else{
                                showToast(response.getString("message"));
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

    private void addListToDB(){
        //===createJSONObject=========================================
        try{
            JSONObject saveObj = makeListToJsonObject();
            if(saveObj != null){
                saveNewList(saveObj);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void saveNewList(JSONObject saveData){
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

        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.createlist_url)
                .addBodyParameter("uid", String.valueOf(MyApp.getInstance().myProfile.getId()))
                .addBodyParameter("list", saveData.toString())
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
                                MyApp.getInstance().bReturn = true;
                                finish();
                            } else{
                                showToast(response.getString("message"));
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

    public JSONObject makeListToJsonObject()throws JSONException {
        JSONObject obj = null;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < dmList.size(); i++) {
            obj = new JSONObject();
            try {
                obj.put("name", dmList.get(i).name);
                obj.put("address", dmList.get(i).address);
                obj.put("city", dmList.get(i).city);
                obj.put("country", dmList.get(i).state);
                obj.put("postal", dmList.get(i).zip);
                if(dmList.get(i).ownername == null) dmList.get(i).ownername = "";
                obj.put("owner_name", dmList.get(i).ownername);
                if(dmList.get(i).secondary == null) dmList.get(i).secondary = "";
                obj.put("secondary", dmList.get(i).secondary);
                if(dmList.get(i).owner_email == null) dmList.get(i).owner_email = "";
                obj.put("email", dmList.get(i).owner_email);
                if(dmList.get(i).owner_phone == null) dmList.get(i).owner_phone = "";
                obj.put("phone", dmList.get(i).owner_phone);
                obj.put("update", dmList.get(i).updateDate);
                obj.put("lat", dmList.get(i).latitude);
                obj.put("lng", dmList.get(i).longitude);
                obj.put("fav", dmList.get(i).fav);
                obj.put("tax_year",dmList.get(i).tax_year);
                obj.put("tax_amount",dmList.get(i).tax_amount);
                obj.put("tax_land",dmList.get(i).tax_land);
                obj.put("tax_improvement",dmList.get(i).tax_improvement);
                obj.put("tax_total",dmList.get(i).tax_total);
                obj.put("estimated_value", dmList.get(i).estimated_value);
                obj.put("estimated_low", dmList.get(i).estimated_low);
                obj.put("estimated_high", dmList.get(i).estimated_high);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            jsonArray.put(obj);
        }

        JSONObject finalobject = new JSONObject();
        finalobject.put("properties", jsonArray);
        finalobject.put("name", curList.listname);
        finalobject.put("updatedate",curList.updateDate);
        return finalobject;
    }

    private void setFloatingActionButton(){
        switch (mode){
            case Constants.EDIT_MODE://editmode
                edit_fab_btn.setVisibility(View.VISIBLE);
                email_csv_btn.setVisibility(View.VISIBLE);
                save_fab_btn.setVisibility(View.GONE);
                edit_fab_btn.bringToFront();
                email_csv_btn.bringToFront();
                break;
            case Constants.CREATE_MODE://addmode
                edit_fab_btn.setVisibility(View.GONE);
                email_csv_btn.setVisibility(View.GONE);
                save_fab_btn.setVisibility(View.VISIBLE);
                save_fab_btn.bringToFront();
                break;
        }
    }

    private void initLayout(){
        recyclerView = (RecyclerView) findViewById(R.id.details_property_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(dmList == null) {
            finish();
        }
        mainAdapter = new ListDetailAdapter(dmList, this);
        mainAdapter.setOnItemClickListener(new ListDetailAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int position) {
                Intent intent = new Intent(ListDetailActivity.this, PropertyDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("property", dmList.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
                return position;
            }
        });

        recyclerView.setAdapter(mainAdapter);
    }

    private void setSupportMapFragment(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_map);
        mapFragment.getMapAsync(this);
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

        if(dmList.size() == 1){
            double one_lat = Double.parseDouble(dmList.get(0).getLatitude());
            double one_lng = Double.parseDouble(dmList.get(0).getLongitude());
            LatLng one_loc = new LatLng(one_lat,one_lng);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(one_loc)
                    .title(dmList.get(0).getName())
                    .infoWindowAnchor(1.0f, 1.0f));
            marker.setTag(0);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(one_loc, 19.0f));
        }

        if(dmList.size() > 1)
            addMarkersOnMap();
    }

    private void addMarkersOnMap(){
        if(mMap != null){
            //mMap.clear();
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            for(int i = 0; i < dmList.size();i++){
                double one_lat = Double.parseDouble(dmList.get(i).getLatitude());
                double one_lng = Double.parseDouble(dmList.get(i).getLongitude());
                LatLng one_loc = new LatLng(one_lat,one_lng);

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(one_loc)
                        .title(dmList.get(i).getName())
                        .infoWindowAnchor(1.0f, 1.0f));
                marker.setTag(i);

                b.include(marker.getPosition());
            }
            LatLngBounds bounds = b.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,300,300,50);
            mMap.moveCamera(cu);
        }
    }

    public void showChooseDlg(Marker marker){
        Button btn_cancel, btn_detail, btn_viewimage;
        final Marker sel_marker = marker;
        final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_choose_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        btn_viewimage = (Button) dialog.findViewById(R.id.btn_cancel);
        btn_cancel = (Button) dialog.findViewById(R.id.btn_remove);
        btn_detail = (Button) dialog.findViewById(R.id.btn_image);

        btn_viewimage.setText("Street View Image");
        btn_cancel.setText("Cancel");
        btn_detail.setText("Property Detail");

        dialog.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
            }
        });

        btn_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
                int mark_ind = (int)sel_marker.getTag();
                Intent intent = new Intent(ListDetailActivity.this, PropertyDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("property", dmList.get(mark_ind));
                intent.putExtras(bundle);
                startActivity(intent);
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

        Picasso.with(ListDetailActivity.this).load(imageUrl).into(iv_view);
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
            Picasso.with(ListDetailActivity.this).load(imageUrl).into(image);

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

    public File makeCSVFile() {
        try {
            if (curList == null) {
                return null;
            }
            File file = new File(Environment.getExternalStorageDirectory(), "/D4M");
            //File file = new File(getFilesDir(), "/D4M");//getCacheDir()
            //File file = new File(getCacheDir(), "/D4M");
            if (!file.exists() && !file.mkdir()) {
                return null;
            }
            File file2 = new File(file, "/" + curList.listname + ".csv");
            if (!file2.exists() && !file2.createNewFile()) {
                return null;
            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file2));
            outputStreamWriter.write("\"Property Address\",\"City\",\" State\",\"Zip\",\"Primary Owner Name\",\"Secondary Owner Name\",\"Mailing Address\",\"Mailing City\",\"Mailing State\",\"Mailing Zip\",\"Phone\",\"Email\"\n");
            String str = "";
            ArrayList<PropertyModel> mList = curList.plist;
            for (PropertyModel one: mList) {
                String b;
                String d;
                String str3 = str + one.address + "," + one.city + "," + one.state + "," + one.zip + ",";
                if(one.ownername.length() > 3){
                    d = str3 + one.ownername + ",";
                } else{
                    d = str3 + "n/a,";
                }

                if(one.secondary.length() > 2){
                    b = d + one.secondary + ",";
                } else{
                    b = d + "n/a,n/a,";
                }

                b = b + one.address + "," + one.city + "," + one.state + "," + one.zip + ","+ one.owner_phone + ","+ one.owner_email + ",";
                str = b + "\n";
            }
            outputStreamWriter.write(str);
            outputStreamWriter.close();
            return file2;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                try {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, FILE_PERMISSIONS_REQUEST);
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
            case FILE_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    //code for deny
                }
                break;
        }
    }
}
