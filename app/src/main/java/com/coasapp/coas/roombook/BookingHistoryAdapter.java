package com.coasapp.coas.roombook;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.MyViewHolder> implements APPConstants {

    ArrayList<JSONObject> arrayList;
    Context context;
    Activity activity;

    public BookingHistoryAdapter(ArrayList<JSONObject> arrayList, Context context, Activity activity) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_booking_history, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd-yyyy H:mm a");
        sdf.setTimeZone(TimeZone.getDefault());
        try {
            final JSONObject object = arrayList.get(position);
            Glide.with(context).load(R.drawable.placeholder).into(holder.imageViewRoom);
            holder.textViewRoom.setText(object.getString("room_title"));

            Date date1 = new java.util.Date(Long.parseLong(object.getString("book_checkin")) * 1000L);
            Date date2 = new java.util.Date(Long.parseLong(object.getString("book_checkout")) * 1000L);

            holder.textViewFrom.setText(sdfNativeDateTime.format(date1));
            holder.textViewTo.setText(sdfNativeDateTime.format(date2));
            SimpleDateFormat simpleDateFormat1 = ((ApplozicSampleApplication)activity.getApplication()).getSdfDatabaseFormat();

            Date dateBook = simpleDateFormat1.parse(object.getString("book_date"));
            SimpleDateFormat simpleDateFormat2 = ((ApplozicSampleApplication)activity.getApplication()).getSdfNativeDevice();

            holder.textViewOrderDate.setText(simpleDateFormat2.format(dateBook));
            holder.textViewFrom.setText(sdfNativeDateTime.format(date1));
            holder.textViewTo.setText(sdfNativeDateTime.format(date2));

            Calendar calendar = Calendar.getInstance();
            String currentDate = simpleDateFormat1.format(calendar.getTime());
            long currentUnix = APPHelper.getUnixTimeZone(currentDate, object.getString("room_timezone"));
            if (currentUnix < Long.parseLong(object.getString("book_checkin"))) {
                holder.buttonCancel.setVisibility(View.VISIBLE);
            } else {
                holder.buttonCancel.setVisibility(View.GONE);
            }
            holder.textViewStatus.setText(object.getString("book_status"));
            holder.textViewAmt.setText(formatter.format(Double.valueOf(object.getString("book_amount"))));
            holder.textViewOrderId.setText("Booking Id: " + object.getString("book_ref"));
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
                    intent.putExtra("role", "buyer");

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
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    JSONObject object = arrayList.get(getAdapterPosition());
                                    try {
                                        new CancelBooking(getAdapterPosition()).execute(object.getString("book_id"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    builder.setMessage("Please contact COASAPP").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                            .show();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage("Reviewed client’s cancellation regulation in the USER TERMS & CONDITIONS?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }
            });
        }
    }

    class CancelBooking extends AsyncTask<String, Void, String> {

        int i;

        CancelBooking(int i) {
            this.i = i;
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("book_id", strings[0]);
            return new RequestHandler().sendPostRequest(MAIN_URL + "cancel_booking.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equals("1")) {
                    arrayList.get(i).put("book_status", "Cancelled");
                    notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
