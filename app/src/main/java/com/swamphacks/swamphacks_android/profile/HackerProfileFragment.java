package com.swamphacks.swamphacks_android.profile;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.swamphacks.swamphacks_android.R;

import net.glxn.qrgen.android.QRCode;

public class HackerProfileFragment extends Fragment {
    private static final String TAG = "MD/HackerProfileFragment";
    private View hackerProfileFragmentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        hackerProfileFragmentView = inflater.inflate(R.layout.fragment_hacker_profile, container, false);

        ImageView qrCode = (ImageView) hackerProfileFragmentView.findViewById(R.id.qr_image);
        final TextView nameView = (TextView) hackerProfileFragmentView.findViewById(R.id.profile_name);
        TextView emailView = (TextView) hackerProfileFragmentView.findViewById(R.id.profile_email);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String email = user.getEmail();
        String dbKey = email.replace("@", "").replace(".", "");

        DatabaseReference myRef = database.getReference().child("confirmed").child(dbKey).child("name");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nameView.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("some error", error.toString());
            }
        });

        Bitmap qr = QRCode.from(user.getEmail()).bitmap();

        qrCode.setImageBitmap(qr);
        emailView.setText(email);

        return hackerProfileFragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
