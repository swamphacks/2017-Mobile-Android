package com.swamphacks.swamphacks_android.profile;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.swamphacks.swamphacks_android.R;

public class VolunteerProfileFragment extends Fragment {
    private static final String TAG = "MD/VolunteerProfileFragment";
    private View volunteerProfileView;
    private String toast;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        volunteerProfileView = inflater.inflate(R.layout.fragment_volunteer_profile, container, false);

        final TextView nameView = (TextView) volunteerProfileView.findViewById(R.id.profile_name);
        final TextView emailView = (TextView) volunteerProfileView.findViewById(R.id.profile_email);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final String email = user.getEmail();
        String dbKey = email.replace("@", "").replace(".", "");

        DatabaseReference myRef = database.getReference().child("confirmed").child(dbKey).child("name");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nameView.setText(dataSnapshot.getValue(String.class));
                emailView.setText("VOLLLL");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("some error", error.toString());
            }
        });

        Button cameraButton = (Button) volunteerProfileView.findViewById(R.id.volunteer_cam_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("clicked ", "camera button");
                openVolunteerCameraView();
            }
        });

        return volunteerProfileView;
    }

    public void openVolunteerCameraView(){
        IntentIntegrator
                .forFragment(this)
                .setPrompt("Scan QR Code")
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                .setOrientationLocked(false)
                .initiateScan();
    }

    private void displayToast() {
        if(getActivity() != null && toast != null) {
            Toast.makeText(getActivity(), toast, Toast.LENGTH_LONG).show();
            toast = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                toast = "Cancelled from fragment";
            } else {
                toast = "Scanned from fragment: " + result.getContents();
            }

            displayToast();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
