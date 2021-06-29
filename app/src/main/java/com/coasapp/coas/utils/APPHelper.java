package com.coasapp.coas.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;

import androidx.appcompat.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.appcompat.widget.PopupMenu;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;


import com.coasapp.coas.R;

import com.coasapp.coas.general.SelectAddressActivity;
import com.coasapp.coas.shopping.AddProductActivity;
import com.connectycube.chat.ConnectycubeRestChatService;
import com.connectycube.chat.model.ConnectycubeChatDialog;
import com.connectycube.core.EntityCallback;
import com.connectycube.core.exception.ResponseException;
import com.connectycube.messenger.data.AppDatabase;
import com.connectycube.messenger.data.User;
import com.connectycube.messenger.data.UserRepository;
import com.connectycube.users.model.ConnectycubeUser;
import com.connectycube.videochat.RTCSession;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;

import static com.connectycube.messenger.utilities.SharedPreferencesManagerKt.CUBE_USER_ID;

/**
 * Created by AK INFOPARK on 31-05-2018.
 */

public class APPHelper implements APPConstants {


    public static void setMapStyle(Context context, GoogleMap googleMap){
        int currentNightMode = getNightModeSetting(context);
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                Log.i("MapStyle", "Style parsing Not Needed");
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = googleMap.setMapStyle(

                            MapStyleOptions.loadRawResourceStyle(
                                    context, R.raw.map_night));
                    Log.i("MapStyle", "Style parsing "+success);

                } catch (Resources.NotFoundException e) {
                    Log.i("MapStyle", "Can't find style. Error: ", e);
                }
                break;
        }
    }

    public static int getNightModeSetting(Context context) {
        return context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }

    public static String getUserIDFromCOAS(String CoasID) {
        return CoasID.substring(CoasID.lastIndexOf("S") + 1);
    }

    public static String removeLastChar(String str) {
        if (str.length() > 0)
            return str.substring(0, str.length() - 1);
        else return str;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getCurrentTime() {
        List<Integer> opponents = new ArrayList<>();
        RTCSession currentSession = null;
        for (int i = 0; i < opponents.size(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("session_id", "" + currentSession.getSessionID());
            contentValues.put("call_time", "" + APPHelper.getCurrentTime());
            contentValues.put("call_user_id", opponents.get(i));
            contentValues.put("call_direction", "in");
            contentValues.put("call_status", "Missed");
            contentValues.put("call_type", 1);
        }
        return sdfDatabaseDateTime.format(Calendar.getInstance().getTime());
    }

    public static void copyText(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("chat", text);
        clipboard.setPrimaryClip(clip);
        APPHelper.showToast(context, "Text copied to clipboard");
    }


    public static void clearArrayList(List<List<JSONObject>> lists) {
        for (int i = 0; i < lists.size(); i++) {
            lists.get(i).clear();
        }
    }

    public static String checkFileExist(Activity activity, String path) {

        File cDir = activity.getExternalFilesDir(null);
        if (path.startsWith(attachmentChatUrl)) {
            String filename = path.substring(path.lastIndexOf("/") + 1);
            String filePath = cDir.getPath() + "/.download/" + path;

            File file = new File(path);
            if (!file.exists()) {
                return "";
            } else {
                return file.getAbsolutePath();
            }
        } else {
            return path;
        }
    }

    public static void share(Activity activity, String content) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        activity.startActivity(shareIntent);
        AlertDialog alertDialog;
        Set<String> strings = new HashSet<>();
        ConnectycubeRestChatService.deleteMessages(strings, false).performAsync(new EntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {

            }

            @Override
            public void onError(ResponseException e) {

            }
        });
    }

    public static boolean isAndroidGoEdition(Context context) {
        final String GMAIL_GO = "com.google.android.gm.lite";
        final String YOUTUBE_GO = "com.google.android.apps.youtube.mango";
        final String GOOGLE_GO = "com.google.android.apps.searchlite";
        final String ASSISTANT_GO = "com.google.android.apps.assistant";

        boolean isGmailGoPreInstalled = isPreInstalledApp(context, GMAIL_GO);
        boolean isYoutubeGoPreInstalled = isPreInstalledApp(context, YOUTUBE_GO);
        boolean isGoogleGoPreInstalled = isPreInstalledApp(context, GOOGLE_GO);
        boolean isAssistantGoPreInstalled = isPreInstalledApp(context, ASSISTANT_GO);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && (isGoogleGoPreInstalled | isAssistantGoPreInstalled)) {
            return true;
        }
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isGmailGoPreInstalled && isYoutubeGoPreInstalled;
    }

    private static boolean isPreInstalledApp(Context context, String packageName) {
        try {
            PackageManager pacMan = context.getPackageManager();
            PackageInfo packageInfo = pacMan.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                //Check if comes with the image OS
                int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
                return (packageInfo.applicationInfo.flags & mask) != 0;
            }
        } catch (PackageManager.NameNotFoundException e) {
            //The app isn't installed
        }
        return false;
    }

    public static void shareFile(Activity activity, String path) {
        Intent shareIntent = new Intent();

        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String extension = path.substring(path.lastIndexOf(".") + 1);
        String mimeType = myMime.getMimeTypeFromExtension(extension);

        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", new File(path));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setAction(Intent.ACTION_SEND);

        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/jpeg");
        activity.startActivity(Intent.createChooser(shareIntent, "Share Via"));


    }


    public static Intent getChatServiceIntent(Context context, Class<?> serviceClass) {

        return new Intent(context, serviceClass);
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }


    public static void exportDB() {
        File sd = Environment.getExternalStorageDirectory();
        String save = Environment.getExternalStorageDirectory() + "/COASAPP";
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        File mFolder = new File(save);
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }
        List<Integer> opponents = null;


        String currentDBPath = "/data/" + "com.coasapp.coas" + "/databases/coasapp.db";
        String backupDBPath = "coas" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(System.currentTimeMillis())) + ".db";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(save, backupDBPath);
        try {
            /*source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();*/
            Log.d("Database", "DB Exported " + save);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Database", "Exception" + e.getMessage());

        }
    }

    public static JSONObject getCallUsers(Context context, String sessionId) {
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        SQLiteDatabase sqLiteDatabase = databaseHandler.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from call_history where session_id = ?", new String[]{sessionId});
        while (cursor.moveToNext()) {
            try {
                JSONObject object = new JSONObject(cursor.getString(cursor.getColumnIndex("call_users_data")));
                Log.i("CallUsers", "Info " + object);
                return object;
            } catch (JSONException e) {
                e.printStackTrace();
                return new JSONObject();
            }
        }
        cursor.close();
        return new JSONObject();
    }

    public static Cursor getCallCursor(Context context, String sessionId, SQLiteDatabase sqLiteDatabase) {
        // DatabaseHandler databaseHandler = new DatabaseHandler(context);
        // SQLiteDatabase sqLiteDatabase = databaseHandler.getWritableDatabase();
        return sqLiteDatabase.rawQuery("select * from call_history where session_id = ?", new String[]{sessionId});
    }

    public static UserRepository getUserRepo(Context context) {

        return UserRepository.Companion.getInstance(
                AppDatabase.Companion.getInstance(context).userDao()
        );
    }


    public static List<User> userList(Context context, List<User> users) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_PREF, 0);
        int conId = sharedPreferences.getInt(CUBE_USER_ID, 0);
        List<User> userList = new ArrayList<>();
        userList.clear();
        for (int i = 0; i < users.size(); i++) {
            Log.i("UserCompare", "" + users.get(i).getUserId() + " " + conId);
            if (users.get(i).getUserId() != conId) {
                userList.add(users.get(i));
            }
        }
        return userList;
    }

    public static void dispatchTakePictureIntent(Context context, Activity activity, File file) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go

            // Continue only if the File was successfully created
            if (file != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        APPHelper.photoProvider(activity),
                        file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(takePictureIntent, 99);
            }
        }
    }


    public static void launchPictureIntent(final Context context, final Activity activity, View view, final File file) {
        PopupMenu popup = new PopupMenu(activity, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_capture, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
         /*       int id = item.getItemId();
                Bundle bundle = new Bundle();
                if (id == R.id.menu_gallery) {
                    Intent takePictureIntent;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                        takePictureIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    } else {
                        takePictureIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    }
                    takePictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    takePictureIntent.setType("image/*");
                    takePictureIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    activity.startActivityForResult(Intent.createChooser(takePictureIntent,"Select Picture"), 99);*/
                int id = item.getItemId();
                Bundle bundle = new Bundle();
                if (id == R.id.menu_gallery) {
               /*     Intent takePictureIntent;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                        takePictureIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    } else {
                        takePictureIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    }
                    takePictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    takePictureIntent.setType("image/*");
                    activity.startActivityForResult(takePictureIntent, 98);

                    */

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    activity.startActivityForResult(Intent.createChooser(intent,"Select Picture"), 98);


                }
                if (id == R.id.menu_camera) {

                    dispatchTakePictureIntent(context, activity, file);
                }
               /* bundle.putString("currentTitle",((HomeActivity)getActivity()).getSupportActionBar().getTitle().toString());
                ChooseImageFragment fragment = new ChooseImageFragment();
                fragment.setArguments(bundle);
                fragment.setFragmentResultListener(MessagesFragment.this);
                FragmentHelper.addFragment(getActivity(), FRAME_HOME, fragment);*/
                return false;
            }
        });
        popup.show();

    }


    public static boolean checkPermissionsGranted(Context context, String[] permissions) {
        boolean granted = true;
        for (String permission : permissions) {
            if (!(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)) {
                granted = false;
            }
        }
        return granted;
    }

    public static boolean checkPermissionsGranted2(Activity context, String[] permissions) {
        boolean granted = true;
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!(context.shouldShowRequestPermissionRationale(permission))) {
                    granted = false;
                }
            }
        }
        return !granted;
    }

    public static boolean checkPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkSelf(Context context, JSONObject object) {

        String coasId = context.getSharedPreferences(APP_PREF, 0).getString("coasId", "0");
        try {
            return object.getString("coas_id").equalsIgnoreCase(coasId);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isContact(Context context, int id) {

        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        SQLiteDatabase sqLiteDatabase = databaseHandler.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from contacts where contact_chat_id = ?", new String[]{String.valueOf(id)});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }


    public static int getOtherUser(Context context, ConnectycubeChatDialog dialog) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_PREF, 0);
        int id = 0;
        List<Integer> listOccupants = dialog.getOccupants();
        for (Integer integer : listOccupants) {
            int loginUserId = sharedPreferences.getInt(CUBE_USER_ID, 0);
            if (loginUserId != integer) {
                id = integer;
                break;
            }
        }
        Log.i("OtherUser", "" + id);
        return id;
    }


    public static String getOtherUsers(Context context, ConnectycubeChatDialog dialog) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_PREF, 0);
        int id = 0;
        StringBuilder users = new StringBuilder();
        List<Integer> listOccupants = dialog.getOccupants();
        for (Integer integer : listOccupants) {
            int loginUserId = sharedPreferences.getInt(CUBE_USER_ID, 0);
            if (loginUserId != integer) users.append(integer).append(",");
        }

        return APPHelper.removeLastChar(users.toString());
    }

    public static void setTerms(Activity activity, View view) {
        TextView textViewTimeChart = view.findViewById(R.id.textViewTermsLink);
        SpannableString ss = new SpannableString(activity.getString(R.string.terms_link));

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                launchChrome(activity, APPConstants.baseUrlLocal2 + "terms-conditions/");
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 47, 73, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewTimeChart.setText(ss);
        textViewTimeChart.setMovementMethod(LinkMovementMethod.getInstance());
        textViewTimeChart.setHighlightColor(Color.TRANSPARENT);
    }


    public static String getContactName(Context context, String phone, String dispName) {

        String loginPhone = new MyPrefs(context, APP_PREF).getString("std_code")
                + new MyPrefs(context, APP_PREF).getString("phone");
   //     Toast.makeText(context, loginPhone, Toast.LENGTH_SHORT).show();
        Log.i("ContactPhone", phone + " " + loginPhone);
        String name = dispName;
        String searchPhone = "" + phone;

        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        SQLiteDatabase sqLiteDatabase = databaseHandler.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from contacts where contact_phone = ?", new String[]{searchPhone.replace("+", "")});

        if (cursor.getCount() > 0) {
            Log.i("ContactPhone", "" + cursor.getCount());
            while (cursor.moveToNext()) {
                name = cursor.getString(cursor.getColumnIndex("contact_name"));
                Log.i("ContactPhone", "" + name);
            }
        }
        cursor.close();
        if (loginPhone.equals(phone)) {
            name = "You";
        }
        sqLiteDatabase.close();
        return name;
    }

    public class CustomComparator implements Comparator<ConnectycubeUser> {
        @Override
        public int compare(ConnectycubeUser o1, ConnectycubeUser o2) {
            return o1.getFullName().compareTo(o2.getFullName());
        }
    }

    public static View getItemView(ViewGroup viewGroup, int res) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(res, viewGroup, false);
    }

    public static List<JSONObject> getJSONObjectsArrayList(JSONArray array) {

        List<JSONObject> list = new ArrayList<>();
        list.clear();
        try {
            for (int i = 0; i < array.length(); i++) {

                JSONObject object = array.getJSONObject(i);
                list.add(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void openDialer(Activity activity, String num) {
        Uri number = Uri.parse("tel:" + num);
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        activity.startActivity(callIntent);
    }


    public static void openDialer(Context activity, String num) {
        Uri number = Uri.parse("tel:" + num);
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        activity.startActivity(callIntent);
    }

    public static void launchChrome(Activity activity, String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(Color.parseColor("#212121"));
        CustomTabsIntent customTabsIntent = builder.build();
        if (isPackageInstalled("com.android.chrome", activity.getPackageManager()))
            customTabsIntent.intent.setPackage("com.android.chrome");

        customTabsIntent.launchUrl(activity, Uri.parse(url));
    }

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {

        boolean found = true;

        try {

            packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {

            found = false;
        }

        return found;
    }

    public static String photoProvider(Activity activity) {
        return activity.getPackageName() + ".provider";
    }

    public static boolean appInstalledOrNot(Activity activity, String uri) {
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    public static void showLog(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    public static ProgressDialog createProgressDialog(Activity activity, String message, boolean cancelable) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(message);
        if (!cancelable)
            progressDialog.setCancelable(cancelable);
        else
            progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    public static void writeToFile(String data, String fileName) {

        /*Calendar calendar = Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
        String time = format.format(calendar.getTime());

        final File path =
                Environment.getExternalStoragePublicDirectory
                        (
                                //Environment.DIRECTORY_PICTURES
                                Environment.DIRECTORY_DCIM + "/COASAPP/" + fileName + "/"
                        );

        // Make sure the path directory exists.
        if (!path.exists()) {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        final File file = new File(path, time + ".txt");

        // Save your stream, don't forget to flush() it before closing it.

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.getMessage());
            //APPHelper.showToast(context, e.getMessage());
        }*/
    }

    public static long getUnixTime(String date) {
        long unixTime = 0;

        try {
            SimpleDateFormat sdfDB = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date1 = sdfDB.parse(date);
            sdfDB.setTimeZone(TimeZone.getTimeZone("GMT"));
            unixTime = date1.getTime() / 1000;
            APPHelper.showLog("COASTimeSelected", sdfDB.format(date1));
            APPHelper.showLog("COASTimeGMT", sdfDB.format(date1));
            APPHelper.showLog("COASTimeUnix", String.valueOf(unixTime));
            Date date3 = new java.util.Date(unixTime * 1000L);
// the format of your date

            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            sdf.setTimeZone(TimeZone.getDefault());
            APPHelper.showLog("COASTimeLocal", sdf.format(date3));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return unixTime;

    }

    public static long getUnixTimeZone(String date, String timeZoneId) {
        long unixTime = 0;

        try {
            SimpleDateFormat sdfDB = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdfDB.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            Date date1 = sdfDB.parse(date);
            APPHelper.showLog("COASTimeSelected", sdfDB.format(date1));
            sdfDB.setTimeZone(TimeZone.getTimeZone("GMT"));
            unixTime = date1.getTime() / 1000;

            APPHelper.showLog("COASTimeGMT", sdfDB.format(date1));
            APPHelper.showLog("COASTimeUnix", String.valueOf(unixTime));
            Date date3 = new java.util.Date(unixTime * 1000L);
// the format of your date

            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            sdf.setTimeZone(TimeZone.getDefault());
            APPHelper.showLog("COASTimeLocal", sdf.format(date3));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return unixTime;

    }

    public static String getGMTTime(String date, String timeZoneId) {
        long unixTime = 0;
        APPHelper.showLog("COASTimeBargain", timeZoneId);
        try {
            SimpleDateFormat sdfDB = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdfDB.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            Date date1 = sdfDB.parse(date);
            APPHelper.showLog("COASTimeBargain", sdfDB.format(date1));
            sdfDB.setTimeZone(TimeZone.getTimeZone("GMT"));
            APPHelper.showLog("COASTimeBargain", sdfDB.format(date1));
            return sdfDB.format(date1);

        } catch (ParseException e) {
            APPHelper.showLog("COASTimeBargain", e.getMessage());
            return date;
        }
    }

    public static boolean checkPermissionStorage(Context context) {
        boolean granted = false;
        granted = ContextCompat.checkSelfPermission(context
                ,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return granted;
    }

    public static boolean checkPermissionLocation(Context context) {
        boolean granted = false;
        granted = ContextCompat.checkSelfPermission(context
                ,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return granted;
    }

    public static void goToAppPage(final Activity activity, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setMessage(msg);

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivityForResult(intent, 900);
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    public static void goToLocationSettings(final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setMessage("Turn on Location Services");

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivity(intent);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    public static ArrayList<JSONObject> getJSONObjectsList(JSONArray array) {

        ArrayList<JSONObject> list = new ArrayList<>();
        list.clear();
        try {
            for (int i = 0; i < array.length(); i++) {

                JSONObject object = array.getJSONObject(i);
                list.add(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean isLastItemDisplaying(RecyclerView recyclerView) {
        if (recyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            return lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1;
        }
        return false;
    }

    public static boolean isLastItemDisplayingLinear(RecyclerView recyclerView) {
        if (recyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            return lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1;
        }
        return false;
    }


    /*public static String getTimezone(double lat,double lng) {
        try {
            GeoApiContext context = new GeoApiContext.Builder().apiKey(MAP_API_KEY).build();

// The API will save the most matching result of your defined address in an array
            //GeocodingResult[] results = GeocodingApi.geocode(context, address).await();

            // .geometry.location returns an LatLng object coressponding to your address;
//getTimeZone returns the timezone and it will be saved as a TimeZone object
            TimeZone timeZone = TimeZoneApi.getTimeZone(context,new LatLng(lat,lng)).await();

// returns the displayname of the timezone
            return timeZone.getID();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "GMT";

    }*/
    public static void launchSelectAddressActivity(Activity activity, int code) {

        Intent intent = new Intent(activity, SelectAddressActivity.class);
        activity.startActivityForResult(intent, code);

    }

    public static String getTimeZoneUrl(double lat, double lng) {

        return "http://api.geonames.org/timezoneJSON?lat=" + lat + "&lng=" + lng + "&username=akinfopark";
    }

    public static String getCurrentTimeLocation(String timezone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        String current = dateFormat.format(new Date());
        Log.i("CurrentTime", current);
        return current;
    }

    public static String getMimeType(String path) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String extension = path.substring(path.lastIndexOf(".") + 1);
        return myMime.getMimeTypeFromExtension(extension);
    }

    public static String getExtension(String path) {

        return path.substring(path.lastIndexOf(".") + 1).toLowerCase();
    }

    public static void openFile(Activity activity, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String path = file.getPath();
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String extension = path.substring(path.lastIndexOf(".") + 1);
        String mimeType = myMime.getMimeTypeFromExtension(extension);

        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, mimeType);
        if (intent.resolveActivity(activity.getPackageManager()) == null) {
            // Nothing can handle this intent
            showToast(activity, "No Apps Available");
        } else {
            try {
                activity.startActivity(intent);
            } catch (Exception e) {
                Log.i("FileOpen", "Err " + e.getMessage());
                showToast(activity, "" + e.getMessage());
                e.printStackTrace();
            }
            // Something actually can
        }

    }

    public static void launchDoc(Activity activity) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        String[] mimeTypes = {
                /*"image/jpg",
                "image/jpeg",
                "image/png",
                "video/mp4",*/
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
       /* String[] mimeTypes = {
                "image/jpg",
                "image/jpeg",
                "image/png"
        };*/
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        activity.startActivityForResult(intent, 92);

    }

    public static String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf("."));
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    public static void launchVideoIntent(Activity activity, View view) {
        PopupMenu popup = new PopupMenu(activity, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_capture, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                Bundle bundle = new Bundle();
                if (id == R.id.menu_gallery) {

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_PICK);

                    intent.setType("video/mp4");
                    activity.startActivityForResult(intent, 93);
                }
                if (id == R.id.menu_camera) {

                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    activity.startActivityForResult(takeVideoIntent, 94);

                }
               /* bundle.putString("currentTitle",((HomeActivity)getActivity()).getSupportActionBar().getTitle().toString());
                ChooseImageFragment fragment = new ChooseImageFragment();
                fragment.setArguments(bundle);
                fragment.setFragmentResultListener(MessagesFragment.this);
                FragmentHelper.addFragment(getActivity(), FRAME_HOME, fragment);*/
                return false;
            }
        });
        popup.show();

    }

    public static String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }

    public static int getDurationInMilliseconds(String path) {

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        int duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        mmr.release();
        return duration;
    }


    public static String getAttachmentData(String attachmentData) {

        if (attachmentData != null) {
            if (!attachmentData.contains("\"name\"") || !attachmentData.contains("\"phone\"")) {
                attachmentData = Base64.decode(attachmentData, Base64.DEFAULT).toString();
            }
        }

        return attachmentData;
    }

    public static JSONObject getLocObj(String data) {
        Log.i("LocationMessage", data);
        if (!data.contains("\"location\"")) {
            data = Arrays.toString(Base64.decode(data, Base64.DEFAULT));
        }
        try {
            Log.i("LocationMessage", data);
            return new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
}


