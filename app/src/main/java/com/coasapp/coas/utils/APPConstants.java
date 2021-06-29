package com.coasapp.coas.utils;

import com.coasapp.coas.ApplozicSampleApplication;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.FOREGROUND_SERVICE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by AK INFOPARK on 06-06-2018.
 */

public interface APPConstants {

    //String MAIN_URL = "http://easemypay.in/coas/webservices/";
    String HOST = "https://www.coasapp.com/";
    String MAIN_URL = HOST + "webservices/";
    String MAIN_URL_PAY = HOST + "payment/";
    String MAIN_URL_IMAGE = HOST + "";
    String baseUrlLocal = "file:///android_asset/";
    String baseUrlLocal2 = "https://www.coasapp.com/";
    //String MAIN_URL = "http://192.168.10.132/coas/webservices/";
    String APP_PREF = "AppPrefs";
    String ROOM_DETAILS = "RoomDetails";
    String CAR_DETAILS = "CarDetails";
    String SPACE = " ";
    String HYPHEN = " - ";
    String SINGLE_QUOTE = "'";
    int SPLASH_DISPLAY_LENGTH = 1000;
    public final static String PUBNUB_PUBLISH_KEY = "pub-c-83d88ea2-2e8e-45b0-82b6-fb91fd9178cf";

    public final static String MAP_API_KEY = "AIzaSyCR_sggK-fSocX3H0iLH0EF3d3tFf8BtgA";
    public final static String PUBNUB_SUBSCRIBE_KEY = "sub-c-fc2c08b2-c7ba-11e8-9ca5-92bdce849b25";
    public final static String PUBNUB_CHANNEL_NAME = "drivers_location";
    String STRIPE_KEY_LIVE = "pk_live_qyrs4j4nbNbitUd2EPTnlFIy";
    String STRIPE_KEY = "pk_test_qVXXcaCNUb5HQeYh9Tl0VNbJ";
    SimpleDateFormat sdfNativeDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
    SimpleDateFormat sdfDatabaseDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    SimpleDateFormat sdfNativeDateTime = new SimpleDateFormat("MM-dd-yyyy h:mm a", Locale.getDefault());
    SimpleDateFormat sdfDatabaseDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    SimpleDateFormat sdfNativeTime = new SimpleDateFormat("h:mm a", Locale.getDefault());
    SimpleDateFormat sdfDatabaseTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
    SimpleDateFormat sdfNativeRoom = new SimpleDateFormat("MM-dd-yyyy h:mm a", Locale.getDefault());
    SimpleDateFormat sdfDatabaseRoom = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    String[] permissions = new String[]{READ_CONTACTS, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION,FOREGROUND_SERVICE, RECORD_AUDIO, CAMERA};
    String[] permissions2 = new String[]{READ_CONTACTS, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION,ACCESS_BACKGROUND_LOCATION, RECORD_AUDIO, CAMERA};
    String ChatPass = "12345678";
    int UP_BUTTON = android.R.id.home;
    String attachmentChatUrl = "https://api.connectycube.com/blobs/";

    String KEY_ORIGINAL_MSG = "original_msg", ORIGINAL_MSG_ID = "original_msg_id", ORIGINAL_MSG_BODY = "original_msg_body",
            ORIGINAL_MSG_SENDER_ID = "original_msg_sender_id", ORIGINAL_MSG_SENDER_NAME = "original_msg_sender_name",
            ORIGINAL_MSG_URL = "original_attachment_url", ORIGINAL_MSG_TYPE = "original_attachment_type",
            ORIGINAL_MSG_DATA = "original_attachment_data", ORIGINAL_MSG_CONTENT_TYPE = "original_content_type";

    String SENDBIRDURL = "https://api-" + ApplozicSampleApplication.APP_ID1 + ".sendbird.com/v3/users";

    public static final String incoming_user_id = "incoming_user_id";
}
