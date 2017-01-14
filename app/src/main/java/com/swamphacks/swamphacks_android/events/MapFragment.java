package com.swamphacks.swamphacks_android.events;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.swamphacks.swamphacks_android.R;

public class MapFragment extends Fragment {

    Button firstFloorButton, basementButton;
    ImageView mapView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        firstFloorButton = (Button) view.findViewById(R.id.first_floor_button);
        basementButton = (Button) view.findViewById(R.id.basement_button);

        mapView = (ImageView) view.findViewById(R.id.map_view);

        firstFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ((BitmapDrawable)mapView.getDrawable()).getBitmap().recycle();
                mapView.setImageResource(R.drawable.map_image_firstfloor);
            }
        });

        basementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ((BitmapDrawable)mapView.getDrawable()).getBitmap().recycle();
                mapView.setImageResource(R.drawable.map_image);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
