package com.coasapp.coas.connectycube;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.connectycube.adapters.RecentChatsAdapter;
import com.coasapp.coas.connectycube.adapters.UsersList1Adapter;
import com.coasapp.coas.connectycube.adapters.UsersListAdapter;
import com.coasapp.coas.connectycube.data.MyConnectycubeUser;
import com.coasapp.coas.general.MyAppCompatActivity;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.LaunchChatCallbacks;
import com.coasapp.coas.utils.LaunchChatUtils;
import com.coasapp.coas.utils.OnItemClick;
import com.coasapp.coas.utils.PostRequestAsyncTask;
import com.coasapp.coas.utils.StaticValues;
import com.connectycube.chat.ConnectycubeRestChatService;
import com.connectycube.chat.model.ConnectycubeChatDialog;
import com.connectycube.chat.model.ConnectycubeDialogType;
import com.connectycube.core.Consts;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.core.request.RequestGetBuilder;
import com.connectycube.messenger.ChatMessageActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.connectycube.messenger.ChatMessageActivityKt.EXTRA_CHAT;
import static com.connectycube.messenger.utilities.SharedPreferencesManagerKt.CUBE_USER_ID;

public class ForwardMessageActivity extends MyAppCompatActivity implements APPConstants, APICallbacks {

    Context context;
    Activity activity;

    String imei = "", search = "", message = "", attachmentType = "", attachmentUrl, attachmentData = "", contentType = "";

    PostRequestAsyncTask asyncTask;

    List<JSONObject> listUsersCon = new ArrayList<>(), listUsersAllC = new ArrayList<>();

    List<MyConnectycubeUser> userListAll = new ArrayList<>(), userList1 = new ArrayList<>(), userList2 = new ArrayList<>();
    List<ConnectycubeChatDialog> chatDialogList = new ArrayList<>(), chatDialogListAll = new ArrayList<>();

    UsersListAdapter usersListAdapter;
    UsersList1Adapter usersList1Adapter;
    RecentChatsAdapter recentChatsAdapter;

    private EditText editTextSearch;
    private SwipeRefreshLayout swipe;
    private TextView textViewRecentChats;
    private RecyclerView recyclerViewRecentChats;
    private TextView textViewContacts;
    private RecyclerView recyclerViewContacts;

