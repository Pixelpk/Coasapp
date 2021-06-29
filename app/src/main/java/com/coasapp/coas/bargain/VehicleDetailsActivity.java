package com.coasapp.coas.bargain;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.coasapp.coas.R;

import com.coasapp.coas.roombook.RoomDetailsActivity;
import com.coasapp.coas.utils.APPHelper;

import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;
import com.coasapp.coas.utils.LaunchChatCallbacks;
import com.coasapp.coas.utils.LaunchChatUtils;
import com.connectycube.chat.ConnectycubeRestChatService;
import com.connectycube.chat.model.ConnectycubeChatDialog;
import com.connectycube.chat.model.ConnectycubeDialogType;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.messenger.ChatMessageActivity;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.connectycube.messenger.ChatMessageActivityKt.EXTRA_CHAT;

public class VehicleDetailsActivity extends AppCompatActivity implements APPConstants {

    String driverId = "";
    float avgRating = 0, totalRating;
    List<JSONObject> listReviews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView imageViewVehicle = findViewById(R.id.imageViewVehicle);
        TextView textViewVehicleName = findViewById(R.id.textViewVehicleName);
        TextView textViewDesc = findViewById(R.id.textViewVehicleDesc);
        TextView textViewVehSeats = findViewById(R.id.textViewVehicleSeats);
        CircleImageView imageView = findViewById(R.id.imageViewSeller);
        TextView textViewSeller = findViewById(R.id.textViewSeller);
        RatingBar ratingBarDriver = findViewById(R.id.ratingBarDriver);

        String details = getIntent().getStringExtra("details");
        try {
            final JSONObject object = new JSONObject(details);
            driverId = object.getString("user_id");
            APPHelper.showLog("VehicleImage", "Vehicle " + object.getString("vehicle_image"));
            Picasso.get().load(MAIN_URL_IMAGE + object.getString("vehicle_image")).into(imageViewVehicle);
            Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + object.getString("image")).into(imageView);
            textViewDesc.setText(object.getString("vehicle_no"));
            textViewVehicleName.setText(object.getString("brand_name") + " " + object.getString("model_name"));
            textViewSeller.setText(object.getString("name"));
            JSONArray arrayReviews = new JSONArray(object.getString("reviews"));
            for (int i = 0; i < arrayReviews.length(); i++) {
                JSONObject object1 = arrayReviews.getJSONObject(i);
                totalRating += Float.parseFloat(object1.getString("bargain_rating"));
                listReviews.add(object1);
            }
            if (arrayReviews.length() > 0) {
                avgRating = totalRating / (arrayReviews.length());
                ratingBarDriver.setRating(avgRating);
            }
            if (avgRating == 0) {
                ratingBarDriver.setRating(0);
                ((TextView) findViewById(R.id.textViewReviews)).setText(" No Ratings");
            }
            //textViewVehSeats.setText("Seats: " + object.getString("vehicle_seats"));
            textViewSeller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

                        new LaunchChatUtils(getApplicationContext(), VehicleDetailsActivity.this, new LaunchChatCallbacks() {
                            @Override
                            public void onChatCreatedSuccess(Intent intent) {
                                intent.putExtra("from", "bargain");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                                        LaunchChatUtils.launchChatMessageActivity(VehicleDetailsActivity.this, intent);

                                    }
                                });
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
            });
                    /*Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);

                    try {
                        intent.putExtra("takeOrder", true);
                        intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                        intent.putExtra(ConversationUIService.USER_ID, object.getString("coas_id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);*/


            findViewById(R.id.textViewReviews).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (avgRating > 0)
                        showRatingDialog();
                }
            });
            findViewById(R.id.buttonRequest).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

                        new LaunchChatUtils(getApplicationContext(), VehicleDetailsActivity.this, new LaunchChatCallbacks() {
                            @Override
                            public void onChatCreatedSuccess(Intent intent) {
                                intent.putExtra("from", "bargain");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                                        LaunchChatUtils.launchChatMessageActivity(VehicleDetailsActivity.this, intent);

                                    }
                                });
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
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showRatingDialog() {
        LayoutInflater li = LayoutInflater.from(VehicleDetailsActivity.this);
        View confirmDialog = li.inflate(R.layout.dialog_reviews, null);

        //Initizliaing confirm button fo dialog box and edittext of dialog box


        //Creating an alertdialog builder
        AlertDialog.Builder alert = new AlertDialog.Builder(VehicleDetailsActivity.this);

        //Adding our dialog box to the view of alert dialog
        alert.setView(confirmDialog);
        final AlertDialog alertDialog = alert.create();

        alertDialog.setCanceledOnTouchOutside(false);
        //Creating a LayoutInflater object for the dialog box

        RecyclerView recyclerView = confirmDialog.findViewById(R.id.recyclerViewReviews);
        RatingBar ratingBar = confirmDialog.findViewById(R.id.ratingBarDriver);
        TextView textViewDriver = confirmDialog.findViewById(R.id.textViewRating);
        ratingBar.setRating(avgRating);
        textViewDriver.setText("" + avgRating);
        DriverReviewsAdapter reviewsAdapter = new DriverReviewsAdapter(listReviews, this, getApplicationContext());
        recyclerView.setAdapter(reviewsAdapter);
        reviewsAdapter.notifyDataSetChanged();
        //Creating an alertdialog builder

       /* WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        alertDialog.getWindow().setAttributes(lp);*/
        // initiateVerification(false);
        alertDialog.show();
    }
}