package com.kevinthesun.csci571hw9;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.kevinthesun.csci571hw9.R;

public class ShowMap extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);
        Intent intent = getIntent();
        String lat = intent.getStringExtra("lat");
        String lng = intent.getStringExtra("lng");

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        MapFragment fragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putString("lat", lat);
        bundle.putString("lng", lng);

        fragment.setArguments(bundle);

        fragmentTransaction.add(R.id.MapContainer, fragment);
        fragmentTransaction.commit();
     }

}
