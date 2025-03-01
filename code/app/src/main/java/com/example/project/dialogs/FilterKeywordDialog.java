package com.example.project.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.EditText;
import com.example.project.R;

public class FilterKeywordDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        EditText input = new EditText(getActivity());

        builder.setTitle(getString(R.string.dialog_filter_keyword_title))
                .setView(input)
                .setPositiveButton(getString(R.string.dialog_filter_keyword_ok), (dialog, which) -> {
                    // Handle keyword filter
                    String keyword = input.getText().toString();
                })
                .setNegativeButton(getString(R.string.dialog_filter_keyword_cancel), (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}
