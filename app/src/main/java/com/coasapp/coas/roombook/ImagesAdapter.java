package com.coasapp.coas.roombook;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.OnItemClick;
import com.bumptech.glide.Glide;


import java.util.ArrayList;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    ArrayList<String> arrayList;
    Activity activity;
    Context context;

    public ImagesAdapter(ArrayList<String> arrayList, Activity activity, Context context) {
        this.arrayList = arrayList;
        this.activity = activity;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_images_large, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(context).load(arrayList.get(position)).into(holder.imageViewSlide);
        holder.imageViewSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.onItemClick(holder.getAdapterPosition());
            }
        });
    }

OnItemClick onItemClick;

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewSlide;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewSlide = itemView.findViewById(R.id.imageViewSlide);

        }
    }
}
