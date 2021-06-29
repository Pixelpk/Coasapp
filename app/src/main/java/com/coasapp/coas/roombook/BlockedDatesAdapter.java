package com.coasapp.coas.roombook;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BlockedDatesAdapter extends RecyclerView.Adapter<BlockedDatesAdapter.VH> implements APPConstants {

    ArrayList<JSONObject> arrayListDates;
    Activity activity;
    Context context;

    public BlockedDatesAdapter(ArrayList<JSONObject> arrayListDates, Activity activity, Context context) {
        this.arrayListDates = arrayListDates;
        this.activity = activity;
        this.context = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_booked_dates, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat format2 = new SimpleDateFormat("h a");
        JSONObject object = arrayListDates.get(position);
        APPHelper.showLog("block", String.valueOf(object));
        try {
            String fromHour = object.getString("from_hour");
            String toHour = object.getString("to_hour");
            Date date1 = format.parse(fromHour + ":00:00");
            int toH = Integer.valueOf(toHour);
if(toH==24){
    toH=0;
}
            Date date2 = format.parse(toH + ":00:00");
            Date date3 = sdfDatabaseDate.parse(object.getString("book_from_date"));
            Date date4 = sdfDatabaseDate.parse(object.getString("book_to_date"));
            holder.textViewFrom.setText(format2.format(date1));
            holder.textViewTo.setText(format2.format(date2));
            holder.textViewDate.setText(sdfNativeDate.format(date3));
            holder.textViewDateTo.setText(sdfNativeDate.format(date4));
            holder.buttonDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delClick.onDelClick(holder.getAdapterPosition());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public interface DelClick {
        void onDelClick(int i);
    }


    class VH extends RecyclerView.ViewHolder {

        TextView textViewDate,textViewDateTo, textViewFrom, textViewTo, textViewDelete;
        Button buttonDel;

        public VH(View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewDateTo = itemView.findViewById(R.id.textViewDateTo);
            textViewFrom = itemView.findViewById(R.id.textViewFrom);
            textViewTo = itemView.findViewById(R.id.textViewTo);
            buttonDel = itemView.findViewById(R.id.buttonDelete);
        }
    }

    DelClick delClick;

    public void setDelClick(DelClick delClick) {
        this.delClick = delClick;
    }

    @Override
    public int getItemCount() {
        return arrayListDates.size();
    }
}
