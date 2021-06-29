package com.connectycube.messenger

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar.DISPLAY_HOME_AS_UP
import androidx.appcompat.app.ActionBar.DISPLAY_SHOW_TITLE
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.coasapp.coas.R
import com.coasapp.coas.connectycube.SortConnectycubeUser
import com.coasapp.coas.utils.APPConstants
import com.coasapp.coas.utils.APPConstants.APP_PREF
import com.coasapp.coas.utils.APPHelper
import com.coasapp.coas.utils.MyPrefs
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import com.connectycube.messenger.adapters.CheckableUsersAdapter
import com.connectycube.messenger.data.AppDatabase
import com.connectycube.messenger.data.User
import com.connectycube.messenger.utilities.InjectorUtils
import com.connectycube.messenger.viewmodels.CreateChatDialogViewModel
import com.connectycube.messenger.vo.Status
import com.connectycube.users.ConnectycubeUsers
import com.connectycube.users.model.ConnectycubeUser
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_create_chat.*
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray
import timber.log.Timber
import java.util.*




    class CreateChatDialogActivity : BaseChatActivity(),
            CheckableUsersAdapter.CheckableUsersAdapterCallback, APPConstants {

        var i = 0
        var connectycubeUsers = arrayListOf<ConnectycubeUser>()
        var connectycubeUsersAll = arrayListOf<ConnectycubeUser>()

        var userList = arrayListOf<User>()
        var usersAll = arrayListOf<String>()

        private val createChatDialogViewModel: CreateChatDialogViewModel by viewModels {
            InjectorUtils.provideCreateChatDialogViewModelFactory(this.application)
        }

        private lateinit var usersAdapter: CheckableUsersAdapter
        private var selectedUsers: MutableList<ConnectycubeUser> = mutableListOf()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_create_chat)
            initToolbar()
            initUserAdapter()
            initViews()
            loadData()
            val editTextSearch = findViewById<EditText>(R.id.editTextSearch);
            editTextSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    filter()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        private fun initToolbar() {
            supportActionBar?.displayOptions = DISPLAY_SHOW_TITLE or DISPLAY_HOME_AS_UP
            title = getText(R.string.new_chat)
        }

        private fun initUserAdapter() {
            usersAdapter = CheckableUsersAdapter(this, this)
        }

        private fun initViews() {
            users_recycler_view.layoutManager = LinearLayoutManager(this)
            users_recycler_view.itemAnimator = DefaultItemAnimator()
            users_recycler_view.adapter = usersAdapter


        }

        private fun loadData() {


            /* usersAll.add("COAS0056")
             usersAll.add("COAS0057")
             usersAll.add("COAS0046")
             usersAll.add("COAS0050")
             var sh = getSharedPreferences(APP_PREF, 0);

             *//*for (i in usersAll.indices-1) {
            if (sh.getString("coasId", "") == usersAll[i]) {
                usersAll.removeAt(i)
            }
        }*//*
        //getUsers()*/


            createChatDialogViewModel.liveSelectedUsers.observe(this) { liveSelectedUsers ->
                selectedUsers = liveSelectedUsers
                invalidateOptionsMenu()
            }
            createChatDialogViewModel.updateSelectedUsersStates()

            createChatDialogViewModel.getUsers().observe(this) { result ->
                Log.i("ConnectycubeLoad", "Load")
                when (result.status) {
                    Status.LOADING -> {
                        showProgress(progressbar)
                        Log.i("ConnectycubeLoad", "Loading")
                    }
                    Status.ERROR -> {
                        hideProgress(progressbar)
                        Log.i("ConnectycubeLoad", "LoadingError" + result.message)
                    }
                    Status.SUCCESS -> {
                        hideProgress(progressbar)
                        val users: List<ConnectycubeUser>? = result.data
                        if (users?.isNotEmpty()!!) {
                            connectycubeUsers.addAll(users)
                            connectycubeUsersAll.addAll(users)
                            usersAdapter.setItems(connectycubeUsers)
                            //usersAdapter.setItems(users)
                            Log.i("UsersListAll", Gson().toJson(connectycubeUsersAll))
                            filter()
                        }

                    }
                }
            }
        }

        fun filter() {

            connectycubeUsers.clear()
            val search = editTextSearch.text.toString().toLowerCase();
            Log.i("UsersList", search)

            for (user: ConnectycubeUser in connectycubeUsersAll) {
                user.fullName = APPHelper.getContactName(applicationContext, user.phone, user.fullName)

                Log.i("UsersListContacts", MyPrefs(applicationContext, APP_PREF).getString("contacts"))
                var contacts = MyPrefs(applicationContext, APP_PREF).getString("contacts")
                if (contacts == null) {
                    contacts = "[]";
                } else {
                    if (contacts == "") {
                        contacts = "[]";
                    }
                }
                var jsonArrayContacts: JSONArray = JSONArray(contacts)

                var contactFound = false;

                for (i in 0 until jsonArrayContacts.length()) {
                    if (jsonArrayContacts.getInt(i) == user.id) {
                        contactFound = true
                    }
                }

                val condition =
                        contactFound
                                && (user.fullName.toLowerCase().contains(search)
                                || user.phone.contains(search)
                                || user.login.toLowerCase().contains(search))
                Log.i("UsersList", user.login.toLowerCase())
                if (condition) {
                    connectycubeUsers.add(user)
                }
            }
            Collections.sort(connectycubeUsers, SortConnectycubeUser())
            Log.i("UsersList", Gson().toJson(connectycubeUsers))

            usersAdapter.setItems(connectycubeUsers)

        }

        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            menuInflater.inflate(R.menu.create_chat_activity, menu)
            return super.onCreateOptionsMenu(menu)
        }

        override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
            val menuItemDone: MenuItem? = menu?.findItem(R.id.action_done)
            menuItemDone?.isVisible = selectedUsers.isNotEmpty()

            if (selectedUsers.size == 1) {
                menuItemDone?.icon = resources.getDrawable(R.drawable.ic_account_check)
            } else if (selectedUsers.size > 1) {
                menuItemDone?.icon = resources.getDrawable(R.drawable.ic_account_multiple_check)
            }

            return super.onPrepareOptionsMenu(menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                android.R.id.home -> finish()
                R.id.action_done -> {
                    val isPrivate = selectedUsers.size < 2
                    if (!isPrivate) startCreateChatDialogDetailActivity()
                    else {
                        Log.i("SelectUser", selectedUsers.get(0).avatar + " " + selectedUsers.get(0).fullName);
                        createChatDialog()
                    }
                }
            }

            return super.onOptionsItemSelected(item)
        }

        private fun startCreateChatDialogDetailActivity() {
            val intent = Intent(this, CreateChatDialogDetailActivity::class.java)
            startActivityForResult(intent, REQUEST_CREATE_DIALOG_DETAILS)
        }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_CANCELED || data == null) return
            when (requestCode) {
                REQUEST_CREATE_DIALOG_DETAILS -> {
                    val name = data.getStringExtra(EXTRA_DIALOG_NAME)
                    val avatar = data.getStringExtra(EXTRA_DIALOG_AVATAR)
                    createChatDialog(name, avatar)
                }
            }
        }

        private fun createChatDialog(name: String? = null, avatar: String? = null) {
            Timber.d("name= $name, avatar= $avatar")
            createChatDialogViewModel.createNewChatDialog(name, avatar).observe(this) { resource ->
                when {
                    resource.status == Status.SUCCESS -> {
                        hideProgress(progressbar)

                        val newChatDialog: ConnectycubeChatDialog? = resource.data
                        if (newChatDialog != null) {
                            startChatActivity(newChatDialog)
                            finish()
                        }
                    }
                    resource.status == Status.LOADING -> showProgress(progressbar)
                    resource.status == Status.ERROR -> {
                        hideProgress(progressbar)
                        Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        public fun startChatActivity(chat: ConnectycubeChatDialog) {
            val intent = Intent(this, ChatMessageActivity::class.java)
            Log.i("ChatDialogIdNew", Gson().toJson(chat))
            intent.putExtra(EXTRA_CHAT, chat)

            if (!chat.isPrivate)
                intent.putExtra("chat_name", chat.name)
            startActivity(intent)
        }

        override fun onUserSelected(user: ConnectycubeUser, checked: Boolean) {
            createChatDialogViewModel.updateUserSelection(user, checked)
        }

        override fun isUserSelected(user: ConnectycubeUser): Boolean {
            return selectedUsers.contains(user)
        }

        override fun finish() {
            super.finish()
            overridePendingTransition(0, R.anim.slide_out_right)
        }

        fun getUsers() {
            var sh = getSharedPreferences(APP_PREF, 0);

            if (i < usersAll.size) {
                var pd: ProgressDialog = ProgressDialog(this);
                pd.show();
                ConnectycubeUsers.getUserByLogin(usersAll[i]).performAsync(object : EntityCallback<ConnectycubeUser> {
                    override fun onSuccess(user: ConnectycubeUser, args: Bundle) {
                        Log.i("ConnectycubeUserDetail", Gson().toJson(user))
                        pd.dismiss();
                        if (usersAll[i] != sh.getString("coasId", "")) {
                            connectycubeUsers.add(user)
                            userList.add(User(user.id, user.login, user.fullName, user))
                        }
                        i++;
                        getUsers()
                    }

                    override fun onError(error: ResponseException) {
                        pd.dismiss();
                        Log.i("ConnectycubeUserDetail", error.message)
                    }
                })
            } else {

                usersAdapter.setItems(connectycubeUsers)

                //addUsersToDb()

                /* val request = OneTimeWorkRequestBuilder<MyChatDatabaseWorker>().build()
                 WorkManager.getInstance(applicationContext).enqueue(request)*/
                //MyTask().execute();
            }
        }

        inner class MyTask : AsyncTask<Void, Void, Void>() {
            var pd: ProgressDialog = ProgressDialog(this@CreateChatDialogActivity);

            override fun onPreExecute() {
                super.onPreExecute()
                pd.show()
            }

            override fun doInBackground(vararg params: Void?): Void? {
                val database = AppDatabase.getInstance(applicationContext)
                database.userDao().insertAll(userList)
                return null;
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                pd.dismiss();
            }
        }


        inner class MyChatDatabaseWorker(
                usersList: List<User>,
                context: Context,
                workerParams: WorkerParameters
        ) : CoroutineWorker(context, workerParams) {

            override suspend fun doWork(): Result = coroutineScope {

                try {
                    val database = AppDatabase.getInstance(applicationContext)
                    database.userDao().insertAll(userList)
                    Result.success()


                } catch (ex: Exception) {
           //         Timber.e("Error seeding database", ex)
                    ex.printStackTrace()
                    Result.failure()
                }
            }


        }

        fun addUsersToDb() {
            suspend fun doWork(): ListenableWorker.Result = coroutineScope {

                try {
                    val database = AppDatabase.getInstance(applicationContext)
                    database.userDao().insertAll(userList)

                    ListenableWorker.Result.success()
                } catch (ex: Exception) {
                 //   Timber.e("Error seeding database", ex)
                    ex.printStackTrace()
                    ListenableWorker.Result.failure()
                }
            }
        }
    }