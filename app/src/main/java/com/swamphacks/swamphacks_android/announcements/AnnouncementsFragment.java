package com.swamphacks.swamphacks_android.announcements;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.swamphacks.swamphacks_android.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class AnnouncementsFragment extends Fragment {
    private static final String TAG = "MD/AnnouncementsFrag";

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcements, container, false);

        // Cache the views that need to be edited later on

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Start everything off by getting the parse data
        //getLatestParseData();
    }

    /**
     * Initializes the countdown based off of the start date and duration if necessary
     *
     * @param startDate - Date(+time) this countdown is supposed to start
     * @param duration  - How long from this start date this countdown timer should end, in milliseconds
     */
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

        } else {
            // Otherwise, hacking already ended =<

            topTime = dateTimeFormatter.print(localStartDT);
            bottomTime = dateTimeFormatter.print(localEndDT);

            // Set the counter to its "finished" state
            mCircularProgress.setProgress(100);
            mCountdownTextView.setText("Done!");
        }

        // Display the Strings in their respective TextViews
        mBottomTimeText.setText(bottomTime);
    }

    private class HackingCountdownTimer extends CountDownTimer {
        // Used to display the time remaining prettily
        DateFormat outFormat;
        // Cached total amount of hacking time in milliseconds, to update the progress circle
        long totalHackingTimeInMillis;

        /**
         * DEPRECATED but here for reference
         *
         * @param millisInFuture The number of millis in the future from the call
         *                       to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                       is called.
         *                       param countDownInterval The interval along the way to receive
         *                       {@link #onTick(long)} callbacks.
         */
//        public HackingCountdownTimer(long millisInFuture, long countDownInterval) {
//            super(millisInFuture, countDownInterval);
//        }
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
}
