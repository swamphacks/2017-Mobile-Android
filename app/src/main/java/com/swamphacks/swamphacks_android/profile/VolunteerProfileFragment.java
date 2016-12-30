package com.swamphacks.swamphacks_android.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.swamphacks.swamphacks_android.LoginActivity;
import com.swamphacks.swamphacks_android.MainActivity;
import com.swamphacks.swamphacks_android.R;
import com.swamphacks.swamphacks_android.camera.VolunteerCamera;

public class VolunteerProfileFragment extends Fragment {
    private static final String TAG = "MD/VolunteerProfileFragment";
    private View volunteerProfileView;

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
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
