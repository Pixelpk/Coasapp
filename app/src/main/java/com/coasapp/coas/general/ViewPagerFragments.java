package com.coasapp.coas.general;

import androidx.fragment.app.Fragment;

public class ViewPagerFragments {
    public ViewPagerFragments(String title, Fragment fragment) {
        this.title = title;
        this.fragment = fragment;
    }

    public String getTitle() {
        return title;
    }

    public Fragment getFragment() {
        return fragment;
    }

    String title;
    Fragment fragment;
}
