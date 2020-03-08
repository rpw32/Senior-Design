package com.design.senior;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;

public class ServingDialog extends AppCompatDialogFragment {
    private EditText numServings;
    private Spinner portionSpinner;
    private Double servingSize;
    private ServingDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.serving_dialog, null);

        // Portion weights and descriptions are passed as arguments
        Bundle bundle = getArguments();
        ArrayList<String> portionWeights = bundle.getStringArrayList("portionWeights");
        ArrayList<String> portionDescriptions = bundle.getStringArrayList("portionDescriptions");
        ArrayList<String> portionDisplayed = new ArrayList<String>();

        // Combine each description and weight into one string
        for (int i = 0; i < portionDescriptions.size(); i++) {
            String portionFormat;
            // If the portion description is "Quantity not specified", break and move to the next portion
            if (portionDescriptions.get(i).equals("Quantity not specified")) {
                continue;
            }
            else {
                portionFormat = portionDescriptions.get(i) + " (" + portionWeights.get(i) + " g)";
            }
            portionDisplayed.add(portionFormat);
        }

        numServings = view.findViewById(R.id.numServings);
        portionSpinner = view.findViewById(R.id.portionSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, portionDisplayed);
        portionSpinner.setAdapter(adapter);

        builder.setView(view)
                .setTitle("How Much?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int index = portionSpinner.getSelectedItemPosition();
                        String stringSize = portionWeights.get(index);
                        servingSize = Double.parseDouble(stringSize);
                        servingSize = servingSize * Double.parseDouble(numServings.getText().toString());
                        listener.servingSelection(servingSize);
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    // Listener is used to pass the servingSize back to the detail page
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ServingDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ServingDialogListener");
        }
    }

    public interface ServingDialogListener {
        void servingSelection(Double servingSize);
    }

}
