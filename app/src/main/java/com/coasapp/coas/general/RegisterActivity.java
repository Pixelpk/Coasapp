package com.coasapp.coas.general;

import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.coasapp.coas.R;

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.InputValidator;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;
/*import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.core.helper.StringifyArrayList;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;*/
/*
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.core.helper.StringifyArrayList;
import com.connectycube.users.ConnectycubeUsers;
import com.connectycube.users.model.ConnectycubeUser;
*/

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity implements APPConstants {

    String name, lastName, email, phone, password, confirmPassword;
    LinearLayout layoutProgress;
    CircleImageView imageViewProfile;
    String file = "";
    EditText editTextConfirmOtp;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        checkPermissions();

        final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.slide_in_left);
        animation.setDuration(3000);

        final EditText editTextName = (EditText) findViewById(R.id.editTextFirstName);
        final EditText editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        final EditText editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        final EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        final EditText editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        final EditText editTextConfirm = (EditText) findViewById(R.id.editTextConfirmPassword);


        imageViewProfile = (CircleImageView) findViewById(R.id.imageViewProfile);
        layoutProgress = (LinearLayout) findViewById(R.id.layoutProgress);
        ImageView buttonReg = (ImageView) findViewById(R.id.buttonRegister);

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, 1);
            }
        });

        editTextName.setAnimation(animation);
        editTextLastName.setAnimation(animation);
        editTextPhone.setAnimation(animation);
        editTextEmail.setAnimation(animation);
        editTextPassword.setAnimation(animation);
        editTextConfirm.setAnimation(animation);

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = editTextName.getText().toString().trim();
                lastName = editTextLastName.getText().toString().trim();
                email = editTextEmail.getText().toString();
                phone = editTextPhone.getText().toString();
                password = editTextPassword.getText().toString().trim();
                confirmPassword = editTextConfirm.getText().toString().trim();
                if (name.equals("") || lastName.equals("") || password.equals("") || phone.equals("")) {
                    APPHelper.showToast(getApplicationContext(), "Fill all fields");
                } else if (!InputValidator.isValidEmail(email)) {
                    APPHelper.showToast(getApplicationContext(), "Invalid Email");
                } else if (!InputValidator.isValidMobile(phone)) {
                    APPHelper.showToast(getApplicationContext(), "Invalid Phone");
                } else if (!confirmPassword.equals(password)) {
                    APPHelper.showToast(getApplicationContext(), "Passwords do not match");
                } else {
                    layoutProgress.setVisibility(View.VISIBLE);
                    new Register().execute();
                }
            }
        });

    }


    public void checkPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = new String[]{android.Manifest.permission.READ_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!hasPermissions(RegisterActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    class Register extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            HashMap<String, String> map = new HashMap<>();
            map.put("first_name", name);
            map.put("last_name", lastName);
            map.put("email", email);
            map.put("phone", phone);
            map.put("password", password);
            return new RequestHandler().sendPostRequest(MAIN_URL + "register.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("response_code").equals("1")) {
                    String coasId = jsonObject.getString("coas_id");
                    /*final ConnectycubeUser user = new ConnectycubeUser("marvin18", "supersecurepwd");
                    user.setLogin(coasId);
                    user.setPassword(password);
                    user.setEmail(email);
                    user.setFullName(name+" "+lastName);
                    user.setPhone(phone);
                    StringifyArrayList<String> tags = new StringifyArrayList<String>();
                    tags.add("iphone");
                    tags.add("apple");
                    user.setTags(tags);

                    ConnectycubeUsers.signUp(user).performAsync(new EntityCallback<ConnectycubeUser>() {
                        @Override
                        public void onSuccess(ConnectycubeUser user, Bundle args) {
                            APPHelper.showToast(getApplicationContext(), "Registration Success");

                            onBackPressed();
                        }

                        @Override
                        public void onError(ResponseException error) {

                        }
                    });*/
                    /*String userId = jsonObject.getString("user_id");
                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("loggedIn", true);
                    editor.putString("name", name);
                    editor.putString("phone", phone);
                    editor.putString("userId", userId);
                    editor.apply();
                    if (!file.equals("")) {
                        new UploadImage().execute(userId);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), MessengerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }*/

                    //editor.putString("email", email);
                } else {

                    APPHelper.showToast(getApplicationContext(), jsonObject.getString("response"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            layoutProgress.setVisibility(View.GONE);
        }
    }
}
