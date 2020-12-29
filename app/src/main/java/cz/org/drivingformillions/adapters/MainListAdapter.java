package cz.org.drivingformillions.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cz.org.drivingformillions.R;
import cz.org.drivingformillions.models.ListModel;

/**
 * Created by Administrator on 12/6/2017.
 */

public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.ViewHolder> implements Filterable {
    private ArrayList<ListModel> mDataSet;
    private ArrayList<ListModel> filteredList;
    private RestFilter restFilter;
    //private SparseBooleanArray mSelectedItemsIds;
    private int mSelectedItems = -1;

    private final Context mContext;
    private MainListAdapter.OnItemClickListener mOnItemClickListener;
    private MainListAdapter.OnItemLongClickListener mOnItemLongClickListener;

    final int sdk = android.os.Build.VERSION.SDK_INT;

    public void setOnItemClickListener (MainListAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener (MainListAdapter.OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
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
    public MainListAdapter(ArrayList<ListModel> dataSet, Context context) {
        mDataSet = dataSet;
        filteredList = dataSet;
        for(int i=0;i<filteredList.size(); i++){
            ListModel one = filteredList.get(i);
            one.index = i;
        }
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.mylists_one_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        String subStr = "Properties:" + String.valueOf(filteredList.get(position).getPlist().size()) + " - Updated:" + filteredList.get(position).getUpdateDate();
        viewHolder.getTv_tilte().setText(filteredList.get(position).getListname());
        viewHolder.getTv_subtitle().setText(subStr);

        if(!filteredList.get(position).isEmptyList()){
            viewHolder.getIv_status().setVisibility(View.VISIBLE);
            Drawable drawable = viewHolder.getIv_status().getDrawable();
            if (filteredList.get(position).getState().equals("active")) {
                drawable.setColorFilter(Color.parseColor("#22bb22"), PorterDuff.Mode.SRC_ATOP);
                viewHolder.getIv_status().setImageDrawable(drawable);
            } else {
                drawable.setColorFilter(Color.parseColor("#cc3333"), PorterDuff.Mode.SRC_ATOP);
                viewHolder.getIv_status().setImageDrawable(drawable);
            }
        } else{
            viewHolder.getIv_status().setVisibility(View.GONE);
        }

        viewHolder.getIv_fav().setVisibility(View.GONE);

        viewHolder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(filteredList.get(position).index);
                }
            }
        });

        viewHolder.getView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mOnItemClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(filteredList.get(position).index);
                }
                return false;
            }
        });

        if(position == mSelectedItems){
            viewHolder.getView().setBackgroundColor(Color.parseColor("#1C83CD"));
        } else {
            viewHolder.getView().setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

    }

    //Remove selected selections
    public void removeSelection() {
        mSelectedItems = -1;
        notifyDataSetChanged();
    }
    //Put or delete selected position into SparseBooleanArray
    public void selectView(int position) {
        if(mSelectedItems == position){
            mSelectedItems = -1;
        } else {
            mSelectedItems = position;
        }
        notifyDataSetChanged();
    }

    public int getSelectedIds(){
        return mSelectedItems;
    }

    public int getSelectedCount(){
        if(mSelectedItems == -1) return 0;
        return mSelectedItems;
    }
    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void setDataList(ArrayList<ListModel> dataSet){
        mDataSet = dataSet;
        filteredList = dataSet;
        /*for(int i=0;i<filteredList.size(); i++){
            ListModel one = filteredList.get(i);
            one.index = i;
        }*/
    }

    public interface OnItemClickListener {
        public int onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        public int onItemLongClick(int position);
    }

    /**
     * Get custom filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (restFilter == null) {
            restFilter = new RestFilter();
        }

        return restFilter;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class RestFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                List<ListModel> tempList = new ArrayList<ListModel>();

                // search content in friend list
                for (ListModel user : mDataSet) {
                    if (user.getListname().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(user);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = mDataSet.size();
                filterResults.values = mDataSet;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<ListModel>) results.values;
            notifyDataSetChanged();
        }
    }

}
