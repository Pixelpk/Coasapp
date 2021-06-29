package com.coasapp.coas.utils;

import android.app.Activity;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class FragmentHelper {


    public static Fragment getFragmentFromActivity(Activity activity, int container) {
        return ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentById(container);
    }

    public static Fragment getFragmentFromFragment(Fragment fragment, int container) {
        return fragment.getChildFragmentManager().findFragmentById(container);
    }


    public static void addFragment(Activity activity, int container, Fragment fragment) {
        ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right).addToBackStack(null).add(container, fragment).commit();
    }

    public static void replaceFragment(Activity activity, int container, Fragment fragment) {
        FragmentManager fragmentManager = ((AppCompatActivity) activity).getSupportFragmentManager();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }
        fragmentManager.beginTransaction().setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right).replace(container, fragment).commit();
    }

    public static void replaceFragmentWithBack(Activity activity, int container, Fragment fragment) {
        ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right).addToBackStack(null).replace(container, fragment).commit();
    }

    public static void replaceFragmentNoAnim(final Activity activity, final int container, final Fragment fragment) {

        FragmentManager fragmentManager = ((AppCompatActivity) activity).getSupportFragmentManager();
        /*for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }*/
        fragmentManager.beginTransaction().replace(container, fragment).commitAllowingStateLoss();
    }

    public static void replaceFragmentNav(final Activity activity, final int container, final Fragment fragment) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction().replace(container, fragment).commitAllowingStateLoss();

            }
        }, 220);

    }


}
