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

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;
import com.coasapp.coas.utils.DatabaseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BuyerOrdersAdapter extends RecyclerView.Adapter<BuyerOrdersAdapter.ViewHolder>implements APPConstants {

    ArrayList<JSONObject> arrayList;
    Context context;
    Activity activity;
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;
    CheckoutAdapter.OnDelClick onDelClick;
    OnConfirmClick onConfirmClick;

    public BuyerOrdersAdapter(ArrayList<JSONObject> arrayList, Context context, Activity activity) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
        databaseHandler = new DatabaseHandler(context);
        sqLiteDatabase = databaseHandler.getWritableDatabase();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_orders_buyer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        final JSONObject object = arrayList.get(position);
       /* Glide.with(context).load(map.get("image")).apply(new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)).into(holder.imageViewProduct);*/
        try {
            JSONArray jsonArray = new JSONArray(object.getString("images"));
            if (jsonArray.length() > 0) {
                Glide.with(context).load(MAIN_URL_IMAGE+jsonArray.getJSONObject(0).getString("image")).into(holder.imageViewProduct);
            }
            holder.textViewOrderId.setText("Order ID: " + object.getString("order_track_id"));
            holder.textViewProduct.setText(object.getString("pro_name"));
            holder.textViewPrice.setText("$"+object.getString("order_amt"));
            holder.textViewQty.setText("x" + object.get("order_qty"));
            holder.textViewStatus.setText(object.getString("order_status"));

            if (object.getString("order_status").equalsIgnoreCase("Delivered") && object.getString("order_approved").equalsIgnoreCase("0")) {
                holder.buttonConfirm.setVisibility(View.VISIBLE);
            }
            if (!object.getString("order_report_status").equalsIgnoreCase("")) {
                holder.buttonConfirm.setVisibility(View.GONE);
            }
            if (object.getString("order_approved").equalsIgnoreCase("1")) {
                holder.textViewStatus.setText("Delivery Confirmed");
                holder.buttonConfirm.setVisibility(View.GONE);

            }
            APPHelper.showLog("Order", object.getString("order_report_status"));
            if (!(object.getString("order_report_status").equalsIgnoreCase(""))) {
                holder.textViewStatus.setText(object.getString("order_report_status"));
            }
            /*holder.imageViewDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onItemSelected(View v) {
//                sqLiteDatabase.delete("cart", "pro_id=?", new String[]{map.get("pro_id")});
//                arrayList.remove(holder.getAdapterPosition());
//                notifyDataSetChanged();
                    onDelClick.onDelClick(holder.getAdapterPosition());
                }
            });*/
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject jsonObject = arrayList.get(holder.getAdapterPosition());
                    Intent intent = new Intent(context, OrderDetailsActivity.class);
                    intent.putExtra("role", "buyer");
                    intent.putExtra("details", jsonObject.toString());
                    activity.startActivity(intent);
                }
            });
            holder.buttonConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onConfirmClick.onConfirmClick(holder.getAdapterPosition());
                }
            });
            if(object.getString("order_status").equalsIgnoreCase("To Ship") ||object.getString("order_status").equalsIgnoreCase("Shipped") ){
                holder.itemView.findViewById(R.id.buttonCancelDelivery).setVisibility(View.VISIBLE);
            }
            else {
                holder.itemView.findViewById(R.id.buttonCancelDelivery).setVisibility(View.GONE);
            }
            holder.itemView.findViewById(R.id.buttonCancelDelivery).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                 onAdapterViewsClicked.onCancelClicked(holder.getAdapterPosition());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public void setOnAdapterViewsClicked(OnAdapterViewsClicked onAdapterViewsClicked) {
        this.onAdapterViewsClicked = onAdapterViewsClicked;
    }

   OnAdapterViewsClicked onAdapterViewsClicked;

    public interface OnAdapterViewsClicked {
        void onCancelClicked(int i);
    }
    public interface OnDelClick {
        void onDelClick(int position);
    }

    public void setOnDelClick(CheckoutAdapter.OnDelClick onDelClick) {
        this.onDelClick = onDelClick;
    }

    public interface OnConfirmClick {
        void onConfirmClick(int position);
    }

    public void setOnConfirmClick(OnConfirmClick onConfirmClick) {
        this.onConfirmClick = onConfirmClick;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProduct, textViewPrice, textViewQty, textViewStatus, textViewOrderId;
        ImageView imageViewProduct, imageViewDel;
        Button buttonConfirm;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderID);

            textViewProduct = itemView.findViewById(R.id.textViewProduct);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQty = itemView.findViewById(R.id.textViewQty);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            buttonConfirm = itemView.findViewById(R.id.buttonConfirmDelivery);
            //imageViewDel = itemView.findViewById(R.id.imageViewDelete);
        }
    }


}
