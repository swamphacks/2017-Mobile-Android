package com.swamphacks.swamphacks_android.sponsors;

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.swamphacks.swamphacks_android.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import data.models.Sponsor;


public class SponsorsFragment extends Fragment {
    private static final String TAG = "MD/Sponsors";
    private SponsorDetailFragment sponsorDetailFragment;
    private boolean sponsorDetailOpen = false;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    ArrayList<Sponsor> mSponsorsList;

    RecyclerView mRecyclerView;

    SponsorsFragment.MainNavAdapter mListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sponsors, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_cards_sponsors);

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this.getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        pushToDetailView(position);
                        TextView tv = (TextView) view.findViewById(R.id.sponsor_name);
                    }
                })
        );

        return view;
    }

    public void pushToDetailView(int position){
        if(!sponsorDetailOpen) {
            sponsorDetailFragment = SponsorDetailFragment.newInstance(mSponsorsList.get(position), 1);
            sponsorDetailFragment.setParent(this);
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(null) //IMPORTANT. Allows the EventDetailsFragment to be closed.
                    .add(R.id.drawer_layout, sponsorDetailFragment)
                    .commit();
            //Hide the toolbar so the event details are full screen.
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            sponsorDetailOpen = true;
        }
    }

    public void closeSponsorDetails() {
        if(sponsorDetailOpen) {
            getActivity().getFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .remove(getFragmentManager().findFragmentById(R.id.drawer_layout)).commit();
        }
        sponsorDetailOpen = false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(mSponsorsList == null) {
            mSponsorsList = new ArrayList<>();
        }

        initList();

        // Get Firebase data of sponsors for the first time
        getSponsors();
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
        mListAdapter = new SponsorsFragment.MainNavAdapter(getActivity());
        mRecyclerView.setAdapter(mListAdapter);
    }

    public void getSponsors() {
        DatabaseReference myRef = database.getReference().child("sponsors");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSponsorsList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Sponsor sponsor = postSnapshot.getValue(Sponsor.class);
                    mSponsorsList.add(sponsor);
                }

                Comparator<Sponsor> priorityBasedComparator = new Comparator<Sponsor>() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public int compare(Sponsor s1, Sponsor s2) {
                        int priority1 = s1.getPriority();
                        int priority2 = s2.getPriority();
                        return Integer.compare(priority1, priority2);
                    }
                };

                Collections.sort(mSponsorsList, priorityBasedComparator);

                updateSponsors();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void updateSponsors() {
        // Notify the adapter that the data changed
        mListAdapter.notifyDataSetChanged();
    }

    class MainNavAdapter extends RecyclerView.Adapter<SponsorsFragment.MainNavAdapter.ViewHolder> {
        Context mContext;

        // Default constructor
        MainNavAdapter(Context context) {
            this.mContext = context;
        }

        // Simple class that holds all the views that need to be reused
        class ViewHolder extends RecyclerView.ViewHolder{
            public TextView nameView;
            public TextView locationView;
            public TextView descriptionView;
            public ImageView logoView;
            public FrameLayout colorView;

            // Default constructor, itemView holds all the views that need to be saved
            public ViewHolder(View itemView) {
                super(itemView);

                // Save the TextViews
//                this.nameView = (TextView) itemView.findViewById(R.id.sponsor_name);
                this.locationView = (TextView) itemView.findViewById(R.id.sponsor_location);
                this.descriptionView = (TextView) itemView.findViewById(R.id.sponsor_description);
                this.colorView = (FrameLayout) itemView.findViewById(R.id.sponsor_color);
                this.logoView = (ImageView) itemView.findViewById(R.id.logo_image);
            }
        }

        @Override
        public SponsorsFragment.MainNavAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            // Create the view for this row
            View row = LayoutInflater.from(mContext).inflate(R.layout.sponsor_list_item, viewGroup, false);

            // Create a new viewHolder which caches all the views that needs to be saved
            SponsorsFragment.MainNavAdapter.ViewHolder viewHolder = new SponsorsFragment.MainNavAdapter.ViewHolder(row);

            return viewHolder;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(SponsorsFragment.MainNavAdapter.ViewHolder viewHolder, int i) {
            Sponsor sponsor = mSponsorsList.get(i);

            if(sponsor.getLogo().length() > 50){
                byte[] decodedString = Base64.decode(sponsor.getLogo(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                viewHolder.logoView.setImageBitmap(decodedByte);
            }
            String tier = sponsor.getTier();

            switch (tier) {
                case "heron":
                    viewHolder.colorView.setBackgroundColor(getResources().getColor(R.color.event_orange));
                    break;
                case "turtle":
                    viewHolder.colorView.setBackgroundColor(getResources().getColor(R.color.event_blue));
                    break;
                case "lilypad":
                    viewHolder.colorView.setBackgroundColor(getResources().getColor(R.color.event_green));
                    break;
                case "other":
                    viewHolder.colorView.setBackgroundColor(getResources().getColor(R.color.event_purple));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mSponsorsList.size();
        }
    }
}
