package com.swamphacks.swamphacks_android.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.swamphacks.swamphacks_android.R;

import org.w3c.dom.Text;

import data.models.Registrant;

public class RegistrationFragment extends Fragment {
    private static final String TAG = "MD/RegistrationFragment";
    private View registrationView;

    private String name, email, school;
    private TextView nameTV, emailTV, schoolTV;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    public static RegistrationFragment newInstance(String email) {
        RegistrationFragment registrationFragment = new RegistrationFragment();

        Bundle args = new Bundle();
        args.putString("email", email);
        registrationFragment.setArguments(args);

        return registrationFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();

        if (args.containsKey("email")) {
            email = args.getString("email");
        }

        String dbKey = email.replace("@", "").replace(".", "");

        DatabaseReference myRef = database.getReference().child("confirmed").child(dbKey);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Registrant registrant = dataSnapshot.getValue(Registrant.class);
                name = registrant.getName();
                email = registrant.getEmail();
                school = registrant.getSchool();

                nameTV.setText(name);
                emailTV.setText(email);
                schoolTV.setText(school);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("some error", error.toString());
            }
        });

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        registrationView = inflater.inflate(R.layout.fragment_registration, container, false);

        nameTV = (TextView) registrationView.findViewById(R.id.registrant_name);
        emailTV = (TextView) registrationView.findViewById(R.id.registrant_email);
        schoolTV = (TextView) registrationView.findViewById(R.id.registrant_school);

        return registrationView;
    }
}
