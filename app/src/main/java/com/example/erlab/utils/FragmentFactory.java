package com.example.erlab.utils;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * @Class FragmentFactory to manipulate the fragment transactions
 * @Author Malik Dawar
 * @Date 08 OCT 2020
 */


public class FragmentFactory {
    private static String TAG = FragmentFactory.class.getName();

    public static void replaceFragment(Fragment fragment, int containerId, Context context) {
        FragmentTransaction fragmentTransaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerId, fragment).commit();
    }

    public static void addFragment(Fragment fragment, int containerId, Context context) {
        FragmentTransaction fragmentTransaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(containerId, fragment).commit();
    }

    public static void replaceFragmentBackStack(Fragment fragment, int containerId, Context context, String TAG) {
        FragmentTransaction fragmentTransaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerId, fragment).addToBackStack(TAG).commit();
    }

    public static void back(Context context) {
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
        } else {
            ((FragmentActivity) context).finish();
        }
    }
}