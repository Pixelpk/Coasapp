package com.coasapp.coas.roombook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Created by AK INFOPARK on 08-06-2018.
 */

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.RoomsViewHolder> implements APPConstants {

    ArrayList<JSONObject> arrayList;
    Context context;
    Activity activity;
    NumberFormat numberFormat;

    public RoomListAdapter(ArrayList<JSONObject> arrayList, Context context, Activity activity) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
        numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
    }

    @Override
    public RoomsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_room_list, parent, false);
        //returing the view
        return new RoomsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RoomsViewHolder holder, int position) {

        final JSONObject object = arrayList.get(position);
        try {
            JSONArray jsonArray = new JSONArray(object.getString("images"));
            if (jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                Glide.with(context).load(MAIN_URL_IMAGE + jsonObject.getString("image")).into(holder.imageViewRoom);
            } else {
                Glide.with(context).load(R.drawable.placeholder).into(holder.imageViewRoom);

            }
            //Toast.makeText(context, jsonObject.getString("image"), Toast.LENGTH_SHORT).show();

            //Glide.with(context).load(hashMap.get("image")).into(holder.imageViewRoom);
            holder.textViewRoom.setText(object.getString("room_title"));
            holder.textViewAddress.setText(object.getString("room_city") + " - " + object.getString("room_state"));
            String price = "";
            String cur = object.getString("currency");

            //cur = object.getString("currency");
            if (Double.parseDouble(object.getString("priceperhour")) == 0) {
                price = object.getString("pricepernight");
                holder.textViewPrice.setText(formatter.format(Double.valueOf(price)) + "/night");

            } else if (Double.parseDouble(object.getString("pricepernight")) == 0) {
                price = object.getString("priceperhour");
                holder.textViewPrice.setText(formatter.format(Double.valueOf(price)) + "/hour");

            }
            /*else {
                price = numberFormat.format(Double.valueOf(object.getString("pricepernight"))) + "/night, " + numberFormat.format(Double.valueOf(object.getString("priceperhour"))) + "/hour";
            }*/

        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RoomDetailsActivity.class);

                intent.putExtra("details", object.toString());
                activity.startActivity(intent);


            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class RoomsViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewRoom;
        TextView textViewRoom, textViewAddress, textViewPrice;

        public RoomsViewHolder(View itemView) {
            super(itemView);
            imageViewRoom = itemView.findViewById(R.id.imageViewRoom);
            textViewRoom = itemView.findViewById(R.id.textViewRoomName);
            textViewAddress = itemView.findViewById(R.id.textViewRoomAdddress);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
        }
    }
}
