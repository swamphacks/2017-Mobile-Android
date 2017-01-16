package com.swamphacks.swamphacks_android.announcements;


import android.app.Fragment;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.swamphacks.swamphacks_android.MainActivity;
import com.swamphacks.swamphacks_android.R;
import data.models.Announcement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AnnouncementsFragment extends Fragment {
    private static final String TAG = "MD/Announcements";

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Caches all the Announcements found
    ArrayList<Announcement> mAnnouncementsList;

    private SwipeRefreshLayout swipeContainer;

    // Caches the listView layout
    RecyclerView mRecyclerView;
    // Adapter for the listView
    MainNavAdapter mListAdapter;

    boolean[] filterList = MainActivity.filterList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcements, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_cards);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mListAdapter.clear();
                getAnnouncements();
                mListAdapter.addAll(mAnnouncementsList);
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(mAnnouncementsList == null) {
            mAnnouncementsList = new ArrayList<>();
        }

        initList();

        // Get Firebase data of announcements for the first time
        getAnnouncements();
    }

    // Set up the test listView for displaying announcements
    private void initList() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        // Create and set the adapter for this recyclerView
        mListAdapter = new MainNavAdapter(getActivity());
        mRecyclerView.setAdapter(mListAdapter);
    }

    public void getAnnouncements() {
        DatabaseReference myRef = database.getReference().child("announcements");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAnnouncementsList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Announcement announcement = postSnapshot.getValue(Announcement.class);
                    int numType = convertTypeToInt(announcement.getType());
                    if(filterList[numType] == true && System.currentTimeMillis() > announcement.getTime()*1000)
                        mAnnouncementsList.add(announcement);
                    updateAnnouncements();
                }
                Collections.sort(mAnnouncementsList, new Comparator<Announcement>() {
                    @Override
                    public int compare(Announcement a1, Announcement a2) {
                        return (int) (a2.getTime() - a1.getTime());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public int convertTypeToInt(String s){
        switch(s){
            case "logistics":
                return 0;
            case "social":
                return 1;
            case "food":
                return 2;
            case "tech talk":
                return 3;
            case "sponsor":
                return 4;
            case "other":
                return 5;
        }
        return -1;
    }

    // Update the announcements shown
    private void updateAnnouncements() {
        // Notify the adapter that the data changed
        mListAdapter.notifyDataSetChanged();
    }

    class MainNavAdapter extends RecyclerView.Adapter<MainNavAdapter.ViewHolder> {
        Context mContext;

        // Default constructor
        MainNavAdapter(Context context) {
            this.mContext = context;
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            public TextView titleView;
            public TextView dateView;
            public TextView descriptionView;
            public FrameLayout colorView;

            public ViewHolder(View itemView) {
                super(itemView);

                // Save the TextViews
                this.titleView = (TextView) itemView.findViewById(R.id.info_title);
                this.dateView = (TextView) itemView.findViewById(R.id.info_date);
                this.descriptionView = (TextView) itemView.findViewById(R.id.info_description);
                this.colorView = (FrameLayout) itemView.findViewById(R.id.announcement_color);
            }
        }

        public void clear() {
            mAnnouncementsList.clear();
            notifyDataSetChanged();
        }

        // Add a list of items
        public void addAll(List<Announcement> list) {
            mAnnouncementsList.addAll(list);
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            // Create the view for this row
            View row = LayoutInflater.from(mContext).inflate(R.layout.announcement_list_item, viewGroup, false);

            // Create a new viewHolder which caches all the views that needs to be saved
            ViewHolder viewHolder = new ViewHolder(row);

            return viewHolder;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            AssetManager am = mContext.getApplicationContext().getAssets();
            Typeface face = Typeface.createFromAsset(am, "fonts/Metropolis-Regular.otf");

            Announcement announcement = mAnnouncementsList.get(i);

            // Set this item's views based off of the announcement data
            viewHolder.titleView.setText(announcement.getName());
            viewHolder.descriptionView.setText(announcement.getDescription());

            String category = announcement.getType();

            switch (category) {
                case "logistics":
                    viewHolder.colorView.setBackgroundColor(getResources().getColor(R.color.event_red));
                    break;
                case "social":
                    viewHolder.colorView.setBackgroundColor(getResources().getColor(R.color.event_blue));
                    break;
                case "food":
                    viewHolder.colorView.setBackgroundColor(getResources().getColor(R.color.event_yellow));
                    break;
                case "tech talk":
                    viewHolder.colorView.setBackgroundColor(getResources().getColor(R.color.event_green));
                    break;
                case "sponsor":
                    viewHolder.colorView.setBackgroundColor(getResources().getColor(R.color.event_orange));
                    break;
                case "other":
                    viewHolder.colorView.setBackgroundColor(getResources().getColor(R.color.event_purple));
                    break;
            }

            // Get the date from this announcement and set it as a relative date
            Date date = new Date(announcement.getTime() * 1000);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            viewHolder.dateView.setText(simpleDateFormat.format(date));

            viewHolder.titleView.setTypeface(face);
            viewHolder.descriptionView.setTypeface(face);
            viewHolder.dateView.setTypeface(face);
        }

        @Override
        public int getItemCount() {
            return mAnnouncementsList.size();
        }
    }
}

