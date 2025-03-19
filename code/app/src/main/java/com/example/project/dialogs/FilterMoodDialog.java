package com.example.project.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.project.R;
/**
 * A DialogFragment that presents a dialog with a list of mood options for the user to select from.
 * The user can choose a mood from the available list.
 */
public class FilterMoodDialog extends DialogFragment {

    private String[] moods;
    /**
     * Creates the dialog that contains a list of mood options for the user to select from.
     * The dialog provides a list of moods, and the user can select one.
     *
     * @param savedInstanceState The saved state of the fragment, if any.
     * @return The created dialog.
     */
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


