package com.coasapp.coas.shopping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MyProductsAdapter extends RecyclerView.Adapter<MyProductsAdapter.MyProductsViewHolder> implements APPConstants {

    ArrayList<JSONObject> productsArrayList;
    Activity activity;
    Context context;
    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

    public MyProductsAdapter(ArrayList<JSONObject> productsArrayList, Activity activity, Context context) {
        this.productsArrayList = productsArrayList;
        this.activity = activity;
        this.context = context;

    }

    @NonNull
    @Override
    public MyProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_my_products, parent, false);
        return new MyProductsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyProductsViewHolder holder, int position) {

        final JSONObject products = productsArrayList.get(position);
        try {
            JSONArray jsonArray = new JSONArray(products.getString("images"));
            if (jsonArray.length() > 0) {
                Glide.with(context).load(MAIN_URL_IMAGE+jsonArray.getJSONObject(0).getString("image")).into(holder.imageViewProduct);
            }
            holder.textViewProduct.setText(products.getString("pro_name")+" - "+products.getString("pro_approve"));
            if(Integer.valueOf(products.getString("count"))==0){
                holder.textViewSku.setText("Sold out");
            }
            else {
                holder.textViewSku.setText(products.getString("count")+" Available");
            }
          /*  holder.textViewMrp.setText(formatter.format(Double.parseDouble(products.getString("mrp"))));
            holder.textViewMrp.setPaintFlags(holder.textViewMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);*/
            //holder.textViewPrice.setText(products.getString("pro_currency") + SPACE + products.getString("price"));
          //  Toast.makeText(activity, products.getString("price"), Toast.LENGTH_SHORT).show();
            holder.textViewPrice.setText(products.getString("pro_currency") + " " + products.getString("price"));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, EditProductActivity.class);
                    intent.putExtra("details", productsArrayList.get(holder.getAdapterPosition()).toString());
                    activity.startActivityForResult(intent, 99);
                }
            });

            holder.itemView.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDelClick.onDelClick(holder.getAdapterPosition());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public interface OnDelClick {
        public void onDelClick(int i);
    }

    OnDelClick onDelClick;

    public void setOnDelClick(OnDelClick onDelClick) {
        this.onDelClick = onDelClick;
    }

    @Override
    public int getItemCount() {
        return productsArrayList.size();
    }

    class MyProductsViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProduct;
        TextView textViewProduct, textViewSku, textViewMrp, textViewPrice;
        CheckBox checkBox;

        MyProductsViewHolder(View itemView) {
            super(itemView);
            imageViewProduct = (ImageView) itemView.findViewById(R.id.imageViewProduct);
            textViewProduct = (TextView) itemView.findViewById(R.id.textViewProduct);
            textViewSku = (TextView) itemView.findViewById(R.id.textViewSku);
            textViewMrp = (TextView) itemView.findViewById(R.id.textViewMrp);
            textViewPrice = (TextView) itemView.findViewById(R.id.textViewPrice);
            //checkBox = (CheckBox) itemView.findViewById(R.id.checkBoxAdd);
        }
    }
}
