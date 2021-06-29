package com.coasapp.coas.general;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.coasapp.coas.R;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationDetail extends MyAppCompatActivity {

    private ImageView imageViewNotification;
    private TextView textViewTitle;
    private TextView textViewDesc;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2019-09-23 15:13:11 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        imageViewNotification = (ImageView)findViewById( R.id.imageViewNotification );
        textViewTitle = (TextView)findViewById( R.id.textViewTitle );
        textViewDesc = (TextView)findViewById( R.id.textViewDesc );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);
        findViews();
        try {
            JSONObject object = new JSONObject(getIntent().getStringExtra("details"));
            if(object.getString("image").equalsIgnoreCase("")){
                imageViewNotification.setVisibility(View.GONE);
            }
            else {
                Glide.with(getApplicationContext()).load(object.getString("image")).into(imageViewNotification);
            }
            textViewDesc.setText(object.getString("description"));
            textViewTitle.setText(object.getString("title"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
