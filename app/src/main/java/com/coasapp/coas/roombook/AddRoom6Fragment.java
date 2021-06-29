package com.coasapp.coas.roombook;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
public class AddRoom6Fragment extends Fragment implements APPConstants {

    LinearLayout layoutProgress;
    MyProductImagesAdapter adapter;
    ArrayList<ProductImages> productImagesArrayList = new ArrayList<>();
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    int pos = 0;


    public AddRoom6Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_room4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewImages);
        layoutProgress = view.findViewById(R.id.layoutProgress);
        final TextInputEditText editTextTitle = view.findViewById(R.id.editTextTitle);
        final TextInputEditText editTextDesc = view.findViewById(R.id.editTextDesc);
        final TextInputEditText editTextPpn = view.findViewById(R.id.editTextPpn);
        final TextInputEditText editTextPph = view.findViewById(R.id.editTextPph);
        adapter = new MyProductImagesAdapter(productImagesArrayList, getContext());
        recyclerView.setAdapter(adapter);
        adapter.setOnDeleteSelectedListener(new MyProductImagesAdapter.OnDeleteSelected() {
            @Override
            public void onClick(int position) {
                productImagesArrayList.remove(position);
                jsonObjectArrayList.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        adapter.setOnImageSelected(new MyProductImagesAdapter.OnImageSelected() {
            @Override
            public void onClick(int position) {


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
                String images = "[]";
                JSONArray array = new JSONArray();
                if (jsonObjectArrayList.size() == 0) {
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
                    if (title.equals("") || desc.equals("") || pph.equals("") || ppn.equals("")) {
                        APPHelper.showToast(getContext(), "Fill all fields");
                    } else {
                        images = array.toString();
                        new AddRoom().execute(title, desc, pph, ppn, images);
                    }
                }

            }
        });

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
            map.put("user_id", sharedPreferences2.getString("userId", "0"));
            return new RequestHandler().sendPostRequest(MAIN_URL + "add_room.php", map);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        APPHelper.showToast(getContext(), "Test");
        APPHelper.showLog("Image", "1");
        if (requestCode == 1 && resultCode == RESULT_OK) {
            APPHelper.showLog("Image", "1");

            // Get the Image from data
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            APPHelper.showLog("Image", "1");
            Uri mImageUri = data.getData();
            // Get the cursor
            Cursor cursor = getContext().getContentResolver().query(mImageUri,
                    filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imageEncoded = cursor.getString(columnIndex);
            ProductImages productImages = new ProductImages();
            productImages.setImage(imageEncoded);
            productImages.setStatus("0");
            productImagesArrayList.add(productImages);
            cursor.close();
            adapter.notifyDataSetChanged();
            int index = productImagesArrayList.size() - 1;
            //new UploadBill().execute(index);

        } else {
            APPHelper.showToast(getContext(), "No image selected");
        }
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
        new UploadBill().execute(index);
    }

    class UploadBill extends AsyncTask<Integer, Integer, String> {


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
                    JSONObject object = new JSONObject();
                    object.put("image", jsonObject.getString("response"));
                    object.put("status", "0");
                    jsonObjectArrayList.add(object);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
