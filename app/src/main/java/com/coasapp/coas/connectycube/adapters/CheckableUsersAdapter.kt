package com.connectycube.messenger.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import  com.coasapp.coas.R
import com.coasapp.coas.utils.APPConstants
import com.coasapp.coas.utils.MyPrefs
import com.connectycube.messenger.utilities.getPrettyLastActivityDate
import com.connectycube.messenger.utilities.loadUserAvatar
import com.connectycube.users.model.ConnectycubeUser
import org.json.JSONArray
import java.util.*

class CheckableUsersAdapter(
        private val context: Context,
        private val callback: CheckableUsersAdapterCallback
) : RecyclerView.Adapter<CheckableUsersAdapter.CheckableUserViewHolder>() {

    var callCheck = false

    private var items: List<ConnectycubeUser> = mutableListOf()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckableUserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_checkable_user, parent, false)
        return CheckableUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckableUserViewHolder, position: Int) {
        Log.i("CallCheck", "" + callCheck);
        val user = getItem(position)
        holder.bind(
                context,
                user,
                isUserChecked(user),
                View.OnClickListener { setUserChecked(user, !isUserChecked(user)) })
    }

    private fun isUserChecked(user: ConnectycubeUser): Boolean {
        return callback.isUserSelected(user)
    }

    private fun setUserChecked(user: ConnectycubeUser, checked: Boolean) {
        var blockedCountries = MyPrefs(context, APPConstants.APP_PREF).getString("blocked_countries");
        if (blockedCountries == null) {
            blockedCountries = "[]";
        } else {
            if (blockedCountries == "") {
                blockedCountries = "[]";
            }
        }
        val arrayBlockedCall = JSONArray(blockedCountries)
        var callsAllowed = true;
        for (i in 0 until arrayBlockedCall.length()) {
            val code = arrayBlockedCall.getJSONObject(i).getString("std_code");
          //  Toast.makeText(context, code.toString(), Toast.LENGTH_SHORT).show()
            if (user.phone.startsWith(code)) {
                callsAllowed = false
            }
        }

        if (checked && callCheck) {
            if (!callsAllowed) {
                callback.onUserSelected(user, false)
                Toast.makeText(context, R.string.calls_not_allowed, Toast.LENGTH_SHORT).show()
            } else {
                callback.onUserSelected(user, checked)
            }
        } else
            callback.onUserSelected(user, checked)
    }

    private fun getItem(position: Int): ConnectycubeUser {
        return items[position]
    }

    fun setItems(users: List<ConnectycubeUser>) {
        items = users
        notifyDataSetChanged()
    }

    inner class CheckableUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.avatar_image_view)
        private val txtName: TextView = itemView.findViewById(R.id.name_text_viw)
        private val txtLastActivity: TextView = itemView.findViewById(R.id.last_activity_text_view)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

        fun bind(
                activityContext: Context,
                connectycubeUser: ConnectycubeUser,
                isSelected: Boolean,
                onClickListener: View.OnClickListener
        ) {
            loadUserAvatar(
                    activityContext,
                    connectycubeUser,
                    imgAvatar
            )

            txtName.text = connectycubeUser.fullName
            txtLastActivity.text = getPrettyLastActivityDate(activityContext, connectycubeUser.lastRequestAt
                    ?: Date())

            checkBox.isChecked = isSelected
            itemView.setOnClickListener {
                onClickListener.onClick(it)
                checkBox.isChecked = isUserChecked(connectycubeUser)
            }
        }
    }

    interface CheckableUsersAdapterCallback {
        fun onUserSelected(user: ConnectycubeUser, checked: Boolean)
        fun isUserSelected(user: ConnectycubeUser): Boolean
    }
}