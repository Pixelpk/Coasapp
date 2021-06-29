package com.coasapp.coas.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.coasapp.coas.connectycube.data.MyConnectycubeUser;
import com.connectycube.chat.ConnectycubeRestChatService;
import com.connectycube.chat.model.ConnectycubeChatDialog;
import com.connectycube.chat.model.ConnectycubeDialogType;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.core.request.PagedRequestBuilder;
import com.connectycube.messenger.ChatMessageActivity;
import com.connectycube.messenger.data.AppDatabase;
import com.connectycube.messenger.data.Chat;
import com.connectycube.messenger.data.User;
import com.connectycube.messenger.utilities.SharedPreferencesManager;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.connectycube.messenger.ChatMessageActivityKt.EXTRA_CHAT;

public class LaunchChatUtils {
    AddUser addUser;
    Context context;
    Activity activity;
    LaunchChatCallbacks chatCallbacks;
    InsertChat1 insertChat1;
    List<User> userList = new ArrayList<>();
    AppDatabase appDatabase;

    public LaunchChatUtils(Context context, Activity activity, LaunchChatCallbacks chatCallbacks) {
        this.context = context;
        this.activity = activity;
        this.chatCallbacks = chatCallbacks;
        appDatabase = AppDatabase.Companion.getInstance(context);
    }

