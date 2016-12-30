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
    private TextView eventNameTV, eventTimeTV, eventLocationNameTV, eventInfoTV;
    private View colorBlock;
    private FrameLayout eventInfoFrame, eventLocationNameFrame;

    // Event Details
    private String eventName;
    private String eventInfo;
    private Date eventStartTime, eventEndTime;
    private int eventColor;

    private EventsFragment parent;

    public static EventDetailFragment newInstance(Event event, int color) {
        EventDetailFragment eventDetailFragment = new EventDetailFragment();

        Bundle args = new Bundle();
        args.putString("title", event.getName());
        args.putString("description", event.getDescription());
//        args.putCharSequenceArray("location", event.getLocations()
//                .toArray(new CharSequence[event.getLocation().size()]));
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
            eventStartTime = new Date(args.getLong("startTime"));
            eventEndTime = new Date(args.getLong("endTime"));
            eventColor = args.getInt("color");
        }

        super.onCreate(savedInstanceState);
    }

    public void setParent(EventsFragment parent) {
        this.parent = parent;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mEventDetailFragView = inflater.inflate(R.layout.fragment_event_detail, container, false);

        //Instantiate TextViews
        eventNameTV = (TextView) mEventDetailFragView.findViewById(R.id.event_title);
        eventTimeTV = (TextView) mEventDetailFragView.findViewById(R.id.details_time);
        eventLocationNameTV = (TextView) mEventDetailFragView.findViewById(R.id.details_location);
        eventInfoTV = (TextView) mEventDetailFragView.findViewById(R.id.details_description);

        eventInfoFrame = (FrameLayout) mEventDetailFragView.findViewById(R.id.info_frame);
        eventLocationNameFrame = (FrameLayout) mEventDetailFragView.findViewById(R.id.location_name_frame);

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
        eventTimeTV.setText(formatDate(eventStartTime, eventEndTime));

        // Can be empty
        if (eventInfo.length() != 0) eventInfoTV.setText(eventInfo);
        else eventInfoFrame.setVisibility(View.GONE);

        String locationName = "";

        if (!locationName.isEmpty()) eventLocationNameTV.setText(locationName);
        else eventLocationNameFrame.setVisibility(View.GONE);
    }

    public String formatDate (Date startTime, Date endTime) {
        //Todo Redo
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMM d", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

        return dayFormat.format(startTime) + "\n" +  timeFormat.format(startTime) + " - " + timeFormat.format(endTime);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }
}