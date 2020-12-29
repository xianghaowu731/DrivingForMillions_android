package cz.org.drivingformillions.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.org.drivingformillions.ListDetailActivity;
import cz.org.drivingformillions.MainActivity;
import cz.org.drivingformillions.MyApp;
import cz.org.drivingformillions.R;
import cz.org.drivingformillions.adapters.MainListAdapter;
import cz.org.drivingformillions.models.ListModel;
import cz.org.drivingformillions.models.PropertyModel;
import cz.org.drivingformillions.services.UrlCollection;


public class MyListsFragment extends Fragment{

    RecyclerView recyclerView;
    TextView tv_nolist;
    ProgressBar pb_listProgress;
    public static ArrayList<ListModel> datalist;
    public static MainListAdapter mainAdapter;
    private View rootView;
    public static ActionMode mActionMode;
    private EditText et_search;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_my_lists, container, false);
        initLayout();
        //=============test==========================
        loadData();

        return rootView;
    }

    private void initLayout(){
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        datalist = new ArrayList<>();

        mainAdapter = new MainListAdapter(datalist, getContext());
        mainAdapter.setOnItemClickListener(new MainListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int position) {

                boolean bActive = true;//((MainActivity)getActivity()).bAppActived;//
                if(bActive){
                    Intent intent = new Intent(getContext(), ListDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("list", datalist.get(position));
                    intent.putExtras(bundle);
                    startActivity(intent);
                    MyApp.getInstance().editmode = true;
                } else{
                    Snackbar.make(rootView, "You have to buy property requests on Profile page.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }

                return position;
            }
        });

        mainAdapter.setOnItemLongClickListener(new MainListAdapter.OnItemLongClickListener() {
            @Override
            public int onItemLongClick(int position) {
                //Select item on long click
                onListItemSelect(position);
                return 0;
            }
        });

        recyclerView.setAdapter(mainAdapter);

        pb_listProgress = (ProgressBar) rootView.findViewById(R.id.lists_progress_bar);
        tv_nolist = (TextView) rootView.findViewById(R.id.no_lists_placeholder);
        et_search = (EditText) ((MainActivity)getActivity()).findViewById(R.id.et_search);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mainAdapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_search.clearFocus();
        pb_listProgress.setVisibility(View.GONE);
    }

    //List item select method
    private void onListItemSelect(int position) {
        mainAdapter.selectView(position);//Toggle the selection
        boolean hasCheckedItems = mainAdapter.getSelectedCount() >= 0;//Check if any items are already selected or not
        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new Toolbar_ActionMode_Callback(getContext(), mainAdapter, datalist, false, this));
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();
        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(datalist.get(mainAdapter.getSelectedCount()).getListname() + " selected");
    }
    //Set action mode null after use
    public static void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null;
    }
    //Delete selected rows
    public void deleteRows() {
        final int selected = mainAdapter.getSelectedIds();//Get selected ids
        //If current id is selected remove the item via key
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel(getString(R.string.waitstring))
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.deletelist_url)
                .addBodyParameter("list_id", String.valueOf(datalist.get(selected).getId()))
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
                                datalist.remove(selected);
                                mainAdapter.setDataList(datalist);
                                mainAdapter.notifyDataSetChanged();
                            } else{
                                Snackbar.make(rootView, response.getString("message"), Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                        } catch (JSONException ex){
                            Snackbar.make(rootView, ex.getMessage(), Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        hud.dismiss();
                        Snackbar.make(rootView, error.getMessage(), Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    }
                });
    }

    private void initData(){

        mainAdapter.setDataList(datalist);
        mainAdapter.notifyDataSetChanged();

        pb_listProgress.setVisibility(View.GONE);
        if(datalist.size() > 0){
            recyclerView.setVisibility(View.VISIBLE);
            tv_nolist.setVisibility(View.GONE);
        } else{
            recyclerView.setVisibility(View.GONE);
            tv_nolist.setVisibility(View.VISIBLE);
        }
    }

    private void loadData(){

        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel(getString(R.string.waitstring))
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        int ind = MyApp.getInstance().myProfile.getId();
        AndroidNetworking.post(UrlCollection.server_url+UrlCollection.getLists_url)
                .addBodyParameter("uid", String.valueOf(ind))
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
                                ArrayList<ListModel> resData = new ArrayList<ListModel>();
                                JSONArray resArray = response.getJSONArray("data");
                                Gson gson = new Gson();
                                for(int i = 0; i < resArray.length(); i++){
                                    JSONObject resObj = resArray.getJSONObject(i);
                                    ListModel oneList = new ListModel();
                                    oneList.index = i;
                                    oneList.id = resObj.getInt("id");
                                    oneList.listname = resObj.getString("name");
                                    oneList.updateDate = resObj.getString("updatedate");
                                    oneList.state = resObj.getString("status");
                                    JSONArray oneArray = resObj.getJSONArray("properties");
                                    ArrayList<PropertyModel> myproArray = new ArrayList<PropertyModel>();
                                    for(int j = 0; j < oneArray.length(); j++){
                                        PropertyModel oneModel = new PropertyModel();
                                        oneModel = gson.fromJson(oneArray.getJSONObject(j).toString(), PropertyModel.class);
                                        myproArray.add(oneModel);
                                    }
                                    oneList.plist = myproArray;
                                    resData.add(oneList);
                                }
                                MyApp.getInstance().mylist = resData;
                                datalist = resData;
                                initData();
                            } else{
                                Snackbar.make(rootView, response.getString("message"), Snackbar.LENGTH_SHORT)
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

    public class Toolbar_ActionMode_Callback implements ActionMode.Callback {
        private Context context;
        private MainListAdapter recyclerView_adapter;
        private List<ListModel> message_models;
        private boolean isListViewFragment;
        private MyListsFragment myFrag;
        public Toolbar_ActionMode_Callback(Context context, MainListAdapter recyclerView_adapter, List<ListModel> message_models, boolean isListViewFragment, MyListsFragment mFrag) {
            this.context = context;
            this.recyclerView_adapter = recyclerView_adapter;
            this.message_models = message_models;
            this.isListViewFragment = isListViewFragment;
            this.myFrag = mFrag;
        }
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.recycle_menu, menu);         //Inflate the menu over action mode
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
            //So here show action menu according to SDK Levels
            if (Build.VERSION.SDK_INT < 11) {
                MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_delete), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            } else {
                menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            return true;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    myFrag.deleteRows();       //delete selected rows
                    break;

            }
            return false;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //When action mode destroyed remove selected selections and set action mode to null
            //First check current fragment action mode
            recyclerView_adapter.removeSelection();         // remove selection
            MyListsFragment.setNullToActionMode();       //Set action mode null
        }
    }
}
