package com.swamphacks.swamphacks_android.sponsors;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swamphacks.swamphacks_android.R;

import java.util.ArrayList;
import java.util.List;

import data.models.Sponsor;
import data.models.SponsorRep;

public class SponsorDetailFragment extends Fragment {
    private static final String TAG = "SponsorDetailFragment";
    private static List<SponsorRep> repList = new ArrayList<>();

    private View mSponsorDetailFragView;
    private TextView sponsorNameTV, sponsorLocationTV, sponsorDescriptionTV;
    private View colorBlock;

    // Event Details
    private String sponsorName;
    private String sponsorLocation;
    private String sponsorDescription;
    private String sponsorLink;
    private String sponsorTier;

    RecyclerView recyclerView;
    SponsorDetailFragment.MainNavAdapter mListAdapter;

    private SponsorsFragment parent;

    public static SponsorDetailFragment newInstance(Sponsor sponsor, int color) {
        SponsorDetailFragment sponsorDetailFragment = new SponsorDetailFragment();

        Bundle args = new Bundle();
        args.putString("name", sponsor.getName());
        args.putString("location", sponsor.getLocation());
        args.putString("description", sponsor.getDescription());
        args.putString("link", sponsor.getLink());
        args.putString("tier", sponsor.getTier());
        sponsorDetailFragment.setArguments(args);
        for(String s : sponsor.getReps().keySet()){
            SponsorRep sponsorRep = new SponsorRep();
            sponsorRep.setName(sponsor.getReps().get(s).get("name"));
            sponsorRep.setTitle(sponsor.getReps().get(s).get("title"));
            sponsorRep.setImage(sponsor.getReps().get(s).get("image"));
            repList.add(sponsorRep);
        }

        return sponsorDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();

        if (args.containsKey("name")) {
            sponsorName = args.getString("name");
            sponsorDescription = args.getString("description");
            sponsorLink = args.getString("link");
            sponsorTier = args.getString("tier");
            sponsorLocation = args.getString("location");
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

        recyclerView = (RecyclerView) mSponsorDetailFragView.findViewById(R.id.list_reps);

        //Instantiate TextViews
        sponsorNameTV = (TextView) mSponsorDetailFragView.findViewById(R.id.sponsor_name);
        sponsorLocationTV = (TextView) mSponsorDetailFragView.findViewById(R.id.sponsor_location);
        sponsorDescriptionTV = (TextView) mSponsorDetailFragView.findViewById(R.id.sponsor_description);

        //Instantiate color header block
//        colorBlock = mEventDetailFragView.findViewById(R.id.header_color_block);
//        colorBlock.setBackgroundColor(eventColor);

        //Hide toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        setSponsorDetails();

        return mSponsorDetailFragView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initList();
    }

    private void initList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Create and set the adapter for this recyclerView
        mListAdapter = new SponsorDetailFragment.MainNavAdapter(getActivity());
        recyclerView.setAdapter(mListAdapter);
    }

    public void setSponsorDetails() {
        sponsorNameTV.setText(sponsorName);
        sponsorDescriptionTV.setText(sponsorDescription);
        sponsorLocationTV.setText(sponsorLocation);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        repList = new ArrayList<SponsorRep>();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    public class MainNavAdapter extends RecyclerView.Adapter<SponsorDetailFragment.MainNavAdapter.ViewHolder> {
        Context mContext;

        // Default constructor
        MainNavAdapter(Context context) {
            this.mContext = context;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView nameView;
            public TextView titleView;

            public ViewHolder(View itemView) {
                super(itemView);

                this.nameView = (TextView) itemView.findViewById(R.id.rep_name);
                this.titleView = (TextView) itemView.findViewById(R.id.rep_title);
            }
        }

        @Override
        public SponsorDetailFragment.MainNavAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View row = LayoutInflater.from(mContext).inflate(R.layout.rep_list_item, viewGroup, false);

            // Create a new viewHolder which caches all the views that needs to be saved
            SponsorDetailFragment.MainNavAdapter.ViewHolder viewHolder = new SponsorDetailFragment.MainNavAdapter.ViewHolder(row);

            return viewHolder;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(SponsorDetailFragment.MainNavAdapter.ViewHolder viewHolder, int position) {
            SponsorRep sponsorRep = repList.get(position);

            // Set this item's views based off of the announcement data
            viewHolder.nameView.setText(sponsorRep.getName());
            viewHolder.titleView.setText(sponsorRep.getTitle());
        }

        @Override
        public int getItemCount() {
            return repList.size();
        }
    }
}
