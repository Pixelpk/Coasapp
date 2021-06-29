package com.coasapp.coas.connectycube.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.coasapp.coas.R;
import com.coasapp.coas.connectycube.ModelClasses.imageviewrecyclerModel;

import java.util.List;

public class imageviewrecyclerAdapter extends RecyclerView.Adapter<imageviewrecyclerAdapter.ViewHolder>{
    private List<imageviewrecyclerModel> listdata;
    Context context;

    // RecyclerView recyclerView;
    public imageviewrecyclerAdapter(List<imageviewrecyclerModel> listdata, Context context) {
        this.listdata = listdata;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.imageviewrecycleritem, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final imageviewrecyclerModel myListData = listdata.get(position);


        Glide.with(context).load(myListData.getUrl()).into( holder.image);
     /*   holder.notification_time.setText(myListData.getUrl());
        holder.notification_desc.setText(myListData.getNotification_desc());
        holder.notification_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Notification Clicked", Toast.LENGTH_SHORT).show();
            }
        });*/

    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout notification_layout;
        public ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            this.image = (ImageView) itemView.findViewById(R.id.imagerecycler_IV);

        }
    }
}