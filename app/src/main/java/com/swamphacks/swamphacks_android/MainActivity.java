package com.swamphacks.swamphacks_android;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.swamphacks.swamphacks_android.announcements.AnnouncementsFragment;
import com.swamphacks.swamphacks_android.announcements.FilterDialogFragment;
import com.swamphacks.swamphacks_android.announcements.FilterListener;
import com.swamphacks.swamphacks_android.countdown.CountdownFragment;
import com.swamphacks.swamphacks_android.events.EventsFragment;
import com.swamphacks.swamphacks_android.profile.HackerProfileFragment;
import com.swamphacks.swamphacks_android.profile.VolunteerProfileFragment;
import com.swamphacks.swamphacks_android.sponsors.SponsorsFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FilterListener{

    private Toolbar toolbar;
    private DrawerLayout drawer;

    private CountdownFragment countdownFragment;
    private EventsFragment eventsFragment;
    private AnnouncementsFragment announcementsFragment;
    private SponsorsFragment sponsorsFragment;
    private HackerProfileFragment hackerProfileFragment;
    private VolunteerProfileFragment volunteerProfileFragment;

    Fragment state;

    //0 -> logistics, 1 -> social, 2 -> food, 3 -> tech talk, 4 -> sponsor, 5 -> other
    public static boolean[] filterList = {true, true, true, true, true, true};
    boolean isVolunteer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Metropolis-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        //Font hacking
        FontOverride.setDefaultFont(this, "DEFAULT", "fonts/Metropolis-Regular.otf");
        FontOverride.setDefaultFont(this, "MONOSPACE", "fonts/Metropolis-Regular.otf");
        FontOverride.setDefaultFont(this, "SERIF", "fonts/Metropolis-Regular.otf");
        FontOverride.setDefaultFont(this, "SANS_SERIF", "fonts/Metropolis-Regular.otf");

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        countdownFragment = new CountdownFragment();
        eventsFragment = new EventsFragment();
        announcementsFragment = new AnnouncementsFragment();
        sponsorsFragment = new SponsorsFragment();
        hackerProfileFragment = new HackerProfileFragment();
        volunteerProfileFragment = new VolunteerProfileFragment();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final String email = user.getEmail();
        String dbKey = email.replace("@", "").replace(".", "");

        DatabaseReference myRef = database.getReference().child("confirmed").child(dbKey).child("volunteer");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isVolunteer = dataSnapshot.getValue(Boolean.class);
                if(isVolunteer){
                    eventsFragment.setVol(isVolunteer);
                    TextView textView = (TextView) findViewById(R.id.user_title);
                    if(textView != null)
                        textView.setText("Volunteer");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("some error", error.toString());
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initView();
    }

    public void initView(){
        updateFragment(countdownFragment, false);
        countdownFragment.getEvents();
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        if(state == sponsorsFragment)
            sponsorsFragment.closeSponsorDetails();
        else if(state == eventsFragment)
            eventsFragment.closeEventDetails();
        else if(state == volunteerProfileFragment)
            volunteerProfileFragment.closeRegistrationConfirmation();
        else if(state == countdownFragment) {
            countdownFragment.closeEventDetail();
            updateFragment(eventsFragment, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        toolbar.getMenu().getItem(0).setVisible(false);
        toolbar.getMenu().getItem(1).setVisible(false);
        toolbar.getMenu().getItem(2).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            this.finish();
            return true;
        }

        if (id == R.id.action_filter) {
            FilterDialogFragment filterDialogFragment = new FilterDialogFragment();
            filterDialogFragment.setListener(this);
            filterDialogFragment.show(getFragmentManager(), "filter");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateFragment(Fragment fragment, boolean addToBackStack) {
        state = fragment;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.content_main, fragment, fragment.getTag());

        if (addToBackStack)
            fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_countdown) {
            updateFragment(countdownFragment, true);
            toolbar.getMenu().getItem(0).setVisible(false);
            toolbar.getMenu().getItem(1).setVisible(false);
            toolbar.getMenu().getItem(2).setVisible(false);
            countdownFragment.getEvents();
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setTitle("");
            setTheme(R.style.LoginActivity);
        } else if (id == R.id.nav_events) {
            updateFragment(eventsFragment, true);
            setToolbarTitle("Events");
            toolbar.getMenu().getItem(0).setVisible(false);
            toolbar.getMenu().getItem(1).setVisible(false);
            toolbar.getMenu().getItem(2).setVisible(true);

            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_place_black_24dp);
            toolbar.setOverflowIcon(drawable);
        } else if (id == R.id.nav_announcements) {
            updateFragment(announcementsFragment, true);
            setToolbarTitle("Announcements");
            toolbar.getMenu().getItem(0).setVisible(false);
            toolbar.getMenu().getItem(1).setVisible(true);
            toolbar.getMenu().getItem(2).setVisible(false);

            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_filter_list_black_24dp);
            toolbar.setOverflowIcon(drawable);
        } else if (id == R.id.nav_sponsors) {
            updateFragment(sponsorsFragment, true);
            setToolbarTitle("Sponsors");
            toolbar.getMenu().getItem(0).setVisible(false);
            toolbar.getMenu().getItem(1).setVisible(false);
            toolbar.getMenu().getItem(2).setVisible(false);
        } else if (id == R.id.nav_profile) {
            Log.d("isvol", "" + isVolunteer);
            if(!isVolunteer)
                updateFragment(hackerProfileFragment, true);
            else {
                updateFragment(volunteerProfileFragment, true);
            }

            toolbar.setTitle("");
            toolbar.getMenu().getItem(0).setVisible(true);
            toolbar.getMenu().getItem(1).setVisible(false);
            toolbar.getMenu().getItem(2).setVisible(false);

            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_more_horiz_black_24dp);
            toolbar.setOverflowIcon(drawable);
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setToolbarTitle(String title){
        SpannableString s = new SpannableString(title);
        s.setSpan(new TypefaceSpan("Metropolis-Regular.otf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        toolbar.setTitle(s);
    }

    @Override
    public void updateView(boolean success, Object message) {
        announcementsFragment.getAnnouncements();
    }
}
