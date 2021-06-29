package com.coasapp.coas.roombook;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.coasapp.coas.R;

import java.util.ArrayList;
import java.util.HashMap;

public class BookContactsAdapter extends RecyclerView.Adapter<BookContactsAdapter.MyViewHolder> {

    Context context;
    ArrayList<HashMap<String, String>> arrayList;
    OnNameChanged onNameChanged;
    OnPhoneChanged onPhoneChanged;
    OnAgeChanged onAgeChanged;

    public interface OnAdapterItemsClick {
        void onDelClick(int i);
    }

    public OnAdapterItemsClick getOnAdapterItemsClick() {
        return onAdapterItemsClick;
    }

    public void setOnAdapterItemsClick(OnAdapterItemsClick onAdapterItemsClick) {
        this.onAdapterItemsClick = onAdapterItemsClick;
    }

    OnAdapterItemsClick onAdapterItemsClick;


    public interface OnNameChanged {
        void onNameChanged(int position, String name);

        void onMNameChanged(int position, String name);

        void onLNameChanged(int position, String name);
    }


    public interface OnPhoneChanged {
        void onPhoneChanged(int position, String phone);
    }

    public interface OnAgeChanged {
        void onAgeChanged(int position, String age);
    }


    public void setOnPhoneChanged(OnPhoneChanged onPhoneChanged) {
        this.onPhoneChanged = onPhoneChanged;
    }

    public void setOnNameChanged(OnNameChanged onNameChanged) {
        this.onNameChanged = onNameChanged;
    }

    public void setOnAgeChanged(OnAgeChanged onAgeChanged) {
        this.onAgeChanged = onAgeChanged;
    }

    public BookContactsAdapter(Context context, ArrayList<HashMap<String, String>> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_contacts, parent, false);
        //returing the view
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        HashMap<String, String> map = arrayList.get(position);
        Log.i("Guest", String.valueOf(map));
        EditText editTextName = holder.editTextName;
        EditText editTextPh = holder.editTextContact;
        editTextName.setText(map.get("guest_name"));
        editTextPh.setText(map.get("guest_phone"));
        if (position == 0) {
            holder.itemView.findViewById(R.id.imageViewDelete).setVisibility(View.INVISIBLE);

        } else {
            holder.itemView.findViewById(R.id.imageViewDelete).setVisibility(View.VISIBLE);
        }
        holder.itemView.findViewById(R.id.imageViewDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAdapterItemsClick.onDelClick(holder.getAdapterPosition());
            }
        });
        holder.editTextAge.setText(map.get("guest_age").replace("0", ""));
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        EditText editTextName, editTextContact, editTextAge, editTextMName, editTextLName;

        public MyViewHolder(View itemView) {
            super(itemView);
            editTextContact = itemView.findViewById(R.id.editTextConPhone);
            editTextName = itemView.findViewById(R.id.editTextConName);
            editTextAge = itemView.findViewById(R.id.editTextAge);
            editTextMName = itemView.findViewById(R.id.editTextConName2);
            editTextLName = itemView.findViewById(R.id.editTextConName3);
            editTextName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    onNameChanged.onNameChanged(getAdapterPosition(), s.toString());

                }
            });
            editTextMName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    onNameChanged.onMNameChanged(getAdapterPosition(), s.toString());

                }
            });
            editTextLName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    onNameChanged.onLNameChanged(getAdapterPosition(), s.toString());

                }
            });
            editTextContact.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    onPhoneChanged.onPhoneChanged(getAdapterPosition(), s.toString());
                }
            });

            editTextAge.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {


                    onAgeChanged.onAgeChanged(getAdapterPosition(), s.toString());
                }
            });
        }
    }
}
