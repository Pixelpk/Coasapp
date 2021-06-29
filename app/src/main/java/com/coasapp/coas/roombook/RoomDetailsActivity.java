package com.coasapp.coas.roombook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.bargain.VehicleDetailsActivity;
import com.coasapp.coas.general.FullScreenImageSlide;
import com.coasapp.coas.shopping.ProductDetailsActivity;
import com.coasapp.coas.shopping.ProductImages;
import com.coasapp.coas.shopping.ProductImagesAdapter;
import com.coasapp.coas.utils.LaunchChatCallbacks;
import com.coasapp.coas.utils.LaunchChatUtils;
import com.coasapp.coas.utils.OnItemClick;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;
import com.connectycube.chat.ConnectycubeRestChatService;
import com.connectycube.chat.model.ConnectycubeChatDialog;
import com.connectycube.chat.model.ConnectycubeDialogType;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.messenger.ChatMessageActivity;
import com.connectycube.messenger.data.User;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.connectycube.messenger.ChatMessageActivityKt.EXTRA_CHAT;


public class RoomDetailsActivity extends AppCompatActivity implements APPConstants {

    ArrayList<String> arrayListImages = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayListAmenities = new ArrayList<>();
    String price = "0.00";
    int pkg;
    ArrayList<ProductImages> imagesArrayList = new ArrayList<>();

