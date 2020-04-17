package com.design.senior;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class TestSettingsActivity extends AppCompatActivity{

    private static final String TAG = TestSettingsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_settings);

        String text;
        double defaultValue = 50;

        // Link variables to their layout IDs
        Button resetButton = findViewById(R.id.resetButton);
        TextView settingsTitle = findViewById(R.id.settingsTitle);
        TextView calorieText = findViewById(R.id.calorie_textView);
        TextView fatText = findViewById(R.id.fat_textView);
        TextView satText = findViewById(R.id.satfat_textView);
        TextView transText = findViewById(R.id.transfat_textView);
        TextView sodiumText = findViewById(R.id.sodium_textView);
        TextView sodiumCondText = findViewById(R.id.sodium_condiment_textView);
        TextView cholesterolText = findViewById(R.id.cholesterol_textView);
        TextView fiberText = findViewById(R.id.fiber_textView);
        TextView flourText = findViewById(R.id.flour_textView);
        TextView sugarText = findViewById(R.id.sugar_textView);
        SeekBar calorieSeek = findViewById(R.id.calorie_seekBar);
        SeekBar fatSeek = findViewById(R.id.fat_seekBar);
        SeekBar satSeek = findViewById(R.id.satfat_seekBar);
        SeekBar transSeek = findViewById(R.id.transfat_seekBar);
        SeekBar sodiumSeek = findViewById(R.id.sodium_seekBar);
        SeekBar sodiumCondSeek = findViewById(R.id.sodium_condiment_seekBar);
        SeekBar cholesterolSeek = findViewById(R.id.cholesterol_seekBar);
        SeekBar fiberSeek = findViewById(R.id.fiber_seekBar);
        SeekBar flourSeek = findViewById(R.id.flour_seekBar);
        SeekBar sugarSeek = findViewById(R.id.sugar_seekBar);

        settingsTitle.append("Set Acceptable Test Values");

        // Set default setting values
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(this);
        String[] settingNames = {"calorie", "fat", "satfat", "transfat", "sodium", "cholesterol", "fiber", "flour", "sugar", "sodiumCond"};

        // Settings default to 50 or 1 if they are not set
        for (int i = 0; i < settingNames.length; i++) {
            if(!mDatabaseHelper.settingsCheckAlreadyExist(settingNames[i])) {
                if(i == 3 || i == 7 || i == 8) { // transfat, flour and sugar all have a max slider value of 1
                    mDatabaseHelper.settingsAddData(settingNames[i], 1);
                }
                else {
                    mDatabaseHelper.settingsAddData(settingNames[i], 50);
                }
            }
        }

        // Set SeekBars to the database values
        calorieSeek.setProgress(mDatabaseHelper.settingsGetData("calorie"));
        fatSeek.setProgress(mDatabaseHelper.settingsGetData("fat"));
        satSeek.setProgress(mDatabaseHelper.settingsGetData("satfat"));
        transSeek.setProgress(mDatabaseHelper.settingsGetData("transfat"));
        sodiumSeek.setProgress(mDatabaseHelper.settingsGetData("sodium"));
        sodiumCondSeek.setProgress(mDatabaseHelper.settingsGetData("sodiumCond"));
        cholesterolSeek.setProgress(mDatabaseHelper.settingsGetData("cholesterol"));
        fiberSeek.setProgress(mDatabaseHelper.settingsGetData("fiber"));
        flourSeek.setProgress(mDatabaseHelper.settingsGetData("flour"));
        sugarSeek.setProgress(mDatabaseHelper.settingsGetData("sugar"));

        DecimalFormat df = new DecimalFormat("#.###"); // Used to format data when printing
        df.setRoundingMode(RoundingMode.CEILING);


        // Set default TextView and SeekBar values
        if (mDatabaseHelper.settingsGetData("calorie") == 50) {
            text = "Calorie Density: " + df.format((defaultValue * 0.02 * 1.25) - .25) + " cal/serving - " + df.format((defaultValue * 0.02 * 1.25) + .25) + " cal/serving";
            calorieText.setText(text);
        }
        else {
            double calorieValue = calorieSeek.getProgress() * 0.02 * 1.25;
            text = "Calorie Density: " + df.format(calorieValue - .25) + " cal/serving - " + df.format(calorieValue + .25) + " cal/serving";
            calorieText.setText(text);
        }
        if (mDatabaseHelper.settingsGetData("fat") == 50) {
            text = "Total Fat Composition: " + df.format(((defaultValue * 0.02 * .175) - .025) * 100) + "% - " + df.format(((defaultValue * 0.02 * .175) + .025) * 100) + "%";
            fatText.setText(text);
        }
        else {
            double fatValue = fatSeek.getProgress() * 0.02 * .175;
            text = "Total Fat Composition: " + df.format((fatValue - .025) * 100) + "% - " + df.format((fatValue + .025) * 100) + "%";
            fatText.setText(text);
        }
        if (mDatabaseHelper.settingsGetData("satfat") == 50) {
            text = "Saturated Fat Composition: " + df.format(((defaultValue * 0.02 * .06) - .01) * 100) + "% - " + df.format(((defaultValue * 0.02 * .06) + .01) * 100) + "%";
            satText.setText(text);
        }
        else {
            double satValue = satSeek.getProgress() * 0.02 * .06;
            text = "Saturated Fat Composition: " + df.format((satValue - .01) * 100) + "% - " + df.format((satValue + .01) * 100) + "%";
            satText.setText(text);
        }
        if (mDatabaseHelper.settingsGetData("transfat") == 1) {
            text = "Trans Fat test will be calculated.";
            transText.setText(text);
        }
        else {
            text = "Trans Fat test will not be calculated.";
            transText.setText(text);
        }
        if (mDatabaseHelper.settingsGetData("sodium") == 50) {
            text = "Sodium to Calorie Ratio: " + df.format((defaultValue * 0.02 * 1) - 1) + " mg/cal - " + df.format((defaultValue * 0.02 * 1) + 1) + " mg/cal";
            sodiumText.setText(text);
        }
        else {
            double sodiumValue = sodiumSeek.getProgress() * 0.02 * 1;
            text = "Sodium to Calorie Ratio: " + df.format(sodiumValue - 1) + " mg/cal - " + df.format(sodiumValue + 1) + " mg/cal";
            sodiumText.setText(text);
        }
        if (mDatabaseHelper.settingsGetData("sodiumCond") == 50) {
            text = "Condiment Sodium Ratio: " + df.format((defaultValue * 0.02 * 2) - 2) + " mg/cal - " + df.format((defaultValue * 0.02 * 2) + 2) + " mg/cal";
            sodiumCondText.setText(text);
        }
        else {
            double sodiumCondValue = sodiumCondSeek.getProgress() * 0.02 * 2;
            text = "Condiment Sodium Ratio: " + df.format(sodiumCondValue - 2) + " mg/cal - " + df.format(sodiumCondValue + 2) + " mg/cal";
            sodiumCondText.setText(text);
        }
        if (mDatabaseHelper.settingsGetData("cholesterol") == 50) {
            text = "Cholesterol: " + df.format((defaultValue * 0.02 * 12.5) - 12.5) + "mg - " + df.format((defaultValue * 0.02 * 12.5) + 12.5) + "mg";
            cholesterolText.setText(text);
        }
        else {
            double cholValue = cholesterolSeek.getProgress() * 0.02 * 12.5;
            text = "Cholesterol: " + df.format(cholValue - 12.5) + "mg - " + df.format(cholValue + 12.5) + "mg";
            cholesterolText.setText(text);
        }
        if (mDatabaseHelper.settingsGetData("fiber") == 50) {
            text = "Fiber to Calorie Ratio: " + df.format((defaultValue * 0.02 * 2) - 1) + " g - " + df.format((defaultValue * 0.02 * 2) + 1) +" g per 100 cal";
            fiberText.setText(text);
        }
        else {
            double fiberValue = fiberSeek.getProgress() * 0.02 * 2;
            text = "Fiber to Calorie Ratio: " + df.format(fiberValue - 1) + " g - " + df.format(fiberValue + 1) + " g per 100 cal";
            fiberText.setText(text);
        }
        if (mDatabaseHelper.settingsGetData("flour") == 1) {
            text = "Added Flours and Grains test will be calculated.";
            flourText.setText(text);
        }
        else {
            text = "Added Flours and Grains test will not be calculated.";
            flourText.setText(text);
        }
        if (mDatabaseHelper.settingsGetData("sugar") == 1) {
            text = "Added Sugars test will be calculated.";
            sugarText.setText(text);
        }
        else {
            text = "Added Sugars test will not be calculated.";
            sugarText.setText(text);
        }

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Settings default to 50 or 1
                for (int i = 0; i < settingNames.length; i++) {
                    if(i == 3 || i == 7 || i == 8) { // transfat, flour and sugar all have a max slider value of 1
                        mDatabaseHelper.settingsAddData(settingNames[i], 1);
                    }
                    else {
                        mDatabaseHelper.settingsAddData(settingNames[i], 50);
                    }
                }
                calorieSeek.setProgress(50);
                fatSeek.setProgress(50);
                satSeek.setProgress(50);
                transSeek.setProgress(1);
                sodiumSeek.setProgress(50);
                sodiumCondSeek.setProgress(50);
                cholesterolSeek.setProgress(50);
                fiberSeek.setProgress(50);
                flourSeek.setProgress(1);
                sugarSeek.setProgress(1);
            }
        });

        calorieSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int calorieProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                calorieProgress = progress;
                double calorieValue = progress * 0.02 * 1.25;
                String text = "Calorie Density: " + df.format(calorieValue - .25) + " cal/serving - " + df.format(calorieValue + .25) + " cal/serving";
                calorieText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDatabaseHelper.settingsAddData("calorie", calorieProgress);
            }
        });

        fatSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int fatProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fatProgress = progress;
                double fatValue = progress * 0.02 * .175;
                String text = "Total Fat Composition: " + df.format((fatValue - .025) * 100) + "% - " + df.format((fatValue + .025) * 100) + "%";
                fatText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDatabaseHelper.settingsAddData("fat", fatProgress);
            }
        });

        satSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int satProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                satProgress = progress;
                double satValue = progress * 0.02 * .06;
                String text = "Saturated Fat Composition: " + df.format((satValue - .01) * 100) + "% - " + df.format((satValue + .01) * 100) + "%";
                satText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDatabaseHelper.settingsAddData("satfat", satProgress);
            }
        });

        transSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int transProgress;
            String text;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                transProgress = progress;
                if (progress == 0) {
                    text = "Trans Fat test will not be calculated.";
                }
                else {
                    text = "Trans Fat test will be calculated.";
                }
                transText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDatabaseHelper.settingsAddData("transfat", transProgress);
            }
        });

        sodiumSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int sodiumProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sodiumProgress = progress;
                double sodiumValue = progress * 0.02 * 1;
                String text = "Sodium to Calorie Ratio: " + df.format(sodiumValue - 1) + " mg/cal - " + df.format(sodiumValue + 1) + " mg/cal";
                sodiumText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDatabaseHelper.settingsAddData("sodium", sodiumProgress);
            }
        });

        sodiumCondSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int sodiumCondProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sodiumCondProgress = progress;
                double sodiumCondValue = progress * 0.02 * 2;
                String text = "Condiment Sodium Ratio: " + df.format(sodiumCondValue - 2) + " mg/cal - " + df.format(sodiumCondValue + 2) + " mg/cal";
                sodiumCondText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDatabaseHelper.settingsAddData("sodiumCond", sodiumCondProgress);
            }
        });

        cholesterolSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int cholProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cholProgress = progress;
                double cholValue = progress * 0.02 * 12.5;
                String text = "Cholesterol: " + df.format(cholValue - 12.5) + "mg - " + df.format(cholValue + 12.5) + "mg";
                cholesterolText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDatabaseHelper.settingsAddData("cholesterol", cholProgress);
            }
        });

        fiberSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int fiberProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fiberProgress = progress;
                double fiberValue = progress * 0.02 * 2;
                String text = "Fiber to Calorie Ratio: " + df.format(fiberValue - 1) + " g - " + df.format(fiberValue + 1) + " g per 100 cal";
                fiberText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDatabaseHelper.settingsAddData("fiber", fiberProgress);
            }
        });

        flourSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int flourProgress;
            String text;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flourProgress = progress;
                if (progress == 0) {
                    text = "Added Flours and Grains test will not be calculated.";
                }
                else {
                    text = "Added Flours and Grains test will be calculated.";
                }
                flourText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDatabaseHelper.settingsAddData("flour", flourProgress);
            }
        });

        sugarSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int sugarProgress;
            String text;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sugarProgress = progress;
                if (progress == 0) {
                    text = "Added Sugars test will not be calculated.";
                }
                else {
                    text = "Added Sugars test will be calculated.";
                }
                sugarText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDatabaseHelper.settingsAddData("sugar", sugarProgress);
            }
        });

    }

}
