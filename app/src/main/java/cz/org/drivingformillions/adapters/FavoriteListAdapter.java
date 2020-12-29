package cz.org.drivingformillions.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cz.org.drivingformillions.R;
import cz.org.drivingformillions.models.ListModel;
import cz.org.drivingformillions.models.PropertyModel;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.ViewHolder> {
    private ArrayList<PropertyModel> mDataSet;

    private final Context mContext;
    private FavoriteListAdapter.OnItemClickListener mOnItemClickListener;

    final int sdk = android.os.Build.VERSION.SDK_INT;

    public void setOnItemClickListener (FavoriteListAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }


    /**
     * Provide a reference to the type of views that you are using
     * (custom {@link RecyclerView.ViewHolder}).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iv_status;
        private final ImageView iv_fav;
        private final TextView tv_tilte;
        private final TextView tv_subtitle;
        private final View view;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            iv_status = (ImageView) v.findViewById(R.id.status_image_view);
            iv_fav = (ImageView) v.findViewById(R.id.list_fav_star);
            tv_tilte = (TextView) v.findViewById(R.id.list_title);
            tv_subtitle = (TextView) v.findViewById(R.id.list_subtitle);
            view = v;
        }

        public ImageView getIv_fav() {
            return iv_fav;
        }

        public ImageView getIv_status() {
            return iv_status;
        }

        public TextView getTv_subtitle() {
            return tv_subtitle;
        }

        public TextView getTv_tilte() {
            return tv_tilte;
        }

        public View getView(){return view;}
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public FavoriteListAdapter(ArrayList<PropertyModel> dataSet, Context context) {
        mDataSet = dataSet;
        mContext = context;
    }

    @Override
    public FavoriteListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.mylists_one_item, viewGroup, false);

        return new FavoriteListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FavoriteListAdapter.ViewHolder viewHolder, final int position) {

        String subStr = "Updated:" + mDataSet.get(position).updateDate;
        viewHolder.getTv_tilte().setText(mDataSet.get(position).address);
        viewHolder.getTv_subtitle().setText(subStr);

        viewHolder.getIv_status().setVisibility(View.GONE);

        if(mDataSet.get(position).fav == 1){
            viewHolder.getIv_fav().setVisibility(View.VISIBLE);
        } else{
            viewHolder.getIv_fav().setVisibility(View.GONE);
        }

        viewHolder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });

    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setDataList(ArrayList<PropertyModel> dataSet){
        mDataSet = dataSet;
    }

    public interface OnItemClickListener {
        public int onItemClick(int position);
    }
}
