package com.coasapp.coas.general;


import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coasapp.coas.R;

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RentPayoutFragment extends Fragment implements APPConstants {

    SwipeRefreshLayout swipeRefreshLayout;
    PayoutAdapter payoutAdapter;
    int start = 0;
    String type = "";
    FrameLayout layoutMore;
    LinearLayout layoutProgress;
    String userId = "0";
    List<JSONObject> listPayout = new ArrayList<>();
    long currentUnix;

    public RentPayoutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rent_payout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        layoutMore = view.findViewById(R.id.layoutMore);
        layoutProgress = view.findViewById(R.id.layoutProgress);
        type = getArguments().getString("cat");
        payoutAdapter = new PayoutAdapter(listPayout, getActivity(), type);
        recyclerView.setAdapter(payoutAdapter);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_PREF, 0);
        userId = sharedPreferences.getString("userId", "0");
        String email = sharedPreferences.getString("email", "");
        Calendar calendar = Calendar.getInstance();
        currentUnix = APPHelper.getUnixTime(sdfDatabaseDateTime.format(calendar.getTime()));
        new GetPayout().execute(userId);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                listPayout.clear();
                payoutAdapter.notifyDataSetChanged();
                start = 0;
                new GetPayout().execute(userId);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isLastItemDisplaying(recyclerView)) {
                    //Calling the method getdata again
                    //getData();

                    new GetPayout().execute(userId);

                }
            }
        });

        payoutAdapter.setOnPayoutListItemClick(new PayoutAdapter.OnPayoutListItemClick() {
            @Override
            public void onRaiseButtonClick(int i) {
                JSONObject object = listPayout.get(i);
                String category = "", orderId = "";
                try {
                    switch (type) {
                        case "rent":
                            category = "Renting";
                            orderId = object.getString("book_ref");
                            break;
                        case "shop":
                            category = "Shopping";
                            orderId = object.getString("order_track_id");
                            break;
                        case "bargain":
                            category = "Bargain";
                            orderId = object.getString("bargain_ref");
                            break;
                    }
                    JSONArray arrayTicket = object.getJSONArray("ticket");
                    if (arrayTicket.length() == 0)
                        showTicketAlert(category, orderId, userId, email);
                    else {
                        JSONObject objectTicket = arrayTicket.getJSONObject(0);

                        Intent intent = new Intent(getContext(), TicketMessagesActivity.class);

                        intent.putExtra("ticketId", objectTicket.getString("ticket_id"));
                        intent.putExtra("ticketNo", objectTicket.getString("ticket_no"));
                        startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isLastItemDisplaying(RecyclerView recyclerView) {
        if (recyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            APPHelper.showLog("Payout", "" + lastVisibleItemPosition);

            return lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1;

        }
        return false;

    }

    class GetPayout extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutMore.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = "payout_history.php";
            /*switch (cat) {
                case "rent":
                    url = "rent_payout.php";
                    break;
                case "shop":
                    url = "shop_payout.php";
                    break;
                case "bargain":
                    url = "bargain_payout.php";
                    break;
            }*/
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", strings[0]);
            map.put("cat", type);
            map.put("index", String.valueOf(start));
            map.put("current_time", String.valueOf(currentUnix));
            return new RequestHandler().sendPostRequest(MAIN_URL + url, map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutMore.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                JSONArray array = object.getJSONArray("payout");
                for (int i = 0; i < array.length(); i++) {
                    listPayout.add(array.getJSONObject(i));
                }
                start = listPayout.size();
                payoutAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void showTicketAlert(String category, String orderId, String userId, String email) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_ticket, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        EditText editTextTitle = dialogView.findViewById(R.id.editTextTicketTitle);
        EditText editTextMsg = dialogView.findViewById(R.id.editTextTicketMsg);
        dialogView.findViewById(R.id.buttonSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTextTitle.getText().toString().trim();
                String msg = editTextMsg.getText().toString().trim();
                if (title.equals("")) {
                    APPHelper.showToast(getContext(), "Enter title & message");
                } else {
                    alertDialog.dismiss();
                    new RaiseTicket().execute(userId, category, orderId, email, title, msg);
                }
            }
        });

        alertDialog.show();
    }

    void showTicketDetails(JSONObject object) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_ticket_details, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        try {
            alertDialog.setTitle(object.getString("ticket_title"));
            TextView textViewMessage = dialogView.findViewById(R.id.textViewMessage);
            TextView textViewCreated = dialogView.findViewById(R.id.textViewCreated);
            TextView textViewUpdate = dialogView.findViewById(R.id.textViewUpdated);
            TextView textViewReply = dialogView.findViewById(R.id.textViewReply);
            TextView textViewStatus = dialogView.findViewById(R.id.textViewTicketStatus);

            textViewMessage.setText(object.getString("ticket_message"));
            textViewCreated.setText(sdfNativeDateTime.format(sdfDatabaseDateTime.parse(object.getString("ticket_created"))));
            textViewStatus.setText(object.getString("ticket_status"));
            if (object.getString("ticket_reply").length() > 0) {
                dialogView.findViewById(R.id.layoutAnswer).setVisibility(View.VISIBLE);
                textViewReply.setVisibility(View.VISIBLE);
                textViewUpdate.setVisibility(View.VISIBLE);
                textViewReply.setText(object.getString("ticket_reply"));
                textViewUpdate.setText(sdfNativeDateTime.format(sdfDatabaseDateTime.parse(object.getString("ticket_updated"))));

            }
            dialogView.findViewById(R.id.buttonOK).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    class RaiseTicket extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", strings[0]);
            map.put("category", strings[1]);
            map.put("order_id", strings[2]);
            map.put("email", strings[3]);
            map.put("title", strings[4]);
            map.put("msg", strings[5]);
            return new RequestHandler().sendPostRequest(MAIN_URL + "raise_ticket.php", map);
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equals("1")) {
                    new GetPayout().execute(userId);
                    Intent intent = new Intent(getContext(), TicketMessagesActivity.class);

                    intent.putExtra("ticketId", object.getString("ticket_id"));
                    intent.putExtra("ticketNo", object.getString("ticket_no"));
                    startActivity(intent);

                }
                APPHelper.showToast(getContext(), object.getString("message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
