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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.coasapp.coas.utils.RequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.coasapp.coas.utils.APPConstants.MAIN_URL;
import static com.coasapp.coas.utils.APPConstants.SPACE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SellerCategoryFragment extends Fragment {

    ArrayList<ProductCategories> productCategoriesArrayList = new ArrayList<>();
    SellerCategoriesAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    String categoryId = "0", catName = "", subCatId = "0";

    public SellerCategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        categoryId = getArguments().getString("cat_id");
        subCatId = getArguments().getString("sub_cat_id");
        catName = getArguments().getString("cat_name");
        Log.d("Cat", categoryId + SPACE + subCatId);
        EditText editTextSearch = view.findViewById(R.id.editTextSearch);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCategories);
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new SellerCategoriesAdapter(getActivity(), getContext(), productCategoriesArrayList, categoryId);
        recyclerView.setAdapter(adapter);
        new GetProductCategories().execute();
        adapter.notifyDataSetChanged();
        adapter.setOnItemSelected(new SellerCategoriesAdapter.OnItemSelected() {
            @Override
            public void onItemSelected(int position) {
                subCatId = productCategoriesArrayList.get(position).getId();
                catName = productCategoriesArrayList.get(position).getName();
                categoryId = getArguments().getString("sub_cat_id");
                // Toast.makeText(getContext(), catName, Toast.LENGTH_SHORT).show();
                Fragment fragment = new SellerCategoryFragment();
                Bundle bundle = new Bundle();
                bundle.putString("cat_id", categoryId);
                bundle.putString("sub_cat_id", subCatId);
                bundle.putString("cat_name", catName);
                bundle.putString("mode", getArguments().getString("mode"));
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.frame, fragment)
                        .addToBackStack(null).commit();
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
                filter(s.toString());
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
        view.findViewById(R.id.buttonOthers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //getActivity().onBackPressed();
                if (getArguments().getString("mode").equals("add"))

                    ((SellerCategoriesActivity) getActivity()).launchAddProductsActivity(categoryId, subCatId);
                else {
                    Log.i("Cat",""+categoryId);
                    Log.i("CatSub","0"+subCatId);
                    Intent intent = new Intent();
                    intent.putExtra("sub_cat_id", subCatId);
                    intent.putExtra("cat_id", "0");
                    intent.putExtra("cat_name", catName);
                    getActivity().setResult(RESULT_OK, intent);
                    getActivity().finish();
                }

            }
        });
        /*if (!subCatId.equals("0")) {
            view.findViewById(R.id.buttonOthers).setVisibility(View.VISIBLE);
        }*/

    }

    void filter(String text) {
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
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> map = new HashMap<>();
            map.put("cat_id", subCatId);
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

                   /* ProductCategories productCategories = new ProductCategories(
                            subCatId,
                           "Others",
                            "0",
                          "",
                            false
                    );
                    productCategoriesArrayList.add(productCategories);*/
                } else {
                    if (getArguments().getString("mode").equals("add"))
                        //getActivity().onBackPressed();
                        ((SellerCategoriesActivity) getActivity()).launchAddProductsActivity(categoryId, subCatId);
                    else {
                        Intent intent = new Intent();
                        intent.putExtra("cat_id", categoryId);
                        intent.putExtra("cat_name", catName);
                        Toast.makeText(getContext(), catName, Toast.LENGTH_SHORT).show();
                        intent.putExtra("sub_cat_id", subCatId);
                        getActivity().setResult(RESULT_OK, intent);
                        getActivity().finish();

                    }
                }

                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
