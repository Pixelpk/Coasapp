package com.coasapp.coas.call;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coasapp.coas.R;
import com.coasapp.coas.sendbird.utils.PrefUtils;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.ImageUtils;
import com.coasapp.coas.utils.TimeUtils;
import com.coasapp.coas.utils.UserInfoUtils;
import com.sendbird.calls.DirectCallLog;
import com.sendbird.calls.DirectCallUser;
import com.sendbird.calls.DirectCallUserRole;


import java.util.ArrayList;
import java.util.List;

class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private List<DirectCallLog> mDirectCallLogs = new ArrayList<>();

    HistoryRecyclerViewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    void setCallLogs(List<DirectCallLog> callLogs) {
        mDirectCallLogs.clear();
        mDirectCallLogs.addAll(callLogs);
    }

    void addCallLogs(List<DirectCallLog> callLogs) {
        mDirectCallLogs.addAll(callLogs);
    }

    void addLatestCallLog(DirectCallLog callLog) {
        mDirectCallLogs.add(0, callLog);
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.fragment_history_recycler_view_item, parent, false);
        return new HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        DirectCallLog callLog = mDirectCallLogs.get(position);
        DirectCallUserRole myRole = callLog.getMyRole();
        final DirectCallUser user;

        if (myRole == DirectCallUserRole.CALLER) {
            user = callLog.getCallee();
            if (callLog.isVideoCall()) {
                holder.imageViewIncomingOrOutgoing.setBackgroundResource(R.drawable.icon_call_video_outgoing_filled);
            } else {
                holder.imageViewIncomingOrOutgoing.setBackgroundResource(R.drawable.icon_call_voice_outgoing_filled);
            }
        } else {
            user = callLog.getCaller();
            if (callLog.isVideoCall()) {
                holder.imageViewIncomingOrOutgoing.setBackgroundResource(R.drawable.icon_call_video_incoming_filled);
            } else {
                holder.imageViewIncomingOrOutgoing.setBackgroundResource(R.drawable.icon_call_voice_incoming_filled);
            }
        }

        ImageUtils.displayCircularImageFromUrl(mContext, user.getProfileUrl(), holder.imageViewProfile);
        UserInfoUtils.setNickname(mContext, user, holder.textViewNickname);
        holder.textViewNickname.setText(APPHelper.getContactName(mContext, user.getUserId(), user.getNickname()));
        UserInfoUtils.setUserId(mContext, user, holder.textViewUserId);
        holder.textViewUserId.setVisibility(View.GONE);

        String endResult = "";
        if (callLog.getEndResult() != null) {
            endResult = callLog.getEndResult().toString().toUpperCase();
        }
        String endResultAndDuration = endResult + mContext.getString(R.string.calls_and_character) + TimeUtils.getTimeStringForHistory(callLog.getDuration());
        holder.textViewEndResultAndDuration.setText(endResultAndDuration);

        holder.textViewStartAt.setText(TimeUtils.getDateString(callLog.getStartedAt()));

        holder.imageViewVideoCall.setOnClickListener(view -> {
          //  CallService.startCallActivity(mContext, user.getUserId(), user.getNickname(), true);
            String calleeId = user.getUserId();

            if (!TextUtils.isEmpty(calleeId)) {
                com.coasapp.coas.sendbird.call.CallService.dial(mContext, calleeId, false);
                PrefUtils.setCalleeId(mContext, calleeId);
            }
        });

        holder.imageViewVoiceCall.setOnClickListener(view -> {
        //    CallService.startCallActivity(mContext, user.getUserId(), user.getNickname(), false);
            String calleeId = user.getUserId();
            if (!TextUtils.isEmpty(calleeId)) {
                com.coasapp.coas.sendbird.call.CallService.dial(mContext, calleeId, false);
                PrefUtils.setCalleeId(mContext, calleeId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDirectCallLogs.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageViewIncomingOrOutgoing;
        final ImageView imageViewProfile;
        final TextView textViewNickname;
        final TextView textViewUserId;
        final TextView textViewEndResultAndDuration;
        final TextView textViewStartAt;
        final ImageView imageViewVideoCall;
        final ImageView imageViewVoiceCall;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewIncomingOrOutgoing = itemView.findViewById(R.id.image_view_incoming_or_outgoing);
            imageViewProfile = itemView.findViewById(R.id.image_view_profile);
            textViewNickname = itemView.findViewById(R.id.text_view_nickname);
            textViewUserId = itemView.findViewById(R.id.text_view_user_id);
            textViewEndResultAndDuration = itemView.findViewById(R.id.text_view_end_result_and_duration);
            textViewStartAt = itemView.findViewById(R.id.text_view_start_at);
            imageViewVideoCall = itemView.findViewById(R.id.image_view_video_call);
            imageViewVoiceCall = itemView.findViewById(R.id.image_view_voice_call);
        }
    }
}
