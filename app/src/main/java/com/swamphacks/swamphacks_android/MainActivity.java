package com.swamphacks.swamphacks_android;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.swamphacks.swamphacks_android.announcements.AnnouncementsFragment;
import com.swamphacks.swamphacks_android.countdown.CountdownFragment;
import com.swamphacks.swamphacks_android.events.EventsFragment;
import com.swamphacks.swamphacks_android.profile.HackerProfileFragment;
import com.swamphacks.swamphacks_android.sponsors.SponsorsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;

    private CountdownFragment countdownFragment;
    private EventsFragment eventsFragment;
    private AnnouncementsFragment announcementsFragment;
    private SponsorsFragment sponsorsFragment;
    private HackerProfileFragment hackerProfileFragment;

    private Fragment state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        countdownFragment = new CountdownFragment();
        eventsFragment = new EventsFragment();
        announcementsFragment = new AnnouncementsFragment();
        sponsorsFragment = new SponsorsFragment();
        hackerProfileFragment = new HackerProfileFragment();

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
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

        return super.onOptionsItemSelected(item);
    }

    private void updateFragment(Fragment fragment, boolean addToBackStack) {
        state = fragment;

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.content_main, fragment);

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
            toolbar.setTitle("Countdown");
            toolbar.getMenu().getItem(0).setVisible(false);
            toolbar.getMenu().getItem(1).setVisible(false);
            toolbar.getMenu().getItem(2).setVisible(false);
        } else if (id == R.id.nav_events) {
            updateFragment(eventsFragment, true);
            toolbar.setTitle("Events");
            toolbar.getMenu().getItem(0).setVisible(false);
            toolbar.getMenu().getItem(1).setVisible(false);
            toolbar.getMenu().getItem(2).setVisible(true);

            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_place_black_24dp);
            toolbar.setOverflowIcon(drawable);
        } else if (id == R.id.nav_announcements) {
            updateFragment(announcementsFragment, true);
            toolbar.setTitle("Announcements");
            toolbar.getMenu().getItem(0).setVisible(false);
            toolbar.getMenu().getItem(1).setVisible(true);
            toolbar.getMenu().getItem(2).setVisible(false);

            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_filter_list_black_24dp);
            toolbar.setOverflowIcon(drawable);
        } else if (id == R.id.nav_sponsors) {
            updateFragment(sponsorsFragment, true);
            toolbar.setTitle("Sponsors");
            toolbar.getMenu().getItem(0).setVisible(false);
            toolbar.getMenu().getItem(1).setVisible(false);
            toolbar.getMenu().getItem(2).setVisible(false);
        } else if (id == R.id.nav_profile) {
            updateFragment(hackerProfileFragment, true);
            toolbar.setTitle("Profile");
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
}
