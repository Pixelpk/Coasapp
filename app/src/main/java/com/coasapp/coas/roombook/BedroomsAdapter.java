package com.coasapp.coas.roombook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coasapp.coas.R;

import java.util.ArrayList;
import java.util.HashMap;

public class BedroomsAdapter extends RecyclerView.Adapter<BedroomsAdapter.ViewHolder> {

    ArrayList<HashMap<String, String>> arrayList;

    public BedroomsAdapter(ArrayList<HashMap<String, String>> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_bedroom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> map = arrayList.get(position);
        if (Integer.parseInt(map.get("king")) > 0) {

            holder.textViewKing.setVisibility(View.VISIBLE);
            holder.textViewKing.setText(map.get("king") + " King Size Bed");
        } else {
            holder.textViewKing.setVisibility(View.INVISIBLE);
        }
        if (Integer.parseInt(map.get("queen")) > 0) {

            holder.textViewQueen.setVisibility(View.VISIBLE);
            holder.textViewQueen.setText(map.get("queen") + " Queen Size Bed");
        } else {
            holder.textViewQueen.setVisibility(View.INVISIBLE);
        }

        if (Integer.parseInt(map.get("single")) > 0) {

            holder.textViewSingle.setVisibility(View.VISIBLE);
            holder.textViewSingle.setText(map.get("single") + " Single Bed");
        } else {
            holder.textViewSingle.setVisibility(View.INVISIBLE);
        }
        if (Integer.parseInt(map.get("double")) > 0) {

            holder.textViewDouble.setVisibility(View.VISIBLE);
            holder.textViewDouble.setText(map.get("double") + " Double Bed");
        } else {
            holder.textViewDouble.setVisibility(View.INVISIBLE);
        }

        holder.textViewBedroom.setText(map.get("bedroom"));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewBedroom, textViewKing, textViewQueen, textViewSingle, textViewDouble;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewBedroom = itemView.findViewById(R.id.textViewBedroom);
            textViewSingle = itemView.findViewById(R.id.textViewSingle);
            textViewQueen = itemView.findViewById(R.id.textViewQueen);
            textViewDouble = itemView.findViewById(R.id.textViewDouble);
            textViewKing = itemView.findViewById(R.id.textViewKing);


        }
    }
}
