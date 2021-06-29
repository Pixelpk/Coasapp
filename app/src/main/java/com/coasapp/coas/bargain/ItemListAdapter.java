package com.coasapp.coas.bargain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.bumptech.glide.Glide;
import com.coasapp.coas.utils.LocationHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.MyViewHolder> implements APPConstants {

    private List<JSONObject> ItemList;
    Activity activity;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, date, time, textViewAddress, textViewAddress2;
        CircleImageView circularImageView;
        Button buttonUpdate;
        Spinner spinner;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.text_name);
            date = (TextView) view.findViewById(R.id.text_date);
            time = (TextView) view.findViewById(R.id.text_time);
            buttonUpdate = view.findViewById(R.id.buttonUpdate);
            circularImageView = view.findViewById(R.id.circle_image);
            spinner = view.findViewById(R.id.spinnerTrip);
            textViewAddress2 = itemView.findViewById(R.id.textViewAddress2);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
        }
    }


    public ItemListAdapter(List<JSONObject> itemList, Activity swipeRefreshActivity, Context applicationContext) {
        this.ItemList = itemList;
        this.activity = swipeRefreshActivity;
        this.context = applicationContext;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_driver_trips, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        JSONObject object = ItemList.get(position);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdfSource = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat sdfSource1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String currentTime = sdfDatabaseDateTime.format(calendar.getTime());
            sdfSource1.setTimeZone(TimeZone.getTimeZone("GMT"));
            //Date time = sdfSource.parse(object.getString("bargain_time"));
            Date date = sdfSource1.parse(object.getString("bargain_date"));
            SimpleDateFormat sdfDestination = new SimpleDateFormat("h:mm a");
            SimpleDateFormat sdfDestination1 = new SimpleDateFormat("MM-dd-yyyy h:mm a");
            //parse the date into another format
            //String strTime = sdfDestination.format(time);
            sdfDestination1.setTimeZone(TimeZone.getDefault());
            String strDate1 = sdfDestination1.format(date);
            APPHelper.showLog("date", "" + date);
            holder.date.setText(strDate1);
            //holder.time.setText(strTime);
            long currentUnix = APPHelper.getUnixTime(currentTime);
            long tripUnix = APPHelper.getUnixTime(object.getString("bargain_date"));
            /*if (currentUnix < tripUnix) {
                holder.name.setText(object.getString("name") + " - " + object.getString("bargain_status"));

            } else {
                if (object.getString("bargain_status").equals("Requested")||object.getString("bargain_status").equals("Pending")) {
                    if (currentUnix - tripUnix >= 3600)
                        holder.name.setText(object.getString("name") + " - " + "Expired");
                    else
                        holder.name.setText(object.getString("name") + " - " + object.getString("bargain_status"));
                } else {
                    holder.name.setText(object.getString("name") + " - " + object.getString("bargain_status"));

                }
            }*/

            String status = object.getString("bargain_status");
            if(status.equalsIgnoreCase("pickup"))
                status="In transit";

            holder.name.setText(object.getString("name") + " - " + status);
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
            Glide.with(context).load(MAIN_URL_IMAGE + object.getString("image")).into(holder.circularImageView);

            holder.buttonUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.spinner.getSelectedItemPosition() == 0) {
                        APPHelper.showToast(context, "Select Valid Status");
                    } else {
                        onImageSelected.onClick(holder.getAdapterPosition(), holder.spinner.getSelectedItem().toString());

                    }
                }
            });
            holder.itemView.findViewById(R.id.imageViewDir).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!LocationHelper.checkPermissionLocation(context)) {
                        APPHelper.goToAppPage(activity, "Allow Location Permission");
                    } else if (!LocationHelper.checkGPS(context)) {
                        LocationHelper.goToLocationSettings(activity);

                    } else {
                        Intent intent = new Intent(context, TrackLocationActivity.class);
                        try {


                            intent.putExtra("driverId", object.getString("bargain_driver_id"));
                            intent.putExtra("detail", object.toString());
                            intent.putExtra("role", "driver");

                        } catch (JSONException e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        activity.startActivityForResult(intent, 99);
                    }

                }
            });

            holder.textViewAddress.setText(object.getString("bargain_source"));
            holder.textViewAddress2.setText(object.getString("bargain_dest"));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemSelected.onItemSelected(holder.getAdapterPosition());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
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

    public interface OnImageSelected {
        void onClick(int position, String s);
    }

    public interface OnItemSelected {
        void onItemSelected(int position);
    }

    OnItemSelected onItemSelected;
    OnImageSelected onImageSelected;


    public void setOnImageSelected(OnImageSelected onImageSelected) {
        APPHelper.showLog("Item", "button");

        this.onImageSelected = onImageSelected;
    }

    public void setOnItemSelected(OnItemSelected onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    @Override
    public int getItemCount() {
        return ItemList.size();
    }
}