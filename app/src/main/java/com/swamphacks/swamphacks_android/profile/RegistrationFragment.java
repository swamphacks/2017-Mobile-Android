package com.swamphacks.swamphacks_android.profile;

import android.annotation.TargetApi;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Build;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.swamphacks.swamphacks_android.R;

import data.models.Registrant;

public class RegistrationFragment extends Fragment {
    private static final String TAG = "MD/RegistrationFragment";
    private View registrationView;
    private String toast;

    private String name, email, school, dbKey;
    private TextView nameTV, emailTV, schoolTV, nameLabel, schoolLabel, emailLabel, correctLabel;
    private Button confirmButton;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private VolunteerProfileFragment parent;

    public static RegistrationFragment newInstance(String email) {
        RegistrationFragment registrationFragment = new RegistrationFragment();

        Bundle args = new Bundle();
        args.putString("email", email);
        registrationFragment.setArguments(args);

        return registrationFragment;
    }

    public void setParent(VolunteerProfileFragment parent){
        this.parent = parent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();

        if (args.containsKey("email")) {
            email = args.getString("email");
        }

        dbKey = email.replace("@", "").replace(".", "");

        DatabaseReference myRef = database.getReference().child("confirmed").child(dbKey);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Registrant registrant = dataSnapshot.getValue(Registrant.class);
                if(registrant != null) {
                    name = registrant.getName();
                    email = registrant.getEmail();
                    school = registrant.getSchool();

                    if(name != null)
                        nameTV.setText(name);
                    if(email != null)
                        emailTV.setText(email);
                    if(school != null)
                        schoolTV.setText(school);

                    if(email != null)
                        toast = "Signed in: " + email;
                } else {
                    getActivity().onBackPressed();
                    toast = "Not valid entry";
                    displayToast();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("some error", error.toString());
            }
        });

        super.onCreate(savedInstanceState);
    }

    private void displayToast() {
        if(getActivity() != null && toast != null) {
            Toast.makeText(getActivity(), toast, Toast.LENGTH_LONG).show();
            toast = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        registrationView = inflater.inflate(R.layout.fragment_registration, container, false);

        nameTV = (TextView) registrationView.findViewById(R.id.registrant_name);
        emailTV = (TextView) registrationView.findViewById(R.id.registrant_email);
        schoolTV = (TextView) registrationView.findViewById(R.id.registrant_school);
        nameLabel = (TextView) registrationView.findViewById(R.id.name_label);
        emailLabel = (TextView) registrationView.findViewById(R.id.email_label);
        schoolLabel = (TextView) registrationView.findViewById(R.id.school_label);
        correctLabel = (TextView) registrationView.findViewById(R.id.correct_label);

        confirmButton = (Button) registrationView.findViewById(R.id.confirm_button);

        AssetManager am = getContext().getApplicationContext().getAssets();
        Typeface face = Typeface.createFromAsset(am, "fonts/Metropolis-Regular.otf");

        nameTV.setTypeface(face);
        nameLabel.setTypeface(face);
        schoolLabel.setTypeface(face);
        schoolTV.setTypeface(face);
        emailTV.setTypeface(face);
        emailLabel.setTypeface(face);
        correctLabel.setTypeface(face);
        confirmButton.setTypeface(face);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference myRef = database.getReference("attendees");
                Registrant registrant = new Registrant();
                registrant.setEmail(email);
                registrant.setName(name);
                registrant.setSchool(school);

                myRef.child(dbKey).setValue(registrant);
                parent.closeRegistrationConfirmation();
                displayToast();
            }
        });

        return registrationView;
    }
}
