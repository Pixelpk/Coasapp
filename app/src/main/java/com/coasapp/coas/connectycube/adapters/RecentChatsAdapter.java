package com.coasapp.coas.connectycube.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.coasapp.coas.R;
import com.coasapp.coas.roombook.AddRoomActivity;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.DatabaseHandler;
import com.coasapp.coas.utils.OnItemClick;
import com.coasapp.coas.utils.OnItemLongClickListener;
import com.connectycube.chat.model.ConnectycubeChatDialog;
import com.connectycube.messenger.data.AppDatabase;
import com.connectycube.messenger.data.Chat;
import com.connectycube.messenger.data.User;
import com.connectycube.messenger.data.UserRepository;
import com.connectycube.users.model.ConnectycubeUser;
import com.google.gson.Gson;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.connectycube.messenger.utilities.SharedPreferencesManagerKt.CUBE_USER_ID;

public class RecentChatsAdapter extends RecyclerView.Adapter<RecentChatsAdapter.ViewHolder> implements APPConstants {

    Context context;
    Activity activity;
    public OnItemClick onItemClick;
    List<ConnectycubeChatDialog> chatDialogList;
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;

    public OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public OnItemLongClickListener onItemLongClickListener;


    public boolean isHome = true;


    public RecentChatsAdapter(Context context, Activity activity, List<ConnectycubeChatDialog> chatDialogList, OnItemClick onItemClick) {
        this.context = context;
        this.activity = activity;
        this.onItemClick = onItemClick;
        this.chatDialogList = chatDialogList;
        databaseHandler = new DatabaseHandler(context);
        sqLiteDatabase = databaseHandler.getReadableDatabase();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(APPHelper.getItemView(parent, R.layout.list_item_chat_dialog_recents));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ConnectycubeChatDialog chatDialog = chatDialogList.get(position);
        //holder.itemView.findViewById(R.id.layoutRoot).setVisibility(View.GONE);

        Chat chat = new Chat(chatDialog.getDialogId(), chatDialog.getLastMessageDateSent(), chatDialog.getCreatedAt().getTime(), chatDialog.getUpdatedAt().getTime(), chatDialog.getUnreadMessageCount(), chatDialog.getName(), chatDialog);
       /* Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase appDatabase = AppDatabase.Companion.getInstance(context);
                appDatabase.chatDao().insert(chat);
               *//* activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.itemView.findViewById(R.id.layoutRoot).setVisibility(View.VISIBLE);
                    }
                });*//*

            }
        });*/

        class InsertUser extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                AppDatabase appDatabase = AppDatabase.Companion.getInstance(context);
                appDatabase.chatDao().insert(chat);
                return null;
            }
        }

        Log.i("ChatDialog", new Gson().toJson(chatDialog));

        holder.lastMessageTextView.setText(chatDialog.getLastMessage());
        if (chatDialog.getLastMessageDateSent() > 0)
            holder.lastMasageDateTextView.setText(sdfNativeDateTime.format(new Date(chatDialog.getLastMessageDateSent() * 1000L)));
        else
            holder.lastMasageDateTextView.setText(sdfNativeDateTime.format(chatDialog.getCreatedAt()));
        if (chatDialog.getUnreadMessageCount() == 0) {
            holder.unreadMessageCountTextView.setVisibility(View.GONE);
        } else {
            holder.unreadMessageCountTextView.setVisibility(View.VISIBLE);
        }
        holder.unreadMessageCountTextView.setText("" + chatDialog.getUnreadMessageCount());

        setData(holder, chatDialog, position);

        class UsersTask extends AsyncTask<Void, Void, List<User>> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected List<User> doInBackground(Void... voids) {
                UserRepository userRepository = UserRepository.Companion.getInstance(AppDatabase.Companion.getInstance(context).userDao());
                return userRepository.getUsersByIdsPvt(chatDialog.getOccupants(), context.getSharedPreferences(APP_PREF, 0).getInt(CUBE_USER_ID, 0));
            }

            @Override
            protected void onPostExecute(List<User> users) {
                super.onPostExecute(users);
                if (users.size() > 0) {
                    ConnectycubeUser connectycubeUser = users.get(0).getConUser();
                    holder.nameTextViw.setText(APPHelper.getContactName(context, connectycubeUser.getPhone(), connectycubeUser.getFullName()));
                    Log.i("ChatDialogUserYes " + position, "" + users.get(0).getConUser());
                    String image = connectycubeUser.getAvatar(), imageToLoad = "";
                    if (image != null) {
                        if (image.startsWith("profile")) {
                            imageToLoad = MAIN_URL_IMAGE + image;
                        } else {
                            imageToLoad = image;
                        }
                    }
                    Glide.with(context).load(imageToLoad).error(R.drawable.ic_avatar_placeholder).into(holder.avatarImageView);
                }
            }
        }
        if (chatDialog.isPrivate()) {
            new UsersTask().execute();
        } else {
            setData(holder, chatDialog, position);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.onItemClick(holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isHome) {
                    PopupMenu popupMenu = new PopupMenu(activity, v);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_dialog_delete, popupMenu.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            if (id == R.id.action_delete) {
                                onItemLongClickListener.onLongClick(holder.getAdapterPosition());
                            }
                            return true;
                        }
                    });

                    popupMenu.show();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatDialogList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatarImageView;
        private TextView nameTextViw;
        private TextView lastMessageTextView;
        private TextView lastMasageDateTextView;
        private TextView unreadMessageCountTextView;

        /**
         * Find the Views in the layout<br />
         * <br />
         * Auto-created on 2020-01-02 17:19:13 by Android Layout Finder
         * (http://www.buzzingandroid.com/tools/android-layout-finder)
         */
        private void findViews(View rootView) {
            avatarImageView = (ImageView) rootView.findViewById(R.id.avatar_image_view);
            nameTextViw = (TextView) rootView.findViewById(R.id.name_text_viw);
            lastMessageTextView = (TextView) rootView.findViewById(R.id.last_message_text_view);
            lastMasageDateTextView = (TextView) rootView.findViewById(R.id.last_masage_date_text_view);
            unreadMessageCountTextView = (TextView) rootView.findViewById(R.id.unread_message_count_text_view);
        }


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            findViews(itemView);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    void setData(ViewHolder holder, ConnectycubeChatDialog chatDialog, int position) {
        String image = chatDialog.getPhoto(), imageToLoad = "";

        holder.nameTextViw.setText(chatDialog.getName());
        Log.i("ChatDialogG " + position, "" + (image != null));
        if (image != null && !image.equals("")) {
            if (image.startsWith("profile")) {
                imageToLoad = MAIN_URL_IMAGE + image;
            } else {
                imageToLoad = image;
            }
            Glide.with(context).load(imageToLoad).error(R.drawable.ic_avatar_placeholder).into(holder.avatarImageView);
        } else {
            Glide.with(context).load(R.drawable.ic_avatar_placeholder).into(holder.avatarImageView);
        }
    }


}
