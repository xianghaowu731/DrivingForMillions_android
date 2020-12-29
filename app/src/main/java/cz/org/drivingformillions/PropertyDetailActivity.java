package cz.org.drivingformillions;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import cz.org.drivingformillions.models.PropertyModel;
import cz.org.drivingformillions.services.Constants;
import cz.org.drivingformillions.services.NetworkUtils;
import cz.org.drivingformillions.services.UrlCollection;

public class PropertyDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PropertyModel mProperty;
    private FloatingActionButton btn_fav;
    private TextView tv_address, tv_city, tv_state, tv_zip, tv_ownername, tv_update, tv_secondary;
    private TextView tv_oaddress, tv_ocity, tv_ostate, tv_ozip, tv_oemail, tv_ophone;
    private TextView tv_tax_year, tv_tax_taxes, tv_tax_land, tv_tax_improve, tv_tax_total;
    private TextView tv_estimated, tv_low, tv_high;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail);
        mProperty = (PropertyModel) getIntent().getExtras().getSerializable("property");
        String title = mProperty.address;//getIntent().getStringExtra("title");
        Toolbar toolbar = (Toolbar) findViewById(R.id.property_detail_toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!= null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setSupportMapFragment();
        initFavoriteButton();
        initFields();
        //loadTaxInfo();
    }

    private void initFields(){
        tv_address = (TextView) findViewById(R.id.property_address);
        tv_city = (TextView) findViewById(R.id.property_city);
        tv_state = (TextView) findViewById(R.id.property_state);
        tv_zip = (TextView) findViewById(R.id.property_zip);
        tv_ownername = (TextView) findViewById(R.id.owner_name);
        tv_oaddress = (TextView) findViewById(R.id.owner_address);
        tv_ocity = (TextView) findViewById(R.id.owner_city);
        tv_ostate = (TextView) findViewById(R.id.owner_state);
        tv_ozip = (TextView) findViewById(R.id.owner_zip);
        tv_secondary = (TextView) findViewById(R.id.secondary_owner_name);
        tv_update = (TextView) findViewById(R.id.data_updated);
        tv_oemail = (TextView) findViewById(R.id.owner_email);
        tv_ophone = (TextView) findViewById(R.id.owner_phone);
        tv_tax_year = (TextView) findViewById(R.id.tv_detail_tax_year);
        tv_tax_taxes = (TextView) findViewById(R.id.tv_detail_tax_taxes);
        tv_tax_land = (TextView) findViewById(R.id.tv_detail_tax_Land);
        tv_tax_improve = (TextView) findViewById(R.id.tv_detail_tax_improvements);
        tv_tax_total = (TextView) findViewById(R.id.tv_detail_tax_total);
        tv_estimated = (TextView) findViewById(R.id.tv_detail_estimated_price);
        tv_low = (TextView) findViewById(R.id.tv_detail_value_low);
        tv_high = (TextView) findViewById(R.id.tv_detail_value_high);

        tv_address.setText(mProperty.address);
        tv_city.setText(mProperty.city);
        tv_state.setText(mProperty.state);
        tv_zip.setText(mProperty.zip);
        if(mProperty.ownername != null && mProperty.ownername.length()>0){
            tv_ownername.setText(mProperty.ownername);
            tv_oaddress.setText(mProperty.address);
            tv_ocity.setText(mProperty.city);
            tv_ostate.setText(mProperty.state);
            tv_ozip.setText(mProperty.zip);
            tv_ophone.setText(mProperty.owner_phone);
            tv_oemail.setText(mProperty.owner_email);
        } else{
            tv_ownername.setText("");
            tv_oaddress.setText("");
            tv_ocity.setText("");
            tv_ostate.setText("");
            tv_ozip.setText("");
            tv_ophone.setText("");
            tv_oemail.setText("");
        }
        tv_secondary.setText(mProperty.secondary);
        tv_update.setText(mProperty.updateDate);

        tv_tax_year.setText(mProperty.tax_year);
        tv_tax_taxes.setText(mProperty.tax_amount);
        tv_tax_land.setText(mProperty.tax_land);
        tv_tax_improve.setText(mProperty.tax_improvement);
        tv_tax_total.setText(mProperty.tax_total);

        tv_estimated.setText(mProperty.estimated_value);
        tv_low.setText(mProperty.estimated_low);
        tv_high.setText(mProperty.estimated_high);
    }

    private void loadTaxInfo(){
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel(getString(R.string.waitstring))
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        String fullurl = String.format(UrlCollection.getOwnerInfo2_url, Constants.ESTATED_API_KEY, mProperty.address, mProperty.city, mProperty.state,mProperty.zip);
        AndroidNetworking.get(fullurl)
                .addHeaders("accept", "application/json")
                .setTag("Android")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try{
                            //========Estated.com==================
                            String status = response.getString("status");
                            if(status.equals("success")){
                                JSONObject data = response.getJSONObject("data");
                                JSONObject property = data.getJSONObject("property");
                                JSONArray taxes = property.getJSONArray("taxes");
                                if(taxes.length()>0){
                                    JSONObject tax = taxes.getJSONObject(0);
                                    String valueStr = tax.getString("tax_year");
                                    tv_tax_year.setText(valueStr);
                                    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
                                    format.setCurrency(Currency.getInstance("USD"));
                                    valueStr = tax.getString("tax_amount");
                                    String result = format.format(Integer.valueOf(valueStr));
                                    tv_tax_taxes.setText(result);
                                    valueStr = tax.getString("land");
                                    result = format.format(Integer.valueOf(valueStr));
                                    tv_tax_land.setText(result);
                                    valueStr = tax.getString("improvement");
                                    result = format.format(Integer.valueOf(valueStr));
                                    tv_tax_improve.setText(result);
                                    valueStr = tax.getString("total");
                                    result = format.format(Integer.valueOf(valueStr));
                                    tv_tax_total.setText(result);
                                }
                                JSONObject valuation = property.getJSONObject("valuation");
                                String priceStr = valuation.getString("value");
                                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
                                format.setCurrency(Currency.getInstance("USD"));
                                String result_sale = "";
                                if(!priceStr.equals("null")){
                                    result_sale = format.format(Integer.valueOf(priceStr));
                                    tv_estimated.setText(result_sale);
                                }
                                priceStr = valuation.getString("low");
                                if(!priceStr.equals("null")){
                                    result_sale = format.format(Integer.valueOf(priceStr));
                                    tv_low.setText(result_sale);
                                }
                                priceStr = valuation.getString("high");
                                if(!priceStr.equals("null")) {
                                    result_sale = format.format(Integer.valueOf(priceStr));
                                    tv_high.setText(result_sale);
                                }

                            }

                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        error.printStackTrace();
                    }
                });
    }

    private void initFavoriteButton(){
        if(btn_fav == null)
            btn_fav = (FloatingActionButton)findViewById(R.id.fab_detail_btn);
        if(mProperty.fav == 1){
            btn_fav.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star_white_24dp));
        } else{
            btn_fav.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star_border_white_24dp));
        }
        btn_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavorite();
            }
        });
    }

    private void setFavorite(){
        if(!NetworkUtils.isNetworkConnected(getApplicationContext())){
            Toast.makeText(getApplicationContext(),getString(R.string.ERROR_NETWORK),Toast.LENGTH_SHORT).show();
            return;
        }
        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.setFavorite_url)
                .addBodyParameter("pro_id", String.valueOf(mProperty.id))
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
                                if(mProperty.fav == 1){
                                    mProperty.fav = 0;
                                } else{
                                    mProperty.fav = 1;
                                }
                                initFavoriteButton();
                            } else{
                                String err = response.getString("message");
                                showToast(err);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        error.printStackTrace();
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

    private void setSupportMapFragment(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.property_map);
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

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Add a marker in Sydney and move the camera
        double one_lat = Double.parseDouble(mProperty.getLatitude());
        double one_lng = Double.parseDouble(mProperty.getLongitude());
        LatLng one_loc = new LatLng(one_lat,one_lng);

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(one_loc)
                .title(mProperty.getName())
                .infoWindowAnchor(1.0f, 1.0f));
        marker.setTag(0);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(one_loc, 19.0f));
    }
}
