package com.coasapp.coas.bargain;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coasapp.coas.R;
import com.bumptech.glide.Glide;
import com.coasapp.coas.utils.APPConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class VehicleListAdapter extends RecyclerView.Adapter<VehicleListAdapter.MyViewHolder> implements APPConstants {

    ArrayList<JSONObject> arrayList;
    Activity activity;
    Context context;

    public VehicleListAdapter(ArrayList<JSONObject> arrayList, Activity activity, Context context) {
        this.arrayList = arrayList;
        this.activity = activity;
        this.context = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_my_vehicle_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        JSONObject object = arrayList.get(position);
        try {

            if(object.getString("vehicle_active").equalsIgnoreCase("1")){
                holder.cardView.setCardBackgroundColor(Color.RED);
            }
            else {
                holder.cardView.setCardBackgroundColor(Color.WHITE);
            }
            Glide.with(context).load(MAIN_URL_IMAGE+object.getString("vehicle_image")).into(holder.imageViewVehicle);

            holder.textViewVehicleName.setText(object.getString("brand_name")+" "+object.getString("model_name"));
            holder.textViewVehiclePrice.setText(object.getString("vehicle_approve"));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick.onItemClick(holder.getAdapterPosition());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public interface OnItemClick {
        void onItemClick(int i);
    }

    OnItemClick onItemClick;

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewSeller, textViewVehicleName, textViewVehiclePrice, textViewFrom, textViewTo, textViewAmt, textViewOrderId;
        ImageView imageViewVehicle;
        CircleImageView imageViewSeller;
        LinearLayout layoutDetails;
        Button buttonCancel;
        CardView cardView;


        public MyViewHolder(View itemView) {
            super(itemView);
cardView=itemView.findViewById(R.id.cardViewVehicle);
            textViewSeller = itemView.findViewById(R.id.textViewSeller);
            textViewVehiclePrice = itemView.findViewById(R.id.textViewVehiclePrice);
            textViewVehicleName = itemView.findViewById(R.id.textViewVehicleName);
            imageViewVehicle = itemView.findViewById(R.id.imageViewVehicle);
            imageViewSeller = itemView.findViewById(R.id.imageViewSeller);
        }
    }
}
