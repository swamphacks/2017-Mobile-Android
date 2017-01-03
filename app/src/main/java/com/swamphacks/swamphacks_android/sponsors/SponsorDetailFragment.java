package com.swamphacks.swamphacks_android.sponsors;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swamphacks.swamphacks_android.R;

import data.models.Sponsor;

public class SponsorDetailFragment extends Fragment {
    private static final String TAG = "SponsorDetailFragment";

    private View mSponsorDetailFragView;
    private TextView sponsorNameTV;
    private View colorBlock;

    // Event Details
    private String sponsorName;

    private SponsorsFragment parent;

    public static SponsorDetailFragment newInstance(Sponsor sponsor, int color) {
        SponsorDetailFragment sponsorDetailFragment = new SponsorDetailFragment();

        Bundle args = new Bundle();
        args.putString("name", sponsor.getName());
        sponsorDetailFragment.setArguments(args);

        return sponsorDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();

        if (args.containsKey("name")) {
            sponsorName = args.getString("name");
        }

        super.onCreate(savedInstanceState);
    }

    public void setParent(SponsorsFragment parent) {
        this.parent = parent;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSponsorDetailFragView = inflater.inflate(R.layout.fragment_sponsor_detail, container, false);

        //Instantiate TextViews
        sponsorNameTV = (TextView) mSponsorDetailFragView.findViewById(R.id.sponsor_name);

        //Instantiate color header block
//        colorBlock = mEventDetailFragView.findViewById(R.id.header_color_block);
//        colorBlock.setBackgroundColor(eventColor);

        //Hide toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        setSponsorDetails();

        return mSponsorDetailFragView;
    }

    public void setSponsorDetails() {
        sponsorNameTV.setText(sponsorName);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }
}
