package com.design.senior;

import java.io.IOException;

public class FoodTestActivity {

    public String calorieDensity(double calories, double servingSize) throws IOException {
        String testResult;
        double density = calories / servingSize;
        if (density <= 1) {
            testResult = "Calorie density is: " + density + ", which is less than or equal to 1. This is a good choice.";
        }
        else if ((density > 1) & (density <= 1.5)) {
            testResult = "Calorie density is: " + density + ", which is between 1 and 1.5. This is an acceptable choice.";
        }
        else {
            testResult = "Calorie density is: " + density + ". This is above 1.5, which may not be a good choice.";
        }
        return testResult;
    }

}
