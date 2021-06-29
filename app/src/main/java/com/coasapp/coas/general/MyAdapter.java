package com.coasapp.coas.general;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.coasapp.coas.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

/**
 * Created by AK INFOPARK on 16-11-2017.
 */

public class MyAdapter extends PagerAdapter {
    private List<String> images;
    private LayoutInflater inflater;
    private Context context;

    public MyAdapter(Context context, List<String> images) {
        this.context = context;
        this.images = images;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View myImageLayout = inflater.inflate(R.layout.slide, view, false);
        PhotoView imageView = myImageLayout
                .findViewById(R.id.image);
        imageView.setImageDrawable(null);
        Log.i("ImagesFull",images.get(position));
        Glide.with(context).load(images.get(position)).into(imageView);
        /*Glide.with(context).load(images.get(position)).asGif().diskCacheStrategy(DiskCacheStrategy.NONE).listener(new RequestListener<Integer, GifDrawable>() {
            @Override
            public boolean onException(Exception e, Integer model, Target<GifDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GifDrawable resource, Integer model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(imageView);*/
        view.addView(myImageLayout, 0);
        return myImageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
