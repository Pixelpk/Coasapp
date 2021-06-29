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

import com.coasapp.coas.bargain.ManageVehicleActivity;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.shopping.MyProductImagesAdapter;
import com.coasapp.coas.shopping.ProductImages;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.webservices.UploadMultipart;
import com.coasapp.coas.utils.APPConstants;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddRoomTitleImageFragment extends Fragment implements APPConstants {


    LinearLayout layoutProgress;
    MyProductImagesAdapter adapter;
    ArrayList<ProductImages> productImagesArrayList = new ArrayList<>();
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    int pos = 0, originalImageSize;
    RadioButton radioButton;

    public AddRoomTitleImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_room_title_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewImages);
        layoutProgress = view.findViewById(R.id.layoutProgress);
        final TextInputEditText editTextTitle = view.findViewById(R.id.editTextTitle);
        final TextInputEditText editTextDesc = view.findViewById(R.id.editTextDesc);
        radioButton = view.findViewById(R.id.switchDefaultImage);
        editTextDesc.setText(sharedPreferences.getString("desc", ""));
        editTextTitle.setText(sharedPreferences.getString("title", ""));
        adapter = new MyProductImagesAdapter(productImagesArrayList, getContext());
        recyclerView.setAdapter(adapter);
        adapter = new MyProductImagesAdapter(productImagesArrayList, getContext());
        recyclerView.setAdapter(adapter);
        String images = sharedPreferences.getString("roomImages", "[]");
        try {
            JSONArray jsonArray = new JSONArray(images);

            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    originalImageSize = i;
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ProductImages productImages = new ProductImages();
                    productImages.setId(jsonObject.getString("image_id"));
                    productImages.setImage(jsonObject.getString("image"));
                    productImages.setColor("#ff000000");
                    productImages.setSource("url");
                    productImages.setStatus(jsonObject.getString("status"));
                    productImagesArrayList.add(productImages);
                    /*JSONObject object = new JSONObject();
                    object.put("image", jsonObject.getString("image"));
                    object.put("status", jsonObject.getString("status"));
                    jsonObjectArrayList.add(object);*/
                    if (jsonObject.getString("status").equals("1")) {
                        productImagesArrayList.get(i).setColor("#ffd50000");
                        radioButton.setChecked(true);
                    }

                }


                /*productImagesArrayList.get(0).setColor("#ffd50000");
                radioButton.setChecked(true);*/

            } else {
                radioButton.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
        adapter.setOnDeleteSelectedListener(new MyProductImagesAdapter.OnDeleteSelected() {
            @Override
            public void onClick(int position) {

                if(productImagesArrayList.get(position).getStatus().equals("1")){
                    APPHelper.showToast(getActivity().getApplicationContext(),"Cannot delete default image");
                }
                else {
                    productImagesArrayList.remove(position);
                    //jsonObjectArrayList.remove(position);
                    if (productImagesArrayList.size() > 0) {
                        radioButton.setVisibility(View.VISIBLE);
                    } else {
                        radioButton.setVisibility(View.GONE);
                    }
                    if (productImagesArrayList.size() == 1) {
                        radioButton.setVisibility(View.VISIBLE);
                        radioButton.setChecked(true);
                        productImagesArrayList.get(0).setStatus("1");
                    }
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

        view.findViewById(R.id.cardViewAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUp(v);
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
                    if (productImagesArrayList.size() > 0) {
                        productImagesArrayList.get(pos).setStatus("1");
                        adapter.notifyDataSetChanged();
                    } else {
                        radioButton.setChecked(false);
                    }
                }

            }
        });
        view.findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String images = "[]";
                JSONArray array = new JSONArray();
                if (productImagesArrayList.size() == 0) {
                    APPHelper.showToast(getContext(), "Please add image(s)");
                } else {
                   /* try {
                        jsonObjectArrayList.get(0).put("status", 1);
                        for (int i = 0; i < jsonObjectArrayList.size(); i++) {

                            array.put(jsonObjectArrayList.get(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                    String title = editTextTitle.getText().toString().trim();
                    String desc = editTextDesc.getText().toString().trim();
                    if (title.equals("") && desc.equals("")) {
                        APPHelper.showToast(getContext(), "Enter Title & Descroption");
                    } else {
                        images = array.toString();
                        images = new Gson().toJson(productImagesArrayList);
                        APPHelper.showLog("Images", images);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("title", title);
                        editor.putString("desc", desc);
                        editor.putString("images", images);
                        editor.apply();
                        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content, new AddRoomRulesFragment()).addToBackStack(null).commit();
                    }


                    /*String ppn = editTextPpn.getText().toString();
                    String pph = editTextPph.getText().toString();
                    if (title.equals("") || desc.equals("") || pph.equals("") || ppn.equals("")) {
                        APPHelper.showToast(getContext(), "Fill all fields");
                    } else {
                        images = array.toString();
                        new AddRoom6Fragment.AddRoom().execute(title, desc, pph, ppn, images);
                    }*/

                }

            }
        });

    }

    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        //Inflating the Popup using xml file
        popupMenu.getMenuInflater().inflate(R.menu.menu_capture, popupMenu.getMenu());

        //registering popup with OnMenuItemClickListener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.menu_camera:


                        ((AddRoomActivity) getActivity()).checkPermissionStorage(0);

                        break;
                    case R.id.menu_gallery:
                        ((AddRoomActivity) getActivity()).checkPermissionStorage(1);
                        break;
                }
                return true;
            }
        });

        popupMenu.show();
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
            productImages.setSource("file");
            productImages.setStatus("0");
            if (productImagesArrayList.size() == 1) {
                productImages.setStatus("1");
            }
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
        if(productImagesArrayList.size()>0){
        productImages.setColor("#ff000000");
        productImages.setStatus("0");}
        else {
            productImages.setColor("#ffd50000");
            productImages.setStatus("1");
        }
        productImages.setSource("file");
        productImagesArrayList.add(productImages);
        if (productImagesArrayList.size() > 0) {
            radioButton.setVisibility(View.VISIBLE);
        } else {
            radioButton.setVisibility(View.GONE);
        }
        if (productImagesArrayList.size() == 1) {
            radioButton.setVisibility(View.VISIBLE);
            radioButton.setChecked(true);
            productImagesArrayList.get(0).setStatus("1");
        }
        adapter.notifyDataSetChanged();
        int index = productImagesArrayList.size() - 1;
        new UploadBill().execute(index);


    }

    class UploadBill extends AsyncTask<Integer, Integer, String> {

        int index;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            index = params[0];
            String res = "";
            String url = MAIN_URL + "upload_room_images.php";
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
            layoutProgress.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {
//                    JSONObject object = new JSONObject();
//                    object.put("image", jsonObject.getString("response"));
//                    object.put("status", "0");
                    //jsonObjectArrayList.add(object);
                    productImagesArrayList.get(index).setSource("url");
                    productImagesArrayList.get(index).setImage(jsonObject.getString("response"));
                }
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Failed Please try again", Toast.LENGTH_SHORT).show();
                productImagesArrayList.remove(index);
                adapter.notifyDataSetChanged();

                e.printStackTrace();
            }

        }
    }
}
