package com.coasapp.coas.connectycube.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.coasapp.coas.R;
import com.coasapp.coas.connectycube.data.MyConnectycubeUser;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.OnItemClick;
import com.connectycube.messenger.data.AppDatabase;
import com.connectycube.messenger.data.User;
import com.connectycube.users.model.ConnectycubeUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersList1Adapter extends RecyclerView.Adapter<UsersList1Adapter.ViewHolder> implements APPConstants {

    Context context;
    Activity activity;
    List<MyConnectycubeUser> list;
    OnItemClick onItemClick;

    public UsersList1Adapter(Context context, Activity activity, List<MyConnectycubeUser> list, OnItemClick onItemClick) {
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

        MyConnectycubeUser object = list.get(i);
        //viewHolder.itemView.findViewById(R.id.layoutRoot).setVisibility(View.GONE);
        /*Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {

               *//* activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.itemView.findViewById(R.id.layoutRoot).setVisibility(View.VISIBLE);
                    }
                });*//*

            }
        });*/


        class InsertUser extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                AppDatabase appDatabase = AppDatabase.Companion.getInstance(context);
               // appDatabase.userDao().insert(object);
                return null;
            }
        }

        InsertUser insertUser = new InsertUser();
       /* insertUser.cancel(true);
        insertUser.execute();*/

        Glide.with(context).load(object.getAvatar()).into(viewHolder.imageViewProfile);
        viewHolder.textViewUserName.setText(object.getStoredName());
        viewHolder.textViewCoasId.setText(object.getLogin());

        viewHolder.textViewCountry.setText(object.getPhone());

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
