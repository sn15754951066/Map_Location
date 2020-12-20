package com.example.map_location;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.sug.SuggestionResult;

import java.util.ArrayList;

public class SuggestAdapter extends RecyclerView.Adapter<SuggestAdapter.ViewHolder> {

    private Context context;
    private ArrayList<SuggestionResult.SuggestionInfo> list;

    public SuggestAdapter(Context context, ArrayList<SuggestionResult.SuggestionInfo> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from( context ).inflate( R.layout.search_item, parent, false );
        return new ViewHolder( inflate );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        SuggestionResult.SuggestionInfo suggestionInfo = list.get( position );
        holder.tv_desc.setText(suggestionInfo.getKey() );
        holder.rootView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iOnClickItem.iOnClickItem( position );
            }
        } );


    }

    @Override
    public int getItemCount() {
        return list.size();
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
