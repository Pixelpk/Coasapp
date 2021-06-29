package com.coasapp.coas.shopping;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.coasapp.coas.R;

import java.util.ArrayList;

public class SellerCategoriesActivity extends AppCompatActivity {

    ArrayList<ProductCategories> productCategoriesArrayList = new ArrayList<>();
    SellerCategoriesAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    String categoryId = "0", subCatId = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_categories);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Fragment fragment = new SellerCategoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString("cat_id", categoryId);
        bundle.putString("sub_cat_id", subCatId);
        bundle.putString("cat_name", "");
        bundle.putString("cat_image", "");
        bundle.putString("mode", getIntent().getStringExtra("mode"));
        fragment.setArguments(bundle);
       getSupportFragmentManager() .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void launchAddProductsActivity(String categoryId, String subCatId) {
        getSupportFragmentManager().popBackStackImmediate();
        Intent intent = new Intent(getApplicationContext(), AddProductActivity.class);
        intent.putExtra("cat_id", categoryId);
        intent.putExtra("sub_cat_id", subCatId);
        startActivityForResult(intent, 99);
    }

    void launchViewProductsActivity(String categoryId) {
        getSupportFragmentManager().popBackStackImmediate();
        Intent intent = new Intent(getApplicationContext(), ProductsActivity.class);
        intent.putExtra("cat_id", categoryId);
        startActivityForResult(intent, 99);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 99) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
