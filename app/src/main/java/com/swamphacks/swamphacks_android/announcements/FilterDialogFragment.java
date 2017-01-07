package com.swamphacks.swamphacks_android.announcements;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LauncherActivity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("filterlist", "" + selected[0] + " " + selected[1]);
        mSelectedItems = new ArrayList();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Filter")
                // Specify the list array, the items to be selected by default, and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.announcements, selected,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    selected[which] = true;
                                } else if (mSelectedItems.contains(which)) {
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
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button okButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button cancelButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        ListView listView = alertDialog.getListView();

        okButton.setBackground(getResources().getDrawable(R.drawable.button_blue_white_border));
        cancelButton.setBackground(getResources().getDrawable(R.drawable.button_blue_white_border));
        listView.setBackgroundColor(R.color.gradient_start);

        return alertDialog;
    }
}
