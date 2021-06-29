package com.coasapp.coas.roombook;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.GetPath;
import com.coasapp.coas.utils.ResizeImage;
import com.coasapp.coas.general.SelectAddressActivity;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;

import java.io.File;
import java.io.IOException;


public class AddRoomActivity extends AppCompatActivity implements APPConstants {
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 100;
    String TAG = "Place";
    File img1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences sharedPreferences = getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
        if (sharedPreferences.getString("mode", "add").equalsIgnoreCase("edit")) {
            getSupportActionBar().setTitle("Edit your listing");
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content, new AddRoomAddressFragment()).commit();
       /* findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onItemSelected(View v) {
                Intent intent = new Intent(getApplicationContext(), FacilitiesActivity.class);
                startActivityForResult(intent, 1);
            }
        });*/
    }

    public void checkPermissionStorage(int code) {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    code);
        } else {
            if (code == 1) {
                ChooseImage(code);
            }
            if (code == 0) {
                ChooseImage2(code);
            }

            //}
        }
    }



    void showPlace1() {
        Intent intent =
                new Intent(getApplicationContext(), SelectAddressActivity.class);
        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

        switch (requestCode) {
            case 1:
                if (granted) {
                    ChooseImage(1);
                }
                break;
            case 0:
                ChooseImage2(0);
                break;
        }

    }

    public void ChooseImage(int code) {
      /*  Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

       // Intent intent = new Intent();
        intent.setType("image/*");*/
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        //intent.setAction(Intent.ACTION_GET_CONTENT);
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), code);
    }

    public void ChooseImage2(int code) {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            img1 = GetPath.createImageFile(AddRoomActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri photoURI = FileProvider.getUriForFile(AddRoomActivity.this, getPackageName()+".provider", img1);
        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                photoURI);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        startActivityForResult(pictureIntent,
                code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
                if (fragment instanceof AddRoomAddressFragment) {
                    /*try {

                        Place place = PlaceAutocomplete.getPlace(this, data);
                        Log.i(TAG, "Place: " + place.getName());*/
                    // ((AddRoomAddressFragment) fragment).setAddress(place);

                    ((AddRoomAddressFragment) fragment).setAddress1(data.getStringExtra("address"), data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));
                   /* } catch (Exception e) {

                    }*/
                }

            } else if (requestCode == 1) {
                Uri uri = data.getData();
                String path = ResizeImage.getResizedImage(GetPath.getPath(getApplicationContext(), uri));
                //APPHelper.showToast(getApplicationContext(), path);
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
                if (fragment instanceof AddRoomTitleImageFragment) {
                    ((AddRoomTitleImageFragment) fragment).addImage(path);
                }
                if (fragment instanceof AddRoomMoreFragment) {
                    ((AddRoomMoreFragment) fragment).addImage(path);
                }

            } else if (requestCode == 0) {
                String path = ResizeImage.getResizedImage(img1.getAbsolutePath());
                //APPHelper.showToast(getApplicationContext(), path);

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
                if (fragment instanceof AddRoomTitleImageFragment) {
                    ((AddRoomTitleImageFragment) fragment).addImage(path);
                }
                if (fragment instanceof AddRoomMoreFragment) {
                    ((AddRoomMoreFragment) fragment).addImage(path);
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            // The user canceled the operation..
            Log.i(TAG, "Canceled");
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
