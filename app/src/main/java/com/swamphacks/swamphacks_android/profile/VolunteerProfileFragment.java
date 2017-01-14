package com.swamphacks.swamphacks_android.profile;

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

    private EditText emailET;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private RegistrationFragment registrationFragment;
    private boolean registrationOpen = false;

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        volunteerProfileView = inflater.inflate(R.layout.fragment_volunteer_profile, container, false);

        emailET = (EditText) volunteerProfileView.findViewById(R.id.volunteer_email_text);

        final TextView nameView = (TextView) volunteerProfileView.findViewById(R.id.profile_name);
        final TextView emailView = (TextView) volunteerProfileView.findViewById(R.id.profile_email);

        Button cameraButton = (Button) volunteerProfileView.findViewById(R.id.volunteer_cam_button);
        Button volunteerEmailButton = (Button) volunteerProfileView.findViewById(R.id.volunteer_email_confirm);

        Bitmap cameraImage = BitmapFactory.decodeResource(getResources(), R.drawable.camera_icon);
        Drawable drawableCamera = new BitmapDrawable(getResources(), getRoundedCornerBitmap(cameraImage, 40));
        cameraButton.setBackground(drawableCamera);

        AssetManager am = getContext().getApplicationContext().getAssets();
        Typeface face = Typeface.createFromAsset(am, "fonts/Metropolis-Regular.otf");

        nameView.setTypeface(face);
        emailView.setTypeface(face);
        cameraButton.setTypeface(face);
        volunteerEmailButton.setTypeface(face);
        emailET.setTypeface(face);

        final String email = user.getEmail();
        String dbKey = email.replace("@", "").replace(".", "");

        DatabaseReference myRef = database.getReference().child("confirmed").child(dbKey).child("name");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nameView.setText(dataSnapshot.getValue(String.class));
                emailView.setText(email);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("some error", error.toString());
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("clicked ", "camera button");
                openVolunteerCameraView();
            }
        });

        volunteerEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmRegistration(emailET.getText().toString());
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
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                toast = "Closed Scanner";
            } else {
                Log.d("person email: ", result.getContents());
                String dbKey = result.getContents().replace("@", "").replace(".", "");

                DatabaseReference myRef = database.getReference().child("confirmed").child(dbKey);

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        confirmRegistration(result.getContents());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.d("Failed to read value.", error.toString());
                    }
                });
                toast = "Signed in: " + result.getContents();
            }

            displayToast();
        }
    }

    public void confirmRegistration(String email){
        registrationFragment = RegistrationFragment.newInstance(email);
        registrationFragment.setParent(this);
        getActivity().getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .add(R.id.drawer_layout, registrationFragment)
                .commit();
        registrationOpen = true;

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    public void closeRegistrationConfirmation(){
        if(registrationOpen) {
            getActivity().getFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .remove(getFragmentManager().findFragmentById(R.id.drawer_layout)).commit();
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
