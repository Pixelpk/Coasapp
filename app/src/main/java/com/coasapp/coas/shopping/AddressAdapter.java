package com.coasapp.coas.shopping;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coasapp.coas.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.MyViewHolder> {

    ArrayList<JSONObject> jsonArray;
    OnDelClick onDelClick;
    OnItemClick onItemClick;

    public AddressAdapter(ArrayList<JSONObject> jsonArray) {
        this.jsonArray = jsonArray;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_addresses, parent, false);
        //returing the view
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        try {

            JSONObject jsonObject = jsonArray.get(position);

            holder.textViewAddress.setText(jsonObject.getString("add_street"));
            holder.textViewCity.setText(jsonObject.getString("add_city"));
            holder.textViewState.setText(jsonObject.getString("add_state"));
            holder.textViewCountry.setText(jsonObject.getString("add_country"));
            holder.textViewZip.setText(jsonObject.getString("add_zipcode"));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick.onItemClick(holder.getAdapterPosition());
                }
            });
            holder.imageViewDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDelClick.onDelClick(holder.getAdapterPosition());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return jsonArray.size();
    }

    public interface OnDelClick {
        void onDelClick(int position);
    }

    public void setOnDelClick(OnDelClick onDelClick) {
        this.onDelClick = onDelClick;
    }
    public interface OnItemClick {
        void onItemClick(int position);
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewAddress, textViewCity, textViewState, textViewCountry, textViewZip, textViewAmt;
        ImageView imageViewDel;
        LinearLayout layoutDetails;
        Button buttonCancel;


        public MyViewHolder(View itemView) {
            super(itemView);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewCity = itemView.findViewById(R.id.textViewCity);
            textViewState = itemView.findViewById(R.id.textViewState);
            textViewCountry = itemView.findViewById(R.id.textViewCountry);
            textViewZip = itemView.findViewById(R.id.textViewZip);
            textViewAmt = itemView.findViewById(R.id.textViewAmount);
            imageViewDel = itemView.findViewById(R.id.imageViewDelete);
        }
    }
}
