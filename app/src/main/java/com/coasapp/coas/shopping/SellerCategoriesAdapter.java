package com.coasapp.coas.shopping;

import android.app.Activity;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class SellerCategoriesAdapter extends RecyclerView.Adapter<SellerCategoriesAdapter.MyViewHolder> implements APPConstants {

    Activity activity;
    Context context;
    ArrayList<ProductCategories> arrayList;
    String catId;
    OnItemSelected onItemSelected;

    public SellerCategoriesAdapter(Activity activity, Context context, ArrayList<ProductCategories> arrayList, String catId) {
        this.activity = activity;
        this.context = context;
        this.arrayList = arrayList;
        this.catId = catId;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_pro_categories, parent, false);

        return new MyViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final ProductCategories categories = arrayList.get(position);
        if (categories.getImage().endsWith("jpg") || categories.getImage().endsWith("png")) {
            if (categories.getImage().startsWith("http")) {
                Glide.with(context).load(categories.getImage()).into(holder.imageViewCategories);

            } else {
                Glide.with(context).load(MAIN_URL_IMAGE + categories.getImage()).into(holder.imageViewCategories);

            }
        } else {
            Glide.with(context).load(R.drawable.placeholder).into(holder.imageViewCategories);
        }
        holder.textViewCategories.setText(categories.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               /* Intent intent = new Intent(context, ProductCategoriesActivity.class);
                intent.putExtra("cat_id", categories.getId());
                activity.startActivity(intent);*/

                onItemSelected.onItemSelected(holder.getAdapterPosition());

            }
        });

    }

    public interface OnItemSelected {
        void onItemSelected(int position);
    }

    public void setOnItemSelected(OnItemSelected onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    public interface OnChecked {
        void onChecked(String catId, boolean checked);
    }


    public interface OnUnchecked {
        void onUnchecked(String catId);
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewCategories;
        TextView textViewCategories;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageViewCategories = (ImageView) itemView.findViewById(R.id.imageViewCategory);
            textViewCategories = (TextView) itemView.findViewById(R.id.textViewCategory);
        }
    }

    public void updateList(ArrayList<ProductCategories> list) {
        arrayList = list;
        notifyDataSetChanged();
    }
}

