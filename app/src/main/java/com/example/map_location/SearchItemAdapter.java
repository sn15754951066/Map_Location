package com.example.map_location;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.search.core.PoiInfo;

import java.util.ArrayList;

public class SearchItemAdapter extends RecyclerView.Adapter<SearchItemAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PoiInfo> poiInfos;

    public SearchItemAdapter(Context context, ArrayList<PoiInfo> poiInfos) {
        this.context = context;
        this.poiInfos = poiInfos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from( context ).inflate( R.layout.search_item, parent, false );
        return new ViewHolder( inflate );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        PoiInfo poiInfo = poiInfos.get( position );
        holder.tv_desc.setText(poiInfo.name+" "+poiInfo.address );
        holder.rootView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iOnClickItem.iOnClickItem( position );
            }
        } );


    }

    @Override
    public int getItemCount() {
        return poiInfos.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public TextView tv_desc;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.tv_desc = (TextView) rootView.findViewById( R.id.tv_desc );
        }

    }

    public interface IOnClickItem{
        void iOnClickItem(int position);
    }
    IOnClickItem iOnClickItem;

    public void setIOnClickItem(IOnClickItem iOnClickItem) {
        this.iOnClickItem = iOnClickItem;
    }
}
