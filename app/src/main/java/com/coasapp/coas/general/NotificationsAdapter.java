package com.coasapp.coas.general;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> implements APPConstants {

    Context context;
    ArrayList<JSONObject> arrayList;
    OnItemClick onItemClick;

    public NotificationsAdapter(Context context, ArrayList<JSONObject> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_notifications, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        int colorUnread = ContextCompat.getColor(context,R.color.black_or_white);
        int colorRead = ContextCompat.getColor(context,R.color.dark_or_light_grey);
        ImageView imageView = holder.imageViewNotifications;
        final TextView textViewTitle = holder.textViewTitle;
        final TextView textViewDesc = holder.textViewDesc;
        JSONObject object = arrayList.get(position);
        try {
            if (object.getString("image").equals("")) {
                Glide.with(context).load(R.mipmap.coas_icon192).into(imageView);
            } else {
                Glide.with(context).load(object.getString("image")).into(imageView);
            }
            textViewTitle.setText(object.getString("title"));
            textViewDesc.setText(object.getString("description"));
            SimpleDateFormat sdfSource1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdfSource1.setTimeZone(TimeZone.getTimeZone("GMT"));
            SimpleDateFormat sdfDestination1 = new SimpleDateFormat("MM-dd-yyyy h:mm a");
            sdfDestination1.setTimeZone(TimeZone.getDefault());
            holder.textViewTime.setText(sdfDestination1.format(sdfSource1.parse(object.getString("time"))));

            if (object.getString("unread").equals("1")) {

                textViewTitle.setTextColor(colorUnread);
                textViewDesc.setTextColor(colorUnread);
            } else {
                textViewTitle.setTextColor(colorRead);
                textViewDesc.setTextColor(colorRead);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.onItemClick(holder.getAdapterPosition());
                textViewTitle.setTextColor(colorRead);
                textViewDesc.setTextColor(colorRead);
            }
        });
    }

    public interface OnItemClick {
        void onItemClick(int position);
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewNotifications, imageViewBook;
        TextView textViewTitle, textViewTime, textViewDesc;

        ViewHolder(View itemView) {
            super(itemView);
            imageViewNotifications = itemView.findViewById(R.id.imageViewNotification);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewDesc = itemView.findViewById(R.id.textViewDesc);
        }
    }
}
