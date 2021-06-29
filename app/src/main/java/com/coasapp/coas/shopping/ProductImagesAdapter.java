package com.coasapp.coas.shopping;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ProductImagesAdapter extends RecyclerView.Adapter<ProductImagesAdapter.MyViewHolder> implements APPConstants {

    public ProductImagesAdapter(ArrayList<ProductImages> imagesArrayList, Activity activity, Context context) {
        this.imagesArrayList = imagesArrayList;
        this.activity = activity;
        this.context = context;
    }

    ArrayList<ProductImages> imagesArrayList;
    Activity activity;
    Context context;
    OnImageSelected onImageSelected;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_product_images, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        final ProductImages productImages = imagesArrayList.get(position);
        Log.i("Thumb", MAIN_URL_IMAGE + productImages.getImage());
        Glide.with(context).load(MAIN_URL_IMAGE + productImages.getImage()).into(holder.imageViewProduct);
        if (productImages.isSelected()) {
            holder.layoutImages.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
        } else {
            holder.layoutImages.setBackgroundColor(Color.parseColor("#50000000"));
        }

        holder.imageViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*for (int i = 0; i < imagesArrayList.size(); i++) {
                   imagesArrayList.get(i).setSelected(false);
                }
                productImages.setSelected(true);
                notifyDataSetChanged();*/
                onImageSelected.onImageSelected(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagesArrayList.size();
    }

    public interface OnImageSelected {
        public void onImageSelected(int position);
    }

    public void setOnImageSelected(OnImageSelected onImageSelected) {
        this.onImageSelected = onImageSelected;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProduct;
        LinearLayout layoutImages;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            layoutImages = itemView.findViewById(R.id.layoutImages);
        }
    }
}
