package com.example.project.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.EditText;
import com.example.project.R;

/**
 * A DialogFragment that presents a dialog with an EditText for the user to input a keyword
 * to filter items by. It provides the options to confirm or cancel the input.
 */
public class FilterKeywordDialog extends DialogFragment {
    /**
     * Creates the dialog that contains an EditText field for entering the filter keyword.
     * The dialog provides options to confirm the input or dismiss the dialog.
     *
     * @param savedInstanceState The saved state of the fragment, if any.
     * @return The created dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create an AlertDialog builder for constructing the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        EditText input = new EditText(getActivity());
        // Set up the dialog's title, input field, and buttons
        builder.setTitle(getString(R.string.dialog_filter_keyword_title))
                .setView(input)// Add the EditText to the dialog's layout
                .setPositiveButton(getString(R.string.dialog_filter_keyword_ok), (dialog, which) -> {
                    // Handle the OK button click
                    String keyword = input.getText().toString();
                })
                .setNegativeButton(getString(R.string.dialog_filter_keyword_cancel), (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}
