package com.coasapp.coas.connectycube.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.MyPrefs;
import com.coasapp.coas.utils.OnItemClick;
import com.connectycube.chat.ConnectycubeChatService;
import com.connectycube.chat.ConnectycubeRestChatService;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.core.request.PagedRequestBuilder;
import com.connectycube.messenger.api.ConnectycubeService;
import com.connectycube.messenger.data.AppDatabase;
import com.connectycube.messenger.data.User;
import com.connectycube.messenger.utilities.SharedPreferencesManager;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.ViewHolder> implements APPConstants {

    Context context;
    Activity activity;
    List<JSONObject> list;

    public CallHistoryAdapter(Context context, Activity activity, List<JSONObject> list) {
        this.context = context;
        this.activity = activity;
        this.list = list;
    }

    public OnItemClick getOnItemClick() {
        return onItemClick;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    OnItemClick onItemClick;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(APPHelper.getItemView(parent, R.layout.row_call_history));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        JSONObject object = list.get(position);
        try {

            if (object.getString("call_direction").equalsIgnoreCase("in")) {
                if (object.getString("call_incoming_status").equalsIgnoreCase("missed")) {
                    holder.imageViewCallDir.setImageResource(R.mipmap.missedcall);
                } else {
                    holder.imageViewCallDir.setImageResource(R.mipmap.incomingcall);
                }
            } else {
                holder.imageViewCallDir.setImageResource(R.mipmap.outgoingcall);
            }


            if (object.getInt("call_type") == 2) {
                holder.textViewCallType.setText("Voice");
            } else {
                holder.textViewCallType.setText("Video");
            }

            List<User> userList = new ArrayList<>();
            try {
                userList.clear();
                JSONObject objectUsers = new JSONObject(object.getString("call_users_data"));
                List<Integer> integerList = new ArrayList<>();
                Log.i("UsersListC", objectUsers.toString());
                Log.i("UsersListC", objectUsers.names().toString());
                if (objectUsers.names().length() > 0) {
                    for (int i = 0; i < objectUsers.names().length(); i++) {
                        integerList.add(Integer.parseInt(objectUsers.names().getString(i)));
                    }
                }

                PagedRequestBuilder pagedRequestBuilder = new PagedRequestBuilder();
                pagedRequestBuilder.setPage(1);
                pagedRequestBuilder.setPerPage(integerList.size());
                Log.i("UsersList", new Gson().toJson(integerList));
                Bundle params = new Bundle();
                ConnectycubeUsers.getUsersByIDs(integerList, pagedRequestBuilder, params).performAsync(new EntityCallback<ArrayList<ConnectycubeUser>>() {
                    @Override
                    public void onSuccess(ArrayList<ConnectycubeUser> connectycubeUsers, Bundle bundle) {
                        ArrayList<ConnectycubeUser> connectycubeUserArrayList = new ArrayList<>();
                        String names = "";
                        for (int i = 0; i < connectycubeUsers.size(); i++) {
                            String name = APPHelper.getContactName(context, connectycubeUsers.get(i).getPhone(), connectycubeUsers.get(i).getFullName());
                            if (SharedPreferencesManager.Companion.getInstance(context).getCurrentUser().getId().equals(connectycubeUsers.get(i).getId()))
                                name = "You";
                            names = names + name + ",";
                        }
                        holder.textViewCallWith.setText(APPHelper.removeLastChar(names));
                    }

                    @Override
                    public void onError(ResponseException e) {

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

            class UsersTask extends AsyncTask<Void, Void, List<User>> {

                @Override
                protected List<User> doInBackground(Void... voids) {

                    List<User> userList = new ArrayList<>();
                    try {
                        userList.clear();
                        JSONObject objectUsers = new JSONObject(object.getString("call_users_data"));
                        List<Integer> integerList = new ArrayList<>();

                        if (object.names().length() > 0) {
                            for (int i = 0; i < object.names().length(); i++) {
                                integerList.add(Integer.parseInt(object.names().getString(i)));
                            }
                            userList.addAll(AppDatabase.Companion.getInstance(context).userDao().getUsersByIdsPvt(integerList, SharedPreferencesManager.Companion.getInstance(context).getCurrentUser().getId()));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return userList;
                }

                @Override
                protected void onPostExecute(List<User> users) {
                    super.onPostExecute(users);
                    String names = "";
                    for (int i = 0; i < users.size(); i++) {
                        names = names + users.get(i).getConUser().getFullName() + ",";
                    }
                    holder.textViewCallWith.setText(names);
                }
            }
            if (object.getString("call_time").length() > 0)
                holder.textViewCallDate.setText(sdfNativeDateTime.format(sdfDatabaseDateTime.parse(object.getString("call_time"))));
            else
                holder.textViewCallDate.setText("");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            holder.textViewCallDate.setText("");
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewCallDir;
        private TextView textViewCallWith;
        private TextView textViewCallDate;
        private TextView textViewCallType;

        /**
         * Find the Views in the layout<br />
         * <br />
         * Auto-created on 2020-01-23 16:05:25 by Android Layout Finder
         * (http://www.buzzingandroid.com/tools/android-layout-finder)
         */
        private void findViews(View rootView) {
            imageViewCallDir = (ImageView) rootView.findViewById(R.id.imageViewCallDir);
            textViewCallWith = (TextView) rootView.findViewById(R.id.textViewCallWith);
            textViewCallDate = (TextView) rootView.findViewById(R.id.textViewCallDate);
            textViewCallType = (TextView) rootView.findViewById(R.id.textViewCallType);
        }


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            findViews(itemView);
        }
    }
}
