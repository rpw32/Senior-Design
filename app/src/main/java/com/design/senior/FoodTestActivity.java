package com.design.senior;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class FoodTestActivity {

    public String calorieDensity(double calories, double servingSize) throws IOException {
        DecimalFormat df = new DecimalFormat("#.##"); // Used to format data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        String testResult;
        double density = calories / servingSize;

        if (density <= 1) {
            testResult = "Calorie density is: " + df.format(density) + ", which is less than or equal to 1. This is a good choice.";
        }
        else if ((density > 1) & (density <= 1.5)) {
            testResult = "Calorie density is: " + df.format(density) + ", which is between 1 and 1.5. This is an acceptable choice.";
        }
        else {
            testResult = "Calorie density is: " + df.format(density) + ". This is above 1.5, which may not be a good choice.";
        }
        return testResult;
    }

}
