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
import android.widget.Button;
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

    private Button checkinButton;
    private boolean isVol = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        checkinButton = (Button) view.findViewById(R.id.checkin_button);
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

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(mEvents == null) {
            mEvents = new ArrayList<>();
        }

        getEvents();
    }

    public void setVol(boolean isVol){
        this.isVol = isVol;
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
                toast = "Closed scanner";
            } else if(!isEvent(result.getContents()).equals("")){
                String event = result.getContents();
                String pointVal = isEvent(event);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                String email = user.getEmail();
                String dbKey = email.replace("@", "").replace(".", "");

                DatabaseReference userEventsRef = database.getReference().child("attendee_events").child(dbKey);
                userEventsRef.child(event).setValue(pointVal);
                toast = "Scanned into " + event;
            } else {
                toast = "Failed to scan event";
            }

            displayToast();
        }
    }

    public String isEvent(String name){
        switch (name){
            case "Pancake Art":
                return "main";
            case "Brain Bowl":
                return  "main";
            case "Musical Chairs":
                return "main";
            case "Balloon Battle":
                return  "main";
            case "Ping Pong":
                return "main";
            case "Stepping Challenge":
                return  "main";
            case "Paper Airplane":
                return  "main";
            case "Youtube Karaoke":
                return "mini";
            case "Rock Paper Scissors":
                return "mini";
            case "Cup Stacking":
                return "mini";
            case "Bubble Wrap":
                return "mini";
            case "Branding Competition":
                return "mini";
            case "Smash Bros":
                return "mini";
            case "Yoga":
                return "mini";
            case "Soylent Art":
                return "mini";
            case "Cornhole":
                return "mini";
        }

        return "";
    }

    private void setUpWeekView() {
        //Set listeners
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);

        //Set up visuals of the calendar
        mWeekView.setBackgroundColor(Color.WHITE);
        mWeekView.setEventTextColor(Color.WHITE);
        mWeekView.setNumberOfVisibleDays(1);
        mWeekView.setTextSize(22);
        mWeekView.setHourHeight(300);
        mWeekView.setHeaderColumnPadding(10);
        mWeekView.setHeaderRowPadding(10);
        mWeekView.setColumnGap(20);
        mWeekView.setHourSeparatorColor(Color.WHITE);
        mWeekView.setHourSeparatorHeight(10);
        mWeekView.setHeaderColumnBackgroundColor(Color.WHITE);
        mWeekView.setHeaderColumnBackgroundColor(Color.BLACK);
        mWeekView.setOverlappingEventGap(2);
        mWeekView.setEventMarginVertical(5);
        mWeekView.setShowNowLine(true);
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
                mWeekView.notifyDatasetChanged();
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mWeekView.notifyDatasetChanged();
                        }
                    });
                } catch(Error error){
                    Log.d("Error: ", error.toString());
                }
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
            weekViewEvent.setLocation(event.getLocation());
            weekViewEvent.setColor(color);

            if (startTime.get(Calendar.MONTH) == month)
                weekViewEvents.add(weekViewEvent);

            id++;
        }

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
            case "techtalk":
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
                return ContextCompat.getColor(getActivity(), R.color.event_red);
            case 1: // Social
                return ContextCompat.getColor(getActivity(), R.color.event_green);
            case 2: // Food
                return ContextCompat.getColor(getActivity(), R.color.event_orange);
            case 3: // Tech Talk
                return ContextCompat.getColor(getActivity(), R.color.event_purple);
            case 4: // Other
                return ContextCompat.getColor(getActivity(), R.color.event_green);
            default:
                return ContextCompat.getColor(getActivity(), R.color.event_yellow);
        }
    }

    public boolean getEventDetailsOpened() {
        return eventDetailsOpen;
    }

    public void setEventDetailsOpened(Boolean bool) {
        eventDetailsOpen = bool;
    }

    public void closeEventDetails() {
        if(eventDetailsOpen) {
            getActivity().getFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .remove(getFragmentManager().findFragmentById(R.id.drawer_layout)).commit();
        }
        setEventDetailsOpened(false);
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        if (!eventDetailsOpen) {
            eventDetailFragment = EventDetailFragment.newInstance(mEvents.get((int) event.getId()), event.getColor());
            eventDetailFragment.setParent(this);
            eventDetailFragment.setVol(isVol);
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
