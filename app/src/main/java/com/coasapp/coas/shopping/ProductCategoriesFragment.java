package com.coasapp.coas.shopping;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.coasapp.coas.general.COASHomeActivity;
import com.coasapp.coas.R;
import com.coasapp.coas.utils.RequestHandler;
import com.coasapp.coas.utils.APPConstants;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductCategoriesFragment extends Fragment implements APPConstants {

    ArrayList<ProductCategories> productCategoriesArrayList = new ArrayList<>();
    SellerCategoriesAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    String categoryId = "0", subCat = "0", categoryName = "", search = "";

    public ProductCategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product_categories, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        categoryId = getArguments().getString("cat_id");
        subCat = getArguments().getString("sub_cat_id");

        Log.d("Cat", categoryId + SPACE + subCat);

        EditText editTextSearch = view.findViewById(R.id.editTextSearch);
        Button buttonSell = view.findViewById(R.id.buttonMyProducts);
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new SellerCategoriesAdapter(getActivity(), getContext(), productCategoriesArrayList, categoryId);
        recyclerView.setAdapter(adapter);
        new GetProductCategories().execute();
        adapter.notifyDataSetChanged();
        adapter.setOnItemSelected(new SellerCategoriesAdapter.OnItemSelected() {
            @Override
            public void onItemSelected(int position) {
                search = editTextSearch.getText().toString().trim();
                subCat = productCategoriesArrayList.get(position).getId();
                categoryId = getArguments().getString("sub_cat_id");

                categoryName = productCategoriesArrayList.get(position).getName();
                Fragment fragment = new ProductCategoriesFragment();
                Bundle bundle = new Bundle();
                bundle.putString("cat_id", categoryId);
                bundle.putString("sub_cat_id", subCat);
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.content, fragment)
                        .addToBackStack(null).commit();
            }
        });

        view.findViewById(R.id.buttonOthers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //getActivity().onBackPressed();
                ((COASHomeActivity) getActivity()).launchAddProductsActivity(categoryId, subCat, categoryName, search);

            }
        });
        view.findViewById(R.id.cardViewAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SellerCategoriesActivity.class);
                intent.putExtra("mode", "add");
                startActivityForResult(intent, 99);
            }
        });
        /*if (!subCat.equals("0")) {
            view.findViewById(R.id.buttonOthers).setVisibility(View.VISIBLE);
        }*/
        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search = editTextSearch.getText().toString().trim();
                    categoryId = getArguments().getString("sub_cat_id");

                    ((COASHomeActivity) getActivity()).launchAddProductsActivity(categoryId, "0", categoryName, search);

                }
                return false;
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //filter(s.toString());
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                productCategoriesArrayList.clear();
                adapter.notifyDataSetChanged();
                new GetProductCategories().execute();

            }
        });

    }


    void filter(String text) {
        //or pro_city_like %'"+text+"%' or pro_state_like %'"+text+"%'
        ArrayList<ProductCategories> temp = new ArrayList<>();
        for (ProductCategories d : productCategoriesArrayList) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.getName().toLowerCase().contains(text)) {
                temp.add(d);
            }
        }
        //update recyclerview
        adapter.updateList(temp);
    }

    class GetProductCategories extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            categoryId = getArguments().getString("cat_id");
            subCat = getArguments().getString("sub_cat_id");

            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("cat_id", subCat);
            return new RequestHandler().sendPostRequest(MAIN_URL + "get_product_categories.php", map);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("categories");
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        ProductCategories productCategories = new ProductCategories(
                                object.getString("cat_id"),
                                object.getString("category"),
                                object.getString("parent"),
                                object.getString("image"),
                                false
                        );
                        productCategoriesArrayList.add(productCategories);
                    }
                } else {
                    //getActivity().onBackPressed();
                    ((COASHomeActivity) getActivity()).launchAddProductsActivity(categoryId, subCat, categoryName, search);
                }

                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
