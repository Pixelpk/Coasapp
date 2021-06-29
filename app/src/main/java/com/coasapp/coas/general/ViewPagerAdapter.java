package com.coasapp.coas.general;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    List<ViewPagerFragments> viewPagerFragments;

    public ViewPagerAdapter(FragmentManager fm, List<ViewPagerFragments> viewPagerFragments) {
        super(fm);
        this.viewPagerFragments = viewPagerFragments;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return viewPagerFragments.get(position).getTitle();
    }



    @Override
    public Fragment getItem(int position) {
        return viewPagerFragments.get(position).getFragment();
    }

    @Override
    public int getCount() {
        return viewPagerFragments.size();
    }
}
