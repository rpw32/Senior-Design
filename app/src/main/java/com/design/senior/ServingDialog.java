package com.design.senior;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ServingDialog extends AppCompatDialogFragment {
    private EditText numServings;
    private Spinner portionSpinner;
    private activity_detail_result.FoodViewModel model;
    private Double servingSize;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        model = new ViewModelProvider(this).get(activity_detail_result.FoodViewModel.class);


        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.serving_dialog, null);

        ArrayList<String> portionWeights = getActivity().getIntent().getExtras().getStringArrayList("portionWeights");
        ArrayList<String> portionDescriptions = getActivity().getIntent().getExtras().getStringArrayList("portionDescriptions");
        ArrayList<String> portionDisplayed = new ArrayList<String>();

        for (int i = 0; i < portionDescriptions.size(); i++) {
            String portionFormat = portionDescriptions.get(i) + "(" + portionWeights.get(i) + " g)";
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
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int index = portionSpinner.getSelectedItemPosition();
                        String stringSize = portionWeights.get(index);
                        servingSize = Double.parseDouble(stringSize);
                        dialog.dismiss();
                    }
                });
        if (servingSize != null) {
            model.setServingSize(servingSize);
        }

        return builder.create();
    }

}
