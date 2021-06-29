package com.coasapp.coas.shopping;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;


import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by AK INFOPARK on 08-05-2018.
 */

public class MyProductImagesAdapter extends RecyclerView.Adapter<MyProductImagesAdapter.MyViewHolder> implements APPConstants {

    ArrayList<ProductImages> arrayListImages;
    Context context;


    public MyProductImagesAdapter(ArrayList<ProductImages> arrayListImages, Context context) {
        this.arrayListImages = arrayListImages;
        this.context = context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_my_product_images, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        ProductImages productImages = arrayListImages.get(position);

        APPHelper.showLog("Status", productImages.getStatus());

        String url = "";
        if (productImages.getSource().equalsIgnoreCase("file")) {
            url = productImages.getImage();

        } else {
            url = MAIN_URL_IMAGE + productImages.getImage();
        }
        Glide.with(context).load(url).into(holder.imageView);
        /*if (position == arrayListImages.size() - 1) {
            holder.imageButton.setVisibility(View.VISIBLE);
        } else {
            holder.imageButton.setVisibility(View.GONE);
        }*/
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myOnDeleteSelected.onClick(holder.getAdapterPosition());
            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageSelected.onClick(holder.getAdapterPosition());
            }
        });

        holder.layoutImage.setBackgroundColor(Color.parseColor(productImages.getColor()));

    }

    public interface OnImageSelected {
        void onClick(int position);
    }

    public interface OnDeleteSelected {
        void onClick(int position);
    }

    OnDeleteSelected myOnDeleteSelected;
    OnLongItemSelected myOnLongItemSelected;
    OnImageSelected onImageSelected;

    public interface OnLongItemSelected {
        void onLongClick(int position);
    }

    public void setOnImageSelected(OnImageSelected onImageSelected) {
        this.onImageSelected = onImageSelected;
    }

    public void setOnDeleteSelectedListener(OnDeleteSelected aOnDeleteSelectedListener) {
        myOnDeleteSelected = aOnDeleteSelectedListener;
    }

    public void setOnLongItemSelectedListener(OnLongItemSelected aOnItemSelectedListener) {
        myOnLongItemSelected = aOnItemSelectedListener;
    }


    @Override
    public int getItemCount() {
        return arrayListImages.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ImageButton imageButton;
        FrameLayout layoutImage;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageViewProduct);
            imageButton = (ImageButton) itemView.findViewById(R.id.imageButtonDelete);
            layoutImage = (FrameLayout) itemView.findViewById(R.id.frameLayoutImage);

        }
    }

}
