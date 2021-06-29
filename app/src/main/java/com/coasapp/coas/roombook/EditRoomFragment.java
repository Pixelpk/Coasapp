package com.coasapp.coas.roombook;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;

import com.coasapp.coas.R;
import com.coasapp.coas.shopping.MyProductImagesAdapter;
import com.coasapp.coas.shopping.ProductImages;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.webservices.UploadMultipart;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditRoomFragment extends Fragment implements APPConstants {
    LinearLayout layoutProgress;
    MyProductImagesAdapter adapter;
    ArrayList<ProductImages> productImagesArrayList = new ArrayList<>();

    ArrayList<ProductImages> productImagesArrayListNew = new ArrayList<>();
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    ArrayList<JSONObject> jsonObjectArrayListNew = new ArrayList<>();
    int originalImageSize = 0;
    int pos = 0;
    String status;

    public EditRoomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewImages);
        layoutProgress = view.findViewById(R.id.layoutProgress);
        Switch switchStatus = view.findViewById(R.id.switchStatus);
        final TextInputEditText editTextTitle = view.findViewById(R.id.editTextTitle);
        final TextInputEditText editTextDesc = view.findViewById(R.id.editTextDesc);
        final TextInputEditText editTextPpn = view.findViewById(R.id.editTextPpn);
        final RadioButton radioButton = view.findViewById(R.id.switchDefaultImage);
        final TextInputEditText editTextPph = view.findViewById(R.id.editTextPph);

        editTextDesc.setText(sharedPreferences.getString("desc", ""));
        editTextTitle.setText(sharedPreferences.getString("title", ""));
        editTextPph.setText(sharedPreferences.getString("priceperhour", "0.00"));
        editTextPpn.setText(sharedPreferences.getString("pricepernight", "0.00"));
        String images = sharedPreferences.getString("roomImages", "[]");
        status = sharedPreferences.getString("status", "1");
        if (sharedPreferences.getString("status", "1").equalsIgnoreCase("1")) {
            switchStatus.setChecked(true);
        }
        switchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    status = "1";
                } else {
                    status = "0";
                }
            }
        });

        adapter = new MyProductImagesAdapter(productImagesArrayList, getContext());
        recyclerView.setAdapter(adapter);
        try {
            JSONArray jsonArray = new JSONArray(images);
            for (int i = 0; i < jsonArray.length(); i++) {
                originalImageSize = i;
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ProductImages productImages = new ProductImages();
                productImages.setId(jsonObject.getString("image_id"));
                productImages.setImage(jsonObject.getString("image"));
                productImages.setColor("#ff000000");
                productImages.setStatus(jsonObject.getString("status"));
                productImagesArrayList.add(productImages);
            }

            productImagesArrayList.get(0).setColor("#ffd50000");
            radioButton.setChecked(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
        adapter.setOnDeleteSelectedListener(new MyProductImagesAdapter.OnDeleteSelected() {
            @Override
            public void onClick(int position) {
                ProductImages productImages = productImagesArrayList.get(position);

                if (productImages.getStatus().equalsIgnoreCase("1")) {
                    APPHelper.showToast(getContext(), "Cannot remove default image");
                } else {

                    new DelImage(position).execute(productImages.getId());
                }
            }
        });

        radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    APPHelper.showLog("Pos", String.valueOf(pos));
                    for (int i = 0; i < productImagesArrayList.size(); i++) {
                        productImagesArrayList.get(i).setStatus("0");
                    }
                    productImagesArrayList.get(pos).setStatus("1");
                    adapter.notifyDataSetChanged();
                }

            }
        });

        adapter.setOnImageSelected(new MyProductImagesAdapter.OnImageSelected() {
            @Override
            public void onClick(int position) {

                pos = position;

                ProductImages productImages = productImagesArrayList.get(position);
                for (int i = 0; i < productImagesArrayList.size(); i++) {
                    productImagesArrayList.get(i).setColor("#ff000000");
                }
                productImages.setColor("#ffd50000");
                if (productImages.getStatus().equalsIgnoreCase("1")) {
                    radioButton.setChecked(true);
                } else {
                    radioButton.setChecked(false);
                }
                adapter.notifyDataSetChanged();
            }
        });

        view.findViewById(R.id.imageViewAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddRoomActivity) getActivity()).ChooseImage(0);
            }
        });

        view.findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray array = new JSONArray();

                String images = "[]";
                try {
                    jsonObjectArrayList.clear();
                    for (int i = 0; i < productImagesArrayList.size(); i++) {
                        JSONObject object = new JSONObject();
                        object.put("image", productImagesArrayList.get(i).getImage());
                        object.put("status", productImagesArrayList.get(i).getStatus());
                        jsonObjectArrayList.add(object);
                    }

                    for (int i = 0; i < jsonObjectArrayList.size(); i++) {
                        array.put(jsonObjectArrayList.get(i));
                    }
                } catch (JSONException e) {
                    APPHelper.showToast(getContext(), e.getMessage());
                }

                /*if (jsonObjectArrayList.size() == 0) {
                    APPHelper.showToast(getContext(), "Please add image(s)");
                } else {
                    try {
                        jsonObjectArrayList.get(0).put("status", 1);
                        for (int i = 0; i < jsonObjectArrayList.size(); i++) {
                            array.put(jsonObjectArrayList.get(i));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String title = editTextTitle.getText().toString().trim();
                    String desc = editTextDesc.getText().toString().trim();
                    String ppn = editTextPpn.getText().toString();
                    String pph = editTextPph.getText().toString();
                    images = array.toString();
                    new AddRoom().execute(title, desc, pph, ppn);
                }*/
                if (jsonObjectArrayList.size() == 0) {
                    APPHelper.showToast(getContext(), "Please add image(s)");
                } else {
                    String title = editTextTitle.getText().toString().trim();
                    String desc = editTextDesc.getText().toString().trim();
                    String ppn = editTextPpn.getText().toString();
                    String pph = editTextPph.getText().toString();
                    images = array.toString();
                    APPHelper.showLog("Images", images);
                    new AddRoom().execute(title, desc, pph, ppn, images);
                }


            }
        });

    }

    public void addImage(String imageEncoded) {


        APPHelper.showLog("Image", "1");

        ProductImages productImages = new ProductImages();
        productImages.setImage(imageEncoded);
        productImages.setId(String.valueOf(System.currentTimeMillis()));
        productImages.setColor("#ff000000");
        productImages.setStatus("0");
        productImagesArrayList.add(productImages);

        adapter.notifyDataSetChanged();
        int index = productImagesArrayList.size() - 1;
        new UploadBill(index).execute(index);
    }

    class DelImage extends AsyncTask<String, Integer, String> {

        int index;

        public DelImage(int index) {
            this.index = index;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(final String... params) {
            String res = "";
            String url = MAIN_URL + "delete_room_image.php?image_id=" + params[0];
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    APPHelper.showToast(getContext(), params[0]);

                }
            });

            return new RequestHandler().sendGetRequest(url);
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {
                    /*JSONObject object = new JSONObject();
                    object.put("image", jsonObject.getString("response"));
                    object.put("status", "0");
                    jsonObjectArrayList.add(object);*/
                    productImagesArrayList.remove(index);
                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class AddRoom extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences2 = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
            HashMap<String, String> map = new HashMap<>();
            map.put("room_id", sharedPreferences.getString("roomId", "0"));
            map.put("room_name", strings[0]);
            map.put("room_desc", strings[1]);
            map.put("pph", strings[2]);
            map.put("ppn", strings[3]);
            map.put("type", sharedPreferences.getString("roomType", "General"));
            map.put("share", sharedPreferences.getString("roomAccType", "Private"));
            map.put("guests", sharedPreferences.getString("guests", "1"));
            map.put("bath_num", sharedPreferences.getString("bathNum", "1"));
            map.put("bath_type", sharedPreferences.getString("bathType", "General"));
            map.put("address", sharedPreferences.getString("roomAddress", "General"));
            map.put("city", sharedPreferences.getString("roomCity", "General"));
            map.put("state", sharedPreferences.getString("roomState", "General"));
            map.put("zip", sharedPreferences.getString("roomZip", "General"));
            map.put("country", sharedPreferences.getString("roomCountry", "General"));
            map.put("lat", sharedPreferences.getString("roomLat", "General"));
            map.put("lng", sharedPreferences.getString("roomLng", "General"));
            map.put("parties", sharedPreferences.getString("parties", "no"));
            map.put("events", sharedPreferences.getString("events", "no"));
            map.put("children", sharedPreferences.getString("children", "no"));
            map.put("infants", sharedPreferences.getString("infants", "no"));
            map.put("pets", sharedPreferences.getString("pets", "no"));
            map.put("surveillance", sharedPreferences.getString("surveillance", "no"));
            map.put("smoking", sharedPreferences.getString("smoking", "no"));
            map.put("weapons", sharedPreferences.getString("weapons", "no"));
            map.put("bedrooms", sharedPreferences.getString("roomBedrooms", "[]"));
            map.put("amenity_limits", sharedPreferences.getString("limits", "no"));
            map.put("rules", sharedPreferences.getString("rules", "no"));
            map.put("terms", sharedPreferences.getString("terms", "no"));
            map.put("amenities", sharedPreferences.getString("roomAmenities", "[]"));
            map.put("images", strings[4]);
            map.put("status", status);
            APPHelper.showLog("RoomUpdate", strings[0]);
            map.put("user_id", sharedPreferences2.getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "update_room.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showToast(getContext(), jsonObject.getString("response"));
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                getActivity().setResult(RESULT_OK);
                getActivity().finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class UploadBill extends AsyncTask<Integer, Integer, String> {


        int index;

        public UploadBill(int index) {
            this.index = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Integer... params) {
            String res = "";
            String url = MAIN_URL + "upload_room_image.php";
            //UploadMultipart multipart = new UploadMultipart();
           /* for (int i = 0; i < productImagesArrayList.size(); i++) {

            }*/
            HashMap<String, String> map = new HashMap<>();
            map.put("status", productImagesArrayList.get(params[0]).getStatus());
            APPHelper.showLog("image", "" + productImagesArrayList.get(params[0]).getStatus());
            map.put("file_name", "" + System.currentTimeMillis());
            UploadMultipart multipart = new UploadMultipart(getContext());
            res = multipart.multipartRequest(url, map, productImagesArrayList.get(params[0]).getImage(), "room", "image/*");
            return res;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {
                    /*JSONObject object = new JSONObject();
                    object.put("image", jsonObject.getString("response"));
                    object.put("status", "0");
                    jsonObjectArrayList.add(object);*/
                    productImagesArrayList.get(index).setImage(jsonObject.getString("response"));
                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


}
