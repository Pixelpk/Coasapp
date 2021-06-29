package com.coasapp.coas.general;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ChangePasswordActivity extends AppCompatActivity implements APPConstants {

    LinearLayout layoutProgress;
    String oldPass, newPass, confirmPass;

    String file = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final EditText editTextOld = findViewById(R.id.edtOldPass);
        final EditText editTextNew = findViewById(R.id.edtNewPass);
        final EditText editTextConfirm = findViewById(R.id.edtConPass);
        layoutProgress = findViewById(R.id.layoutProgress);
        findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPass = editTextOld.getText().toString().trim();
                newPass = editTextNew.getText().toString().trim();
                confirmPass = editTextConfirm.getText().toString().trim();
                if (oldPass.equals("") || newPass.equals("") || confirmPass.equals("")) {
                    APPHelper.showToast(getApplicationContext(), "Fill all values");
                } else if (!newPass.equals(confirmPass)) {
                    APPHelper.showToast(getApplicationContext(), "New Password and Confirm Password mismatch");
                } else {
                    new ChangePass().execute();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("action", getIntent().getIntExtra("action", 0));
        setResult(RESULT_OK, intent);
        finish();
    }

    class ChangePass extends AsyncTask<Void, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> map = new HashMap<>();
            map.put("old", oldPass);
            map.put("new", newPass);
            map.put("user_id", sharedPreferences.getString("userId", "0"));

            return new RequestHandler().sendPostRequest(MAIN_URL + "change_password.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutProgress.setVisibility(View.GONE);

            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("response_code").equals("1")) {
                    SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("loggedIn", false);
                    editor.apply();
                    Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.log_out_successful), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), COASLoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                   /* UserLogoutTask.TaskListener userLogoutTaskListener = new UserLogoutTask.TaskListener() {

                        @Override
                        public void onSuccess(Context context) {
                            userLogoutTask = null;
                            SharedPreferences sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("loggedIn", false);
                            editor.apply();
                            Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.log_out_successful), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, COASLoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            userLogoutTask = null;
                            AlertDialog alertDialog = new AlertDialog.Builder(ChangePasswordActivity.this).create();
                            alertDialog.setTitle(getString(R.string.text_alert));
                            alertDialog.setMessage(exception.toString());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok_alert),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            if (!isFinishing()) {
                                alertDialog.show();
                            }
                        }
                    };

                    userLogoutTask = new UserLogoutTask(userLogoutTaskListener, ChangePasswordActivity.this);
                    userLogoutTask.execute((Void) null);
*/                }

                APPHelper.showToast(getApplicationContext(), object.getString("response"));
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

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }
}
