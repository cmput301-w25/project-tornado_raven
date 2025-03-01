package com.example.project.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.project.R;

public class FilterMoodDialog extends DialogFragment {

    private String[] moods;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Retrieve mood strings from resources
        moods = new String[]{
                getString(R.string.dialog_filter_mood_happy),
                getString(R.string.dialog_filter_mood_sad),
                getString(R.string.dialog_filter_mood_angry),
                getString(R.string.dialog_filter_mood_scared)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_filter_mood_title))
                .setItems(moods, (dialog, which) -> {
                    // Handle mood selection
                });

        return builder.create();
    }
}


