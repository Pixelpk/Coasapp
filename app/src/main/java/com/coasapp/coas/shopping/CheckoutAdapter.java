package com.coasapp.coas.shopping;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.coasapp.coas.R;

import com.coasapp.coas.utils.APPHelper;
import com.coasapp.coas.utils.APPConstants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.coasapp.coas.utils.DatabaseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> implements APPConstants {

    ArrayList<HashMap<String, String>> arrayList;
    Context context;
    Activity activity;
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;
    OnDelClick onDelClick;
    OnItemClick onItemClick;


    public CheckoutAdapter(ArrayList<HashMap<String, String>> arrayList, Context context, Activity activity) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
        databaseHandler = new DatabaseHandler(context);
        sqLiteDatabase = databaseHandler.getWritableDatabase();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final HashMap<String, String> map = arrayList.get(position);
        Glide.with(context).load(MAIN_URL_IMAGE + map.get("image")).apply(new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)).into(holder.imageViewProduct);
        holder.textViewProduct.setText(map.get("product"));
        holder.textViewPrice.setText(formatter.format(Double.valueOf(map.get("amount"))));
        if (map.get("count").equals("0")) {
            holder.textViewActual.setText("Out of stock");
        } else if (Integer.parseInt(map.get("qty")) > Integer.parseInt(map.get("count"))) {
            holder.textViewActual.setText("Only " + map.get("count") + " available in stock.");
        } else {
            holder.textViewActual.setText("");
        }
        //holder.textViewQty.setText("x" + map.get("qty"));
       /* if(map.get("qty").equalsIgnoreCase("0")){
            holder.textViewQty.setText("Out of stock");
        }
        else{
            holder.textViewQty.setText("x" + map.get("qty"));
        }*/
        holder.textViewQty.setText(map.get("qty"));
        holder.imageViewDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sqLiteDatabase.delete("cart", "pro_id=?", new String[]{map.get("pro_id")});
//                arrayList.remove(holder.getAdapterPosition());
//                notifyDataSetChanged();
                onDelClick.onDelClick(holder.getAdapterPosition());
            }
        });

        String address = map.get("address");
        ArrayList<String> arrayListAddress = new ArrayList<>();
        final ArrayList<JSONObject> jsonObjectsAddress = new ArrayList<>();

        try {
            JSONObject object = new JSONObject();
            object.put("address_id", "0");
            object.put("add_street", "Pickup");
            object.put("add_state", "");
            object.put("add_city", "");
            object.put("add_country", "");
            object.put("add_zipcode", "");
            object.put("add_contact_name", "");
            object.put("add_phone", "");
            jsonObjectsAddress.add(object);
            arrayListAddress.add("Pickup");
            JSONArray jsonArray = new JSONArray(address);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObjectsAddress.add(jsonObject);
                arrayListAddress.add(jsonObject.getString("add_street"));
                APPHelper.showLog("Address", jsonObject.getString("add_street"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, R.layout.spinner_address, arrayListAddress);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_address_item);
        holder.spinnerAddress.setAdapter(arrayAdapter);
        holder.spinnerAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {

                    onItemClick.onItemClick(holder.getAdapterPosition(), jsonObjectsAddress.get(position).getString("add_street") + ",\n" + jsonObjectsAddress.get(position).getString("add_state")
                            + ",\n" + jsonObjectsAddress.get(position).getString("add_city") + ",\n" + jsonObjectsAddress.get(position).getString("add_country") + ",\n" + jsonObjectsAddress.get(position).getString("add_zipcode")
                            + "\n" + jsonObjectsAddress.get(position).getString("add_contact_name") + " - " + jsonObjectsAddress.get(position).getString("add_phone"));
                    if (jsonObjectsAddress.get(position).getString("add_street").equalsIgnoreCase("pickup")) {
                        onItemClick.onItemClick(holder.getAdapterPosition(), jsonObjectsAddress.get(position).getString("add_street"));
                    }
                    onItemClick.onItemClick(holder.getAdapterPosition(), jsonObjectsAddress.get(position).getString("add_street"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        holder.itemView.findViewById(R.id.imageViewPlus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plusClick.onPlusClick(holder.getAdapterPosition());
            }
        });
        holder.itemView.findViewById(R.id.imageViewMinus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minusClick.onMinusClick(holder.getAdapterPosition());
            }
        });
    }

    public interface OnItemClick {
        void onItemClick(int adapterPosition, String addressId);
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public interface OnDelClick {
        void onDelClick(int position);
    }

    public void setOnDelClick(OnDelClick onDelClick) {
        this.onDelClick = onDelClick;
    }

    public interface PlusClick {
        void onPlusClick(int i);
    }

    PlusClick plusClick;

    public void setOnPlusClick(PlusClick plusClick) {
        this.plusClick = plusClick;
    }

    public interface MinusClick {
        void onMinusClick(int i);
    }

    MinusClick minusClick;

    public void setOnMinusClick(MinusClick minusClick) {
        this.minusClick = minusClick;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProduct, textViewPrice, textViewQty, textViewActual;
        ImageView imageViewProduct, imageViewDel;
        Spinner spinnerAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewActual = itemView.findViewById(R.id.textActual);
            textViewProduct = itemView.findViewById(R.id.textViewProduct);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQty = itemView.findViewById(R.id.textViewQty);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            imageViewDel = itemView.findViewById(R.id.imageViewDelete);
            spinnerAddress = itemView.findViewById(R.id.spinnerAddress);
        }
    }
}
