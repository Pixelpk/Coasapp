package com.coasapp.coas.shopping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coasapp.coas.R;

import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;
import com.coasapp.coas.utils.DatabaseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SellerOrdersAdapter extends RecyclerView.Adapter<SellerOrdersAdapter.ViewHolder> implements APPConstants {

    ArrayList<JSONObject> arrayList;
    Context context;
    Activity activity;
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;
    OnDelClick onDelClick;

    OnShipClick onShipClick;


    public SellerOrdersAdapter(ArrayList<JSONObject> arrayList, Context context, Activity activity) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
       /* databaseHandler = new DatabaseHandler(context);
        sqLiteDatabase = databaseHandler.getWritableDatabase();*/
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_orders_sellers, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final JSONObject object = arrayList.get(position);
       /* Glide.with(context).load(map.get("image")).apply(new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)).into(holder.imageViewProduct);*/
        try {
            JSONArray jsonArray = new JSONArray(object.getString("images"));
            if (jsonArray.length() > 0) {
                Glide.with(context).load(MAIN_URL_IMAGE + jsonArray.getJSONObject(0).getString("image")).into(holder.imageViewProduct);
            }
            holder.textViewOrderId.setText("Order ID: " + object.getString("order_track_id"));
            holder.textViewProduct.setText(object.getString("pro_name"));
            holder.textViewPrice.setText("$" + object.getString("order_amt"));
            holder.textViewQty.setText("x" + object.get("order_qty"));
            holder.textViewDeliver.setText(object.getString("order_address"));
            holder.textViewStatus.setText(object.getString("order_status"));
            if (object.getString("order_status").equalsIgnoreCase("to ship")) {
                holder.buttonShip.setVisibility(View.VISIBLE);
                holder.buttonDeliver.setVisibility(View.GONE);
            }
            if (object.getString("order_address").equalsIgnoreCase("pickup")) {
                holder.buttonShip.setVisibility(View.GONE);
                holder.buttonDeliver.setVisibility(View.VISIBLE);
            }
            if (object.getString("order_status").equalsIgnoreCase("Shipped")) {
                holder.buttonShip.setVisibility(View.VISIBLE);
                holder.buttonDeliver.setVisibility(View.VISIBLE);
            }
            if (object.getString("order_status").equalsIgnoreCase("delivered")) {
                holder.buttonShip.setVisibility(View.GONE);
                holder.buttonDeliver.setVisibility(View.GONE);
            }
            if (object.getString("order_approved").equalsIgnoreCase("1")) {
                holder.textViewStatus.setText("Delivery Confirmed");

            }
            if (object.getString("order_status").equalsIgnoreCase("cancelled")) {
                holder.buttonShip.setVisibility(View.GONE);
                holder.buttonDeliver.setVisibility(View.GONE);

            }
            if (!(object.getString("order_report_status").equalsIgnoreCase(""))) {
                holder.textViewStatus.setText(object.getString("order_report_status"));
            }
            holder.buttonDeliver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDelClick.onDelClick(holder.getAdapterPosition());
                }
            });
            holder.buttonShip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onShipClick.onShipClick(holder.getAdapterPosition());
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject jsonObject = arrayList.get(holder.getAdapterPosition());
                    Intent intent = new Intent(context, OrderDetailsActivity.class);
                    intent.putExtra("role", "seller");
                    intent.putExtra("details", jsonObject.toString());
                    activity.startActivity(intent);
                }
            });

            /*holder.imageViewDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onItemSelected(View v) {
//                sqLiteDatabase.delete("cart", "pro_id=?", new String[]{map.get("pro_id")});
//                arrayList.remove(holder.getAdapterPosition());
//                notifyDataSetChanged();
                    onDelClick.onDelClick(holder.getAdapterPosition());
                }
            });*/
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public interface OnDelClick {
        void onDelClick(int position);
    }

    public interface OnShipClick {
        void onShipClick(int position);
    }

    public void setOnDelClick(OnDelClick onDelClick) {
        this.onDelClick = onDelClick;
    }

    public void setOnShipClick(OnShipClick onShipClick) {
        this.onShipClick = onShipClick;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProduct, textViewPrice, textViewQty, textViewStatus, textViewDeliver, textViewOrderId;
        ImageView imageViewProduct, imageViewDel;
        Button buttonShip, buttonDeliver;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderID);

            textViewProduct = itemView.findViewById(R.id.textViewProduct);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQty = itemView.findViewById(R.id.textViewQty);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            buttonDeliver = itemView.findViewById(R.id.buttonDeliver);
            buttonShip = itemView.findViewById(R.id.buttonShip);
            textViewDeliver = itemView.findViewById(R.id.tvDeliver);
            //imageViewDel = itemView.findViewById(R.id.imageViewDelete);
        }
    }


}

