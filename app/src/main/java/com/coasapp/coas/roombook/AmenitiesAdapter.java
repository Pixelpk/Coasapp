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
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;

public class AmenitiesAdapter extends RecyclerView.Adapter<AmenitiesAdapter.ViewHolder> {

    Context context;
    Activity activity;
    ArrayList<HashMap<String, String>> arrayList;

    public AmenitiesAdapter(Context context, Activity activity, ArrayList<HashMap<String, String>> arrayList) {
        this.context = context;
        this.activity = activity;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_amenities, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(context).load(arrayList.get(position).get("image")).into(holder.imageViewAmenity);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewAmenity;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewAmenity = itemView.findViewById(R.id.imageViewAmenity);
        }
    }
}
