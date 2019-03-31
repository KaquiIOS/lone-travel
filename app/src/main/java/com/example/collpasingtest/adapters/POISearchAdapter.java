package com.example.collpasingtest.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.collpasingtest.POIResultParser;
import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.SearchRecyclerViewAdapterCallback;
import com.example.collpasingtest.models.SearchInfo;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class POISearchAdapter extends RecyclerView.Adapter<POISearchAdapter.POISearchVH> {

    private ArrayList<SearchInfo> searchList;
    private SearchRecyclerViewAdapterCallback callback;
    private Context context;

    public POISearchAdapter(Context context) {
        this.context = context;
        this.searchList = new ArrayList<>();
    }

    public void setCallback(SearchRecyclerViewAdapterCallback callback) {
        this.callback = callback;
    }

    public void setSearchList(ArrayList<SearchInfo> searchList) {
        this.searchList = (ArrayList<SearchInfo>)searchList.clone();
    }

    public void clear() {
        this.searchList.clear();
    }

    @NonNull
    @Override
    public POISearchVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_info, parent, false);
        return new POISearchVH(view);
    }

    public ArrayList<SearchInfo> getSearchList() {
        return searchList;
    }

    @Override
    public void onBindViewHolder(@NonNull POISearchVH holder, int position) {

        final SearchInfo info = searchList.get(position);

        holder.mTitleTextView.setText(info.getTitle());
        holder.mAddressTextView.setText(info.getAddress());

        // 클릭시 타이틀만 전달
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 타이틀만 넘겨주기
                callback.updateData(info.getTitle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public static class POISearchVH extends RecyclerView.ViewHolder {

        public TextView mTitleTextView, mAddressTextView;

        public POISearchVH(View itemView) {
            super(itemView);

            mTitleTextView = itemView.findViewById(R.id.text_view_item_search_info_title);
            mAddressTextView = itemView.findViewById(R.id.text_view_item_search_info_address);
        }
    }

    public void filter(String keyword) {
        if (keyword.length() >= 2) {
            try {
                POIResultParser parser = new POIResultParser(this, context);
                searchList.addAll(parser.execute(keyword).get());
                //notifyDataSetChanged();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
