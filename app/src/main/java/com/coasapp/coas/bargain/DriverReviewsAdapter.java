package com.coasapp.coas.bargain;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.coasapp.coas.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DriverReviewsAdapter extends RecyclerView.Adapter<DriverReviewsAdapter.ViewHolder> {

    List<JSONObject> listReviews;
    Activity activity;
    Context context;

    public DriverReviewsAdapter(List<JSONObject> listReviews, Activity activity, Context context) {
        this.listReviews = listReviews;
        this.activity = activity;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_driver_reviews, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        JSONObject object = listReviews.get(position);
        try {
            holder.textViewName.setText(object.getString("name"));
            holder.textViewReview.setText(object.getString("bargain_feedback"));
            holder.ratingBar.setRating(Float.parseFloat(object.getString("bargain_rating")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listReviews.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewReview;
        RatingBar ratingBar;
        public ViewHolder(View itemView) {
            super(itemView);
            ratingBar = itemView.findViewById(R.id.ratingBar3);
            textViewReview = itemView.findViewById(R.id.textViewReviews);
            textViewName = itemView.findViewById(R.id.textViewName);
        }
    }
}
