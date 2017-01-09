package com.swamphacks.swamphacks_android.events;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.swamphacks.swamphacks_android.R;
import data.models.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";

    // Decalre Views.
    private View mEventDetailFragView;
    private TextView eventNameTV, eventTimeTV, eventLocationNameTV, eventInfoTV, eventTimeHourTV;
    private View colorBlock;

    // Event Details
    private String eventName;
    private String eventInfo;
    private String eventLocation;
    private Date eventStartTime, eventEndTime;
    private int eventColor;

    private Fragment parent;

    public static EventDetailFragment newInstance(Event event, int color) {
        EventDetailFragment eventDetailFragment = new EventDetailFragment();

        Bundle args = new Bundle();
        args.putString("title", event.getName());
        args.putString("description", event.getDescription());
        args.putString("location", event.getLocation());
        args.putLong("startTime", event.getStart());
//        args.putLong("duration", event.getDuration());
        args.putInt("color", color);
        eventDetailFragment.setArguments(args);

        return eventDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();

        // Either has all of the keys or none, so only checking for the title.
        if (args.containsKey("title")) {
            eventName = args.getString("title");
            eventInfo = args.getString("description");
            eventLocation = args.getString("location");
            eventStartTime = new Date(args.getLong("startTime"));
            eventEndTime = new Date(args.getLong("endTime"));
            eventColor = args.getInt("color");
        }

        super.onCreate(savedInstanceState);
    }

    public void setParent(Fragment parent) {
        this.parent = parent;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mEventDetailFragView = inflater.inflate(R.layout.fragment_event_detail, container, false);

        //Instantiate TextViews
        eventNameTV = (TextView) mEventDetailFragView.findViewById(R.id.event_title);
        eventTimeTV = (TextView) mEventDetailFragView.findViewById(R.id.details_time);
        eventTimeHourTV = (TextView) mEventDetailFragView.findViewById(R.id.details_hourtime);
        eventLocationNameTV = (TextView) mEventDetailFragView.findViewById(R.id.details_location);
        eventInfoTV = (TextView) mEventDetailFragView.findViewById(R.id.details_description);

        //Instantiate color header block
        colorBlock = mEventDetailFragView.findViewById(R.id.header_color_block);
        colorBlock.setBackgroundColor(eventColor);

        //Hide toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        setEventDetails();

        return mEventDetailFragView;
    }

    public void setEventDetails() {
        eventNameTV.setText(eventName);
        eventTimeTV.setText(formatDate(eventStartTime));
        eventTimeHourTV.setText(formatTime(eventStartTime));
        eventLocationNameTV.setText(eventLocation);

        // Can be empty
        if (eventInfo.length() != 0) eventInfoTV.setText(eventInfo);
        String locationName = "";

        if (!locationName.isEmpty()) eventLocationNameTV.setText(locationName);
    }

    public String formatDate (Date startTime) {
        //Todo Redo
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMM d", Locale.US);

        return dayFormat.format(startTime);
    }

    public String formatTime(Date startTime){
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

        return timeFormat.format(startTime);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }
}