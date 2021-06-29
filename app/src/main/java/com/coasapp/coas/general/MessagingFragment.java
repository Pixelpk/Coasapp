package com.coasapp.coas.general;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.coasapp.coas.ApplozicSampleApplication;
import com.coasapp.coas.R;
import com.coasapp.coas.connectycube.CallHistoryActivity;
import com.coasapp.coas.connectycube.adapters.RecentChatsAdapter;
import com.coasapp.coas.connectycube.adapters.UsersList1Adapter;
import com.coasapp.coas.connectycube.adapters.UsersListAdapter;
import com.coasapp.coas.connectycube.data.MyConnectycubeUser;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.LaunchChatCallbacks;
import com.coasapp.coas.utils.LaunchChatUtils;
import com.coasapp.coas.utils.DatabaseHandler;

import com.coasapp.coas.utils.MyPrefs;
import com.coasapp.coas.utils.OnItemClick;
import com.coasapp.coas.utils.OnItemLongClickListener;
import com.coasapp.coas.utils.PostRequestAsyncTask;
import com.coasapp.coas.utils.SortChatDialogs;
import com.coasapp.coas.utils.StaticValues;
import com.connectycube.chat.ConnectycubeRestChatService;
import com.connectycube.chat.model.ConnectycubeChatDialog;
import com.connectycube.chat.model.ConnectycubeDialogType;
import com.connectycube.core.Consts;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.core.request.PagedRequestBuilder;
import com.connectycube.core.request.RequestGetBuilder;
import com.connectycube.messenger.ChatDialogActivity;
import com.connectycube.messenger.ChatMessageActivity;
import com.connectycube.messenger.CreateChatDialogActivity;
import com.connectycube.messenger.adapters.ChatDialogAdapter;
import com.connectycube.messenger.data.AppDatabase;
import com.connectycube.messenger.data.Chat;
import com.connectycube.messenger.data.User;
import com.connectycube.messenger.utilities.ConverterWraperKt;
import com.connectycube.messenger.utilities.InjectorUtils;
import com.connectycube.messenger.viewmodels.ChatDialogListViewModel;
import com.connectycube.messenger.viewmodels.CreateChatDialogViewModel;
import com.connectycube.messenger.viewmodels.CreateChatDialogViewModelFactory;
import com.connectycube.messenger.vo.Resource;
import com.connectycube.messenger.vo.Status;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static com.connectycube.messenger.ChatMessageActivityKt.EXTRA_CHAT;
import static com.connectycube.messenger.ChatMessageActivityKt.EXTRA_CHAT_ID;
import static com.connectycube.messenger.utilities.SharedPreferencesManagerKt.CUBE_USER_ID;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessagingFragment extends Fragment implements APPConstants, ChatDialogAdapter.ChatDialogAdapterCallback {

    Context context;
    Activity activity;
    int i = 1;
    List<MyConnectycubeUser> userListAll = new ArrayList<>(), userList1 = new ArrayList<>(), userList2 = new ArrayList<>();
    List<User> userListU = new ArrayList<>();
    List<Integer> listUserIds = new ArrayList<>();
    List<Map<String, String>> listContacts = new ArrayList<>();
    List<String> listContactsDb = new ArrayList<>(), listContactsDblimit = new ArrayList<>(), listContactsDbFiltered = new ArrayList<>();

    View rootView;
    SharedPreferences sharedPreferences;
    AddUser addUser;
    String findContacts = "", deleteContacts = "", allContacts = "";
    SwipeRefreshLayout swipeRefreshLayout;
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;

    String chatId = "";

    PostRequestAsyncTask asyncTask;

    GetContacts getContacts;

    String imei = "";
    RecyclerView chats_recycler_view;
    private RecyclerView recyclerViewContacts;

    ChatDialogAdapter chatDialogAdapter;
    RecentChatsAdapter recentChatsAdapter;
    EditText editTextSearch;
    UsersListAdapter usersListAdapter;
    UsersList1Adapter usersList1Adapter;

    List<JSONObject> listUsers = new ArrayList<>(), listUsersAll = new ArrayList<>();


    ArrayList<ConnectycubeChatDialog> chatDialogList = new ArrayList<>(), chatDialogListAll = new ArrayList<>();


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           /* arrayListContacts.clear();
            arrayListContacts.addAll(intent.getStringArrayListExtra("contacts"));
            getContext().stopService(new Intent(getContext(), ContactSyncService.class));*/
            //clearList();
            getDialogsLatest();

        }
    };


    APICallbacks apiCallbacks = new APICallbacks() {
        @Override
        public void taskStart() {
            //swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        public void taskEnd(String type, String response) {

            try {
                JSONObject objectRes = new JSONObject(response);
                if (type.equalsIgnoreCase("contacts")) {

                    /*JSONArray arrayContacts = objectRes.getJSONArray("contacts");

                    listUserIds.clear();
                    listUsersAll.clear();
                    userListAll.clear();

                    for (int i = 0; i < arrayContacts.length(); i++) {


                        JSONObject object = arrayContacts.getJSONObject(i);
                        listUserIds.add(Integer.parseInt(object.getString("user_chat_id")));
                        listUsersAll.add(object);
                        listUsers.add(object);

                        ContentValues values = new ContentValues();
                        values.put("contact_synced", 1);
                        sqLiteDatabase.execSQL("update contacts set contact_synced = 1, contact_image = '" + MAIN_URL_IMAGE + object.getString("image") + "', contact_chat_id = " + object.getString("user_chat_id") + " where contact_phone like '%" + (object.getString("country_code").replace("+", "") + object.getString("phone")) + "%'");

                    }
                    //filter();
                    APPHelper.exportDB();
                    new MyPrefs(context, APP_PREF).putString("contacts", new Gson().toJson(listUserIds));

                    getUsers();*/
                }
            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
    };

    LaunchChatUtils launchChatUtils;


    public MessagingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);



    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_message_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_call_log) {
            Intent intent = new Intent(getContext(), CallHistoryActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messaging, container, false);
    }

    CreateChatDialogViewModel createChatDialogViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        activity = getActivity();
        CreateChatDialogViewModelFactory factory = InjectorUtils.INSTANCE.provideCreateChatDialogViewModelFactory(((ApplozicSampleApplication) activity.getApplication()));

        createChatDialogViewModel = ViewModelProviders.of(this, factory).get(CreateChatDialogViewModel.class);
        launchChatUtils = new LaunchChatUtils(context, activity, new LaunchChatCallbacks() {
            @Override
            public void onChatCreatedSuccess(Intent intent) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cleartext();
                        rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                        LaunchChatUtils.launchChatMessageActivity(activity, intent);
                    }
                });


            }

            @Override
            public void onChatCreatedError() {

            }
        });
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("chats"));
        sharedPreferences = context.getSharedPreferences(APP_PREF, 0);
        view.findViewById(R.id.fab_dialogs_new_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CreateChatDialogActivity.class);
                startActivity(intent);
            }
        });
        recyclerViewContacts = (RecyclerView) view.findViewById(R.id.recyclerViewContacts);
        chats_recycler_view = view.findViewById(R.id.recyclerViewRecentChats);
        chats_recycler_view.setLayoutManager(new LinearLayoutManager(context));
        initChatAdapter();
        editTextSearch = view.findViewById(R.id.editTextSearch);
        //chatDialogListViewModel = InjectorUtils.INSTANCE.provideChatDialogListViewModelFactory(getActivity());
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        rootView = view;
        databaseHandler = new DatabaseHandler(getActivity());
        sqLiteDatabase = databaseHandler.getWritableDatabase();
        /*AppUser appUser = new AppUser(218493, "Sooraj","COAS0056", "12345678");
        appUsers.add(appUser);
        appUser = new AppUser(218497,"Test" ,"COAS0057", "12345678");
        appUsers.add(appUser);
        appUser = new AppUser(206575, "Raja Malaysia","COAS0046", "12345678");
        appUsers.add(appUser);
        appUser = new AppUser(206998, "Raja Indua","COAS0050", "12345678");
        appUsers.add(appUser);

        for (int i = 0; i < appUsers.size(); i++) {
            AppUser user = appUsers.get(i);
            userList.add(new User(user.getUserId(), user.getLogin(), "", new ConnectycubeUser(user.getLogin(), user.getPassword())));

        }
        new AddUser().execute();*/
       /* usersAll.add("COAS0056");
        usersAll.add("COAS0057");
        usersAll.add("COAS0046");
        usersAll.add("COAS0050");
        usersAll.add("COAS0050");
        usersAll.add("COAS0062");*/

        view.findViewById(R.id.buttonMessenger).setVisibility(View.GONE);
        view.findViewById(R.id.buttonMessenger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatDialogActivity.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearValues();
                swipeRefreshLayout.setRefreshing(false);

                syncContacts();

                //getDialogs();
            }
        });

        /*chats_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (APPHelper.isLastItemDisplayingLinear(recyclerView)) {
                    getDialogs();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
*/
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
                if (s.toString().equals("")) {
                    view.findViewById(R.id.textViewContacts).setVisibility(View.GONE);
                    recyclerViewContacts.setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.textViewContacts).setVisibility(View.VISIBLE);
                    recyclerViewContacts.setVisibility(View.VISIBLE);
                }
            }
        });

        if (getArguments().containsKey(EXTRA_CHAT_ID)) {
            chatId = getArguments().getString(EXTRA_CHAT_ID);
        }
        clearValues();
        if (!StaticValues.contactSynced)
            syncContacts();
        else
            getDialogs();

    }

    void initChatAdapter() {
        usersListAdapter = new UsersListAdapter(context, activity, listUsers, new OnItemClick() {
            @Override
            public void onItemClick(int position) {

                JSONObject object = listUsers.get(position);
                sendMessage(object);
                //Log.i("SelectUser", object.toString());

            }
        });

        usersList1Adapter = new UsersList1Adapter(context, activity, userList1, new OnItemClick() {
            @Override
            public void onItemClick(int position) {
                MyConnectycubeUser connectycubeUser = userList1.get(position);
                List<Integer> integers = new ArrayList<>();
                integers.add(connectycubeUser.getId());
                /*createChatDialogViewModel.createNewChatDialogS(connectycubeUser.getFullName(),null,integers).observe(getViewLifecycleOwner(),
                        new Observer<Resource<ConnectycubeChatDialog>>() {
                            @Override
                            public void onChanged(Resource<ConnectycubeChatDialog> connectycubeChatDialogResource) {
                                rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                                if (connectycubeChatDialogResource.getStatus() == Status.SUCCESS) {
                                    ConnectycubeChatDialog chatDialog = connectycubeChatDialogResource.getData();
                                    Intent intent = new Intent(context, ChatMessageActivity.class);
                                    Log.i("ChatDialogIdNew", new Gson().toJson(chatDialog));
                                    intent.putExtra(EXTRA_CHAT, chatDialog);
                                    startActivity(intent);
                                }
                            }
                        });*/

                rootView.findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

                launchChatUtils.createChatDialogUser(userList1.get(position)/*.getLogin()*/);

            }
        });

        recyclerViewContacts.setAdapter(usersList1Adapter);
        /*chatDialogAdapter = new ChatDialogAdapter(getActivity());
        chatDialogAdapter.setCallback(this);*/

        recentChatsAdapter = new RecentChatsAdapter(context, activity, chatDialogList, new OnItemClick() {
            @Override
            public void onItemClick(int position) {

                //launchChat(chatDialogList.get(position));

                launchChatUtils.createChatDialogExisting(chatDialogList.get(position));

            }
        });
        recentChatsAdapter.isHome = true;
        recentChatsAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onLongClick(int i) {
                swipeRefreshLayout.setRefreshing(true);
                ConnectycubeChatDialog chatDialog = chatDialogList.get(i);
                String dialogId = chatDialog.getDialogId();
                ConnectycubeRestChatService.deleteDialog(dialogId, false).performAsync(new EntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        swipeRefreshLayout.setRefreshing(false);
                       /* clearList();
                        getDialogs();*/
                        new DeleteMsg().execute(chatDialog.getDialogId());
                    }

                    @Override
                    public void onError(ResponseException error) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
        chats_recycler_view.setAdapter(recentChatsAdapter);
    }

    public void syncContacts() {
        getContactList();
        getDeleteContacts();
        getContacts = new GetContacts();
        //getContacts.execute();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                addContactList();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findContacts();
                    }
                });
            }
        });
       /* getContactList();
        getDeleteContacts();
        addContactList();*/
       /* findContacts();
        uploadContactsToServer();*/
    }


    void filter() {

        String search = editTextSearch.getText().toString().toLowerCase();
        if (!search.equalsIgnoreCase("")) {
            recyclerViewContacts.setVisibility(View.VISIBLE);

        }
        chatDialogList.clear();
        for (ConnectycubeChatDialog chatDialog : chatDialogListAll) {
            //Log.i("Dialog",new Gson().toJson(chatDialog));
            boolean condition = chatDialog.getName().toLowerCase().contains(search)/* || chatDialog.getLastMessage().toLowerCase().contains(search)*/;
            if (condition)
                chatDialogList.add(chatDialog);
        }
       /* listUsers.clear();
        for (JSONObject object : listUsersAll) {
            //listUsers.add(object);
            try {

                object.put("name", APPHelper.getContactName(context, object.getString("country_code").replace("+", "") + object.getString("phone"), object.getString("name")));

                boolean condition = !(object.getString("user_id").equals(sharedPreferences.getString("userId", "0")))
                        && (object.getString("coas_id").toLowerCase().contains(search)
                        || object.getString("phone").toLowerCase().contains(search)
                        || object.getString("name").toLowerCase().contains(search));
                if (condition)
                    listUsers.add(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/
        userList1.clear();


        for (MyConnectycubeUser myConnectycubeUser : userListAll) {
            //listUsers.add(object);
            /*  try {*/
            myConnectycubeUser.setStoredName(APPHelper.getContactName(context, myConnectycubeUser.getPhone(), myConnectycubeUser.getFullName()));
            boolean condition = !(myConnectycubeUser.getId().equals(sharedPreferences.getInt(CUBE_USER_ID, -1)))
                    && (myConnectycubeUser.getLogin().toLowerCase().contains(search)
                    || myConnectycubeUser.getPhone().toLowerCase().contains(search)
                    || myConnectycubeUser.getFullName().toLowerCase().contains(search)
                    || myConnectycubeUser.getStoredName().toLowerCase().contains(search));
            if (condition) {
                //listUsers.add(object);
                userList1.add(myConnectycubeUser);

            }
            usersList1Adapter.notifyDataSetChanged();
          /*  } catch (JSONException e) {
                e.printStackTrace();
            }*/
        }
        // Log.i("SelectUserList", new Gson().toJson(listUsers));
        recentChatsAdapter.notifyDataSetChanged();

        if (!chatId.equalsIgnoreCase("")) {

            for (int i = 0; i < chatDialogList.size(); i++) {
                ConnectycubeChatDialog chatDialog = chatDialogList.get(i);
                Log.i("ChatDialogSearch", chatDialog.getDialogId() + " " + getArguments().getString(EXTRA_CHAT_ID));
                if (chatDialog.getDialogId().equals(getArguments().getString(EXTRA_CHAT_ID))) {
                    rootView.findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
               /* new LaunchChatUtils(context, activity, new LaunchChatCallbacks() {
                    @Override
                    public void onChatCreatedSuccess(Intent intent) {
                        cleartext();
                        chatId = "";
                        LaunchChatUtils.launchChatMessageActivity(activity, intent);
                        rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                    }

                    @Override
                    public void onChatCreatedError() {
                        rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                    }
                }).createChatDialogExisting(chatDialogList.get(position));*/
                    launchChatUtils.createChatDialogExisting(chatDialogList.get(i));
                    break;

                }
            }
        }
    }


    @Override
    public void onChatDialogSelected(@NotNull ConnectycubeChatDialog chatDialog) {

    }

    @Override
    public void onChatDialogsListUpdated(@NotNull List<? extends ConnectycubeChatDialog> currentList) {

    }

    @Override
    public void onChatDialogDelete(@NotNull ConnectycubeChatDialog chatDialog) {

    }


    class GetContacts extends AsyncTask<Void, Void, List<Map<String, String>>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //listContacts.clear();
            rootView.findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Map<String, String>> doInBackground(Void... voids) {
            List<Map<String, String>> list = new ArrayList<>();
            /*String[] PROJECTION = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cur = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);

            if (cur!=null) {

                while (cur.moveToNext()) {

                    String phoneNo = cur.getString(cur.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("[^0-9]", "");
                    String name = cur.getString(cur.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    if (phoneNo.length() > 7) {
                        Log.i("ContactScan", phoneNo);
                        Map<String, String> map = new HashMap<>();
                        map.put("contact_no", phoneNo);
                        map.put("contact_name", name);
                   *//* if (phoneNo.startsWith("0"))
                        contacts.add(phoneNo.substring(1));
                    else*//*
                        list.add(map);

                    }

                }
                cur.close();

            }*/


            /*Log.i("ContactScan", "Add");
            sqLiteDatabase.delete("contacts", null, null);
            Log.i("ContactFind", String.valueOf(sqLiteDatabase.rawQuery("select * from contacts", null).getCount()));
            List<ContentValues> contentValuesList = new ArrayList<>();
            List<ContentValues> contentValuesList2 = new ArrayList<>();
            for (Map<String, String> map : listContacts) {
                String phoneNo = map.get("contact_no");
                String name = map.get("contact_name");
                String query = "select * from contacts where contact_phone = '" + phoneNo + "'";
                Cursor cursor = sqLiteDatabase.rawQuery(query, null);
                Log.i("ContactFind", query + " " + cursor.getCount());
                if (cursor.getCount() == 0) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("contact_phone", phoneNo);
                    contentValues.put("contact_name", name);
                    contentValues.put("contact_synced", 0);
                    contentValues.put("contact_deleted", 0);
                    contentValuesList.add(contentValues);

                } else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("contact_name", name);
                    contentValuesList2.add(contentValues);
                }
                cursor.close();
            }
            sqLiteDatabase.beginTransaction();
            for (ContentValues contentValues : contentValuesList) {
                sqLiteDatabase.insert("contacts", null, contentValues);
            }
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();

            sqLiteDatabase.beginTransaction();
            for (ContentValues contentValues : contentValuesList2) {
                sqLiteDatabase.update("contacts", contentValues, "contact_phone=?", new String[]{contentValues.getAsString("contact_phone")});
            }
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();
            APPHelper.exportDB();*/

            addContactList();

            return list;
        }

        @Override
        protected void onPostExecute(List<Map<String, String>> maps) {
            super.onPostExecute(maps);
            rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);

            findContacts();



        }
    }


    private void getContactList() {

        listContacts.clear();
        String[] PROJECTION = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (cur != null) {

            while (cur.moveToNext()) {
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    String phoneNo2 = cur.getString(cur.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("[^0-9]", "");
                    String name = cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));

                    String phoneNo = phoneNo2;
              //      Toast.makeText(context, phoneNo, Toast.LENGTH_SHORT).show();
                    if (phoneNo.length() > 7) {
                        //Log.i("ContactScan", phoneNo);
                        Map<String, String> map = new HashMap<>();
                        map.put("contact_no", phoneNo);
                        map.put("contact_name", name);
                   /* if (phoneNo.startsWith("0"))
                        contacts.add(phoneNo.substring(1));
                    else*/
                        listContacts.add(map);

                    }

                }
            }
            String currentPhone = sharedPreferences.getString("std_code", "") + sharedPreferences.getString("phone", "");
         //   Toast.makeText(context, currentPhone, Toast.LENGTH_SHORT).show();
            Map<String, String> map = new HashMap<>();
            map.put("contact_no", currentPhone.replaceAll("[^0-9]", ""));
            map.put("contact_name", "Me");
            listContacts.add(map);
            Log.i("CB-SDKScanned", listContacts.toString());
            cur.close();
        }

    }

    public int checkContactExistInScan(String phone) {
        int index = -1;

        for (int j = 0; j < listContacts.size(); j++) {
            Map<String, String> map = listContacts.get(j);
            if (map.get("contact_no") != null) {
                if (map.get("contact_no").equalsIgnoreCase(phone)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }


    public void getDeleteContacts() {
        String query = "select * from contacts";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        //Log.i("ContactExist", "" + cursor.getCount());

        while (cursor.moveToNext()) {
            String phone = cursor.getString(cursor.getColumnIndex("contact_phone"));
            //Log.i("ContactExist", "" + findContactExist(phone) + " " + phone);
            if (findContactExist(phone) == 0) {
                //deleteContacts += phone + "|";
                sqLiteDatabase.delete("contacts", "contact_phone=?", new String[]{phone});
            }
        }
        cursor.close();
        //APPHelper.exportDB();
    }

   /* private void addContactList() {
       *//* listContacts.clear();
        Map<String, String> map1 = new HashMap<>();

        map1.put("contact_no", "919486795321");
        map1.put("contact_name", "Soorajbsnl");
        listContacts.add(map1);

        map1 = new HashMap<>();
        map1.put("contact_no", "918903935641");
        map1.put("contact_name", "Selvakumar");
        listContacts.add(map1);

        map1 = new HashMap<>();
        map1.put("contact_no", "12816582503");
        map1.put("contact_name", "Safamuku");
        listContacts.add(map1);*//*


        sqLiteDatabase.delete("contacts", null, null);
        //Log.i("ContactFind", String.valueOf(sqLiteDatabase.rawQuery("select * from contacts", null).getCount()));
        List<ContentValues> contentValuesList = new ArrayList<>();
        List<ContentValues> contentValuesList2 = new ArrayList<>();
        contentValuesList.clear();
        contentValuesList2.clear();
        //Collections.sort(listContacts, new SortContacts());
        for (Map<String, String> map : listContacts) {
            String phoneNo = map.get("contact_no");
            String name = map.get("contact_name");
            String query = "select * from contacts where contact_phone = '" + phoneNo + "'";
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
    //        Log.i("CB-SDKInsert", query + " " + cursor.getCount());
            if (cursor.getCount() == 0) {

                ContentValues contentValues = new ContentValues();
                contentValues.put("contact_phone", removecountrycode(phoneNo));
                contentValues.put("contact_name", name);
                contentValues.put("contact_synced", 0);
                contentValues.put("contact_deleted", 0);
                contentValuesList.add(contentValues);

            }
            else if (cursor.moveToNext() == false)
            {
                ContentValues contentValues = new ContentValues();
                contentValues.put("contact_name", name);
                contentValuesList2.add(contentValues);

            }

           *//* else {
                ContentValues contentValues = new ContentValues();
                contentValues.put("contact_name", name);
                contentValuesList2.add(contentValues);
                map.remove(map);
            }*//*
            cursor.close();
        }
        //sqLiteDatabase.beginTransaction();
        for (ContentValues contentValues : contentValuesList) {
            sqLiteDatabase.insert("contacts", null, contentValues);
        }
        // sqLiteDatabase.setTransactionSuccessful();
        //sqLiteDatabase.endTransaction();

        //sqLiteDatabase.beginTransaction();
        for (ContentValues contentValues : contentValuesList2) {
            sqLiteDatabase.update("contacts", contentValues, "contact_phone=?", new String[]{contentValues.getAsString("contact_phone")});
        }
        //sqLiteDatabase.setTransactionSuccessful();
        //sqLiteDatabase.endTransaction();


        *//* findContacts();
        uploadContactsToServer();*//*

        *//*getContacts = new GetContacts();
        getContacts.execute();*//*
        APPHelper.exportDB();
    }
*/

    private void addContactList() {
       /* listContacts.clear();
        Map<String, String> map1 = new HashMap<>();

        map1.put("contact_no", "919486795321");
        map1.put("contact_name", "Soorajbsnl");
        listContacts.add(map1);

        map1 = new HashMap<>();
        map1.put("contact_no", "918903935641");
        map1.put("contact_name", "Selvakumar");
        listContacts.add(map1);

        map1 = new HashMap<>();
        map1.put("contact_no", "12816582503");
        map1.put("contact_name", "Safamuku");
        listContacts.add(map1);*/


        sqLiteDatabase.delete("contacts", null, null);
        //Log.i("ContactFind", String.valueOf(sqLiteDatabase.rawQuery("select * from contacts", null).getCount()));
        List<ContentValues> contentValuesList = new ArrayList<>();
        List<ContentValues> contentValuesList2 = new ArrayList<>();
        contentValuesList.clear();
        contentValuesList2.clear();
        //Collections.sort(listContacts, new SortContacts());
        for (Map<String, String> map : listContacts) {
            String phoneNo = map.get("contact_no");
            String name = map.get("contact_name");
            String query = "select * from contacts where contact_phone = '" + phoneNo + "'";
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            Log.i("CB-SDKInsert", query + " " + cursor.getCount());
            if (cursor.getCount() == 0) {

                ContentValues contentValues = new ContentValues();
                contentValues.put("contact_phone", phoneNo);
                contentValues.put("contact_name", name);
                contentValues.put("contact_synced", 0);
                contentValues.put("contact_deleted", 0);
                contentValuesList.add(contentValues);

            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put("contact_name", name);
                contentValuesList2.add(contentValues);
            }
            cursor.close();
        }
        //sqLiteDatabase.beginTransaction();
        for (ContentValues contentValues : contentValuesList) {
            sqLiteDatabase.insert("contacts", null, contentValues);
        }
        // sqLiteDatabase.setTransactionSuccessful();
        //sqLiteDatabase.endTransaction();

        //sqLiteDatabase.beginTransaction();
        for (ContentValues contentValues : contentValuesList2) {
            sqLiteDatabase.update("contacts", contentValues, "contact_phone=?", new String[]{contentValues.getAsString("contact_phone")});
        }
        //sqLiteDatabase.setTransactionSuccessful();
        //sqLiteDatabase.endTransaction();


        /* findContacts();
        uploadContactsToServer();*/

        /*getContacts = new GetContacts();
        getContacts.execute();*/
        APPHelper.exportDB();
    }


    public int findContactExist(String phone) {

       /* ContentResolver cr = getActivity().getContentResolver();

        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[]{phone}, null);
        int count = pCur.getCount();
        while (pCur.moveToNext()) {
            String phoneNo1 = pCur.getString(pCur.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER));

        }
        pCur.close();*/
        /*for (int i = 0; i < arrayListContacts.size(); i++) {
            if (arrayListContacts.get(i).contains(phone)) {
                return 1;
            }
        }*/

        //Log.i("ContactDel", new Gson().toJson(listContacts));
        for (int i = 0; i < listContacts.size(); i++) {
            //Log.i("" + listContacts.get(i).get("contact_no"), " " + phone);
            String p = listContacts.get(i).get("contact_no");
        //    Toast.makeText(context, p, Toast.LENGTH_SHORT).show();
          //  String p = removecountrycode(p2);

            if (p.equals(phone)) {
                return 1;
            }
        }
       /* if (allContacts.replace(" ", "").replace("-", "").contains(phone.replace(" ", "").replace("-", ""))) {
            return 1;
        }*/
        return 0;

    }

    void clearValues() {
        chatId = "";
        findContacts = "";
        deleteContacts = "";
        listContactsDb.clear();
        userListAll.clear();
        listUserIds.clear();
        userListU.clear();
        usersList1Adapter.notifyDataSetChanged();
        clearList();
    }

    public Cursor getContactCursor() {
        int start = listContactsDb.size();
        String query = "select * from contacts where contact_synced = 0 limit " + start + ", 100";

        //String query = "select * from contacts";
        Cursor c = sqLiteDatabase.rawQuery(query, null);
        Log.i("CB-SDKCount", query + " " + c.getCount());
        return c;
    }

/*    public void findContacts() {

        int start = listContactsDb.size();
        listContactsDblimit.clear();
        //String query = "select * from contacts";

        //Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        Cursor cursor = getContactCursor();

        int count = cursor.getCount();


        while (cursor.moveToNext()) {
            String phone2 = "+" + cursor.getString(cursor.getColumnIndex("contact_phone"));

           String phone = removecountrycode(phone2);
        //    Toast.makeText(context, phone, Toast.LENGTH_SHORT).show();
         //   Toast.makeText(context, phone, Toast.LENGTH_SHORT).show();
            //   Log.i("CB-SDKFind", phone);

            //findContacts += phone + "|";
            //if (!listContactsDb.contains(phone))
            listContactsDb.add(phone);
            //if (!listContactsDblimit.contains(phone))
            listContactsDblimit.add(phone);
        }
        cursor.close();

        //Toast.makeText(getActivity(), "" + findContacts, Toast.LENGTH_SHORT).show();
        //APPHelper.exportDB();
        uploadContactsToServer();

        getUsers();
    }*/

    public void findContacts() {

        int start = listContactsDb.size();
        listContactsDblimit.clear();
        //String query = "select * from contacts";

        //Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        Cursor cursor = getContactCursor();

        int count = cursor.getCount();


        while (cursor.moveToNext()) {
            String phone = "+" + cursor.getString(cursor.getColumnIndex("contact_phone"));
            Log.i("CB-SDKFind", phone);
            //findContacts += phone + "|";
            //if (!listContactsDb.contains(phone))
            listContactsDb.add(phone);
            //if (!listContactsDblimit.contains(phone))
            listContactsDblimit.add(phone);
        }
        cursor.close();

        //Toast.makeText(getActivity(), "" + findContacts, Toast.LENGTH_SHORT).show();
        //APPHelper.exportDB();
        uploadContactsToServer();

        getUsers();
    }


    public void uploadContactsToServer() {

        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_PREF, 0);
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", sharedPreferences.getString("userId", ""));
        map.put("device_id", sharedPreferences.getString("token", ""));
        map.put("user_chat_id", String.valueOf(sharedPreferences.getInt(CUBE_USER_ID, 0)));
        map.put("find_contacts", APPHelper.removeLastChar(findContacts/*.replace("+", "").replace("-", "")*/));
        map.put("delete_contacts", APPHelper.removeLastChar(deleteContacts/*.replace("+", "").replace("-", "")*/));
        asyncTask = new PostRequestAsyncTask(context, map, "contacts", apiCallbacks);
        asyncTask.execute(MAIN_URL + "update_chat_contacts.php");
    }

    public void getUsers() {
        listContactsDbFiltered.clear();
        for (String phone : listContactsDblimit) {
           // String phone = removecountrycode(phone1);
            if (!listContactsDbFiltered.contains(phone)) {
                listContactsDbFiltered.add(phone);
            }
        }
        Log.i("CB-SDKGetUsersTotal", "" + listContactsDb.size());
        Log.i("CB-SDKGetUsers", listContactsDbFiltered.toString());
        //userListAll.clear();
        // listUserIds.clear();
        swipeRefreshLayout.setRefreshing(true);
        PagedRequestBuilder pagedRequestBuilder = new PagedRequestBuilder();
        pagedRequestBuilder.setPage(i);

        pagedRequestBuilder.setPerPage(listContactsDbFiltered.size());

        Bundle params = new Bundle();
        /*ConnectycubeUsers.getUsersByIDs(listUserIds, pagedRequestBuilder, params).performAsync(new EntityCallback<ArrayList<ConnectycubeUser>>() {
            @Override
            public void onSuccess(ArrayList<ConnectycubeUser> users, Bundle args) {
                StaticValues.contactSynced = true;
                swipeRefreshLayout.setRefreshing(false);
                Log.i("UsersList", new Gson().toJson(users));
                for (int i = 0; i < users.size(); i++) {
                    ConnectycubeUser connectycubeUser = users.get(i);
                    userList.add(new User(connectycubeUser.getId(), connectycubeUser.getLogin(), connectycubeUser.getFullName(), connectycubeUser));
                }

                addUser = new AddUser();
                addUser.execute();

            }

            @Override
            public void onError(ResponseException error) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/

        ConnectycubeUsers.getUsersByPhoneNumbers(listContactsDbFiltered, pagedRequestBuilder, params).performAsync(new EntityCallback<ArrayList<ConnectycubeUser>>() {
            @Override
            public void onSuccess(ArrayList<ConnectycubeUser> users, Bundle bundle) {
                //swipeRefreshLayout.setRefreshing(false);
                Log.i("CB-SDKGetUsersResult", new Gson().toJson(users));

                for (int i = 0; i < users.size(); i++) {
                    ConnectycubeUser connectycubeUser = users.get(i);
                    if (connectycubeUser.getAvatar() != null) {
                        if (connectycubeUser.getAvatar().startsWith("profile")) {
                            connectycubeUser.setAvatar(MAIN_URL_IMAGE + connectycubeUser.getAvatar());
                        }
                    }
                    MyConnectycubeUser myConnectycubeUser = new MyConnectycubeUser();
                    myConnectycubeUser.setFullName(connectycubeUser.getFullName());
                    myConnectycubeUser.setLogin(connectycubeUser.getLogin());
                    myConnectycubeUser.setId(connectycubeUser.getId());
                    myConnectycubeUser.setAvatar(connectycubeUser.getAvatar());
                    myConnectycubeUser.setPhone(connectycubeUser.getPhone());
                    userListAll.add(myConnectycubeUser);
                    listUserIds.add(connectycubeUser.getId());
                    StaticValues.myConnectycubeUsers = userListAll;
                    userListU.add(new User(connectycubeUser.getId(), connectycubeUser.getLogin(), connectycubeUser.getFullName(), connectycubeUser));
                    ContentValues values = new ContentValues();
                    values.put("contact_synced", 1);
                    sqLiteDatabase.execSQL("update contacts set contact_synced = 1, contact_image = '" + connectycubeUser.getAvatar() + "', contact_chat_id = " + connectycubeUser.getId() + " where contact_phone like '%" + connectycubeUser.getPhone().replace("+", "") + "%'");
                }
                new MyPrefs(context, APP_PREF).putString("contacts", new Gson().toJson(listUserIds));
                Log.i("UsersList", new Gson().toJson(listUserIds));
                Cursor cursor = getContactCursor();
                if (cursor.getCount() == 0) {
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        AppDatabase appDatabase = AppDatabase.Companion.getInstance(context);
                        appDatabase.userDao().delete();
                        appDatabase.userDao().insertAll(userListU);
                        activity.runOnUiThread(() -> {
                            swipeRefreshLayout.setRefreshing(false);
                            rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                            filter();
                            getDialogs();
                        });

                    });
                } else {
                    findContacts();
                }
                addUser = new AddUser();
                //addUser.execute();
            }

            @Override
            public void onError(ResponseException e) {
                swipeRefreshLayout.setRefreshing(false);
                Log.i("UserLoadError", "Error " + e.getMessage());
                Toast.makeText(context, "Error Loading Users: " + e.getMessage(), Toast.LENGTH_LONG).show();

            }
        });


    }

    class AddUser extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rootView.findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AppDatabase database = AppDatabase.Companion.getInstance(context);
            database.userDao().delete();
            database.userDao().insertAll(userListU);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


        }
    }

    void clearList() {
        chatDialogListAll.clear();
        chatDialogList.clear();
        recentChatsAdapter.notifyDataSetChanged();
    }

    public void getDialogs() {
        clearList();
        swipeRefreshLayout.setRefreshing(true);
        RequestGetBuilder requestBuilder = new RequestGetBuilder();
        requestBuilder.setLimit(100);
        requestBuilder.setSkip(0);
//requestBuilder.sortAsc(Consts.DIALOG_LAST_MESSAGE_DATE_SENT_FIELD_NAME);

        ConnectycubeRestChatService.getChatDialogs((ConnectycubeDialogType) null, requestBuilder).performAsync(new EntityCallback<ArrayList<ConnectycubeChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<ConnectycubeChatDialog> dialogs, Bundle params) {

                swipeRefreshLayout.setRefreshing(false);

                int totalEntries = params.getInt(Consts.TOTAL_ENTRIES);
                chatDialogListAll.clear();
                chatDialogList.clear();
                recentChatsAdapter.notifyDataSetChanged();
                chatDialogListAll.addAll(dialogs);
                chatDialogList.addAll(dialogs);

                List<Chat> chats = ConverterWraperKt.convertToChats(chatDialogListAll);
                filter();
                // new InsertChats().execute(chats);
            }

            @Override
            public void onError(ResponseException exception) {
                Toast.makeText(context, "Error Loading Chats: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    public void getDialogsLatest() {
        swipeRefreshLayout.setRefreshing(true);
        RequestGetBuilder requestBuilder = new RequestGetBuilder();
        requestBuilder.setLimit(1);
        requestBuilder.setSkip(0);
//requestBuilder.sortAsc(Consts.DIALOG_LAST_MESSAGE_DATE_SENT_FIELD_NAME);

        ConnectycubeRestChatService.getChatDialogs((ConnectycubeDialogType) null, requestBuilder).performAsync(new EntityCallback<ArrayList<ConnectycubeChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<ConnectycubeChatDialog> dialogs, Bundle params) {

                swipeRefreshLayout.setRefreshing(false);

                int totalEntries = params.getInt(Consts.TOTAL_ENTRIES);
                if (dialogs.size() > 0) {
                    ConnectycubeChatDialog dialog = dialogs.get(0);
                    int pos = checkDialogExists(dialog.getDialogId());
                    if (pos != -1) {
                        chatDialogListAll.set(pos, dialog);
                    } else {
                        chatDialogListAll.add(0, dialog);
                    }
                    Collections.sort(chatDialogListAll, new SortChatDialogs());
                }
                filter();
                // new InsertChats().execute(chats);
            }

            @Override
            public void onError(ResponseException exception) {
                Toast.makeText(context, "Error Loading Chats: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    int checkDialogExists(String dialogId) {
        int index = -1;
        for (int i = 0; i < chatDialogListAll.size(); i++) {
            ConnectycubeChatDialog chatDialog = chatDialogListAll.get(i);
            if (chatDialog.getDialogId().equals(dialogId)) {
                index = i;
                break;
            }
        }
        return index;
    }

    void loadDialogs(boolean option, List<ConnectycubeChatDialog> dialogs) {

        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase appDatabase = AppDatabase.Companion.getInstance(context);
                if (option) {
                    appDatabase.chatDao().delete();
                    for (ConnectycubeChatDialog dialog : dialogs
                    ) {
                        Chat chat = new Chat(dialog.getDialogId(), dialog.getLastMessageDateSent(), dialog.getCreatedAt().getTime(), dialog.getUpdatedAt().getTime(), dialog.getUnreadMessageCount(), dialog.getName(), dialog);
                        appDatabase.chatDao().insert(chat);
                    }
                }

                List<Chat> dialogs1 = appDatabase.chatDao().getChatsR();
                for (Chat c : dialogs1) {
                 /*   ConnectycubeChatDialog chatDialog = new ConnectycubeChatDialog();
                    chatDialog.setName(c.getName());
                    chatDialog.set(c.get());*/
                }


            }
        });
    }


    void sendMessage(JSONObject object) {
        List<Integer> user = new ArrayList<>();
        try {
            Integer chatID = Integer.valueOf(object.getString("user_chat_id"));
            user.add(chatID);
            ConnectycubeChatDialog dialog = new ConnectycubeChatDialog();
            dialog.setType(ConnectycubeDialogType.PRIVATE);
            dialog.setOccupantsIds(user);
            rootView.findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
            //or just use DialogUtils
            //ConnectycubeChatDialog dialog = DialogUtils.buildPrivateDialog(recipientId);


            /*ConnectycubeUsers.getUserByLogin(object.getString("coas_id")).performAsync(new EntityCallback<ConnectycubeUser>() {
                @Override
                public void onSuccess(ConnectycubeUser connectycubeUser, Bundle bundle) {
                    try {

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onError(ResponseException e) {
                    rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                }
            });*/
            new LaunchChatUtils(context, activity, new LaunchChatCallbacks() {
                @Override
                public void onChatCreatedSuccess(Intent intent) {
                    cleartext();
                    rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                    LaunchChatUtils.launchChatMessageActivity(activity, intent);
                }

                @Override
                public void onChatCreatedError() {
                    rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                }
            }).createChatDialog(object.getString("coas_id"));


           /* new LaunchChatUtils(getContext(), getActivity(), new LaunchChatCallbacks() {
                @Override
                public void onChatCreatedSuccess() {
                    rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                }

                @Override
                public void onChatCreatedError() {
                    rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                }
            }).createChatDialog(Integer.parseInt(object.getString("user_chat_id")));*/
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    void cleartext() {
        if (!editTextSearch.getText().toString().equals("")) {
            editTextSearch.setText("");
        }
        editTextSearch.clearFocus();
    }

    class DeleteMsg extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rootView.findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {
            AppDatabase database = AppDatabase.Companion.getInstance(context);
            database.messageDao().deleteMsgByDialogId(strings[0]);
            Log.i("MessageCountAfterDel " + strings[0], String.valueOf(database.messageDao().postsByDialogIdList(strings[0]).size()));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            rootView.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
            //clearList();
            getDialogs();

        }
    }

    void launchChat(ConnectycubeChatDialog dialog) {

        Intent intent = new Intent(context, ChatMessageActivity.class);
        intent.putExtra(EXTRA_CHAT, dialog);
        startActivity(intent);
        //getDialogs();
    }

    class InsertChats extends AsyncTask<List<Chat>, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(List<Chat>... chats) {
            AppDatabase database = AppDatabase.Companion.getInstance(context);
            database.chatDao().delete();
            database.chatDao().insertAll(chats[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            swipeRefreshLayout.setRefreshing(false);

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //sqLiteDatabase.close();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        if (asyncTask != null) asyncTask.cancel(true);
        if (addUser != null) addUser.cancel(true);
        if (getContacts != null) getContacts.cancel(true);

    }

    public String removecountrycode(String phoneNumber) {

        if (phoneNumber.startsWith("+")) {
            if (phoneNumber.length() == 13) {
                String str_getMOBILE = phoneNumber.substring(3);
                return str_getMOBILE;
            } else if (phoneNumber.length() == 14) {
                String str_getMOBILE = phoneNumber.substring(4);
                return str_getMOBILE;
            } else if (phoneNumber.length() == 12) {
                String str_getMOBILE = phoneNumber.substring(2);
                return str_getMOBILE;
            }


        }
            return phoneNumber;

    }



}
