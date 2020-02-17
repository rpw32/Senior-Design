package com.design.senior;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class activity_detail_result extends AppCompatActivity {
    private TextView mTextViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_result);

        int fdcId;

        // If called from CameraMainActivity, will store the UPC
        Intent intent = getIntent();
        String gtinUpc = CameraMainActivity.Companion.getMessage(intent);

        // If a UPC is given, the fdcId will be found
        if (gtinUpc != null) {

            // Preparation for API call
            APIRequestActivity inst1 = new APIRequestActivity();
            JSONObject response;

            try {
                response = inst1.searchRequest(gtinUpc, "", "true", "", "", "");
                JSONArray foodsArray = response.getJSONArray("foods");
                JSONObject food = foodsArray.getJSONObject(0);
                fdcId = food.getInt("fdcId");
                detailParse(fdcId);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Otherwise, the fdcId must be given. If it isn't bundled, the function returns
        else {
            Bundle b = getIntent().getExtras();
            fdcId = 0;
            if (b != null) {
                fdcId = Integer.parseInt(b.getString("fdcId"));
                detailParse(fdcId);
            }
            else
                return;
        }
    }

    private void detailParse(int fdcId) { // Given the fdcId, will parse all the details for the given food

        try {
            APIRequestActivity inst1 = new APIRequestActivity();
            FoodInformation food1 = new FoodInformation();
            JSONObject response = inst1.detailRequest(fdcId);

            JSONObject foodPortion;
            JSONObject nutrient;
            JSONObject loopFoodNutrient;
            JSONArray foodNutrients;

            DecimalFormat df = new DecimalFormat("#.##"); // Used to format data when printing
            df.setRoundingMode(RoundingMode.CEILING);

            int nutrientId; // id of the nutrient in the foodNutrients array
            double nutrientAmount; // used to find "amount" value when looping through foodNutrients array

            food1.foodClass = response.getString("foodClass");
            food1.description = response.getString("description");
            foodNutrients = response.getJSONArray("foodNutrients");
            if (food1.foodClass.equals("Branded")) {
                food1.brandOwner = response.getString("brandOwner");
                food1.gtinUpc = response.getString("gtinUpc");
                food1.ingredients = response.getString("ingredients");
                food1.servingSize = response.getDouble("servingSize");
                food1.servingSizeUnit = response.getString("servingSizeUnit");
                food1.householdServingFullText = response.getString("householdServingFullText");
                food1.brandedFoodCategory = response.getString("brandedFoodCategory");
            }
            else { // Foods from the Survey database come with food portions
                JSONArray foodPortions = response.getJSONArray("foodPortions");
                // Portion weights and descriptions are stored in lists
                List<Double> portionWeights = new ArrayList<>();
                List<String> portionDescriptions = new ArrayList<>();

                // Loop through all portions
                for (int i = 0; i < foodPortions.length(); i++) {
                    foodPortion = foodPortions.getJSONObject(i);
                    portionWeights.add(foodPortion.getDouble("gramWeight"));
                    portionDescriptions.add(foodPortion.getString("portionDescription"));
                }
                foodPortion = foodPortions.getJSONObject(0); // Default to first food portion
                food1.servingSize = foodPortion.getDouble("gramWeight");
                food1.servingSizeUnit = "g";
                food1.householdServingFullText = foodPortion.getString("portionDescription");
            }

            food1.fdcId = response.getInt("fdcId");

            for (int i = 0; i < foodNutrients.length(); i++) { // Loop through the food nutrient array
                loopFoodNutrient = foodNutrients.getJSONObject(i); // get nutrient i
                nutrient = loopFoodNutrient.getJSONObject("nutrient"); // "nutrient" object is always at index 2
                nutrientId = (int) nutrient.get("id"); // get the nutrient id
                nutrientAmount = loopFoodNutrient.getDouble("amount"); // get the amount
                nutrientAmount = (nutrientAmount / 100) * food1.servingSize; // amount is based on 100g, this converts the amount to the appropriate serving size
                switch (nutrientId) { // add the nutrient to the food based on the id
                    case 1087:
                        food1.calcium = nutrientAmount;
                        break;
                    case 1089:
                        food1.iron = nutrientAmount;
                        break;
                    case 1104:
                        food1.vitaminA = nutrientAmount;
                        break;
                    case 1162:
                        food1.vitaminC = nutrientAmount;
                        break;
                    case 1110:
                        food1.vitaminD = nutrientAmount;
                        break;
                    case 1092:
                        food1.potassium = nutrientAmount;
                        break;
                    case 1003:
                        food1.protein = nutrientAmount;
                        break;
                    case 1004:
                        food1.fat = nutrientAmount;
                        break;
                    case 1005:
                        food1.carbohydrates = nutrientAmount;
                        break;
                    case 1008:
                        food1.calories = nutrientAmount;
                        break;
                    case 2000:
                        food1.sugars = nutrientAmount;
                        break;
                    case 1079:
                        food1.fiber = nutrientAmount;
                        break;
                    case 1093:
                        food1.sodium = nutrientAmount;
                        break;
                    case 1253:
                        food1.cholesterol = nutrientAmount;
                        break;
                    case 1257:
                        food1.transFat = nutrientAmount;
                        break;
                    case 1258:
                        food1.saturatedFat = nutrientAmount;
                        break;
                }
            }

            // Begin printing to detail page
            mTextViewResult = findViewById(R.id.foodTitle);
            mTextViewResult.append(food1.description);
            mTextViewResult = findViewById(R.id.servingSize);
            mTextViewResult.append("Serving size: " + df.format(food1.servingSize) + " " + food1.servingSizeUnit + "\n");
            mTextViewResult.append("Portion description: " + food1.householdServingFullText);

            // Printing Macros / Micros
            mTextViewResult = findViewById(R.id.calories);
            mTextViewResult.append("Calories: " + df.format(food1.calories));
            mTextViewResult = findViewById(R.id.fat);
            mTextViewResult.append("Fat: " + df.format(food1.fat) + " g");
            mTextViewResult = findViewById(R.id.saturatedFat);
            mTextViewResult.append("Saturated Fat: " + df.format(food1.saturatedFat) + " g");
            mTextViewResult = findViewById(R.id.transFat);
            mTextViewResult.append("Trans Fat: " + df.format(food1.transFat) + " g");
            mTextViewResult = findViewById(R.id.cholesterol);
            mTextViewResult.append("Cholesterol: " + df.format(food1.cholesterol) + " mg");
            mTextViewResult = findViewById(R.id.sodium);
            mTextViewResult.append("Sodium: " + df.format(food1.sodium) + " mg");
            mTextViewResult = findViewById(R.id.carbohydrates);
            mTextViewResult.append("Carbohydrates: " + df.format(food1.carbohydrates) + " g");
            mTextViewResult = findViewById(R.id.fiber);
            mTextViewResult.append("Fiber: " + df.format(food1.fiber) + " g");
            mTextViewResult = findViewById(R.id.sugars);
            mTextViewResult.append("Sugars: " + df.format(food1.sugars) + " g");
            mTextViewResult = findViewById(R.id.protein);
            mTextViewResult.append("Protein: " + df.format(food1.protein) + " g");
            mTextViewResult = findViewById(R.id.vitaminD);
            mTextViewResult.append("Vitamin D: " + df.format(food1.vitaminD) + " IU");
            mTextViewResult = findViewById(R.id.calcium);
            mTextViewResult.append("Calcium: " + df.format(food1.calcium) + " mg");
            mTextViewResult = findViewById(R.id.iron);
            mTextViewResult.append("Iron: " + df.format(food1.iron) + " mg");
            mTextViewResult = findViewById(R.id.potassium);
            mTextViewResult.append("Potassium: " + df.format(food1.potassium) + " mg");
            mTextViewResult = findViewById(R.id.vitaminA);
            mTextViewResult.append("Vitamin A: " + df.format(food1.vitaminA) + " IU");
            mTextViewResult = findViewById(R.id.vitaminC);
            mTextViewResult.append("Vitamin C: " + df.format(food1.vitaminC) + " IU");

            // Only foods from the Branded database have this information
            if (food1.foodClass.equals("Branded")) {
                mTextViewResult = findViewById(R.id.ingredients);
                mTextViewResult.append("Ingredients: " + food1.ingredients);
                mTextViewResult = findViewById(R.id.brandOwner);
                mTextViewResult.append("Brand Owner: " + food1.brandOwner);
                mTextViewResult = findViewById(R.id.brandedFoodCategory);
                mTextViewResult.append("Food Category: " + food1.brandedFoodCategory);
                mTextViewResult = findViewById(R.id.gtinUpc);
                mTextViewResult.append("Universal Product Code: " + food1.gtinUpc);
            }

            // Not necessary to print, but can be handy
            mTextViewResult = findViewById(R.id.fdcId);
            mTextViewResult.append("Food Data Central ID: " + food1.fdcId);

            // Calorie Density test
            FoodTestActivity test1 = new FoodTestActivity();
            String calorieDensity = test1.calorieDensity(food1.calories, food1.servingSize);
            mTextViewResult = findViewById(R.id.calorieDensity);
            mTextViewResult.append("\n\nCalorie Density Test: \n" + calorieDensity);

            // Dialog box to get serving size from the user
            mTextViewResult = findViewById(R.id.servingSize);
            mTextViewResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    servingDialog();
                }
            });

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    public void servingDialog() {
        ServingDialog servingDialog = new ServingDialog();
        servingDialog.show(getSupportFragmentManager(), "serving dialog");
    }

}


