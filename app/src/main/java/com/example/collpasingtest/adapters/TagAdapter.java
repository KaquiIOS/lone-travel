package com.example.collpasingtest.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;

import java.util.ArrayList;
import java.util.Iterator;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagHolder> {

    private Context context;
    private ArrayList<String> tagList;
    private RecyclerViewOnClickListener clickListener;
    private boolean canRemove = true;

    public TagAdapter(Context context) {
        this.context = context;
        this.tagList = new ArrayList<>();
    }

    public void setClickListener(RecyclerViewOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setCanRemove(boolean canRemove) {
        this.canRemove = canRemove;
    }

    public void addTag(String tag) {
        if(tagList.size() == 3) {
            tagList.remove(0);
        }

        this.tagList.add(tag);
    }

    public void removeTag(int pos) {
        tagList.remove(pos);
    }

    @NonNull
    @Override
    public TagHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tag, parent, false);
        return new TagHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagHolder holder, final int position) {

        holder.tagTextView.setText("#" + tagList.get(position));

        if(canRemove) {
            holder.removeBtn.setVisibility(View.VISIBLE);
            holder.removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(position);
                }
            });
        }
    }

    @Override
    public long getItemId(int position) {
        return tagList.hashCode();
    }

    public void setTagList(ArrayList<String> tagList) {
        this.tagList = new ArrayList<>(tagList);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public static class TagHolder extends RecyclerView.ViewHolder {
        public TextView tagTextView;
        public ImageView removeBtn;
        public TagHolder(View itemView) {
            super(itemView);
            tagTextView = itemView.findViewById(R.id.text_view_item_tag);
            removeBtn = itemView.findViewById(R.id.image_view_item_tag_remove);
        }
    }

    public ArrayList<String> getTagList() {
        return tagList;
    }
}
