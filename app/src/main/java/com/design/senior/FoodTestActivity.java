package com.design.senior;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import org.javatuples.Pair;

public class FoodTestActivity {

    /*
      TODO: Error handling
      TODO: Check ingredients list for cholesterol
    */

    /*
    These return Pairs of values, the first being the value of the test and the second being the rating
    2 = Good, 1 = Acceptable, 0 = Bad
     */
    public Pair<String, Integer> calorieDensity(double calories, double servingSize) throws IOException {
        DecimalFormat df = new DecimalFormat("#.##"); // Used to format data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        String testResult;
        Integer testRating = -1;
        double density = calories / servingSize;

        if (density <= 1) {
            testResult = "Calorie Density is: " + df.format(density) + " cal/serving";
            testRating = 2;
        }
        else if ((density > 1) & (density <= 1.5)) {
            testResult = "Calorie Density is: " + df.format(density) + " cal/serving";
            testRating = 1;
        }
        else {
            testResult = "Calorie Density is: " + df.format(density) + " cal/serving";
            testRating = 0;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            // error: no rating given
        }
        return Pair.with(testResult, testRating);
    }

    public Pair<String, Integer> totalFat(double calories, double fat) throws IOException {
        DecimalFormat df = new DecimalFormat("#.##"); // Formats data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        String testResult = "";
        double fatComp = (fat * 9) / calories;         // There are 9 calories per gram of fat
        double fatCompPerc = fatComp * 100;
        Integer testRating = -1;


        if((fatComp < 0) | (fatComp > 1)){             // Database error, negative fat composition is impossible

        }
        else if((fatComp <= .15) & (fatComp >= 0.0)) {
            testResult = "Total Fat Composition is: " + df.format(fatCompPerc) + "%. This is a good choice.";
            testRating = 2;
        }
        else if((fatComp > .15) & (fatComp <= .2)){
            testResult = "Total Fat Composition is: " + df.format(fatCompPerc) + "%. This is between 15% and 20%. This is an acceptable choice.";
            testRating = 1;
        }
        else {
            testResult = "Total Fat Composition is: " + df.format(fatCompPerc) + "%. This is above 20%, which may not be a good choice.";
            testRating = 0;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            // check to see if a rating has been given
        }

        Pair<String, Integer> result = Pair.with(testResult,testRating);
        return result;
    }

    public Pair<String, Integer> saturatedFat(double calories, double satFat) throws IOException {
        DecimalFormat df = new DecimalFormat("#.##"); // Formats data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        String testResult = "";
        double satFatComp = (satFat * 9) / calories;                //fat is 9 cals/g. This calculates Saturated Fat composition
        double satFatCompPerc = satFatComp*100;
        Integer testRating = -1;

        if((satFatComp < 0) | (satFatComp > 1)){                    // Database error, negative fat composition is impossible

        }
        else if((satFatComp <= .05) & (satFatComp >= 0.0)) {
            testResult = "Saturated Fat Composition is: " + df.format(satFatCompPerc) + "%. This is a good choice.";
            testRating = 2;
        }
        else if((satFatComp > .05) & (satFatComp <= .07)){
            testResult = "Saturated Fat Composition is: " + df.format(satFatCompPerc) + "%. This is between 5% and 7%. This is an acceptable choice";
            testRating = 1;
        }
        else {
            testResult = "Saturated Fat Composition is: " + df.format(satFatCompPerc) + "%. This is above 7%, which may not be a good choice.";
            testRating = 0;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            // check to see if a rating has been given
        }

        return Pair.with(testResult,testRating);
    }

    public Pair<String, Integer> transFat(double transFats, String ingredients) throws IOException {

        DecimalFormat df = new DecimalFormat("#.##"); // Formats data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        if(ingredients == null) { // Some foods don't have ingredients lists. This gives them a default empty string
            ingredients = "";
        }

        String testResult = "";
        Integer testRating = -1;

        if((ingredients.contains("hydrogenated")) | (transFats > 0.0)){
            testResult = "This food may contains hydrogenated oils which contain trans fats. This may not be a good choice";
            testRating = 0;

        }
        else {
            testResult = "This food does not contain any trans fats making it a good choice.";
            testRating = 2;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            //error: no rating given
        }
        return Pair.with(testResult,testRating);
    }

    public Pair<String, Integer> cholesterolContent(double cholesterol, String ingredients) throws IOException {

        DecimalFormat df = new DecimalFormat("#.##"); // Formats data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        String testResult = "";
        Integer testRating = -1;

        if(cholesterol > 25){
            testResult = "The cholesterol is above 25mg which may not be a good choice.";
            testRating = 0;
        }
        else if((cholesterol <= 25.0) & (cholesterol > 0.0)){
            testResult = "The cholesterol is below 25mg which is an acceptable choice.";
            testRating = 1;
        }
        else if(cholesterol == 0){
            testResult = "The cholesterol is 0 making this a good choice";
            testRating = 2;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            //error: no rating given
        }
        return Pair.with(testResult, testRating);
    }

    public Pair<String, Integer> sodiumContent(double calories, double sodium, String category) throws IOException {
        DecimalFormat df = new DecimalFormat("#.##"); // Used to format data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        if (category == null) {
            category = "";
        }

        String testResult = "";
        Integer testRating = -1;
        double sodiumToCaloriesRatio = sodium/calories;

        if(sodiumToCaloriesRatio <= 1.0){
            testResult = "Sodium to Calorie Ratio: " + df.format(sodiumToCaloriesRatio) + " mg/cal";
            testRating = 2;
        }
        else if((sodiumToCaloriesRatio >= 1.0) & (sodiumToCaloriesRatio <= 4.0) & (category.contains("condiment"))){
            testResult = "Sodium to Calorie Ratio: " + df.format(sodiumToCaloriesRatio) + " mg/cal";
            testRating = 1;
        }
        else if((sodiumToCaloriesRatio >= 1.0) & (sodiumToCaloriesRatio <= 2.0) & (!category.contains("condiment"))){
            testResult = "Sodium to Calorie Ratio: " + df.format(sodiumToCaloriesRatio) + " mg/cal";
            testRating = 1;
        }
        else if(((sodiumToCaloriesRatio > 2.0) & (!category.contains("condiment"))) | ((sodiumToCaloriesRatio > 4.0) & (category.contains("condiment")))){
            testResult = "Sodium to Calorie Ratio: " + df.format(sodiumToCaloriesRatio) + " mg/cal";
            testRating = 0;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            // error: no rating given
        }

        return Pair.with(testResult, testRating);
    }

    public Pair<String, Integer> fiberContent(double calories, double fiber) throws IOException {

        DecimalFormat df = new DecimalFormat("#.##"); // Used to format data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        double fiberToCalorieRatio = fiber/calories;
        String testResult = "";
        Integer testRating = -1;

        if(fiberToCalorieRatio >= .03){
            testResult = "Fiber to Calorie Ratio: " + df.format(fiberToCalorieRatio*100) + "g per 100 cals.";
            testRating = 2;
        }
        else if((fiberToCalorieRatio >= .02) & (fiberToCalorieRatio <= .03)) {
            testResult = "Fiber to Calorie Ratio: " + df.format(fiberToCalorieRatio*100) + "g per 100 cals.";
            testRating = 1;
        }
        else if(fiberToCalorieRatio < .02){
            testResult = "Fiber to Calorie Ratio: " + df.format(fiberToCalorieRatio*100) + "g per 100 cals.";
            testRating = 0;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            // error: no rating given
        }
        return Pair.with(testResult,testRating);
    }



}