    ConnectycubeChatDialog connectycubeChatDialog;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2020-01-02 14:33:36 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        textViewRecentChats = (TextView) findViewById(R.id.textViewRecentChats);
        recyclerViewRecentChats = (RecyclerView) findViewById(R.id.recyclerViewRecentChats);
        textViewContacts = (TextView) findViewById(R.id.textViewContacts);
        recyclerViewContacts = (RecyclerView) findViewById(R.id.recyclerViewContacts);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward_message);
        context = getApplicationContext();
        activity = this;
        findViews();
        getMessageContent();


        usersListAdapter = new UsersListAdapter(context, this, listUsersCon, new OnItemClick() {
            @Override
            public void onItemClick(int position) {
                /*JSONObject object = listUsersCon.get(position);

                sendMessage(object);*/

            }
        });

        usersList1Adapter = new UsersList1Adapter(context, this, userList1, new OnItemClick() {
            @Override
            public void onItemClick(int position) {


                sendMessagC(userList2.get(position));

            }
        });

        recyclerViewContacts.setAdapter(usersList1Adapter);
        recentChatsAdapter = new RecentChatsAdapter(context, this, chatDialogList, new OnItemClick() {
            @Override
            public void onItemClick(int position) {
                ConnectycubeChatDialog chatDialog = chatDialogList.get(position);
                new LaunchChatUtils(context, ForwardMessageActivity.this, new LaunchChatCallbacks() {
                    @Override
                    public void onChatCreatedSuccess(Intent intent) {
                       /* intent.putExtra("message", getIntent().getSerializableExtra("message"));

                        if (getIntent().getBooleanExtra("is_attachment", false)) {
                            intent.putExtra("attachment", getIntent().getSerializableExtra("attachment"));

           *//* intent.putExtra("attachment_data", attachmentData);
            intent.putExtra("attachment_url", attachmentUrl);
            intent.putExtra("attachment_type", "" + attachmentType);
            intent.putExtra("content_type", "" + contentType);*//*
                            Log.i("Forward", new Gson().toJson(getIntent().getSerializableExtra("attachment")));

                        }
                        intent.putExtra("is_attachment", getIntent().getBooleanExtra("is_attachment", false));

                        LaunchChatUtils.launchChatMessageActivity(ForwardMessageActivity.this, intent);*/
                        putExtraForward(intent);
                        findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                    }

                    @Override
                                public void onChatCreatedError() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            findViewById(R.id.layoutProgress).setVisibility(View.GONE);

                                        }
                                    });
                                }
                }).createChatDialogExisting(chatDialog);


            }
        });
        recentChatsAdapter.isHome = false;
        recyclerViewRecentChats.setAdapter(recentChatsAdapter);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter();
            }
        });
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearValues();
                getDialogs();

            }
        });
        getDialogs();

        userListAll.addAll(StaticValues.myConnectycubeUsers);
        usersList1Adapter.notifyDataSetChanged();

        swipe.setRefreshing(true);


    }

    void getDialogs() {
        swipe.setRefreshing(true);
        RequestGetBuilder requestBuilder = new RequestGetBuilder();
        requestBuilder.setLimit(10);
        requestBuilder.setSkip(0);
//requestBuilder.sortAsc(Consts.DIALOG_LAST_MESSAGE_DATE_SENT_FIELD_NAME);

        ConnectycubeRestChatService.getChatDialogs((ConnectycubeDialogType) null, requestBuilder).performAsync(new EntityCallback<ArrayList<ConnectycubeChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<ConnectycubeChatDialog> dialogs, Bundle params) {
                swipe.setRefreshing(false);
                int totalEntries = params.getInt(Consts.TOTAL_ENTRIES);
                chatDialogListAll.addAll(dialogs);
                chatDialogList.addAll(dialogs);
                filter();
            }

            @Override
            public void onError(ResponseException exception) {
                swipe.setRefreshing(false);
            }
        });
    }

    void clearValues() {
       /* listUsersCon.clear();
        listUsersAllC.clear();*/
        chatDialogList.clear();
        chatDialogListAll.clear();
    }

    @Override
    public void taskStart() {

    }

    @Override
    public void taskEnd(String type, String response) {
        swipe.setRefreshing(false);
        try {
            JSONObject objectRes = new JSONObject(response);
            if (type.equals("contacts")) {

                JSONArray arrayContacts = objectRes.getJSONArray("contacts");
                listUsersAllC.addAll(APPHelper.getJSONObjectsArrayList(arrayContacts));
                filter();
                getDialogs();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void filter() {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, 0);
        search = editTextSearch.getText().toString().toLowerCase();
       /* listUsersCon.clear();
        for (JSONObject object : listUsersAllC) {
            //listUsers.add(object);

            try {
                object.put("name", APPHelper.getContactName(context, object.getString("country_code").replace("+", "") + object.getString("phone"), object.getString("name")));

                boolean condition = !(object.getString("user_id").equals(sharedPreferences.getString("userId", "0")))
                        && (object.getString("coas_id").toLowerCase().contains(search)
                        || object.getString("phone").toLowerCase().contains(search)
                        || object.getString("name").toLowerCase().contains(search));
                if (condition)
                    listUsersCon.add(object);

                Collections.sort(listUsersCon, new SortAppUser());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/
        chatDialogList.clear();
        for (ConnectycubeChatDialog chatDialog : chatDialogListAll) {
            //Log.i("Dialog",new Gson().toJson(chatDialog));
            boolean condition = chatDialog.getName().toLowerCase().contains(search);
            if (condition)
                chatDialogList.add(chatDialog);
        }
        userList1.clear();
        userList2.clear();

        for (MyConnectycubeUser myConnectycubeUser : userListAll) {
            //listUsers.add(object);
            /*  try {*/
            myConnectycubeUser.setStoredName(APPHelper.getContactName(context, myConnectycubeUser.getPhone(), myConnectycubeUser.getFullName()));
            boolean condition = !(myConnectycubeUser.getId().equals(sharedPreferences.getInt(CUBE_USER_ID, -1)))
                    && (myConnectycubeUser.getLogin().toLowerCase().contains(search)
                    || myConnectycubeUser.getPhone().toLowerCase().contains(search)
                    || myConnectycubeUser.getStoredName().toLowerCase().contains(search));
            if (condition) {
                //listUsers.add(object);
                userList1.add(myConnectycubeUser);
                userList2.add(myConnectycubeUser);
            }
        }
        usersList1Adapter.notifyDataSetChanged();
        recentChatsAdapter.notifyDataSetChanged();


    }

    void callAPI() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            APPHelper.showLog("Device", task.getException().getMessage());
                            return;
                        }

                        // Get new Instance ID token
                        imei = task.getResult().getToken();

                        // Log and toast

                    }
                });

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, 0);
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", sharedPreferences.getString("userId", ""));
        map.put("device_id", sharedPreferences.getString("token", ""));
        map.put("user_chat_id", String.valueOf(sharedPreferences.getInt(CUBE_USER_ID, 0)));
        map.put("find_contacts", "");
        map.put("delete_contacts", "");
        asyncTask = new PostRequestAsyncTask(context, map, "contacts", this);
        asyncTask.execute(MAIN_URL + "update_chat_contacts.php");
    }

    void sendMessage(JSONObject object) {


        List<Integer> user = new ArrayList<>();
        try {
            /*Integer chatID = Integer.valueOf(object.getString("user_chat_id"));
            user.add(chatID);
            ConnectycubeChatDialog dialog = new ConnectycubeChatDialog();
            dialog.setType(ConnectycubeDialogType.PRIVATE);
            dialog.setOccupantsIds(user);*/
            findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
            //or just use DialogUtils
            //ConnectycubeChatDialog dialog = DialogUtils.buildPrivateDialog(recipientId);


            new LaunchChatUtils(context, ForwardMessageActivity.this, new LaunchChatCallbacks() {
                @Override
                public void onChatCreatedSuccess(Intent intent) {
                    /*intent.putExtra("message", getIntent().getSerializableExtra("message"));

                    if (getIntent().getBooleanExtra("is_attachment", false)) {
                        intent.putExtra("attachment", getIntent().getSerializableExtra("attachment"));

           *//* intent.putExtra("attachment_data", attachmentData);
            intent.putExtra("attachment_url", attachmentUrl);
            intent.putExtra("attachment_type", "" + attachmentType);
            intent.putExtra("content_type", "" + contentType);*//*
                        Log.i("Forward", new Gson().toJson(getIntent().getSerializableExtra("attachment")));

                    }
                    intent.putExtra("is_attachment", getIntent().getBooleanExtra("is_attachment", false));*/

                    putExtraForward(intent);


                    findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                }

                @Override
                                public void onChatCreatedError() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            findViewById(R.id.layoutProgress).setVisibility(View.GONE);

                                        }
                                    });
                                }
            }).createChatDialog(object.getString("coas_id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void sendMessagC(MyConnectycubeUser object) {


        List<Integer> user = new ArrayList<>();
        /*Integer chatID = Integer.valueOf(object.getString("user_chat_id"));
        user.add(chatID);
        ConnectycubeChatDialog dialog = new ConnectycubeChatDialog();
        dialog.setType(ConnectycubeDialogType.PRIVATE);
        dialog.setOccupantsIds(user);*/
        findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
        //or just use DialogUtils
        //ConnectycubeChatDialog dialog = DialogUtils.buildPrivateDialog(recipientId);


        new LaunchChatUtils(context, ForwardMessageActivity.this, new LaunchChatCallbacks() {
            @Override
            public void onChatCreatedSuccess(Intent intent) {
                /*intent.putExtra("message", getIntent().getSerializableExtra("message"));

                if (getIntent().getBooleanExtra("is_attachment", false)) {
                    intent.putExtra("attachment", getIntent().getSerializableExtra("attachment"));

       *//* intent.putExtra("attachment_data", attachmentData);
        intent.putExtra("attachment_url", attachmentUrl);
        intent.putExtra("attachment_type", "" + attachmentType);
        intent.putExtra("content_type", "" + contentType);*//*
                    Log.i("Forward", new Gson().toJson(getIntent().getSerializableExtra("attachment")));

                }
                intent.putExtra("is_attachment", getIntent().getBooleanExtra("is_attachment", false));*/

                putExtraForward(intent);


                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
            }

            @Override
                                public void onChatCreatedError() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            findViewById(R.id.layoutProgress).setVisibility(View.GONE);

                                        }
                                    });
                                }
        }).createChatDialogUser(object/*.getLogin()*/);

    }

    void createDialog(ConnectycubeChatDialog dialog) {
        ConnectycubeRestChatService.createChatDialog(dialog).performAsync(new EntityCallback<ConnectycubeChatDialog>() {
            @Override
            public void onSuccess(ConnectycubeChatDialog createdDialog, Bundle params) {

                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                Log.i("Forwarded", "Success" + new Gson().toJson(createdDialog.getOccupants()));
                launchChat(createdDialog);
            }

            @Override
            public void onError(ResponseException exception) {
                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                Log.i("Forwarded", "" + exception.getMessage());
            }
        });

    }

    void launchChat(ConnectycubeChatDialog dialog) {
        Intent intent = new Intent(context, ChatMessageActivity.class);
        intent.putExtra(EXTRA_CHAT, dialog);
        intent.putExtra("message", getIntent().getSerializableExtra("message"));

        if (getIntent().getBooleanExtra("is_attachment", false)) {
            intent.putExtra("attachment", getIntent().getSerializableExtra("attachment"));

           /* intent.putExtra("attachment_data", attachmentData);
            intent.putExtra("attachment_url", attachmentUrl);
            intent.putExtra("attachment_type", "" + attachmentType);
            intent.putExtra("content_type", "" + contentType);*/
            Log.i("Forward", new Gson().toJson(getIntent().getSerializableExtra("attachment")));

        }
        intent.putExtra("is_attachment", getIntent().getBooleanExtra("is_attachment", false));

        startActivity(intent);
        finish();
    }

    void putExtraForward(Intent intent) {
        intent.putExtra("message", message);

        if (getIntent().getBooleanExtra("is_attachment", false)) {
            intent.putExtra("attachment", getIntent().getSerializableExtra("attachment"));

           /* intent.putExtra("attachment_data", attachmentData);
            intent.putExtra("attachment_url", attachmentUrl);
            intent.putExtra("attachment_type", "" + attachmentType);
            intent.putExtra("content_type", "" + contentType);*/
            Log.i("Forward", new Gson().toJson(getIntent().getSerializableExtra("attachment")));

        }
        intent.putExtra("is_attachment", getIntent().getBooleanExtra("is_attachment", false));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LaunchChatUtils.launchChatMessageActivity(ForwardMessageActivity.this, intent);
                finish();
            }
        });

    }


    void getMessageContent() {
        message = getIntent().getStringExtra("message");
        /*if (getIntent().hasExtra("attachment_url")) {
            attachmentUrl = getIntent().getStringExtra("attachment_url");
            attachmentType = getIntent().getStringExtra("attachment_type");
            attachmentData = getIntent().getStringExtra("attachment_data");
            contentType = getIntent().getStringExtra("content_type");
        }*/
    }

   /* void sendMessage(ConnectycubeChatDialog createdDialog) {
        ConnectycubeChatMessage chatMessage = new ConnectycubeChatMessage();
        chatMessage.setBody(message);
        if (attachmentUrl.length() > 0) {

            ConnectycubeAttachment attachment = new ConnectycubeAttachment(attachmentType);
            attachment.setContentType(contentType);
            if(!attachmentType.equals("contact")) {
                if (attachmentUrl.startsWith(attachmentChatUrl)) {
                    attachment.setUrl(attachmentUrl);
                }
            }
            attachment.setType(attachmentType);
        }
        chatMessage.setSaveToHistory(true);

        try {
            createdDialog.sendMessage(chatMessage);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplicationContext(), ChatMessageActivity.class);
        intent.putExtra(EXTRA_CHAT, createdDialog);
        startActivity(intent);
    }*/
}
