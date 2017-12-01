package cz.org.drivingformillions;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import cz.org.drivingformillions.fragments.AboutFragment;
import cz.org.drivingformillions.fragments.ChangePassword;
import cz.org.drivingformillions.fragments.FavoriteFragment;
import cz.org.drivingformillions.fragments.MyListsFragment;
import cz.org.drivingformillions.fragments.ProfileFragment;
import cz.org.drivingformillions.models.ListModel;
import cz.org.drivingformillions.services.SaveSharedPrefrence;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BillingProcessor.IBillingHandler{

    private static final String LOG_TAG = "D4M";

    // PRODUCT & SUBSCRIPTION IDS
    //private static final String PRODUCT_ID = "com.anjlab.test.iab.s2.p5";
    private static final String SUBSCRIPTION_ID = "cz.org.drivingformillions.iab.subs1";
    private static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnydIqHDo88drGZ9PFZx3cFfg2BLsHyXzFvacV3LUW812G2mTEDHb2OtTCgrLYBLHPzBG/yT9B15OcGzGe+G0Ce0YTyhowKNPP1wPUMH9ZKu7IuGRahFGop4KAi+oYivHyGKCHTnubL6ZeoUbKs4kM/hz229ku+xCAIv7YPU5k5q2jbp+JJIKi3Bems++9v49Yso19dnOYqXVUvOVqeun4NxmcpnNTXlET+9UMGCEXS8rDNpaAr6rrs6VgmWHG5j3Zy+1DHfgcDWxc72zSaSTXSWFcUDN8DnoV6f8AoqbHKJxop94nz3p8NuEzVKxDJkuItpRkLba2eayFxlJE45EhwIDAQAB"; // PUT YOUR MERCHANT KEY HERE;
    // put your Google merchant id here (as stated in public profile of your Payments Merchant Center)
    // if filled library will provide protection against Freedom alike Play Market simulators
    //private static final String MERCHANT_ID=null;

    private BillingProcessor bp;
    private boolean readyToPurchase = false;
    public boolean bAppActived = false;

    Toolbar toolbar;
    public TextView nav_tv_email, nav_tv_request, nav_tv_username;
    public EditText et_search;
    private Menu my_menu;
    private FloatingActionButton fab;
    private SaveSharedPrefrence sharedPreferences;
    private MenuItem refresh_item,search_item;
    private boolean bRefresh = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bAppActived = true;
                if(bAppActived){
                    ListModel curList = new ListModel();
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("ListKey", curList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    MyApp.getInstance().editmode = false;
                } else{
                    showToast("You have to buy property requests on Profile page.");
                    //switchProfilePage();
                }

            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
        nav_tv_email = (TextView) header.findViewById(R.id.nav_tv_useremail);
        nav_tv_request = (TextView) header.findViewById(R.id.nav_tv_request);
        nav_tv_username = (TextView) header.findViewById(R.id.nav_tv_username);
        et_search = (EditText) findViewById(R.id.et_search);

        sharedPreferences = new SaveSharedPrefrence();

        bp = new BillingProcessor(this, LICENSE_KEY, this);//bp = new BillingProcessor(this, LICENSE_KEY, MERCHANT_ID, this);
        try {
            if(bp.loadOwnedPurchasesFromGoogle()) {
                if (bp.isSubscribed(SUBSCRIPTION_ID)) {
                    bAppActived = true;
                } else {
                    bAppActived =false;
                }
            } else {
                bAppActived = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        initData();//test
    }

    private void switchProfilePage(){
        fab.setVisibility(View.INVISIBLE);
        et_search.setVisibility(View.GONE);
        toolbar.setTitle("My Profile");
        refresh_item.setVisible(false);
        search_item.setVisible(false);
        ProfileFragment profile_frag = new ProfileFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, profile_frag);
        fragmentTransaction.commit();
    }

    private void initData(){
        MyApp.getInstance().mylist = new ArrayList<>();
    }

    public void buySubscribe(){
        if (!readyToPurchase) {
            Snackbar.make(getWindow().getDecorView(),"Billing not initialized.", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        bp.subscribe(this,SUBSCRIPTION_ID);
        bRefresh = true;
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        if (bp.isSubscribed(SUBSCRIPTION_ID)) {
            bAppActived = true;
        }
    }
    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        showToast("onBillingError: " + Integer.toString(errorCode));
    }
    @Override
    public void onBillingInitialized() {
        //showToast("onBillingInitialized");
        readyToPurchase = true;
    }
    @Override
    public void onPurchaseHistoryRestored() {
        //showToast("onPurchaseHistoryRestored");
        for(String sku : bp.listOwnedProducts())
            Log.d(LOG_TAG, "Owned Managed Product: " + sku);
        for(String sku : bp.listOwnedSubscriptions())
            Log.d(LOG_TAG, "Owned Subscription: " + sku);
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nav_tv_email.setText(MyApp.getInstance().myProfile.getEmail());
        nav_tv_request.setText("");
        String first = MyApp.getInstance().myProfile.getFirstname();
        String last = MyApp.getInstance().myProfile.getLastname();
        if(first != null && last != null){
            nav_tv_username.setText(first + " " + last);
        }else{
            nav_tv_username.setText(MyApp.getInstance().myProfile.getUsername());
        }
        MyApp.getInstance().bReturn = false;
        if(!bRefresh) showListsFragment();
        bRefresh = false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void showListsFragment(){
        toolbar.setTitle("My Lists");
        fab.setVisibility(View.VISIBLE);
        et_search.setVisibility(View.GONE);
        MyListsFragment list_frag = new MyListsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, list_frag);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_lists, menu);
        my_menu = menu;
        refresh_item = my_menu.findItem(R.id.action_refresh);
        search_item = my_menu.findItem(R.id.action_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            showListsFragment();
            return true;
        } else if(id == R.id.action_search){
            et_search.setText("");
            et_search.setVisibility(View.VISIBLE);
            et_search.requestFocus();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        fab.setVisibility(View.INVISIBLE);
        et_search.setVisibility(View.GONE);
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_my_lists) {
            toolbar.setTitle("My Lists");
            fab.setVisibility(View.VISIBLE);
            refresh_item.setVisible(true);
            search_item.setVisible(true);
            MyListsFragment list_frag = new MyListsFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, list_frag);
            fragmentTransaction.commit();
        } else if (id == R.id.menu_favorites) {
            toolbar.setTitle("Favorites");
            refresh_item.setVisible(true);
            search_item.setVisible(false);
            FavoriteFragment fav_frag = new FavoriteFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fav_frag);
            fragmentTransaction.commit();

        } else if (id == R.id.menu_my_profile) {
            toolbar.setTitle("My Profile");
            refresh_item.setVisible(false);
            search_item.setVisible(false);
            ProfileFragment profile_frag = new ProfileFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, profile_frag);
            fragmentTransaction.commit();
        } else if (id == R.id.menu_change_password) {
            toolbar.setTitle("Change Password");
            refresh_item.setVisible(false);
            search_item.setVisible(false);
            ChangePassword pass_frag = new ChangePassword();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, pass_frag);
            fragmentTransaction.commit();
        } else if (id == R.id.menu_sign_out) {
            signOut();
        } else if (id == R.id.menu_about) {
            toolbar.setTitle("About");
            refresh_item.setVisible(false);
            search_item.setVisible(false);
            AboutFragment about_frag = new AboutFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, about_frag);
            fragmentTransaction.commit();
        } else if (id == R.id.menu_contact) {
            showToast("Email us at Success@DrivingForMillions.com");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void showToast(String txt){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.mytoast,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(txt);
        text.setTextColor(getResources().getColor(R.color.colorWhite));

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM|Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void signOut(){
        sharedPreferences.saveKeyRemember(getApplicationContext(), "false");
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
