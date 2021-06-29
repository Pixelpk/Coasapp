package com.coasapp.coas.shopping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by AK INFOPARK on 04-06-2018.
 */

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.MyProductsViewHolder> implements APPConstants {

    ArrayList<JSONObject> productsArrayList;
    Activity activity;
    Context context;
    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
    OnCheckChanged onCheckChanged;
    SharedPreferences sharedPreferences;
    String currency = "";

    public ProductsAdapter(ArrayList<JSONObject> productsArrayList, Activity activity, Context context) {

        this.productsArrayList = productsArrayList;
        this.activity = activity;
        this.context = context;
        sharedPreferences = activity.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        currency = sharedPreferences.getString("currency", "USD");

    }

    @Override
    public MyProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_products, parent, false);
        return new MyProductsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyProductsViewHolder holder, int position) {

        final JSONObject products = productsArrayList.get(position);
        try {
            JSONArray jsonArray = new JSONArray(products.getString("images"));
            if (jsonArray.length() > 0) {
                if (!jsonArray.getJSONObject(0).getString("image").startsWith("http://"))
                    Glide.with(context).load(MAIN_URL_IMAGE + jsonArray.getJSONObject(0).getString("image")).into(holder.imageViewProduct);
                else
                    Glide.with(context).load(jsonArray.getJSONObject(0).getString("image")).into(holder.imageViewProduct);


            }
            holder.textViewProduct.setText(products.getString("pro_name")/*+" - "+ String.format("%.2f", Double.valueOf(products.getString("distance")))+" miles"*/);
            holder.textViewSku.setText(products.getString("pro_condition"));
           /* holder.textViewMrp.setText(formatter.format(Double.parseDouble(products.getString("mrp"))));
            holder.textViewMrp.setPaintFlags(holder.textViewMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);*/
           if(Integer.valueOf(products.getString("count"))>0){
               holder.textViewAvailable.setText("Available");

           }
           else {
               holder.textViewAvailable.setText("Out of Stock");

           }
        //    holder.textViewPrice.setText(formatter.format(Double.valueOf(products.getString("price"))));
            holder.textViewPrice.setText(products.getString("pro_currency") + " " +products.getString("price"));

            /*if (!currency.equalsIgnoreCase(products.getString("pro_currency"))) {
                holder.textViewPrice.setText(currency + (Double.valueOf(products.getString("price")) * 0.5));
            }*/

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProductDetailsActivity.class);
                    intent.putExtra("details", products.toString());
                    activity.startActivity(intent);
                }
            });

      /*  holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckChanged.onCheckChanged(products.getProductId(), isChecked);
            }
        });*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface OnCheckChanged {
        void onCheckChanged(String id, boolean checked);
    }

    public void setOnCheckChanged(OnCheckChanged onCheckChanged) {
        this.onCheckChanged = onCheckChanged;
    }

    @Override
    public int getItemCount() {
        return productsArrayList.size();
    }

    class MyProductsViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProduct;
        TextView textViewProduct, textViewSku, textViewMrp, textViewPrice, textViewAvailable;
        CheckBox checkBox;

        MyProductsViewHolder(View itemView) {
            super(itemView);
            textViewAvailable = itemView.findViewById(R.id.textViewAvailable);

            imageViewProduct = (ImageView) itemView.findViewById(R.id.imageViewProduct);
            textViewProduct = (TextView) itemView.findViewById(R.id.textViewProduct);
            textViewSku = (TextView) itemView.findViewById(R.id.textViewSku);
            textViewMrp = (TextView) itemView.findViewById(R.id.textViewMrp);
            textViewPrice = (TextView) itemView.findViewById(R.id.textViewPrice);
            //checkBox = (CheckBox) itemView.findViewById(R.id.checkBoxAdd);
        }
    }

    public void updateList(ArrayList<JSONObject> list) {
        productsArrayList = list;
        notifyDataSetChanged();
    }
}
