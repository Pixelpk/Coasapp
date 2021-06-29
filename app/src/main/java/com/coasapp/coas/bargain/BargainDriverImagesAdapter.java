package com.coasapp.coas.bargain;

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
import com.coasapp.coas.shopping.MyProductImagesAdapter;
import com.coasapp.coas.shopping.ProductImages;
import com.coasapp.coas.utils.APPHelper;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static com.coasapp.coas.utils.APPConstants.MAIN_URL_IMAGE;

public class BargainDriverImagesAdapter extends RecyclerView.Adapter<BargainDriverImagesAdapter.MyViewHolder>{
    ArrayList<ProductImages> arrayListImages;
    Context context;


    public BargainDriverImagesAdapter(ArrayList<ProductImages> arrayListImages, Context context) {
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
            holder.imageButton.setVisibility(View.VISIBLE);
            url = productImages.getImage();

        } else {
            holder.imageButton.setVisibility(View.GONE);
            url = MAIN_URL_IMAGE + productImages.getUrlImage();
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

    MyProductImagesAdapter.OnDeleteSelected myOnDeleteSelected;
    MyProductImagesAdapter.OnLongItemSelected myOnLongItemSelected;
    MyProductImagesAdapter.OnImageSelected onImageSelected;

    public interface OnLongItemSelected {
        void onLongClick(int position);
    }

    public void setOnImageSelected(MyProductImagesAdapter.OnImageSelected onImageSelected) {
        this.onImageSelected = onImageSelected;
    }

    public void setOnDeleteSelectedListener(MyProductImagesAdapter.OnDeleteSelected aOnDeleteSelectedListener) {
        myOnDeleteSelected = aOnDeleteSelectedListener;
    }

    public void setOnLongItemSelectedListener(MyProductImagesAdapter.OnLongItemSelected aOnItemSelectedListener) {
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
