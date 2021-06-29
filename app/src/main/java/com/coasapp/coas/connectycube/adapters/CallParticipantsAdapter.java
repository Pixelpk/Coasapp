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
import com.coasapp.coas.utils.APPHelper;
import com.connectycube.messenger.utilities.SharedPreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallParticipantsAdapter extends RecyclerView.Adapter<CallParticipantsAdapter.ViewHolder> {

    Context context;
    Activity activity;
    List<JSONObject> list;

    public CallParticipantsAdapter(Context context, Activity activity, List<JSONObject> list) {
        this.context = context;
        this.activity = activity;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(APPHelper.getItemView(parent, R.layout.row_call_participants));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        JSONObject object = list.get(position);
        try {
            if (object.getInt("user_id") == SharedPreferencesManager.Companion.getInstance(context).getCurrentUser().getId())

                holder.textViewParticipantName.setText("You");
            else
                holder.textViewParticipantName.setText(object.getString("name"));
            holder.textViewCallStatus.setText(object.getString("status"));
            if (!object.getString("image").equals(""))
                Glide.with(context).load(object.getString("image")).into(holder.imageViewProfile);
            else
                Glide.with(context).load(R.drawable.ic_avatar_placeholder).into(holder.imageViewProfile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imageViewProfile;
        private TextView textViewParticipantName;
        private TextView textViewCallStatus;

        /**
         * Find the Views in the layout<br />
         * <br />
         * Auto-created on 2020-01-24 09:41:31 by Android Layout Finder
         * (http://www.buzzingandroid.com/tools/android-layout-finder)
         */
        private void findViews(View rootView) {
            imageViewProfile = (CircleImageView) rootView.findViewById(R.id.imageViewProfile);
            textViewParticipantName = (TextView) rootView.findViewById(R.id.textViewParticipantName);
            textViewCallStatus = (TextView) rootView.findViewById(R.id.textViewCallStatus);
        }


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            findViews(itemView);
        }
    }
}
