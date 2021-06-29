package com.coasapp.coas.roombook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.OnDeleteClick;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MyRoomsAdapter extends RecyclerView.Adapter<MyRoomsAdapter.ViewHolder> implements APPConstants {


    ArrayList<JSONObject> arrayList;
    Context context;
    Activity activity;
    NumberFormat numberFormat;

    public MyRoomsAdapter(ArrayList<JSONObject> arrayList, Context context, Activity activity) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
        numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_my_rooms, parent, false);

        //returing the view
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final JSONObject jsonObject = arrayList.get(position);
        APPHelper.showLog("Room", jsonObject.toString());

        try {
            JSONArray array = new JSONArray(jsonObject.getString("images"));
            if (array.length() > 0) {
                Glide.with(context).load(MAIN_URL_IMAGE + array.getJSONObject(0).getString("image")).into(holder.imageViewRoom);
            }

            holder.textViewRoom.setText(jsonObject.getString("room_title") + " - " + jsonObject.getString("room_Approval_status"));
            holder.textViewAddress.setText(jsonObject.getString("room_street"));
            double priceh = Double.valueOf(jsonObject.getString("priceperhour"));
            double pricen = Double.valueOf(jsonObject.getString("pricepernight"));
            StringBuilder builder = new StringBuilder();
            /*if(priceh>0){
                builder.append()
            }*/

            holder.textViewPrice.setText(numberFormat.format(Double.valueOf(jsonObject.getString("pricepernight"))) + "/night, " + numberFormat.format(Double.valueOf(jsonObject.getString("priceperhour"))) + "/hour");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONArray jsonArray = new JSONArray(jsonObject.getString("bedrooms"));
                        JSONArray jsonArray1 = new JSONArray(jsonObject.getString("amenities"));
                        JSONArray jsonArrayImages = new JSONArray(jsonObject.getString("images"));
                        Intent intent = new Intent(context, AddRoomActivity.class);
                        SharedPreferences sharedPreferences = activity.getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("roomId", jsonObject.getString("room_id"));
                        editor.putString("roomLat", jsonObject.getString("room_latitude"));
                        editor.putString("roomTimeZone", jsonObject.getString("room_timezone"));
                        editor.putString("roomLng", jsonObject.getString("room_longitude"));
                        editor.putString("roomType", jsonObject.getString("room_HomeType"));
                        editor.putString("roomAccType", jsonObject.getString("room_type"));
                        editor.putString("guests", jsonObject.getString("room_TotalGuest"));
                        editor.putString("bedrooms", String.valueOf(jsonArray.length()));
                        editor.putString("apt", jsonObject.getString("room_apt"));
                        editor.putString("bathType", jsonObject.getString("room_Bathroom"));
                        editor.putString("bathNum", jsonObject.getString("room_GuestBathroom"));
                        editor.putString("roomAddress", jsonObject.getString("room_street"));
                        editor.putString("roomZip", jsonObject.getString("room_zip"));
                        editor.putString("roomCity", jsonObject.getString("room_city"));
                        editor.putString("roomState", jsonObject.getString("room_state"));
                        editor.putString("roomCountry", jsonObject.getString("room_country"));
                        editor.putString("roomBedrooms", jsonArray.toString());
                        editor.putString("roomBedrooms1", jsonArray.toString());
                        editor.putString("roomLat", jsonObject.getString("room_latitude"));
                        editor.putString("roomLng", jsonObject.getString("room_longitude"));
                        editor.putString("roomImages", jsonArrayImages.toString());
                        editor.putString("roomAmenities", jsonObject.getString("amenities"));
                        editor.putString("events", jsonObject.getString("event_allowed"));
                        // editor.putString("surveillance", jsonObject.getString("room_sureillance"));
                        //editor.putString("limits", jsonObject.getString("room_AmenityLimit"));
                        editor.putString("parties", jsonObject.getString("party_allowed"));
                        editor.putString("smoking", jsonObject.getString("smoking_allowed"));
                        editor.putString("children", jsonObject.getString("room_SuitableChildren"));
                        //editor.putString("infants", jsonObject.getString("room_SuitableInfants"));
                        editor.putString("pets", jsonObject.getString("room_SuitablePets"));
                        editor.putString("rules", jsonObject.getString("room_rules"));
                        // editor.putString("terms", jsonObject.getString("room_terms"));
                        editor.putString("title", jsonObject.getString("room_title"));
                        editor.putString("desc", jsonObject.getString("room_desc"));
                        editor.putString("priceperhour", jsonObject.getString("priceperhour"));
                        editor.putString("pricepernight", jsonObject.getString("pricepernight"));
                        editor.putString("status", jsonObject.getString("room_status"));
                        editor.putString("mode", "edit");
                        editor.putString("room_govt_id", jsonObject.getString("room_govt_id"));
                        editor.putString("govt_req", jsonObject.getString("govt_id_required"));
                        editor.putString("book_type", jsonObject.getString("room_book_type"));
                        editor.putString("bookings", jsonObject.getString("bookings"));
                        APPHelper.showLog("book", jsonObject.getString("bookings"));
                        editor.putString("currency", jsonObject.getString("room_currency"));
                        editor.putString("room_checkin", jsonObject.getString("room_checkin"));
                        editor.putString("returned", jsonObject.getString("new_hoster"));
                        editor.putString("apt", jsonObject.getString("room_apt"));
                        editor.putString("room_checkout", jsonObject.getString("room_checkout"));
                        editor.putString("cleaning", jsonObject.getString("room_cleaning_fee"));
                        editor.putString("negotiable", jsonObject.getString("room_negotiable"));
                        editor.apply();
                        APPHelper.showLog("book", jsonObject.getString("room_book_type") + jsonObject.getString("priceperhour") + jsonObject.getString("pricepernight"));

                        activity.startActivityForResult(intent, 1);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });
            holder.itemView.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeleteClick.onDeleteClick(holder.getAdapterPosition());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    OnDeleteClick onDeleteClick;

    public void setOnDeleteClick(OnDeleteClick onDeleteClick) {
        this.onDeleteClick = onDeleteClick;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewRoom;
        TextView textViewRoom, textViewAddress, textViewPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewRoom = itemView.findViewById(R.id.imageViewRoom);
            textViewRoom = itemView.findViewById(R.id.textViewRoomName);
            textViewAddress = itemView.findViewById(R.id.textViewRoomAdddress);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
        }
    }

}