    public void createChatDialog(String login) {

        ConnectycubeUsers.getUserByLogin(login).performAsync(new EntityCallback<ConnectycubeUser>() {
            @Override
            public void onSuccess(ConnectycubeUser connectycubeUser, Bundle bundle) {
                        /*if (!usersAll.get(i).equals(sh.getString("coasId", ""))) {

                        }*/
                ArrayList<Integer> occupantIds = new ArrayList<Integer>();
                occupantIds.add(connectycubeUser.getId());

                ConnectycubeChatDialog dialog = new ConnectycubeChatDialog();
                dialog.setType(ConnectycubeDialogType.PRIVATE);
                dialog.setOccupantsIds(occupantIds);
                Log.i("ChatDialogIns", new Gson().toJson(connectycubeUser));


                userList.add(new User(connectycubeUser.getId(), connectycubeUser.getLogin(), connectycubeUser.getFullName(), connectycubeUser));

                //or just use DialogUtils
                //ConnectycubeChatDialog dialog = DialogUtils.buildPrivateDialog(recipientId);

                ConnectycubeRestChatService.createChatDialog(dialog).performAsync(new EntityCallback<ConnectycubeChatDialog>() {
                    @Override
                    public void onSuccess(ConnectycubeChatDialog createdDialog, Bundle params) {

                        Log.i("ChatDialogId", new Gson().toJson(createdDialog));
                        if (createdDialog.getUnreadMessageCount() == null) {
                            createdDialog.setUnreadMessageCount(0);
                        }
                        if (createdDialog.getCreatedAt() == null) {
                            createdDialog.setCreatedAt(new Date());
                        }
                        if (createdDialog.getUpdatedAt() == null) {
                            createdDialog.setUpdatedAt(new Date());
                        }
                        createdDialog.setName(connectycubeUser.getFullName());
                       /* insertChat = new InsertChat();
                        insertChat.setChatCallbacks(chatCallbacks);
                        insertChat.execute(createdDialog);*/

                        insertChatContact(createdDialog, false);
                    }

                    @Override
                    public void onError(ResponseException exception) {
                        chatCallbacks.onChatCreatedError();
                        Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onError(ResponseException e) {
                chatCallbacks.onChatCreatedError();
            }
        });
    }

    public void createChatDialogUser(MyConnectycubeUser user) {
        ArrayList<Integer> occupantIds = new ArrayList<Integer>();
        occupantIds.add(user.getId());

        ConnectycubeChatDialog dialog = new ConnectycubeChatDialog();
        dialog.setType(ConnectycubeDialogType.PRIVATE);
        dialog.setOccupantsIds(occupantIds);
        dialog.setName(user.getFullName());
        dialog.setPhoto(user.getAvatar());
        Log.i("ChatDialogIns", new Gson().toJson(user));


        //or just use DialogUtils
        //ConnectycubeChatDialog dialog = DialogUtils.buildPrivateDialog(recipientId);

        ConnectycubeRestChatService.createChatDialog(dialog).performAsync(new EntityCallback<ConnectycubeChatDialog>() {
            @Override
            public void onSuccess(ConnectycubeChatDialog createdDialog, Bundle params) {
                createdDialog.setName(user.getFullName());
                Log.i("ChatDialogId", new Gson().toJson(createdDialog));
                if (createdDialog.getUnreadMessageCount() == null) {
                    createdDialog.setUnreadMessageCount(0);
                }
                if (createdDialog.getCreatedAt() == null) {
                    createdDialog.setCreatedAt(new Date());
                }
                if (createdDialog.getUpdatedAt() == null) {
                    createdDialog.setUpdatedAt(new Date());
                }

                createdDialog.setName(user.getFullName());
                       /* insertChat = new InsertChat();
                        insertChat.setChatCallbacks(chatCallbacks);
                        insertChat.execute(createdDialog);*/
                boolean isContact = APPHelper.isContact(context, APPHelper.getOtherUser(context, createdDialog));

                insertChatContact(createdDialog, isContact);
            }

            @Override
            public void onError(ResponseException exception) {
                chatCallbacks.onChatCreatedError();
                Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    public void createChatDialogExisting(ConnectycubeChatDialog chatDialog) {
        boolean isContact = APPHelper.isContact(context, APPHelper.getOtherUser(context, chatDialog));
        Log.i("IsContact", "" + isContact);
        if (chatDialog.isPrivate()) {
            if (isContact) {
                insertChatContact(chatDialog, isContact);
            } else {
                createUsersDialog(chatDialog);
            }
        } else {
            createUsersDialog(chatDialog);
        }
    }

    void createUsersDialog(ConnectycubeChatDialog chatDialog) {
        List<Integer> listUserIds = chatDialog.getOccupants();
        PagedRequestBuilder pagedRequestBuilder = new PagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(listUserIds.size());
        Log.i("DialogUsersList", new Gson().toJson(listUserIds));
        Bundle params = new Bundle();
        ConnectycubeUsers.getUsersByIDs(listUserIds, pagedRequestBuilder, params).performAsync(new EntityCallback<ArrayList<ConnectycubeUser>>() {
            @Override
            public void onSuccess(ArrayList<ConnectycubeUser> users, Bundle args) {

                Log.i("DialogUsersList", new Gson().toJson(users));
                for (int i = 0; i < users.size(); i++) {
                    ConnectycubeUser connectycubeUser = users.get(i);
                    userList.add(new User(connectycubeUser.getId(), connectycubeUser.getLogin(), connectycubeUser.getFullName(), connectycubeUser));
                }

                insertChatContact(chatDialog, false);

            }

            @Override
            public void onError(ResponseException error) {
                chatCallbacks.onChatCreatedError();
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    List<User> users;

    public void insertChatContact(ConnectycubeChatDialog chatDialog, boolean isContact) {
        //insertChat = new InsertChat();
       /* insertChat.isContact = isContact;
        insertChat.setChatCallbacks(chatCallbacks);*/
        //insertChat.execute(chatDialog);


        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {

                Chat chat = new Chat(chatDialog.getDialogId(), chatDialog.getLastMessageDateSent(), chatDialog.getCreatedAt().getTime(), chatDialog.getUpdatedAt().getTime(), chatDialog.getUnreadMessageCount(), chatDialog.getName(), chatDialog);
                appDatabase.chatDao().insert(chat);
                Log.i("CreatedDialogSearch", new Gson().toJson(chat));

                users = appDatabase.userDao().getUsersByIdsPvt(chatDialog.getOccupants(), SharedPreferencesManager.Companion.getInstance(context).getCurrentUser().getId());

                if (!isContact) {
                    insertUsers(chatDialog);
                } else {
                    Intent intent = new Intent(context, ChatMessageActivity.class);
                    intent.putExtra(EXTRA_CHAT, chatDialog);
                    if (users.size() > 0) {
                        ConnectycubeUser connectycubeUser = users.get(0).getConUser();
                        String image = connectycubeUser.getAvatar();
                        String nametoshow = APPHelper.getContactName(context, connectycubeUser.getPhone(), connectycubeUser.getFullName());
                        intent.putExtra("private_name", nametoshow);
                        intent.putExtra("private_image", image);
                    }
                    chatCallbacks.onChatCreatedSuccess(intent);
                }
            }
        });
    }


    void insertUsers(ConnectycubeChatDialog chatDialog) {

        addUser = new AddUser();
        // addUser.execute(chatDialog);

        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                ConnectycubeUser userLogged = SharedPreferencesManager.Companion.getInstance(context).getCurrentUser();
                userList.add(new User(userLogged.getId(), userLogged.getLogin(), userLogged.getFullName(), userLogged));
                appDatabase.userDao().insertAll(userList);
                users = appDatabase.userDao().getUsersByIdsPvt(chatDialog.getOccupants(), SharedPreferencesManager.Companion.getInstance(context).getCurrentUser().getId());
                Intent intent = new Intent(context, ChatMessageActivity.class);
                intent.putExtra(EXTRA_CHAT, chatDialog);
                if (users.size() > 0) {
                    ConnectycubeUser connectycubeUser = users.get(0).getConUser();
                    String image = connectycubeUser.getAvatar();
                    String nametoshow = APPHelper.getContactName(context, connectycubeUser.getPhone(), connectycubeUser.getFullName());
                    intent.putExtra("private_name", nametoshow);
                    intent.putExtra("private_image", image);
                }
                chatCallbacks.onChatCreatedSuccess(intent);
            }
        });
    }

    class AddUser extends AsyncTask<ConnectycubeChatDialog, Void, ConnectycubeChatDialog> {
        List<User> users;
        long startTime = System.currentTimeMillis();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ConnectycubeChatDialog doInBackground(ConnectycubeChatDialog... voids) {

            //database.userDao().delete();
            Log.i("ChatDialogInsUser", new Gson().toJson(userList));
            appDatabase.userDao().insertAll(userList);
            users = appDatabase.userDao().getUsersByIdsPvt(voids[0].getOccupants(), SharedPreferencesManager.Companion.getInstance(context).getCurrentUser().getId());

            return voids[0];
        }

        @Override
        protected void onPostExecute(ConnectycubeChatDialog chatDialog) {
            super.onPostExecute(chatDialog);
            long endTime = System.currentTimeMillis();

            Log.i("ChatDialogInsTime2", "" + (endTime - startTime));

            Intent intent = new Intent(context, ChatMessageActivity.class);
            intent.putExtra(EXTRA_CHAT, chatDialog);
            if (users.size() > 0) {
                ConnectycubeUser connectycubeUser = users.get(0).getConUser();
                String image = connectycubeUser.getAvatar();
                String nametoshow = APPHelper.getContactName(context, connectycubeUser.getPhone(), connectycubeUser.getFullName());
                intent.putExtra("private_name", nametoshow);
                intent.putExtra("private_image", image);
            }
            chatCallbacks.onChatCreatedSuccess(intent);

        }
    }

    public static void launchChatMessageActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
    }


   /* void cancel() {
        if (insertChat != null) {
            insertChat.cancel(true);
        }
    }*/

    class InsertChat1 extends AsyncTask<ConnectycubeChatDialog, Void, ConnectycubeChatDialog> {

        LaunchChatCallbacks chatCallbacks;
        List<User> users;
        long startTime = System.currentTimeMillis();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ConnectycubeChatDialog doInBackground(ConnectycubeChatDialog... connectycubeChatDialogs) {
            ConnectycubeChatDialog dialog = connectycubeChatDialogs[0];
            Chat chat = new Chat(dialog.getDialogId(), dialog.getLastMessageDateSent(), dialog.getCreatedAt().getTime(), dialog.getUpdatedAt().getTime(), dialog.getUnreadMessageCount(), dialog.getName(), dialog);
            AppDatabase appDatabase = AppDatabase.Companion.getInstance(context);
            appDatabase.chatDao().insert(chat);
            Log.i("ChatDialogIns", new Gson().toJson(chat));
            users = appDatabase.userDao().getUsersByIdsPvt(dialog.getOccupants(), SharedPreferencesManager.Companion.getInstance(context).getCurrentUser().getId());
            return connectycubeChatDialogs[0];
        }

        @Override
        protected void onPostExecute(ConnectycubeChatDialog chatDialog) {
            super.onPostExecute(chatDialog);

           /* chatCallbacks.onChatCreatedSuccess();
            Intent intent = new Intent(context, ChatMessageActivity.class);
            intent.putExtra(EXTRA_CHAT, chatDialog);
            activity.startActivity(intent);*/


            Intent intent = new Intent(context, ChatMessageActivity.class);
            intent.putExtra(EXTRA_CHAT, chatDialog);
            if (users.size() > 0) {
                ConnectycubeUser connectycubeUser = users.get(0).getConUser();
                String image = connectycubeUser.getAvatar();
                String nametoshow = APPHelper.getContactName(context, connectycubeUser.getPhone(), connectycubeUser.getFullName());
                intent.putExtra("private_name", nametoshow);
                intent.putExtra("private_image", image);
            }
            chatCallbacks.onChatCreatedSuccess(intent);
        }

        public LaunchChatCallbacks getChatCallbacks() {
            return chatCallbacks;
        }

        public void setChatCallbacks(LaunchChatCallbacks chatCallbacks) {
            this.chatCallbacks = chatCallbacks;
        }
    }


}
