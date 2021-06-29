package com.coasapp.coas.general;

import android.app.Activity;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coasapp.coas.ApplozicSampleApplication;
import com.coasapp.coas.R;
import com.coasapp.coas.utils.OnItemClick;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TicketListAdapter extends RecyclerView.Adapter<TicketListAdapter.ViewHolder> implements APPConstants {

    List<JSONObject> list;
    Activity activity;
    OnItemClick onItemClick;

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_ticket_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        JSONObject object = getList().get(position);
        try {
            holder.textViewNo.setText(object.getString("ticket_no"));
            holder.textViewTitle.setText(object.getString("ticket_title"));
            holder.textViewStatus.setText(object.getString("ticket_status"));
            if (object.getString("ticket_status").equalsIgnoreCase("closed")) {
                holder.textViewStatus.setTextColor(Color.parseColor("#43A047"));
            } else {
                holder.textViewStatus.setTextColor(Color.RED);


            }

            SimpleDateFormat simpleDateFormat1 = ((ApplozicSampleApplication) activity.getApplication()).getSdfDatabaseFormat();

            Date dateBook = simpleDateFormat1.parse(object.getString("ticket_created"));
            SimpleDateFormat simpleDateFormat2 = ((ApplozicSampleApplication) activity.getApplication()).getSdfNativeDevice();
            try {
                holder.textViewTime.setText(simpleDateFormat2.format(dateBook));
            } catch (Exception e) {
                holder.textViewTime.setText("" + dateBook);
                e.printStackTrace();
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick.onItemClick(holder.getAdapterPosition());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public List<JSONObject> getList() {
        return list;
    }

    public void setList(List<JSONObject> list) {
        this.list = list;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTime, textViewStatus, textViewNo, textViewTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewNo = itemView.findViewById(R.id.textViewTicketNo);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewStatus = itemView.findViewById(R.id.textViewTicketStatus);
        }
    }

}
