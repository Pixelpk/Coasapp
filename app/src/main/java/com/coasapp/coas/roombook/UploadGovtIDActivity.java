package com.coasapp.coas.roombook;

import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.GetPath;
import com.coasapp.coas.utils.ResizeImage;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.webservices.UploadMultipart;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class UploadGovtIDActivity extends AppCompatActivity implements APPConstants {

    ImageView imageViewGovt;
    File img1;
    String imgPath = "";
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE/*, Manifest.permission.CAMERA*/};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_govt_id);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActivityCompat.requestPermissions(this, permissions, 99);

        imageViewGovt = findViewById(R.id.imageViewGovtId);
        findViewById(R.id.buttonChoose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imgPath.equals(""))
                    APPHelper.showToast(getApplicationContext(), "Upload Image");
                else
                    new UploadBill(0).execute();
            }
        });

        imageViewGovt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUp(findViewById(R.id.buttonChoose));
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 99) {
            boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            /*boolean granted2 = grantResults[1] == PackageManager.PERMISSION_GRANTED;*/
            if (!granted) {
                showAlert("You need to allow storage permissions to add product images");
            }
        }

    }


    public void showAlert(String errorMsg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UploadGovtIDActivity.this);
        alertDialogBuilder.setMessage(errorMsg);

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        ActivityCompat.requestPermissions(UploadGovtIDActivity.this, permissions, 99);
                    }
                });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void showPopUp(View v) {
        final int[] code = {0};
        PopupMenu popupMenu = new PopupMenu(UploadGovtIDActivity.this, v);
        //Inflating the Popup using xml file
        popupMenu.getMenuInflater().inflate(R.menu.menu_capture, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.menu_camera:
                        try {
                            Intent pictureIntent = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);
                            img1 = GetPath.createImageFile(UploadGovtIDActivity.this);
                            Uri photoURI = FileProvider.getUriForFile(UploadGovtIDActivity.this, getPackageName()+".provider", img1);
                            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    photoURI);

                            code[0] = 0;

                            startActivityForResult(pictureIntent, code[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.menu_gallery:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);

                        code[0] = 5;

                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), code[0]);
                        break;

                }

                return true;
            }
        });

        popupMenu.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            imgPath = ResizeImage.getResizedImage(img1.getAbsolutePath());
            Glide.with(getApplicationContext()).load(imgPath).into(imageViewGovt);
        } else if (requestCode == 5) {
            imgPath = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), data.getData()));
            Glide.with(getApplicationContext()).load(imgPath).into(imageViewGovt);
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
            findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            String res = "";
            String url = MAIN_URL + "upload_room_images.php";
            //UploadMultipart multipart = new UploadMultipart();
           /* for (int i = 0; i < productImagesArrayList.size(); i++) {

            }*/
            HashMap<String, String> map = new HashMap<>();
            map.put("file_name", String.valueOf(System.currentTimeMillis()));
            UploadMultipart multipart = new UploadMultipart(getApplicationContext());
            res = multipart.multipartRequest(url, map, imgPath, "room", "image/*");
            return res;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            findViewById(R.id.layoutProgress).setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                APPHelper.showLog("Upload", s);
                if (jsonObject.getString("response_code").equals("1")) {
                    imgPath = jsonObject.getString("response");
                    Intent intent = new Intent();
                    intent.putExtra("image", imgPath);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
}
