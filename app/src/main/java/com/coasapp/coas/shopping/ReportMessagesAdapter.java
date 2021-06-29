package com.coasapp.coas.shopping;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.List;

public class ReportMessagesAdapter extends RecyclerView.Adapter<ReportMessagesAdapter.VH> implements APPConstants {

    Activity activity;
    List<JSONObject> listReportMsg;
    String userId;

    public ReportMessagesAdapter(Activity activity, List<JSONObject> listReportMsg) {
        this.activity = activity;
        this.listReportMsg = listReportMsg;
        SharedPreferences sharedPreferences = activity.getSharedPreferences(APP_PREF, 0);
        userId = sharedPreferences.getString("userId", "0");
    }


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_report, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        JSONObject object = listReportMsg.get(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 8, 8, 8);
        try {
            if (object.getString("report_user_id").equalsIgnoreCase(userId)) {
                params.gravity = Gravity.RIGHT;
                holder.cardViewMessage.setBackgroundColor(Color.RED);
            } else {
                params.gravity = Gravity.LEFT;
                holder.cardViewMessage.setBackgroundColor(Color.DKGRAY);

            }
            holder.cardViewMessage.setLayoutParams(params);
            holder.cardViewMessage.setRadius(4.0f);
            holder.textViewTime.setText(sdfNativeDateTime.format(sdfDatabaseDateTime.parse(object.getString("report_date"))));
            holder.textViewMessage.setText(object.getString("report_message"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listReportMsg.size();
    }

    class VH extends RecyclerView.ViewHolder {
        CardView cardViewMessage;
        TextView textViewMessage, textViewTime;

        public VH(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            cardViewMessage = itemView.findViewById(R.id.cardViewMsg);
        }
    }
}
