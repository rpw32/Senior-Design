package com.design.senior;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class activity_detail_result extends AppCompatActivity implements ServingDialog.ServingDialogListener {
    private TextView textView;
    private ScrollView scrollView;
    private TableLayout detailTable;
    private Double servingSelection;
    private int fdcId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_result);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        detailTable = (TableLayout) findViewById(R.id.detailTable);

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
                String totalHits = response.getString("totalHits");
                if (Integer.parseInt(totalHits) < 1) {
                    Context context = getApplicationContext();
                    CharSequence text = "UPC not found. Try searching instead?";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    Intent intent1 = new Intent(this, activity_search_result.class);
                    startActivity(intent1);
                }
                else {
                    JSONArray foodsArray = response.getJSONArray("foods");
                    JSONObject food = foodsArray.getJSONObject(0);
                    fdcId = food.getInt("fdcId");
                    detailParse(fdcId);
                }
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
            scrollView.scrollTo(0, 0); // Reset scrollView position to the top
            detailTable.removeAllViews(); // Reset table, make sure its empty before populating it again

            APIRequestActivity inst1 = new APIRequestActivity();
            FoodInformation food1 = new FoodInformation();
            JSONObject response = inst1.detailRequest(fdcId);

            JSONObject foodPortion;
            JSONObject nutrient;
            JSONObject loopFoodNutrient;
            JSONArray foodNutrients;

            // Portion weights and descriptions are stored in lists
            ArrayList<String> portionWeights = new ArrayList<>();
            ArrayList<String> portionDescriptions = new ArrayList<>();

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
                if (servingSelection == null)
                    food1.servingSize = response.getDouble("servingSize");
                else
                    food1.servingSize = servingSelection;
                food1.servingSizeUnit = response.getString("servingSizeUnit");
                food1.householdServingFullText = response.getString("householdServingFullText");
                food1.brandedFoodCategory = response.getString("brandedFoodCategory");
            } else { // Foods from the Survey database come with food portions
                JSONArray foodPortions = response.getJSONArray("foodPortions");

                // Loop through all portions
                for (int i = 0; i < foodPortions.length(); i++) {
                    foodPortion = foodPortions.getJSONObject(i);
                    Double portionWeight = foodPortion.getDouble("gramWeight");
                    portionWeights.add(portionWeight.toString());
                    portionDescriptions.add(foodPortion.getString("portionDescription"));
                }
                foodPortion = foodPortions.getJSONObject(0); // Default to first food portion
                if (servingSelection == null)
                    food1.servingSize = foodPortion.getDouble("gramWeight");
                else
                    food1.servingSize = servingSelection;
                food1.servingSizeUnit = "g";
                food1.householdServingFullText = foodPortion.getString("portionDescription");
            }

            food1.fdcId = response.getInt("fdcId");
            fdcId = food1.fdcId;

            for (int i = 0; i < foodNutrients.length(); i++) { // Loop through the food nutrient array
                loopFoodNutrient = foodNutrients.getJSONObject(i); // get nutrient i
                nutrient = loopFoodNutrient.getJSONObject("nutrient"); // grab nutrient as JSONObject
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

            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT); // Set parameters for the searchTable rows, so they wrap to content
            TableRow.LayoutParams textParams = new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1f); // Set parameters for the textView

            Integer rowNumber = 0;

            // Begin printing to detail page
            TableRow row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(24);
            textView.append(food1.description);
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            // Printing Macros / Micros
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            Integer servingViewId = View.generateViewId();
            textView.setId(servingViewId);
            textView.append("Serving size: " + df.format(food1.servingSize) + " " + food1.servingSizeUnit + "\n");
            textView.append("Portion description: " + food1.householdServingFullText);
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Calories: " + df.format(food1.calories));
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Fat: " + df.format(food1.fat) + " g");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Saturated Fat: " + df.format(food1.saturatedFat) + " g");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Trans Fat: " + df.format(food1.transFat) + " g");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Cholesterol: " + df.format(food1.cholesterol) + " mg");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Sodium: " + df.format(food1.sodium) + " mg");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Carbohydrates: " + df.format(food1.carbohydrates) + " g");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Fiber: " + df.format(food1.fiber) + " g");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Sugars: " + df.format(food1.sugars) + " g");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Protein: " + df.format(food1.protein) + " g");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Vitamin D: " + df.format(food1.vitaminD) + " IU");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Calcium: " + df.format(food1.calcium) + " mg");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Iron: " + df.format(food1.iron) + " mg");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Potassium: " + df.format(food1.potassium) + " mg");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Vitamin A: " + df.format(food1.vitaminA) + " IU");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Vitamin C: " + df.format(food1.vitaminC) + " IU");
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;


            // Only foods from the Branded database have this information
            if (food1.foodClass.equals("Branded")) {
                row = new TableRow(this);
                row.setLayoutParams(lp);
                textView = new TextView(this);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setLayoutParams(textParams);
                textView.append("Ingredients: " + food1.ingredients);
                row.addView(textView);
                detailTable.addView(row, (rowNumber));
                rowNumber++;

                row = new TableRow(this);
                row.setLayoutParams(lp);
                textView = new TextView(this);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setLayoutParams(textParams);
                textView.append("Brand Owner: " + food1.brandOwner);
                row.addView(textView);
                detailTable.addView(row, (rowNumber));
                rowNumber++;

                row = new TableRow(this);
                row.setLayoutParams(lp);
                textView = new TextView(this);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setLayoutParams(textParams);
                textView.append("Food Category: " + food1.brandedFoodCategory);
                row.addView(textView);
                detailTable.addView(row, (rowNumber));
                rowNumber++;

                row = new TableRow(this);
                row.setLayoutParams(lp);
                textView = new TextView(this);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setLayoutParams(textParams);
                textView.append("Universal Product Code: " + food1.gtinUpc);
                row.addView(textView);
                detailTable.addView(row, (rowNumber));
                rowNumber++;

            }

            // Not necessary to print, but can be handy
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Food Data Central ID: " + food1.fdcId);
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;


            // Calorie Density test
            FoodTestActivity test1 = new FoodTestActivity();
            String calorieDensity = test1.calorieDensity(food1.calories, food1.servingSize);

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("\n\nCalorie Density Test: \n" + calorieDensity);
            row.addView(textView);
            detailTable.addView(row, (rowNumber));
            rowNumber++;


            // Dialog box to get serving size from the user
            textView = findViewById(servingViewId);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // If the food is branded, the portions are defaulted to the household serving size
                    if (food1.foodClass.equals("Branded")) {
                        portionWeights.clear();
                        portionDescriptions.clear();
                        Double portionWeight = food1.servingSize;
                        portionWeights.add(portionWeight.toString());
                        portionDescriptions.add(food1.householdServingFullText);
                    }
                    servingDialog(portionWeights, portionDescriptions);
                }
            });
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    public void servingDialog(ArrayList<String> portionWeights, ArrayList<String> portionDescriptions) {
        ServingDialog servingDialog = new ServingDialog();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("portionWeights", portionWeights);
        bundle.putStringArrayList("portionDescriptions", portionDescriptions);
        servingDialog.setArguments(bundle);
        servingDialog.show(getSupportFragmentManager(), "serving dialog");
    }

    @Override
    public void servingSelection(Double servingSize) {
        servingSelection = servingSize;
        detailParse(fdcId);
    }

}


