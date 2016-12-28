package com.swamphacks.swamphacks_android.announcements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.swamphacks.swamphacks_android.MainActivity;
import com.swamphacks.swamphacks_android.R;

import java.util.ArrayList;
import java.util.List;

public class FilterDialogFragment extends DialogFragment{
    List<String> mSelectedItems;
    boolean[] selected = MainActivity.filterList;
    FilterListener listener;

    public void setListener(FilterListener listener){
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("filterlist", "" + selected[0] + " " + selected[1]);
        mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Filter")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.announcements, selected,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    selected[which] = true;
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    selected[which] = false;
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.filterList = selected;
                        listener.updateView(true, "Yaaas");
//                        AnnouncementsFragment announcementsFragment = (AnnouncementsFragment) getFragmentManager().findFragmentByTag("announcements");
//                        announcementsFragment.getAnnouncements();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
