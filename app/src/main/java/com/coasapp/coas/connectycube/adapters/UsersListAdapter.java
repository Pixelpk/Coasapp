package com.coasapp.coas.connectycube.adapters;

import android.app.Activity;
import android.content.Context;


import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.OnItemClick;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder> implements APPConstants {

    Context context;
    Activity activity;
    List<JSONObject> list;
    OnItemClick onItemClick;

    public UsersListAdapter(Context context, Activity activity, List<JSONObject> list, OnItemClick onItemClick) {
        this.context = context;
        this.activity = activity;
        this.list = list;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = APPHelper.getItemView(viewGroup, R.layout.row_users);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        JSONObject object = list.get(i);
        try {
            Glide.with(context).load(MAIN_URL_IMAGE+object.getString("image")).into(viewHolder.imageViewProfile);
            viewHolder.textViewUserName.setText(object.getString("name"));
            viewHolder.textViewCoasId.setText(object.getString("coas_id"));

            viewHolder.textViewCountry.setText(object.getString("phone"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.onItemClick(viewHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageViewProfile;
        private TextView textViewUserName;
        private TextView textViewCoasId;
        private TextView textViewCountry;

        /**
         * Find the Views in the layout<br />
         * <br />
         * Auto-created on 2019-09-23 11:31:48 by Android Layout Finder
         * (http://www.buzzingandroid.com/tools/android-layout-finder)
         */
        private void findViews(View rootView) {
            imageViewProfile = (CircleImageView) rootView.findViewById(R.id.imageViewProfile);
            textViewUserName = (TextView) rootView.findViewById(R.id.textViewUserName);
            textViewCoasId = (TextView) rootView.findViewById(R.id.textViewCoasId);
            textViewCountry = (TextView) rootView.findViewById(R.id.textViewCountry);
        }


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            findViews(itemView);
        }
    }
}