    ProductImagesAdapter productImagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);
        boolean you = false;
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "0");

       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Room Details");

        RecyclerView recyclerViewImages = findViewById(R.id.recyclerViewImages);
        RecyclerView recyclerViewBed = findViewById(R.id.recyclerViewBedroom);
        RecyclerView recyclerViewAmenities = findViewById(R.id.recyclerViewAmenities);
        TextView textViewHost = findViewById(R.id.textViewHoster);
        TextView textViewTerms = findViewById(R.id.textViewTerms);
        TextView textViewAddress = findViewById(R.id.textViewRoomAddress);
        CircleImageView imageViewHost = findViewById(R.id.imageViewHosted);
        RecyclerView recyclerViewImagesThumb = findViewById(R.id.recyclerViewImagesThumb);

        final Spinner spinnerType = findViewById(R.id.spinnerType);
        TextView textViewDesc = findViewById(R.id.textViewDesc);
        Button buttonBook = findViewById(R.id.buttonBook);
        recyclerViewImagesThumb.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        productImagesAdapter = new ProductImagesAdapter(imagesArrayList, this, getApplicationContext());
        recyclerViewImagesThumb.setAdapter(productImagesAdapter);
        try {
            final JSONObject object = new JSONObject(getIntent().getStringExtra("details"));
            String hostId = object.getString("user_id");
            textViewDesc.setText(Html.fromHtml(object.getString("room_desc")).toString());
            textViewHost.setText(object.getString("name"));
            textViewAddress.setText(object.getString("room_city") + HYPHEN + object.getString("room_state"));
            if (userId.equalsIgnoreCase(hostId)) {
                textViewHost.setText("You");
                you = true;
            }
            //textViewTerms.setText(object.getString("room_rules"));
            if (object.getString("party_allowed").equalsIgnoreCase("yes"))
                textViewTerms.append("Party Allowed");
            else {
                textViewTerms.append("Party Not Allowed");
            }
            if (object.getString("event_allowed").equalsIgnoreCase("yes"))
                textViewTerms.append("\nEvents Allowed");
            else {
                textViewTerms.append("\nEvents Not Allowed");
            }
            if (object.getString("smoking_allowed").equalsIgnoreCase("yes"))
                textViewTerms.append("\nSmoking Allowed");
            else {
                textViewTerms.append("\nSmoking Not Allowed");
            }
            if (object.getString("room_SuitableChildren").equalsIgnoreCase("yes"))
                textViewTerms.append("\nChildren Allowed");
            else {
                textViewTerms.append("\nNot suitable for Children");
            }
            if (object.getString("room_SuitablePets").equalsIgnoreCase("yes"))
                textViewTerms.append("\nPets Allowed");
            else {
                textViewTerms.append("\nNot suitable for Pets");
            }
            textViewTerms.append(("\n" + object.getString("room_rules")));
            Glide.with(getApplicationContext()).load(MAIN_URL_IMAGE + object.getString("image")).into(imageViewHost);
            boolean finalYou = you;
            imageViewHost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        if (!object.getString("user_id").equalsIgnoreCase(sharedPreferences.getString("userId", "0"))) {
                            findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);

                            new LaunchChatUtils(getApplicationContext(), RoomDetailsActivity.this, new LaunchChatCallbacks() {
                                @Override
                                public void onChatCreatedSuccess(Intent intent) {
                                    intent.putExtra("from","renting");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            LaunchChatUtils.launchChatMessageActivity(RoomDetailsActivity.this,intent);
                                            findViewById(R.id.layoutProgress).setVisibility(View.GONE);

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

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                   /* Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                    intent.putExtra("takeOrder", true);
                    intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                    intent.putExtra(ConversationUIService.USER_ID, getIntent().getStringExtra("coas_id"));
                    startActivity(intent);*/


                }
            });
            ImagesAdapter imagesAdapter = new ImagesAdapter(arrayListImages, this, getApplicationContext());
            recyclerViewImages.setAdapter(imagesAdapter);
            imagesAdapter.setOnItemClick(new OnItemClick() {
                @Override
                public void onItemClick(int position) {
                    Intent intent = new Intent(getApplicationContext(), FullScreenImageSlide.class);
                    intent.putExtra("position", position);
                    intent.putExtra("images", arrayListImages);
                    startActivity(intent);
                }
            });

            BedroomsAdapter bedroomsAdapter = new BedroomsAdapter(arrayList);
            recyclerViewBed.setAdapter(bedroomsAdapter);
            AmenitiesAdapter amenitiesAdapter = new AmenitiesAdapter(getApplicationContext(), this, arrayListAmenities);
            recyclerViewAmenities.setAdapter(amenitiesAdapter);
            try {
                JSONArray jsonArrayImages = new JSONArray(object.getString("images"));
                for (int i = 0; i < jsonArrayImages.length(); i++) {
                    arrayListImages.add(MAIN_URL_IMAGE + jsonArrayImages.getJSONObject(i).getString("image"));
                    ProductImages productImages = new ProductImages();
                    productImages.setImage(jsonArrayImages.getJSONObject(i).getString("image"));
                    productImages.setSelected(false);
                    imagesArrayList.add(productImages);
                }
                JSONArray jsonArrayBedrooms = new JSONArray(object.getString("bedrooms"));
                for (int i = 0; i < jsonArrayBedrooms.length(); i++) {
                    JSONObject jsonObject = jsonArrayBedrooms.getJSONObject(i);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("bedroom", jsonObject.getString("bedroom_name"));
                    map.put("king", jsonObject.getString("king"));
                    map.put("queen", jsonObject.getString("queen"));
                    map.put("single", jsonObject.getString("singlebed"));
                    map.put("double", jsonObject.getString("doublebed"));
                    arrayList.add(map);
                }
                JSONArray jsonArrayAmenities = new JSONArray(object.getString("amenities"));
                for (int i = 0; i < jsonArrayAmenities.length(); i++) {
                    JSONObject jsonObject = jsonArrayAmenities.getJSONObject(i);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("amenity", jsonObject.getString("amenity"));
                    map.put("image", MAIN_URL_IMAGE + jsonObject.getString("amenity_image"));
                    arrayListAmenities.add(map);
                }
                amenitiesAdapter.notifyDataSetChanged();
                bedroomsAdapter.notifyDataSetChanged();
                imagesAdapter.notifyDataSetChanged();
                productImagesAdapter.notifyDataSetChanged();
                imagesArrayList.get(0).setSelected(true);
                productImagesAdapter.notifyDataSetChanged();
                productImagesAdapter.setOnImageSelected(new ProductImagesAdapter.OnImageSelected() {
                    @Override
                    public void onImageSelected(int position) {
                        for (int i = 0; i < imagesArrayList.size(); i++) {
                            imagesArrayList.get(i).setSelected(false);
                        }
                        imagesArrayList.get(position).setSelected(true);
                        imagesAdapter.notifyDataSetChanged();
                        recyclerViewImages.scrollToPosition(position);
                        productImagesAdapter.notifyDataSetChanged();
                        APPHelper.showLog("Image", imagesArrayList.get(position).getImage());

                    }

                });

            } catch (JSONException e) {
                e.printStackTrace();
            }


            buttonBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (spinnerType.getSelectedItemPosition() == 0) {
                        APPHelper.showToast(getApplicationContext(), "Please select package");
                    } else {
                        Intent intent = new Intent(getApplicationContext(), RoomBookingActivity.class);

                        try {
                            intent.putExtra("pkg", pkg);
                            intent.putExtra("details", object.toString());
                            intent.putExtra("unitprice", price);
                            intent.putExtra("title", object.getString("room_title"));
                            intent.putExtra("rules", object.getString("room_rules"));
                            intent.putExtra("terms", "");
                            intent.putExtra("room_id", object.getString("room_id"));

                            startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            recyclerViewImages.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerViewImages.getLayoutManager());
                    int pos = layoutManager.findFirstVisibleItemPosition();
                    for (int i = 0; i < imagesArrayList.size(); i++) {
                        imagesArrayList.get(i).setSelected(false);
                    }
                    imagesArrayList.get(pos).setSelected(true);
                    productImagesAdapter.notifyDataSetChanged();
                    imagesAdapter.notifyDataSetChanged();
                }
            });
            TextView textViewRoom = findViewById(R.id.textViewRoomName);
            final TextView textViewPrice = findViewById(R.id.textViewPrice);
            textViewRoom.setText(object.getString("room_title"));
            spinnerType.setSelection(1);

            if (Double.parseDouble(object.getString("pricepernight")) == 0) {
                spinnerType.setVisibility(View.GONE);
                price = object.getString("priceperhour");
                pkg = 1;
                textViewPrice.setText(formatter.format(Double.valueOf(price)) + "/hour");

            }

            if (Double.parseDouble(object.getString("priceperhour")) == 0) {
                spinnerType.setVisibility(View.GONE);
                price = object.getString("pricepernight");
                pkg = 2;
                textViewPrice.setText(formatter.format(Double.valueOf(price)) + "/night");
            }

            if (object.getString("room_negotiable").equalsIgnoreCase("yes")) {
                textViewPrice.append(" Negotiable");
            }
            spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    pkg = position;
                    try {
                        if (position == 2) {


                            price = object.getString("pricepernight");

                            textViewPrice.setText(object.getString("room_currency") + price + "/night");
                        } else if (position == 1) {
                            price = object.getString("priceperhour");

                            textViewPrice.setText(object.getString("room_currency") + price + "/hour");

                        } else {
                            textViewPrice.setText("");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        /*TextView textView = (TextView) findViewById(R.id.textViewDesc);
        textView.setText(Html.fromHtml(getString(R.string.desc), null, new UlTagHandler()));*/
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
}
