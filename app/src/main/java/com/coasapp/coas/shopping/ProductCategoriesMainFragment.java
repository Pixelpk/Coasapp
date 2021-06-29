package com.coasapp.coas.shopping;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coasapp.coas.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductCategoriesMainFragment extends Fragment {


    public ProductCategoriesMainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_categories_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       Fragment fragment = new ProductCategoriesFragment();
       Bundle bundle = new Bundle();
        bundle.putString("cat_id", "0");
        bundle.putString("sub_cat_id", "0");
        bundle.putString("cat_name", "");
        fragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction().replace(R.id.contentPro, fragment).commit();
    }
}
