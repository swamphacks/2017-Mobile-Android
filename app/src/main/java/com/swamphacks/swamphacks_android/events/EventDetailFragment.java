package com.swamphacks.swamphacks_android.events;

import android.app.Fragment;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.swamphacks.swamphacks_android.R;
import com.swamphacks.swamphacks_android.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDetailFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";

    // Decalre Views.
    private View mEventDetailFragView;
    private TextView eventNameTV, eventTimeTV, eventLocationNameTV, eventInfoTV;
    private View colorBlock; //Header color. Matches color of event in calendar.
    private FrameLayout eventInfoFrame, eventLocationNameFrame;

    // Date arrays
    private final String[] dayOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday"};
    private final String[] monthOfYear = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November",
            "December"};

    // Event Details
    private String eventName;
    private String eventInfo;
    private CharSequence[] eventLocationIds;
    private Date eventStartTime, eventEndTime;
    private int eventColor;

    private EventsFragment parent;

    /**
     * Creates a new instance of the EventDetailsFragment.
     * @param event Event to display in detailed view.
     * @param color Color of the event.
     * @return An EventDetailsFragment with the passed Event and color.
     */
    public static EventDetailFragment newInstance(Event event, int color) {
        EventDetailFragment f = new EventDetailFragment();

        Bundle args = new Bundle();
        args.putString("title", event.getName());
        args.putString("info", event.getInfo());
        args.putCharSequenceArray("locationIds", event.getLocations()
                .toArray(new CharSequence[event.getLocations().size()]));
        args.putLong("startTime", event.getStart());
        args.putLong("duration", event.getDuration());
        args.putInt("color", color);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();

        // Either has all of the keys or none, so only checking for the title.
        if (args.containsKey("title")) {
            eventName = args.getString("title");
            eventInfo = args.getString("info");
            eventLocationIds = args.getCharSequenceArray("locationIds");
            eventStartTime = new Date(args.getLong("startTime"));
            eventEndTime = new Date(args.getLong("startTime") + (args.getLong("duration") * 1000));
            eventColor = args.getInt("color");
        }

        super.onCreate(savedInstanceState);
    }

    public void setParent(EventsFragment parent) {
        this.parent = parent;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
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

    /**
     * Method to use the Event object to populate the view using the appropriate info.
     */
    public void setEventDetails() {
        // These better exist...
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
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMM d", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

        return dayFormat.format(startTime) + "\n"
                +  timeFormat.format(startTime) + " - " + timeFormat.format(endTime);
    }

    public ArrayList<Location> getLocations(String[] locationIds) {
        return new ArrayList<Location>();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }
}