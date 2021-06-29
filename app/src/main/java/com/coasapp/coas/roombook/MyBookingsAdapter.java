package com.coasapp.coas.roombook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coasapp.coas.ApplozicSampleApplication;
import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class MyBookingsAdapter extends RecyclerView.Adapter<MyBookingsAdapter.MyViewHolder> implements APPConstants {

    ArrayList<JSONObject> arrayList;
    Context context;
    Activity activity;

    public MyBookingsAdapter(ArrayList<JSONObject> arrayList, Context context, Activity activity) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_my_bookings, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        try {
            final JSONObject object = arrayList.get(position);
            Glide.with(context).load(R.drawable.placeholder).into(holder.imageViewRoom);
            holder.textViewRoom.setText(object.getString("room_title") + " - " + object.getString("name"));
            Date date1 = new java.util.Date(Long.parseLong(object.getString("book_checkin")) * 1000L);
            Date date2 = new java.util.Date(Long.parseLong(object.getString("book_checkout")) * 1000L);

            holder.textViewFrom.setText(sdfNativeDateTime.format(date1));
            holder.textViewTo.setText(sdfNativeDateTime.format(date2));
            SimpleDateFormat simpleDateFormat1 = ((ApplozicSampleApplication)activity.getApplication()).getSdfDatabaseFormat();
            Date dateBook = simpleDateFormat1.parse(object.getString("book_date"));

            SimpleDateFormat simpleDateFormat2 = ((ApplozicSampleApplication)activity.getApplication()).getSdfNativeDevice();
simpleDateFormat2.setTimeZone(TimeZone.getDefault());
            holder.textViewOrderDate.setText(simpleDateFormat2.format(dateBook));
           /* if (object.getString("book_status").equalsIgnoreCase("Booked")) {
                holder.buttonCancel.setVisibility(View.VISIBLE);
            }*/
            holder.textViewStatus.setText(object.getString("book_status"));
            holder.textViewAmt.setText(object.getString("book_amount"));
            holder.textViewOrderId.setText("Booking Id: "+object.getString("book_ref"));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if (holder.layoutDetails.getVisibility() == View.GONE) {
                        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_down1);
                        holder.layoutDetails.setVisibility(View.VISIBLE);
                        holder.layoutDetails.startAnimation(animation);
                    } else {
                        holder.layoutDetails.setVisibility(View.GONE);
                    }*/

                    Intent intent = new Intent(context, BookingDetailsActivity.class);
                    intent.putExtra("details", object.toString());
                    intent.putExtra("role", "seller");
                    activity.startActivityForResult(intent,99);
                }
            });

            if (object.getString("book_status").equalsIgnoreCase("Cancelled")) {
                holder.buttonCancel.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void updateList(ArrayList<JSONObject> jsonObjectArrayList) {
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewRoom, textViewOrderDate, textViewStatus, textViewFrom, textViewTo, textViewAmt, textViewOrderId;
        ImageView imageViewRoom;
        LinearLayout layoutDetails;
        Button buttonCancel;


        public MyViewHolder(View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderID);

            textViewRoom = itemView.findViewById(R.id.textViewRoom);
            textViewFrom = itemView.findViewById(R.id.textViewCheckInDate);
            textViewTo = itemView.findViewById(R.id.textViewCheckOutDate);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            textViewAmt = itemView.findViewById(R.id.textViewAmount);
            imageViewRoom = itemView.findViewById(R.id.imageViewRoom);
            layoutDetails = itemView.findViewById(R.id.layoutMore);
            buttonCancel = itemView.findViewById(R.id.buttonCancel);
        }
    }
}
