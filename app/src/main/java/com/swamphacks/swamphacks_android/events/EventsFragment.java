package com.swamphacks.swamphacks_android.events;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

import com.alamkanak.weekview.*;
import data.models.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class EventsFragment extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener {

    public static final String TAG = "EventsFragment";

    String toast;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Declaring Views
    private LinearLayout mScheduleContainer;
    private WeekView mWeekView;

    // Event data structures
    private ArrayList<Event> mEvents;
    private ArrayList<WeekViewEvent> weekViewEvents;

    // Booleans
    private boolean eventDetailsOpen = false; //Prevents multiple EventDetailFragments from opening.

    // Declares the EventDetailsFragment
    private EventDetailFragment eventDetailFragment;

    private FloatingActionButton checkinButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        checkinButton = (FloatingActionButton) view.findViewById(R.id.checkin_button);

        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEventCheckinView();
            }
        });

        mScheduleContainer = (LinearLayout) view.findViewById(R.id.schedule_container);
        mWeekView = (WeekView) view.findViewById(R.id.week_view);
        setUpWeekView();

        return view;
    }

    public void openEventCheckinView(){
        IntentIntegrator
                .forFragment(this)
                .setPrompt("Scan Event QR Code")
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
                toast = "Cancelled from fragment";
            } else if(isEvent(result.getContents())){
                final String event = result.getContents();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                final String email = user.getEmail();
                String dbKey = email.replace("@", "").replace(".", "");

                final DatabaseReference userEventsRef = database.getReference().child("confirmed").child(dbKey).child("events");

                userEventsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean has = false;
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            if(postSnapshot.getKey() == event){
                                has = true;
                                break;
                            }
                        }
                        if(!has){
                            userEventsRef.child(event).setValue(getPointValue(event));
                        }
                        else {
                            toast = "Already checked into event!";
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.d("some error", error.toString());
                    }
                });
            } else {
                toast = "Failed to scan event";
            }

            displayToast();
        }
    }

    public boolean isEvent(String name){
        switch (name){
            case "":
                return true;
            case "a":
                return  true;
        }

        return false;
    }

    public int getPointValue(String name){
        switch (name){
            case "":
                return 10;
            case "a":
                return 20;
        }

        return 0;
    }

    private void setUpWeekView() {
        //Set listeners
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);

        //Set up visuals of the calendar
        mWeekView.setBackgroundColor(Color.WHITE);
        mWeekView.setEventTextColor(Color.WHITE);
        mWeekView.setNumberOfVisibleDays(2);
        mWeekView.setTextSize(22);
        mWeekView.setHourHeight(120);
        mWeekView.setHeaderColumnPadding(8);
        mWeekView.setHeaderRowPadding(16);
        mWeekView.setColumnGap(8);
        mWeekView.setHourSeparatorColor(Color.WHITE);
        mWeekView.setHourSeparatorHeight(4);
        mWeekView.setHeaderColumnBackgroundColor(Color.WHITE);
        mWeekView.setHeaderColumnBackgroundColor(Color.BLACK);
        mWeekView.setOverlappingEventGap(2);

    }

    public void getEvents() {
        DatabaseReference myRef = database.getReference().child("events");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEvents = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Event event = postSnapshot.getValue(Event.class);
                    mEvents.add(event);
                }
                Log.d(TAG, "got " + mEvents.size() + " events");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWeekView.notifyDatasetChanged();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public ArrayList<WeekViewEvent> createWeekViewEvents(ArrayList<Event> events, int month) {
        weekViewEvents = new ArrayList<>();

        long id = 0;

        for (Event event : events) {
            // Create start event.
            GregorianCalendar startTime = new GregorianCalendar(TimeZone.getDefault());
            startTime.setTime(new Date(event.getStart()));

            // Create end event.
            GregorianCalendar endTime = new GregorianCalendar(TimeZone.getDefault());
            endTime.setTime(new Date(event.getEnd()*1000));

            // Set color based on EventType (Category).
            int color = getEventColor(convertTypeToInt(event.getCategory()));

            // Create a WeekViewEvent
            WeekViewEvent weekViewEvent = new WeekViewEvent(id, event.getName(), startTime, endTime);
            weekViewEvent.setColor(color);

            if (startTime.get(Calendar.MONTH) == month)
                weekViewEvents.add(weekViewEvent);

            id++;
        }

        Log.d(TAG, "created " + weekViewEvents.size() + " events");
        return weekViewEvents;
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
            case "other":
                return 4;
        }
        return -1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public int getEventColor(int eventType) {
        switch (eventType) {
            case 0: // Logistics
                return ContextCompat.getColor(getActivity(), R.color.event_blue);
            case 1: // Social
                return ContextCompat.getColor(getActivity(), R.color.event_red);
            case 2: // Food
                return ContextCompat.getColor(getActivity(), R.color.event_yellow);
            case 3: // Tech Talk
                return ContextCompat.getColor(getActivity(), R.color.event_purple);
            case 4: // Other
                return ContextCompat.getColor(getActivity(), R.color.event_green);
            default:
                return ContextCompat.getColor(getActivity(), R.color.event_blue);
        }
    }

    public boolean getEventDetailsOpened() {
        return eventDetailsOpen;
    }

    public void setEventDetailsOpened(Boolean bool) {
        eventDetailsOpen = bool;
    }

    public void refreshEvents() {
        getEvents();
    }

    public void closeEventDetails() {
        //Close the EventDetailsFragment
        getActivity().getFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .remove(getFragmentManager().findFragmentById(R.id.drawer_layout)).commit();
        setEventDetailsOpened(false);
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        if (!eventDetailsOpen) {
            eventDetailFragment =
                    EventDetailFragment.newInstance(mEvents.get((int) event.getId()), event.getColor());
            eventDetailFragment.setParent(this);
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(null) //IMPORTANT. Allows the EventDetailsFragment to be closed.
                    .add(R.id.drawer_layout, eventDetailFragment)
                    .commit();
            //Hide the toolbar so the event details are full screen.
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            //Prevents other events from being clicked while one event's details are being shown.
            setEventDetailsOpened(true);
        }
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        if (mEvents == null || mEvents.size() == 0) {
            getEvents();
            return new ArrayList<>();
        } else {
            return createWeekViewEvents(mEvents, newMonth - 1);
        }
    }
}
