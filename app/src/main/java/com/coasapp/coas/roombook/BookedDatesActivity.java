package com.coasapp.coas.roombook;

import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.view.View;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.coasapp.coas.R;
import com.coasapp.coas.general.MyAppCompatActivity;
import com.coasapp.coas.utils.APICallbacks;
import com.coasapp.coas.utils.APIService;
import com.coasapp.coas.utils.APPConstants;
import com.coasapp.coas.utils.APPHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class BookedDatesActivity extends MyAppCompatActivity implements APPConstants {
    private CalendarView calendarView;
    List<EventDay> eventDays = new ArrayList<>();
    List<Calendar> calendars = new ArrayList<>();


    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2019-08-06 12:47:16 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        calendarView = findViewById(R.id.calendarView);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_dates);

        findViews();

        Calendar calendarMin = Calendar.getInstance();
        calendarMin.setTimeInMillis(System.currentTimeMillis()-86400000);
        APPHelper.showLog("Date",""+calendarMin.getTime());
        calendarView.setMinimumDate(calendarMin);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.calendarbooked);
        //drawable = CalendarUtils.getDrawableText(getApplicationContext(),"BOOKED",Typeface.DEFAULT_BOLD,android.R.color.holo_red_dark,5);

        APICallbacks apiCallbacks = new APICallbacks() {
            @Override
            public void taskStart() {
                findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
            }

            @Override
            public void taskEnd(String type, String response) {
                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray array1 = object.getJSONArray("booked_customer");
                    JSONArray array2 = object.getJSONArray("booked_hoster");
                    for (int i = 0; i < array1.length(); i++) {
                        JSONObject object1 = array1.getJSONObject(i);
                        Long checkin = Long.valueOf(object1.getString("book_checkin"));
                        Long checkout = Long.valueOf(object1.getString("book_checkout"));
                        for (long j = checkin; j <= checkout; j = j + 86400) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(j * 1000L);
                            APPHelper.showLog("Booked", calendar.get(Calendar.DAY_OF_MONTH) + "-"
                                    + (calendar.get(Calendar.MONTH) + 1) + "-"
                                    + calendar.get(Calendar.YEAR) + " "
                                    + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                                    + calendar.get(Calendar.MINUTE));
                            eventDays.add(new EventDay(calendar, drawable));
                        }
                    }
                    for (int i = 0; i < array2.length(); i++) {
                        JSONObject object1 = array2.getJSONObject(i);
                        Long checkin = Long.valueOf(object1.getString("from_unix"));
                        Long checkout = Long.valueOf(object1.getString("to_unix"));
                        for (long j = checkin; j <= checkout; j = j + 86400) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(j * 1000L);
                            APPHelper.showLog("Booked", calendar.get(Calendar.DAY_OF_MONTH) + "-"
                                    + (calendar.get(Calendar.MONTH) + 1) + "-"
                                    + calendar.get(Calendar.YEAR) + " "
                                    + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                                    + calendar.get(Calendar.MINUTE));
                            eventDays.add(new EventDay(calendar, drawable));
                        }
                    }
                    calendarView.setEvents(eventDays);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        APIService apiService = new APIService(apiCallbacks, getApplicationContext());
        HashMap<String, String> map = new HashMap<>();
        map.put("room_id", getIntent().getStringExtra("room_id"));
        apiService.callAPI(map, APPConstants.MAIN_URL + "room_booked_dates.php", "booked");

    }

}
