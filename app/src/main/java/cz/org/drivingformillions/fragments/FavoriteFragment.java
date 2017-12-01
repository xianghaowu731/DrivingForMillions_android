package cz.org.drivingformillions.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import cz.org.drivingformillions.LoginActivity;
import cz.org.drivingformillions.MainActivity;
import cz.org.drivingformillions.MyApp;
import cz.org.drivingformillions.PropertyDetailActivity;
import cz.org.drivingformillions.R;
import cz.org.drivingformillions.adapters.FavoriteListAdapter;
import cz.org.drivingformillions.adapters.MainListAdapter;
import cz.org.drivingformillions.models.ListModel;
import cz.org.drivingformillions.models.PropertyModel;
import cz.org.drivingformillions.models.UserModel;
import cz.org.drivingformillions.services.NetworkUtils;
import cz.org.drivingformillions.services.UrlCollection;


public class FavoriteFragment extends Fragment {
    public static ArrayList<PropertyModel> datalist;
    private View rootview;
    private FavoriteListAdapter favAdapter;
    RecyclerView recyclerView;
    TextView tv_nofav;
    ProgressBar pb_listProgress;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    public static FavoriteFragment newInstance(String param1, String param2) {
        FavoriteFragment fragment = new FavoriteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_favorite, container, false);

        initLayout();
        //=============test==========================
        initData();
        return rootview;
    }

    private void initLayout(){
        recyclerView = (RecyclerView) rootview.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        datalist = new ArrayList<>();

        favAdapter = new FavoriteListAdapter(datalist, getContext());
        favAdapter.setOnItemClickListener(new FavoriteListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int position) {
                Intent intent = new Intent(getActivity(), PropertyDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("property", datalist.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
                return position;
            }
        });

        recyclerView.setAdapter(favAdapter);

        pb_listProgress = (ProgressBar) rootview.findViewById(R.id.lists_progress_bar);
        tv_nofav = (TextView) rootview.findViewById(R.id.no_lists_placeholder);
        initProgress();
    }

    private void initData(){
        if(datalist != null) datalist.clear();
        else datalist = new ArrayList<>();
        for(int i = 0; i < MyApp.getInstance().mylist.size(); i++){
            ArrayList<PropertyModel> mList = MyApp.getInstance().mylist.get(i).plist;
            for(int j = 0; j < mList.size(); j++ ){
                if(mList.get(j).fav == 1){
                    datalist.add(mList.get(j));
                }
            }
        }
        initProgress();
        favAdapter.setDataList(datalist);
        favAdapter.notifyDataSetChanged();
    }

    private void initProgress(){
        pb_listProgress.setVisibility(View.GONE);
        if(datalist.size() == 0){
            recyclerView.setVisibility(View.GONE);
            tv_nofav.setVisibility(View.VISIBLE);
        } else{
            recyclerView.setVisibility(View.VISIBLE);
            tv_nofav.setVisibility(View.GONE);
        }
    }
}
