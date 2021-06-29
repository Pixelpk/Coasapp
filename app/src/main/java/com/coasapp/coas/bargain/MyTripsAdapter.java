package com.coasapp.coas.bargain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.general.ViewLocationActivity;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MyTripsAdapter extends RecyclerView.Adapter<MyTripsAdapter.VH> implements APPConstants {

    List<JSONObject> arrayListTrips;
    Activity activity;
    Context context;


    public MyTripsAdapter(List<JSONObject> arrayListTrips, Activity activity, Context context) {
        this.arrayListTrips = arrayListTrips;
        this.activity = activity;
        this.context = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_trips, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {

        JSONObject object = arrayListTrips.get(position);
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdfSource = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat sdfSource1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdfSource1.setTimeZone(TimeZone.getTimeZone("GMT"));
            //Date time = sdfSource.parse(object.getString("bargain_time"));
            Date date1 = sdfSource1.parse(object.getString("bargain_date"));
            SimpleDateFormat sdfDestination = new SimpleDateFormat("h:mm a");
            SimpleDateFormat sdfDestination1 = new SimpleDateFormat("MM-dd-yyyy h:mm a");
            sdfDestination1.setTimeZone(TimeZone.getDefault());
            //parse the date into another format
            //String strTime = sdfDestination.format(time);
            String strDate = sdfDestination1.format(date1);
            Button buttonCancel = holder.itemView.findViewById(R.id.buttonCancel);
            if (object.getString("bargain_status").equalsIgnoreCase("accepted")
                    && object.getString("paid").equalsIgnoreCase("1")) {

                buttonCancel.setVisibility(View.VISIBLE);
            } else {
                buttonCancel.setVisibility(View.GONE);
            }
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAdapterViewsClicked.onCancelClicked(holder.getAdapterPosition());
                }
            });
            APPHelper.showLog("date", date1 + " ");

            String currentTime = sdfDatabaseDateTime.format(calendar.getTime());
            long currentUnix = APPHelper.getUnixTime(currentTime);
            long tripUnix = APPHelper.getUnixTime(object.getString("bargain_date"));
            Log.i("TimeDiff", "" + (tripUnix + " " + object.getString("bargain_date")));
            Log.i("TimeDiff", "" + (currentUnix + " " + currentTime));
            Log.i("TimeDiff", "" + (currentUnix - tripUnix));
            /*if (currentUnix < tripUnix) {
                if (object.getString("bargain_status").equals("pickup")) {
                    holder.textViewStatus.setText(object.getString("bargain_status") + " " + sdfDestination1.format(sdfSource1.parse(object.getString("pickup_time"))));
                } else if (object.getString("bargain_status").equalsIgnoreCase("dropoff")) {
                    holder.textViewStatus.setText(object.getString("bargain_status") + " " + sdfDestination1.format(sdfSource1.parse(object.getString("dropoff_time"))));

                } else
                    holder.textViewStatus.setText(object.getString("bargain_status"));

            } else {

                if (object.getString("bargain_status").equals("Requested")||object.getString("bargain_status").equals("Pending")) {
                    if (currentUnix - tripUnix >= 3600)
                        holder.textViewStatus.setText("Expired");
                    else
                        holder.textViewStatus.setText(object.getString("bargain_status"));
                } else {
                    if (object.getString("bargain_status").equals("pickup")) {
                        holder.textViewStatus.setText(object.getString("bargain_status") + " " + sdfDestination1.format(sdfSource1.parse(object.getString("pickup_time"))));
                    } else if (object.getString("bargain_status").equalsIgnoreCase("dropoff")) {
                        holder.textViewStatus.setText(object.getString("bargain_status") + " " + sdfDestination1.format(sdfSource1.parse(object.getString("dropoff_time"))));

                    } else
                        holder.textViewStatus.setText(object.getString("bargain_status"));

                }
            }*/
            if (object.getString("bargain_status").equals("pickup")) {
                holder.textViewStatus.setText("In Transit");
            } else if (object.getString("bargain_status").equalsIgnoreCase("dropoff")) {
                holder.textViewStatus.setText(object.getString("bargain_status") + " " + sdfDestination1.format(sdfSource1.parse(object.getString("dropoff_time"))));

            } else
                holder.textViewStatus.setText(object.getString("bargain_status"));
            holder.textViewDate.setText(strDate);
            holder.textViewAmt.setText(object.getString("bargain_ref"));
            holder.textViewAddress.setText(object.getString("bargain_source"));
            holder.textViewAddress2.setText(object.getString("bargain_dest"));
            holder.textViewVehicle.setText(object.getString("model_name") + " - " + object.getString("name"));
            /*if (object.getString("bargain_status").equalsIgnoreCase("dropoff")) {
                if (Double.parseDouble(object.getString("bargain_rating")) > 0) {
                    holder.layoutRating.setVisibility(View.VISIBLE);
                    holder.textViewRating.setVisibility(View.GONE);
                } else {
                    //holder.layoutRating.setVisibility(View.GONE);
                    holder.textViewRating.setVisibility(View.VISIBLE);
                }


            } else {
                holder.textViewRating.setVisibility(View.GONE);
                holder.layoutRating.setVisibility(View.GONE);
            }

            holder.textViewRating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTextViewRatingClicked.onRatingClicked(holder.getAdapterPosition());
                }
            });*/
            holder.itemView.findViewById(R.id.imageViewDir).setOnClickListener(v -> {
                Intent intent = new Intent(context, ViewLocationActivity.class);
                try {
                    intent.putExtra("driverId", object.getString("bargain_driver_id"));
                    intent.putExtra("detail", object.toString());
                    intent.putExtra("lat1", object.getString("source_lat"));
                    intent.putExtra("lng1", object.getString("source_lng"));
                    intent.putExtra("lat2", object.getString("dest_lat"));
                    intent.putExtra("lng2", object.getString("dest_lng"));

                    intent.putExtra("role", "customer");

                    activity.startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            holder.itemView.setOnClickListener(v -> onItemSelected.onItemSelected(holder.getAdapterPosition()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    public interface OnTextViewRatingClicked {
        void onRatingClicked(int i);
    }

    OnTextViewRatingClicked onTextViewRatingClicked;

    public void setOnTextViewRatingClicked(OnTextViewRatingClicked onTextViewRatingClicked) {
        this.onTextViewRatingClicked = onTextViewRatingClicked;
    }

    public interface OnImageSelected {
        void onClick(int position, String s);
    }

    public interface OnItemSelected {
        void onItemSelected(int position);
    }

    OnItemSelected onItemSelected;
    OnImageSelected onImageSelected;


    public void setOnItemSelected(OnItemSelected onItemSelected) {
        this.onItemSelected = onItemSelected;
    }


    public void setOnAdapterViewsClicked(OnAdapterViewsClicked onAdapterViewsClicked) {
        this.onAdapterViewsClicked = onAdapterViewsClicked;
    }

    OnAdapterViewsClicked onAdapterViewsClicked;

    public interface OnAdapterViewsClicked {
        void onCancelClicked(int i);
    }


    @Override
    public int getItemCount() {
        return arrayListTrips.size();
    }

    class VH extends RecyclerView.ViewHolder {

        TextView textViewAmt, textViewStatus, textViewVehicle, textViewDate, textViewAddress, textViewAddress2, textViewRating;
        LinearLayout layoutRating;
        RatingBar ratingBarDriver;

        public VH(View itemView) {
            super(itemView);
            //layoutRating = itemView.findViewById(R.vehicleId.layoutMyRating);
            // ratingBarDriver = itemView.findViewById(R.vehicleId.ratingBar);
            textViewAddress2 = itemView.findViewById(R.id.textViewAddress2);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewAmt = itemView.findViewById(R.id.textViewAmount);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewVehicle = itemView.findViewById(R.id.textViewVehicle);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }
}
