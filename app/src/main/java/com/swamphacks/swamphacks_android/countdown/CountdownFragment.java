package com.swamphacks.swamphacks_android.countdown;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.swamphacks.swamphacks_android.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import data.models.Event;

public class CountdownFragment extends Fragment {
    private static final String TAG = "MD/CountdownFrag";

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Countdown views
    private ProgressBar mCircularProgress;
    private TextView mCountdownTextView;

    // Title textViews
    TextView mTopTitleText, mTopTimeText, mBottomTitleText, mBottomTimeText;

    // For testing the countdown timer
    private final long countdownLength = 10 * 1000;
    private final long countdownUpdateIntervals = 1 * 750;

    private Date startDate;
    private long duration;

    private RecyclerView recyclerView;
    private CountdownFragment.MainNavAdapter mListAdapter;

    private List<Event> nowEvents = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_countdown, container, false);

        // Cache the views that need to be edited later on
        mCircularProgress = (ProgressBar) view.findViewById(R.id.progressbar_counter);
        mCountdownTextView = (TextView) view.findViewById(R.id.timer_text);

        recyclerView = (RecyclerView) view.findViewById(R.id.list_happening_now);

        return view;
    }

    public void getEvents() {
        DatabaseReference myRef = database.getReference().child("events");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Event event = postSnapshot.getValue(Event.class);
                    long current = System.currentTimeMillis();
                    if(current < event.getEnd()*1000 && current > event.getStart())
                        nowEvents.add(event);
                }
                Log.d(TAG, "got " + nowEvents.size() + " events");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initList();
    }

    private void initList() {
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Create and set the adapter for this recyclerView
        mListAdapter = new CountdownFragment.MainNavAdapter(getActivity());
        recyclerView.setAdapter(mListAdapter);
    }

    private void initCountdownIfNecessary(Date startDate, long duration) {
        // Get the local date+time
        DateTime localDateTime = new DateTime();

        // Get the local date time zone to convert DT's to local times
        String localTZID = TimeZone.getDefault().getID();
        DateTimeZone localDTZ = DateTimeZone.forID(localTZID);

        // Get the start date in local time zone
        DateTime localStartDT = new DateTime(startDate); // Get the startDate in joda time library in EST tz
        localStartDT.toDateTime(localDTZ);

        // Get the endDT in local time
        DateTime localEndDT = new DateTime(localStartDT);
        localEndDT = localEndDT.plus(duration - 10800000);

        // Get the current, start, and end times in millis
        long curTime = localDateTime.getMillis();
        long startTime = startDate.getTime();
        long endTime = startTime + duration;

        Log.d(TAG, "Start Date Orig: " + startDate.toString() + " | " + startDate.getTime() + " | Duration: " + duration);
        Log.d(TAG, "Joda Start Date: " + localStartDT.toString() + " | " + localStartDT.getMillis());
        Log.d(TAG, "Joda End Date: " + localEndDT.toString() + " | " + localEndDT.getMillis() + " | Supposed: " + endTime);

        // Get a resources reference, to get the necessary display strings
        Resources res = getActivity().getResources();
        // Holds the strings to display
        String topTitle, topTime = null, bottomTitle, bottomTime = null;

        // Returns date times in the format similar to "DayName, MonthName DD, YYYY at HH:MM AM/PM."
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("EEEE, MMMM d, yyyy 'at' hh:mm a.");

        if (curTime < startTime) {
            // If so, it's not hack time just yet

            topTime = dateTimeFormatter.print(localStartDT);
            bottomTime = dateTimeFormatter.print(localEndDT);
        } else if (curTime < endTime) {
            // If so, hacking already started

            topTime = dateTimeFormatter.print(localStartDT);
            bottomTime = dateTimeFormatter.print(localEndDT);

            // Calculate the time remaining and the total time of hacking
            long timeRemaining = endTime - curTime;
            long totalHackingTime = endTime - startTime;

            // Start the countdown timer
            HackingCountdownTimer timer = new HackingCountdownTimer(timeRemaining, totalHackingTime);
            timer.start();
        } else {
            // Otherwise, hacking already ended =<

            topTime = dateTimeFormatter.print(localStartDT);
            bottomTime = dateTimeFormatter.print(localEndDT);

            // Set the counter to its "finished" state
            mCircularProgress.setProgress(100);
            mCountdownTextView.setText("Done!");
        }

        // Display the Strings in their respective TextViews
        mTopTimeText.setText(topTime);
        mBottomTimeText.setText(bottomTime);
    }

    private class HackingCountdownTimer extends CountDownTimer {
        // Used to display the time remaining prettily
        DateFormat outFormat;
        // Cached total amount of hacking time in milliseconds, to update the progress circle
        long totalHackingTimeInMillis;

        public HackingCountdownTimer(long millisInFuture, long totalHackingTimeInMillis) {
            super(millisInFuture, countdownUpdateIntervals);

            // Cache the total hacking time to determine progress later
            this.totalHackingTimeInMillis = totalHackingTimeInMillis;

            // Set up the formatter, to display the time remaining prettily later
            outFormat = new SimpleDateFormat("HH:mm:ss");
            outFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long hours = millisUntilFinished / 3600000;
            long minutes = (millisUntilFinished - (hours * 3600000)) / 60000;
            long seconds = (millisUntilFinished - (hours * 3600000) - (minutes * 60000));

            // Padding hrs, mins, and secs to prevent out of range on substring & to improve ux
            String hrs, min, sec;
            hrs = (hours < 10) ? "0" + String.valueOf(hours) : String.valueOf(hours);
            min = (minutes < 10) ? "0" + String.valueOf(minutes) : String.valueOf(minutes);
            sec = (seconds < 10) ? "0" + String.valueOf(seconds) : String.valueOf(seconds);

            // Update the countdown timer textView
            mCountdownTextView.setText(hrs + ":" + min + (":" + sec).substring(0, 3));

            // Update the progress [maxProgressInt - maxProgressInt*timeRemaining/total time]
            int progress = (int) (100 - 100 * millisUntilFinished / totalHackingTimeInMillis);
            mCircularProgress.setProgress(progress);
        }

        @Override
        public void onFinish() {
            mCircularProgress.setProgress(100);
            mCountdownTextView.setText("Done!");
        }
    }

    class MainNavAdapter extends RecyclerView.Adapter<CountdownFragment.MainNavAdapter.ViewHolder> {
        Context mContext;

        // Default constructor
        MainNavAdapter(Context context) {
            this.mContext = context;
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            public TextView nameView;
            public TextView locationView;
            public FrameLayout colorView;

            public ViewHolder(View itemView) {
                super(itemView);

                this.nameView = (TextView) itemView.findViewById(R.id.event_name);
                this.locationView = (TextView) itemView.findViewById(R.id.event_location);
                this.colorView = (FrameLayout) itemView.findViewById(R.id.event_color);
            }
        }

        @Override
        public CountdownFragment.MainNavAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View row = LayoutInflater.from(mContext).inflate(R.layout.event_list_item, viewGroup, false);

            // Create a new viewHolder which caches all the views that needs to be saved
            CountdownFragment.MainNavAdapter.ViewHolder viewHolder = new CountdownFragment.MainNavAdapter.ViewHolder(row);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(CountdownFragment.MainNavAdapter.ViewHolder viewHolder, int position) {
            Event event = nowEvents.get(position);

            // Set this item's views based off of the announcement data
            viewHolder.nameView.setText(event.getName());
            viewHolder.locationView.setText(event.getLocation());

            String category = event.getCategory();

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
        }

        @Override
        public int getItemCount() {
            return nowEvents.size();
        }
    }
}

