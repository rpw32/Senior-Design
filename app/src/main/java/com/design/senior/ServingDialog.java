package com.design.senior;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class ServingDialog extends AppCompatDialogFragment {
    private EditText numServings;
    private Spinner portionSpinner;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.serving_dialog, null);

        builder.setView(view)
                .setTitle("How Much?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double servingSize = Double.parseDouble(numServings.getText().toString());

                    }
                });

        numServings = view.findViewById(R.id.numServings);
        portionSpinner = view.findViewById(R.id.portionSpinner);

        return builder.create();

    }

    public interface ServingDialogListener{
        void applyTexts(double servingSize, String spinnerChoice);
    }
}
