package com.coasapp.coas.general;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.List;

public class  PayoutAdapter extends RecyclerView.Adapter<PayoutAdapter.MyViewHolder> implements APPConstants {

    List<JSONObject> listPayout;
    Activity activity;
    Context context;
    String type = "";

    public PayoutAdapter(List<JSONObject> listPayout, Activity activity, String type) {

        this.listPayout = listPayout;
        this.activity = activity;
        context = activity.getApplicationContext();
        this.type = type;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_payout_history, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        JSONObject object = listPayout.get(position);

        try {
            switch (type) {
                case "rent":
                    holder.textViewAmt.setText(formatter.format(Double.valueOf(object.getString("book_receivable"))));
                    holder.textViewOrderId.setText(object.getString("book_ref"));
                    holder.textViewOrderName.setText(object.getString("room_title"));
                    break;
                case "shop":
                    holder.textViewAmt.setText(formatter.format(Double.valueOf(object.getString("order_amt_receivable"))));
                    holder.textViewOrderId.setText(object.getString("order_track_id"));
                    holder.textViewOrderName.setText(object.getString("pro_name"));

                    break;
                case "bargain":
                    holder.textViewAmt.setText(formatter.format(Double.valueOf(object.getString("bargain_receivable"))));
                    holder.textViewOrderId.setText(object.getString("bargain_ref"));
                    holder.textViewOrderName.setText(object.getString("model_name"));

                    break;
            }

            JSONArray arrayTicket = object.getJSONArray("ticket");
            switch (object.getString("trans_status")) {
                case "Pending":
                    holder.textViewDate.setVisibility(View.GONE);
                    holder.buttonTicket.setVisibility(View.VISIBLE);
                    if (arrayTicket.length() > 0) {
                        //holder.buttonTicket.setEnabled(false);
                        holder.buttonTicket.setText("Ticket Raised");
                    } else {
                        //holder.buttonTicket.setEnabled(true);
                        holder.buttonTicket.setText("Raise Ticket");
                    }
                    holder.textViewStatus.setTextColor(Color.RED);

                    break;
                case "Success":
                    holder.textViewDate.setVisibility(View.VISIBLE);
                    holder.buttonTicket.setVisibility(View.GONE);

                    holder.textViewStatus.setTextColor(Color.parseColor("#2E7D32"));
                    holder.textViewDate.setText("on\n" + sdfNativeDateTime.format(sdfDatabaseDateTime.parse(object.getString("trans_date"))));

                    break;
                case "Processing":

                    holder.buttonTicket.setVisibility(View.GONE);

                    holder.textViewDate.setVisibility(View.VISIBLE);
                    holder.textViewDate.setText("as on\n" + sdfNativeDateTime.format(sdfDatabaseDateTime.parse(object.getString("trans_date"))));

                    holder.textViewStatus.setTextColor(Color.parseColor("#EF6C00"));
                    break;
                case "Failed":
                    holder.buttonTicket.setVisibility(View.VISIBLE);

                    holder.textViewDate.setVisibility(View.GONE);
                    holder.textViewStatus.setTextColor(Color.RED);
                    break;
                default:
                    holder.buttonTicket.setVisibility(View.GONE);
                    break;

            }
            if (object.getString("trans_date").equals("0000-00-00 00:00:00"))
                holder.textViewDate.setVisibility(View.GONE);
            else {

                holder.textViewDate.setVisibility(View.VISIBLE);
            }
            holder.textViewStatus.setText(object.getString("trans_status"));
            holder.buttonTicket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPayoutListItemClick.onRaiseButtonClick(holder.getAdapterPosition());
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
        return listPayout.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDate, textViewOrderId, textViewAmt, textViewStatus, textViewOrderName;
        Button buttonTicket;

        public MyViewHolder(View itemView) {
            super(itemView);
            textViewAmt = itemView.findViewById(R.id.textViewAmount);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderID);
            textViewOrderName = itemView.findViewById(R.id.textViewOrderName);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            buttonTicket = itemView.findViewById(R.id.buttonTicket);
        }
    }

    OnPayoutListItemClick onPayoutListItemClick;

    public interface OnPayoutListItemClick {
        public void onRaiseButtonClick(int i);
    }

    public void setOnPayoutListItemClick(OnPayoutListItemClick onPayoutListItemClick) {
        this.onPayoutListItemClick = onPayoutListItemClick;
    }
}
