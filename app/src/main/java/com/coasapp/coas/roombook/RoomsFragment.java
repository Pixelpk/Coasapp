package com.coasapp.coas.roombook;


import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoomsFragment extends Fragment implements APPConstants {

    //ArrayList<JSONObject> arrayListRooms = new ArrayList<>();
    ArrayList<JSONObject> arrayListRooms1 = new ArrayList<>();
    int start = 0;
    boolean filter = false;
    RoomListAdapter adapter;
    ScrollView roomFilter;
    FrameLayout layoutMore;
    double lat, lng, minPrice = 0.0, maxPrice = 0.00, distance = 0.0, minPrice2 = 0.0, maxPrice2 = 0.00;
    int step1, step2, step3, min1, min2, min3, max1, max2, max3, step4, step5, min4, max4, min5, max5, seek1, seek2, seek3, seek4, seek5, spinnerPos;
    String pricefilter = "", pkgfilter = "", distanceFilter = "", finalfilter = "", userId;
    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
    SwipeRefreshLayout swipeRefreshLayout;
    String sortFilter = " order by room_id desc";
    String search = "", bookType = "", sort = "latest";
    SharedPreferences sharedPreferences;
    Spinner spinnerSort, spinnerPkg;
    SeekBar seekBarDistance, seekBarMinPrice, seekBarMaxPrice;
    TextView textViewDistance, textViewMinPrice, textViewMaxPrice;
    Button buttonApply, buttonClear;

    AlertDialog alertDialogFilter;

    public RoomsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        step1 = 1;
        max1 = 50;
        min1 = 0;
        step2 = 10;
        max2 = 3000;
        min2 = 10;

        min3 = 10;
        step3 = 10;
        max3 = 6000;
        EditText editTextSearch = view.findViewById(R.id.editTextSearch);
        layoutMore = view.findViewById(R.id.layoutMore);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewRooms);
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        sharedPreferences = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        roomFilter = view.findViewById(R.id.roomFilter);
        roomFilter.setVisibility(View.GONE);
        showFilterDialog();
        //recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        FloatingActionButton buttonFilter = view.findViewById(R.id.floatingActionButtonFilter);
        adapter = new RoomListAdapter(arrayListRooms1, getContext(), getActivity());
        recyclerView.setAdapter(adapter);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                arrayListRooms1.clear();
                adapter.notifyDataSetChanged();
                search = s.toString();
                start = 0;
                new GetRooms().execute();
                /*for (int i = 0; i < arrayListRooms.size(); i++) {
                    JSONObject jsonObject = arrayListRooms.get(i);
                    try {
                        if (jsonObject.getString("room_title").toLowerCase().contains(search) || jsonObject.getString("room_street").toLowerCase().contains(search)) {
                            arrayListRooms1.add(jsonObject);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }*/
            }
        });
        /*HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("name", "Casino");
        hashMap.put("address", "Downtown, NYC");
        hashMap.put("image", "https://skift.com/wp-content/uploads/2017/09/oyo-rooms-1-1.jpg");
        hashMap.put("unitprice", "$5000");
        arrayListRooms.add(hashMap);
        hashMap = new HashMap<>();
        hashMap.put("name", "Casino");
        hashMap.put("address", "Downtown, NYC");
        hashMap.put("image", "https://skift.com/wp-content/uploads/2017/09/oyo-rooms-1-1.jpg");
        hashMap.put("unitprice", "$5000");
        arrayListRooms.add(hashMap);
        adapter.notifyDataSetChanged();*/
        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (!filter) {
                    minPrice = 1;
                    maxPrice = 10;
                }





                min4 = 0;
                step4 = 100;
                max4 = 2000;

                min5 = 0;
                step5 = 100;
                max5 = 2000;
                roomFilter.setVisibility(View.VISIBLE);*/
                alertDialogFilter.show();
            }
        });
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        lat = Double.parseDouble(sharedPreferences.getString("lat", "0.0"));
        lng = Double.parseDouble(sharedPreferences.getString("lng", "0.0"));
        userId = sharedPreferences.getString("userId", "0");
        APPHelper.showLog("Loc", "" + lat + " " + lng);
        new GetRooms().execute();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                start = 0;
                swipeRefreshLayout.setRefreshing(false);
                //arrayListRooms.clear();
                arrayListRooms1.clear();
                adapter.notifyDataSetChanged();
                new GetRooms().execute();
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isLastItemDisplaying(recyclerView)) {
                    //Calling the method getdata again
                    //getData();

                    new GetRooms().execute();

                }
            }
        });


        spinnerSort = view.findViewById(R.id.spinnerSort);

        seekBarDistance = view.findViewById(R.id.seekBarDistance);
        seekBarMinPrice = view.findViewById(R.id.seekBarMinPrice);
        seekBarMaxPrice = view.findViewById(R.id.seekBarMaxPrice);

        textViewDistance = view.findViewById(R.id.textViewDistance);
        textViewMinPrice = view.findViewById(R.id.textViewMinPrice);
        textViewMaxPrice = view.findViewById(R.id.textViewMaxPrice);
        buttonApply = view.findViewById(R.id.buttonApply);
        buttonClear = view.findViewById(R.id.buttonClear);
        textViewMinPrice.setText(formatter.format(minPrice));
        textViewMaxPrice.setText(formatter.format(maxPrice));
        /*seekBarDistance.setMax((max1 - min1) / step1);
        seekBarMinPrice.setMax((max2 - min2) / step2);
        seekBarMaxPrice.setMax((max3 - min3) / step2);
        seekBarMinPrice2.setMax((max4 - min4) / step4);
        seekBarMaxPrice2.setMax((max5 - min5) / step4);
        seekBarDistance.setProgress(seek5);

        seekBarMinPrice.setProgress(seek1);
        seekBarMinPrice2.setProgress(seek3);
        seekBarMaxPrice.setProgress(seek2);
        seekBarMaxPrice2.setProgress(seek4);
        textViewMaxPrice.setText(formatter.format(maxPrice));
        textViewMinPrice.setText(formatter.format(minPrice));
        APPHelper.showToast(getActivity(), "" + distance);
        if (distance > 0) {
            textViewDistance.setText(distance + " miles");
        }
        if (sort.contains("asc")) {
            spinnerSort.setSelection(1);
        } else if (sort.contains("desc")) {
            spinnerSort.setSelection(2);
        }*/

        seekBarMinPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minPrice = min2 + (progress * step2);
                textViewMinPrice.setText(numberFormat.format(minPrice));
                //textViewMaxPrice.setText(numberFormat.format(minPrice));
                seek1 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarMaxPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seek2 = progress;
                maxPrice = min2 + (progress * step2);
                textViewMaxPrice.setText(numberFormat.format(maxPrice));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = min1 + (progress * step1);

                if (distance > 0) {
                    textViewDistance.setText(distance + " miles");

                } else {
                    textViewDistance.setText("Any Distance");

                }
                seek5 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        spinnerPkg = view.findViewById(R.id.spinnerPack);
        spinnerPkg.setSelection(spinnerPos);
        spinnerPkg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerPos = position;
                switch (position) {

                    case 0:
                        bookType = "";

                        pkgfilter = " and priceperhour > 0";
                       /* seekBarMinPrice.setVisibility(View.GONE);
                        seekBarMaxPrice.setVisibility(View.GONE);
                        seekBarMinPrice2.setVisibility(View.VISIBLE);
                        seekBarMaxPrice2.setVisibility(View.VISIBLE);*/
                        break;
                    case 1:
                        bookType = "Hourly";
                        pkgfilter = " and pricepernight > 0";
                       /* seekBarMinPrice2.setVisibility(View.GONE);
                        seekBarMaxPrice2.setVisibility(View.GONE);
                        seekBarMinPrice.setVisibility(View.VISIBLE);
                        seekBarMaxPrice.setVisibility(View.VISIBLE);*/
                        break;
                    case 2:
                        bookType = "Nightly";
                        pkgfilter = " and pricepernight > 0";
                       /* seekBarMinPrice2.setVisibility(View.GONE);
                        seekBarMaxPrice2.setVisibility(View.GONE);
                        seekBarMinPrice.setVisibility(View.VISIBLE);
                        seekBarMaxPrice.setVisibility(View.VISIBLE);*/
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //APPHelper.showLog("spinner", String.valueOf(spinnerPkg.getSelectedItemPosition()));
        if (spinnerPkg.getSelectedItemPosition() == 0) {
            APPHelper.showLog("spinner", String.valueOf(maxPrice2));
            textViewMinPrice.setText(numberFormat.format(minPrice2));
            textViewMaxPrice.setText(numberFormat.format(maxPrice2));
        } else {
            textViewMinPrice.setText(numberFormat.format(minPrice));
            textViewMaxPrice.setText(numberFormat.format(maxPrice));

        }
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = true;
                switch (spinnerSort.getSelectedItemPosition()) {
                    case 0:
                        sortFilter = " order by room_id desc";
                        sort = "latest";
                        break;
                    case 1:
                        if (spinnerPkg.getSelectedItemPosition() == 0) {
                            sortFilter = " order by priceperhour asc";

                        } else {
                            sortFilter = " order by pricepernight asc";

                        }
                        sort = "priceasc";
                        break;
                    case 2:
                        if (spinnerPkg.getSelectedItemPosition() == 0) {
                            sortFilter = " order by priceperhour desc";

                        } else {
                            sortFilter = " order by pricepernight desc";

                        }
                        sort = "pricedesc";
                        break;
                }
                /*if (spinnerPkg.getSelectedItemPosition() == 0) {

                    pricefilter = " and (priceperhour between " + minPrice2 + " and " + maxPrice2 + ") ";
                    if (minPrice2 == 0 && maxPrice2 == 0) {
                        pricefilter = "";
                    } else if (maxPrice2 <= minPrice2) {
                        pricefilter = " and (priceperhour >= " + minPrice2 + ") ";
                    }
                } else {
                    pricefilter = " and (pricepernight between " + minPrice + " and " + maxPrice + ") ";
                    if (minPrice == 0 && maxPrice == 0) {
                        pricefilter = "";
                    } else if (maxPrice <= minPrice) {
                        pricefilter = " and (priceperhour >= " + minPrice + ") ";
                    }
                }*/
                finalfilter = pricefilter + pkgfilter + sortFilter;
                roomFilter.setVisibility(View.GONE);
                //arrayListRooms.clear();
                arrayListRooms1.clear();
                start = 0;
                adapter.notifyDataSetChanged();

                new GetRooms().execute();
                //new GetRooms().execute();
                APPHelper.showLog("Filter", finalfilter);
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayListRooms1.clear();
                adapter.notifyDataSetChanged();
                new GetRooms().execute();
                resetFilter();
            }
        });

        view.findViewById(R.id.cardViewAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddRoomActivity.class);
                startActivityForResult(intent, 1);
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(ROOM_DETAILS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString("mode", "add");
                editor.apply();
            }
        });
        return view;
    }

    private boolean isLastItemDisplaying(RecyclerView recyclerView) {
        if (recyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            return lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1;
        }
        return false;
    }

    void showFilterDialog() {
        /*step1 = 1;
        max1 = 50;
        min1 = 0;

        step2 = 500;
        max2 = 50000;
        min2 = 0;

        min3 = 0;
        step3 = 500;
        max3 = 50000;

        min4 = 0;
        step4 = 100;
        max4 = 2000;

        min5 = 0;
        step5 = 100;
        max5 = 2000;*/

        LayoutInflater li = LayoutInflater.from(getActivity());
        //Creating a view to get the dialog box
        View viewFilter = li.inflate(R.layout.dialog_room_filter, null);

        //Initizliaing confirm button fo dialog box and edittext of dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Adding our dialog box to the view of alert dialog
        builder.setView(viewFilter);


        //Creating an alert dialog
        alertDialogFilter = builder.create();
        alertDialogFilter.setCanceledOnTouchOutside(false);
        //Displaying the alert dialog

        final Spinner spinnerSort = viewFilter.findViewById(R.id.spinnerSort);

        SeekBar seekBarDistance = viewFilter.findViewById(R.id.seekBarDistance);
        final SeekBar seekBarMinPrice = viewFilter.findViewById(R.id.seekBarMinPrice);
        final SeekBar seekBarMaxPrice = viewFilter.findViewById(R.id.seekBarMaxPrice);
        final SeekBar seekBarMinPrice2 = viewFilter.findViewById(R.id.seekBarMinPrice2);
        final SeekBar seekBarMaxPrice2 = viewFilter.findViewById(R.id.seekBarMaxPrice2);
        final TextView textViewDistance = viewFilter.findViewById(R.id.textViewDistance);
        final TextView textViewMinPrice = viewFilter.findViewById(R.id.textViewMinPrice);
        final TextView textViewMaxPrice = viewFilter.findViewById(R.id.textViewMaxPrice);
        Button buttonApply = viewFilter.findViewById(R.id.buttonApply);
        Button buttonClear = viewFilter.findViewById(R.id.buttonClear);

        /*seekBarDistance.setMax((max1 - min1) / step1);
        seekBarMinPrice.setMax((max2 - min2) / step2);
        seekBarMaxPrice.setMax((max3 - min3) / step2);
        seekBarMinPrice2.setMax((max4 - min4) / step4);
        seekBarMaxPrice2.setMax((max5 - min5) / step4);
        seekBarDistance.setProgress(seek5);

        seekBarMinPrice.setProgress(seek1);
        seekBarMinPrice2.setProgress(seek3);
        seekBarMaxPrice.setProgress(seek2);
        seekBarMaxPrice2.setProgress(seek4);
        textViewMaxPrice.setText(formatter.format(maxPrice));
        textViewMinPrice.setText(formatter.format(minPrice));
        APPHelper.showToast(getActivity(), "" + distance);
        if (distance > 0) {
            textViewDistance.setText(distance + " miles");
        }
        if (sort.contains("asc")) {
            spinnerSort.setSelection(1);
        } else if (sort.contains("desc")) {
            spinnerSort.setSelection(2);
        }*/

        seekBarMinPrice.setMax((max2 - min2) / step2);
        seekBarMaxPrice.setMax((max3 - min2) / step2);
        seekBarDistance.setMax(max1 - min1);
       /* minPrice = 10;
        maxPrice = 10;
        distance = 0;*/
        textViewMinPrice.setText(formatter.format(10));
        textViewMaxPrice.setText(formatter.format(10));
        textViewDistance.setText("Any Distance");
        seekBarMinPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    minPrice = min2 + (progress * step2);
                    textViewMinPrice.setText(numberFormat.format(minPrice));
                    //textViewMaxPrice.setText(numberFormat.format(minPrice));
                    seek1 = progress;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarMaxPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seek2 = progress;
                    maxPrice = (min2 + (progress * step2));
                    textViewMaxPrice.setText(numberFormat.format(maxPrice));
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarMinPrice2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seek3 = progress;
                    minPrice2 =( min4 + (progress * step4));
                    textViewMinPrice.setText(numberFormat.format(minPrice2));
                }

                //textViewMaxPrice.setText(numberFormat.format(minPrice));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarMaxPrice2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seek4 = progress;
                maxPrice2 = min4 + (progress * step4);
                textViewMaxPrice.setText(numberFormat.format(maxPrice2));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = min1 + (progress * step1);

                if (distance > 0) {
                    textViewDistance.setText(distance + " miles");

                } else {
                    textViewDistance.setText("Any Distance");

                }
                seek5 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        final Spinner spinnerPkg = viewFilter.findViewById(R.id.spinnerPack);
        spinnerPkg.setSelection(spinnerPos);
        spinnerPkg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerPos = position;
                switch (position) {

                    case 0:
                        bookType = "";

                        pkgfilter = " and priceperhour > 0";
                       /* seekBarMinPrice.setVisibility(View.GONE);
                        seekBarMaxPrice.setVisibility(View.GONE);
                        seekBarMinPrice2.setVisibility(View.VISIBLE);
                        seekBarMaxPrice2.setVisibility(View.VISIBLE);*/
                        break;
                    case 1:
                        bookType = "Hourly";
                        pkgfilter = " and pricepernight > 0";
                       /* seekBarMinPrice2.setVisibility(View.GONE);
                        seekBarMaxPrice2.setVisibility(View.GONE);
                        seekBarMinPrice.setVisibility(View.VISIBLE);
                        seekBarMaxPrice.setVisibility(View.VISIBLE);*/
                        break;
                    case 2:
                        bookType = "Nightly";
                        pkgfilter = " and pricepernight > 0";
                       /* seekBarMinPrice2.setVisibility(View.GONE);
                        seekBarMaxPrice2.setVisibility(View.GONE);
                        seekBarMinPrice.setVisibility(View.VISIBLE);
                        seekBarMaxPrice.setVisibility(View.VISIBLE);*/
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //APPHelper.showLog("spinner", String.valueOf(spinnerPkg.getSelectedItemPosition()));
        if (spinnerPkg.getSelectedItemPosition() == 0) {
            APPHelper.showLog("spinner", String.valueOf(maxPrice2));
            textViewMinPrice.setText(numberFormat.format(minPrice2));
            textViewMaxPrice.setText(numberFormat.format(maxPrice2));
        } else {
            textViewMinPrice.setText(numberFormat.format(minPrice));
            textViewMaxPrice.setText(numberFormat.format(maxPrice));

        }
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (spinnerSort.getSelectedItemPosition()) {
                    case 0:
                        sortFilter = " order by room_id desc";
                        sort = "latest";
                        break;
                    case 1:
                        if (spinnerPkg.getSelectedItemPosition() == 0) {
                            sortFilter = " order by priceperhour asc";

                        } else {
                            sortFilter = " order by pricepernight asc";

                        }
                        sort = "priceasc";
                        break;
                    case 2:
                        if (spinnerPkg.getSelectedItemPosition() == 0) {
                            sortFilter = " order by priceperhour desc";

                        } else {
                            sortFilter = " order by pricepernight desc";

                        }
                        sort = "pricedesc";
                        break;
                }
                if (spinnerPkg.getSelectedItemPosition() == 0) {

                    pricefilter = " and (priceperhour between " + minPrice2 + " and " + maxPrice2 + ") ";
                    if (minPrice2 == 0 && maxPrice2 == 0) {
                        pricefilter = "";
                    } else if (maxPrice2 <= minPrice2) {
                        pricefilter = " and (priceperhour >= " + minPrice2 + ") ";
                    }
                } else {
                    pricefilter = " and (pricepernight between " + minPrice + " and " + maxPrice + ") ";
                    if (minPrice == 0 && maxPrice == 0) {
                        pricefilter = "";
                    } else if (maxPrice <= minPrice) {
                        pricefilter = " and (priceperhour >= " + minPrice + ") ";
                    }
                }
                finalfilter = pricefilter + pkgfilter + sortFilter;
                alertDialogFilter.hide();
                //arrayListRooms.clear();
                arrayListRooms1.clear();
                start = 0;
                adapter.notifyDataSetChanged();
                new GetRooms().execute();
                //new GetRooms().execute();
                APPHelper.showLog("Filter", finalfilter);
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distance = 0;
                bookType = "";
                start = 0;

                pricefilter = "";
                pkgfilter = "";
                finalfilter = "";
                //arrayListRooms.clear();
                arrayListRooms1.clear();
                adapter.notifyDataSetChanged();
                sort = "latest";
                minPrice = 0;
                maxPrice = 0;
                spinnerPkg.setSelection(0);
                spinnerSort.setSelection(0);
                seekBarDistance.setProgress(0);
                seekBarMinPrice.setProgress(0);
                seekBarMaxPrice.setProgress(0);
                textViewMinPrice.setText(formatter.format(0));
                textViewMaxPrice.setText(formatter.format(0));
                new GetRooms().execute();
                alertDialogFilter.dismiss();
            }
        });

    }

    class GetRooms extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutMore.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            lat = Double.parseDouble(sharedPreferences.getString("lat", "0.0"));
            lng = Double.parseDouble(sharedPreferences.getString("lng", "0.0"));
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", userId);
            map.put("minprice", String.valueOf(minPrice + minPrice2));
            map.put("maxprice", String.valueOf(maxPrice + maxPrice2));
            map.put("search", search);
            map.put("distance", String.valueOf(distance));
            //map.put("filters", finalfilter);
            map.put("sort", sort);
            map.put("book_type", bookType);
            map.put("lat", String.valueOf(lat));
            map.put("lng", String.valueOf(lng));
            map.put("index", String.valueOf(start));
            return new RequestHandler().sendPostRequest(MAIN_URL + "get_rooms1.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            layoutMore.setVisibility(View.GONE);

            try {
                JSONArray jsonArray = new JSONArray(s);
                /*if (distance == 0.0) {
                    APPHelper.showLog("dist", "no near filter");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        //arrayListRooms.add(jsonObject);
                        arrayListRooms1.add(jsonObject);
                    }
                } else {
                    APPHelper.showLog("dist", "near filter");

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);


                        double latitude = Double.parseDouble(jsonObject.getString("room_latitude"));
                        double longitude = Double.parseDouble(jsonObject.getString("room_longitude"));
                        double d = new FindDistance().distance(lat, lng, latitude, longitude);
                        APPHelper.showLog("dist", distance + " " + d + " " + latitude + " " + longitude + " " + lat + " " + lng);
                        if (d <= distance) {
                            APPHelper.showLog("dist", "near");

                            //arrayListRooms.add(jsonObject);
                            arrayListRooms1.add(jsonObject);
                        }

                    }

                }*/
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    //arrayListRooms.add(jsonObject);
                    arrayListRooms1.add(jsonObject);
                }
                start = arrayListRooms1.size();
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void hideFilter() {

        roomFilter.setVisibility(View.GONE);

    }

    public void resetFilter() {
        filter = false;
        distance = 0;
        bookType = "";
        start = 0;
        spinnerPkg.setSelection(0);
        spinnerSort.setSelection(0);
        seekBarDistance.setProgress(0);
        seekBarMinPrice.setProgress(0);
        seekBarMaxPrice.setProgress(0);
        textViewMinPrice.setText(formatter.format(1));
        textViewMaxPrice.setText(formatter.format(10));
        pricefilter = "";
        pkgfilter = "";
        finalfilter = "";
        //arrayListRooms.clear();

        sort = "latest";
        minPrice = 0;
        maxPrice = 0;
        roomFilter.setVisibility(View.GONE);

    }

}
