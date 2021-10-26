package com.example.erlab.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.erlab.utils.FragmentFactory;
import com.example.erlab.R;
import com.example.erlab.fragments.HomeFragment;

/**
 * @Class MainActivity The base activity
 * @Author Malik Dawar
 * @Date 08 OCT 2020
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HomeFragment homeFragment = new HomeFragment();
        FragmentFactory.replaceFragment(homeFragment, R.id.container, this);

    }
}