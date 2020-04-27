package com.design.senior;

import android.content.Context;
import android.provider.ContactsContract;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

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
    public Pair<String, Integer> calorieDensity(double calories, double servingSize, int settingValue) throws IOException {
        if (settingValue == 0) {
            return Pair.with("", -1);
        }

        DecimalFormat df = new DecimalFormat("#.##"); // Used to format data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        double testValue = settingValue * 0.02 * 1.25;

        String testResult;
        Integer testRating = -1;
        double density = calories / servingSize;

        if (density <= (testValue - .25)) {
            testResult = "Calorie Density: " + df.format(density) + " cal/g";
            testRating = 2;
        }
        else if ((density > (testValue - .25)) & (density <= (testValue + .25))) {
            testResult = "Calorie Density: " + df.format(density) + " cal/g";
            testRating = 1;
        }
        else {
            testResult = "Calorie Density: " + df.format(density) + " cal/g";
            testRating = 0;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            // error: no rating given
        }
        return Pair.with(testResult, testRating);
    }

    public Pair<String, Integer> totalFat(double calories, double fat, int settingValue) throws IOException {
        if (settingValue == 0) {
            return Pair.with("", -1);
        }

        DecimalFormat df = new DecimalFormat("#.##"); // Formats data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        double testValue = settingValue * 0.02 * .175;

        String testResult = "";
        double fatComp = (fat * 9) / calories;         // There are 9 calories per gram of fat
        if (calories == 0) {
            fatComp = 0;
        }
        double fatCompPerc = fatComp * 100;
        Integer testRating = -1;


        if((fatComp < 0) | (fatComp > 1)){             // Database error, negative fat composition is impossible

        }
        else if((fatComp <= (testValue - .025))) {
            testResult = "Total Fat Composition: " + df.format(fatCompPerc) + "%";
            testRating = 2;
        }
        else if((fatComp > (testValue - .025)) & (fatComp <= (testValue + .025))){
            testResult = "Total Fat Composition: " + df.format(fatCompPerc) + "%";
            testRating = 1;
        }
        else {
            testResult = "Total Fat Composition: " + df.format(fatCompPerc) + "%";
            testRating = 0;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            // check to see if a rating has been given
        }

        Pair<String, Integer> result = Pair.with(testResult,testRating);
        return result;
    }

    public Pair<String, Integer> saturatedFat(double calories, double satFat, int settingValue) throws IOException {
        if (settingValue == 0) {
            return Pair.with("", -1);
        }

        DecimalFormat df = new DecimalFormat("#.##"); // Formats data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        double testValue = settingValue * 0.02 * .06;

        String testResult = "";
        double satFatComp = (satFat * 9) / calories;                //fat is 9 cals/g. This calculates Saturated Fat composition
        if (calories == 0) {
            satFatComp = 0;
        }
        double satFatCompPerc = satFatComp*100;
        Integer testRating = -1;

        if((satFatComp < 0) | (satFatComp > 1)){                    // Database error, negative fat composition is impossible

        }
        else if((satFatComp <= (testValue - .01))) {
            testResult = "Saturated Fat Composition: " + df.format(satFatCompPerc) + "%";
            testRating = 2;
        }
        else if((satFatComp > (testValue - .01)) & (satFatComp <= (testValue + .01))){
            testResult = "Saturated Fat Composition: " + df.format(satFatCompPerc) + "%";
            testRating = 1;
        }
        else {
            testResult = "Saturated Fat Composition: " + df.format(satFatCompPerc) + "%";
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
            testResult = "This food may have hydrogenated oils which contain trans fats.";
            testRating = 0;

        }
        else {
            testResult = "This food does not contain any trans fats.";
            testRating = 2;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            //error: no rating given
        }
        return Pair.with(testResult,testRating);
    }

    public Pair<String, Integer> cholesterolContent(double cholesterol, int settingValue) throws IOException {
        if (settingValue == 0) {
            return Pair.with("", -1);
        }

        DecimalFormat df = new DecimalFormat("#.##"); // Formats data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        double testValue = settingValue * 0.02 * 12.5;

        String testResult = "";
        Integer testRating = -1;

        if(cholesterol > (testValue + 12.5)){
            testResult = "Cholesterol: " + df.format(cholesterol) + " mg";
            testRating = 0;
        }
        else if(cholesterol <= (testValue + 12.5) & (cholesterol > (testValue - 12.5))){
            testResult = "Cholesterol: " + df.format(cholesterol) + " mg";
            testRating = 1;
        }
        else if(cholesterol <= (testValue - 12.5)){
            testResult = "Cholesterol: " + df.format(cholesterol) + " mg";
            testRating = 2;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            //error: no rating given
        }
        return Pair.with(testResult, testRating);
    }

    public Pair<String, Integer> sodiumContent(double calories, double sodium, String category, int settingValue, int condSettingValue) throws IOException {
        if (settingValue == 0) {
            return Pair.with("", -1);
        }

        DecimalFormat df = new DecimalFormat("#.##"); // Used to format data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        if (category == null) {
            category = "";
        }

        double testValue = settingValue * 0.02 * 1;
        double condTestValue = condSettingValue * 0.02 * 2;

        String testResult = "";
        Integer testRating = -1;
        double sodiumToCaloriesRatio = sodium/calories;

        if (calories == 0) {
            testResult = "Condiment Sodium to Calorie Ratio: N/A";
            testRating = 3;
            return Pair.with(testResult, testRating);
        }

        if (category.contains("Ketchup, Mustard, BBQ & Cheese Sauce") || category.contains("Salad Dressing & Mayonnaise") || category.contains("Gravy Mix")) {
            if (sodiumToCaloriesRatio <= (condTestValue - 2)) {
                testResult = "Condiment Sodium to Calorie Ratio: " + df.format(sodiumToCaloriesRatio) + " mg/cal";
                testRating = 2;
            }
            else if (sodiumToCaloriesRatio >= (condTestValue - 2) & sodiumToCaloriesRatio <= (condTestValue + 2)) {
                testResult = "Condiment Sodium to Calorie Ratio: " + df.format(sodiumToCaloriesRatio) + " mg/cal";
                testRating = 1;
            }
            else if (sodiumToCaloriesRatio > (condTestValue + 2)) {
                testResult = "Condiment Sodium to Calorie Ratio: " + df.format(sodiumToCaloriesRatio) + " mg/cal";
                testRating = 0;
            }
        }
        else {
            if (sodiumToCaloriesRatio <= (testValue - 1)) {
                testResult = "Sodium to Calorie Ratio: " + df.format(sodiumToCaloriesRatio) + " mg/cal";
                testRating = 2;
            }
            else if (sodiumToCaloriesRatio >= (testValue - 1) & sodiumToCaloriesRatio <= (testValue + 1)) {
                testResult = "Sodium to Calorie Ratio: " + df.format(sodiumToCaloriesRatio) + " mg/cal";
                testRating = 1;
            }
            else if (sodiumToCaloriesRatio > (testValue + 1)) {
                testResult = "Sodium to Calorie Ratio: " + df.format(sodiumToCaloriesRatio) + " mg/cal";
                testRating = 0;
            }
        }

        if((testRating == -1) | (testResult.isEmpty())){
            // error: no rating given
        }

        return Pair.with(testResult, testRating);
    }

    public Pair<String, Integer> fiberContent(double calories, double fiber, int settingValue) throws IOException {
        if (settingValue == 0) {
            return Pair.with("", -1);
        }

        DecimalFormat df = new DecimalFormat("#.##"); // Used to format data when printing
        df.setRoundingMode(RoundingMode.CEILING);

        double testValue = settingValue * 0.02 * 2;

        double fiberToCalorieRatio = (fiber/calories) * 100;
        String testResult = "";
        Integer testRating = -1;

        if (calories == 0) {
            testResult = "Fiber to Calorie Ratio: N/A";
            testRating = 3;
        }
        else if(fiberToCalorieRatio >= (testValue + 1)){
            testResult = "Fiber to Calorie Ratio: " + df.format(fiberToCalorieRatio) + " g per 100 cal";
            testRating = 2;
        }
        else if(fiberToCalorieRatio >= (testValue - 1) & (fiberToCalorieRatio <= (testValue + 1))) {
            testResult = "Fiber to Calorie Ratio: " + df.format(fiberToCalorieRatio) + " g per 100 cal";
            testRating = 1;
        }
        else if(fiberToCalorieRatio < (testValue - 1)){
            testResult = "Fiber to Calorie Ratio: " + df.format(fiberToCalorieRatio) + " g per 100 cal";
            testRating = 0;
        }
        if((testRating == -1) | (testResult.isEmpty())){
            // error: no rating given
        }
        return Pair.with(testResult,testRating);
    }

    public Pair<String, Integer> flours(String ingredients){
        String[] badflours = {"white","wheat","durum", "semolina","bleached", "unbleached"};
        String[] goodflours = {"whole","rolled","cracked","sprouted","stone ground"};
        String testResult = "";
        Integer testRating = -1;
        Boolean bad = false;
        Boolean goodf = false;

        if(ingredients == null) { // Some foods don't have ingredients lists. This gives them a default empty string
            ingredients = "";
        }

        for(int i = 0; i<badflours.length; i++){
            if( Pattern.compile(Pattern.quote(badflours[i]), Pattern.CASE_INSENSITIVE).matcher(ingredients).find()){
                bad = true;
            }
        }
        for(int j = 0; j<goodflours.length; j++){
            if(Pattern.compile(Pattern.quote(goodflours[j]), Pattern.CASE_INSENSITIVE).matcher(ingredients).find()){
                goodf = true;
            }
        }

        if(goodf & bad){
            testRating = 1;
            testResult = "This food contains good and bad flours.";
        }
        else if(goodf & !bad) {
            testRating = 2;
            testResult = "This food contains good flours or grains.";
        }
        else if(bad){
            testRating = 0;
            testResult = "This food contains bad flours or grains.";
        }
        else{
            testRating = 2;
            testResult = "This food does not contain flours or grains.";

        }

        return Pair.with(testResult,testRating);

    }

    public Pair<String, Integer> sugars(String ingredients){
        String[] badsug = {"sugar","brown sugar","raw sugar","honey","agave syrup"};
        String testResult = "";
        Integer testRating = -1;
        Boolean bad = false;

        if(ingredients == null) { // Some foods don't have ingredients lists. This gives them a default empty string
            ingredients = "";
        }

        for(int i = 0; i < badsug.length; i++){
            if(Pattern.compile(Pattern.quote(badsug[i]), Pattern.CASE_INSENSITIVE).matcher(ingredients).find()){
                bad = true;
            }
        }
        String[] temp_ingr = ingredients.split("\\s+");
        for(int j = 0; j<temp_ingr.length; j++){
            if(temp_ingr[j].endsWith("ose") | temp_ingr[j].endsWith("ol"))
                bad = true;
        }

        if(bad){
            testRating = 0;
            testResult = "This food contains added sugars.";
        }
        else{
            testRating = 2;
            testResult = "This food does not contain added sugars.";
        }
        return Pair.with(testResult, testRating);
    }

}

