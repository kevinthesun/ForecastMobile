package com.kevinthesun.csci571hw9;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.hamweather.aeris.communication.AerisCallback;
import com.hamweather.aeris.communication.AerisEngine;
import com.hamweather.aeris.maps.AerisMapView;
import com.hamweather.aeris.maps.MapViewFragment;
import com.hamweather.aeris.maps.interfaces.OnAerisMapLongClickListener;
import com.hamweather.aeris.tiles.AerisTile;
import com.kevinthesun.csci571hw9.R;


public class MapFragment extends MapViewFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AerisEngine.initWithKeys(this.getString(R.string.aeris_client_id), this.getString(R.string.aeris_client_secret), "com.kevinthesun.csci571hw9");
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (AerisMapView)view.findViewById(R.id.aerisfragment_map);
        mapView.init(savedInstanceState, AerisMapView.AerisMapType.GOOGLE);
        mapView.addLayer(AerisTile.RADSAT);
        Double lat = Double.parseDouble(getArguments().getString("lat"));
        Double lng = Double.parseDouble(getArguments().getString("lng"));
        mapView.moveToLocation(new LatLng(lat, lng), 9);
        return view;
    }
}
