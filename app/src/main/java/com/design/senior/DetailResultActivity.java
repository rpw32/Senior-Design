package com.design.senior;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Spannable;
import android.view.Gravity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.javatuples.Pair;
import org.w3c.dom.Text;


public class DetailResultActivity extends AppCompatActivity implements ServingDialog.ServingDialogListener {
    private TextView textView, textViewL, textViewR, title;
    private ScrollView scrollView;
    private TableLayout detailTable, testTable;
    private Double servingSelection;
    private int fdcId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_result);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        detailTable = (TableLayout) findViewById(R.id.detailTable);
        testTable = (TableLayout) findViewById(R.id.testTable);
        title = (TextView) findViewById(R.id.title);

        // servingSelection is restored if the screen was rotated
        if (savedInstanceState != null) {
            servingSelection = savedInstanceState.getDouble("servingSelection");
            // If servingSelection is 0, then it was not bundled in savedInstanceState. Default to null
            if (servingSelection == 0) {
                servingSelection = null;
            }
        }

        // If called from CameraMainActivity, will store the UPC
        Intent intent = getIntent();
        String gtinUpc = CameraMainActivity.Companion.getMessage(intent);


        // If a UPC is given, the fdcId will be found
        if (gtinUpc != null) {

            // Check for internet connectivity
            InternetCheck internetCheck = new InternetCheck();
            if (!internetCheck.isOnline()) {
                Context context = getApplicationContext();
                CharSequence text = "Internet connection not detected.";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                finish();
            }
            // Preparation for API call
            APIRequestActivity inst1 = new APIRequestActivity();
            JSONObject response;

            try {
                response = inst1.searchRequest(gtinUpc, "", "true", "", "", "");
                String totalHits = response.getString("totalHits");

                // First attempt is made with scanned UPC. If failed, will try again with altered UPC
                if (Integer.parseInt(totalHits) < 1) {
                    // API will sometimes only recognize the UPC if it's 14 characters
                    if (gtinUpc.length() == 12) {
                        gtinUpc = "00" + gtinUpc;
                        response = inst1.searchRequest(gtinUpc, "", "true", "", "", "");
                        totalHits = response.getString("totalHits");
                    }
                }

                // If UPC does not return any results after both attempts, a manual search will be attempted
                if (Integer.parseInt(totalHits) < 1) {
                    DatabaseHelper mDatabaseHelper = new DatabaseHelper(this);
                    String upcTitle = null;
                    Context context = getApplicationContext();
                    CharSequence text = "UPC not found in database. Try searching instead?";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                    if (mDatabaseHelper.upcCheckAlreadyExist(gtinUpc)) {
                        upcTitle = mDatabaseHelper.upcGetData(gtinUpc);
                    }
                    else {
                        // Check for connectivity before making call
                        if (internetCheck.isOnline()) {
                            // Call upcitemdb
                            response = inst1.upcLookup(gtinUpc);
                        }

                        // Check if the response contains the UPC information
                        if (response.has("items")) {
                            JSONArray itemsArray = response.getJSONArray("items");
                            JSONObject itemsObject = itemsArray.getJSONObject(0);
                            upcTitle = itemsObject.getString("title");
                        }

                        // Add to local database
                        mDatabaseHelper.upcAddData(gtinUpc, upcTitle);
                    }

                    Intent intent1 = new Intent(this, SearchResultActivity.class);

                    // Bundle the upcTitle if the API call was successful
                    if (upcTitle != null) {
                        intent1.putExtra("upcTitle", upcTitle);
                    }

                    startActivity(intent1);
                    finish();
                }
                // If UPC does return a result, fdcId is found and detailParse() is called
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
        // If the UPC isn't given, the fdcId must be given. If it isn't bundled, the function returns
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
            testTable.removeAllViews();

            FoodInformation food1 = new FoodInformation();
            DatabaseHelper mDatabaseHelper = new DatabaseHelper(this);
            JSONObject response;

            // Check local database for response. If it exists, fetch the response locally
            if (mDatabaseHelper.detailCheckAlreadyExist(fdcId)) {
                String databaseResponse = mDatabaseHelper.detailGetData(fdcId);
                response = new JSONObject(databaseResponse); // Responses are converted to JSONObjects so they can be parsed
            }
            // Else the response is fetched and saved locally
            else {
                // Check for internet connectivity
                InternetCheck internetCheck = new InternetCheck();
                if (!internetCheck.isOnline()) {
                    Context context = getApplicationContext();
                    CharSequence text = "Internet connection not detected.";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    finish();
                }

                APIRequestActivity inst1 = new APIRequestActivity();
                response = inst1.detailRequest(fdcId);
                String databaseString = response.toString();
                mDatabaseHelper.detailAddData(fdcId, databaseString);
            }

            JSONObject foodPortion;
            JSONObject nutrient;
            JSONObject loopFoodNutrient;
            JSONArray foodNutrients;
            Double defaultServing = 0.00;

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
                defaultServing = response.getDouble("servingSize");
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

            // Begin printing to detail page
            title.setText(food1.description);
            
            // Printing Macros / Micros
            TableRow row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            Integer servingViewId = View.generateViewId();
            textView.setId(servingViewId);
            textView.append("Serving size: " + df.format(food1.servingSize) + " " + food1.servingSizeUnit + "\n");
            textView.append("Portion description: " + food1.householdServingFullText);
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Calories: " + df.format(food1.calories) + " kcal");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Fat: " + df.format(food1.fat) + " g");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Saturated Fat: " + df.format(food1.saturatedFat) + " g");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Trans Fat: " + df.format(food1.transFat) + " g");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Cholesterol: " + df.format(food1.cholesterol) + " mg");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Sodium: " + df.format(food1.sodium) + " mg");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Carbohydrates: " + df.format(food1.carbohydrates) + " g");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Fiber: " + df.format(food1.fiber) + " g");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Sugars: " + df.format(food1.sugars) + " g");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Protein: " + df.format(food1.protein) + " g");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Vitamin D: " + df.format(food1.vitaminD) + " IU");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Calcium: " + df.format(food1.calcium) + " mg");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Iron: " + df.format(food1.iron) + " mg");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Potassium: " + df.format(food1.potassium) + " mg");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Vitamin A: " + df.format(food1.vitaminA) + " IU");
            row.addView(textView);
            detailTable.addView(row);
            

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append("Vitamin C: " + df.format(food1.vitaminC) + " IU");
            row.addView(textView);
            detailTable.addView(row);
            


            // Only foods from the Branded database have this information
            if (food1.foodClass.equals("Branded")) {
                row = new TableRow(this);
                row.setLayoutParams(lp);
                Spannable.Factory spannableFactory;
                spannableFactory = Spannable.Factory.getInstance();
                textView = new ExpandableTextView(this);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setLayoutParams(textParams);
                textView.setText("Ingredients: " + food1.ingredients);
                row.addView(textView);
                detailTable.addView(row);
                

                row = new TableRow(this);
                row.setLayoutParams(lp);
                textView = new TextView(this);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setLayoutParams(textParams);
                textView.append("Brand Owner: " + food1.brandOwner);
                row.addView(textView);
                detailTable.addView(row);
                

                row = new TableRow(this);
                row.setLayoutParams(lp);
                textView = new TextView(this);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setLayoutParams(textParams);
                textView.append("Food Category: " + food1.brandedFoodCategory);
                row.addView(textView);
                detailTable.addView(row);
                

//                row = new TableRow(this);
//                row.setLayoutParams(lp);
//                textView = new TextView(this);
//                textView.setTextColor(Color.parseColor("#000000"));
//                textView.setLayoutParams(textParams);
//                textView.append("Universal Product Code: " + food1.gtinUpc);
//                row.addView(textView);
//                detailTable.addView(row);
//                

            }

            // Not necessary to print, but can be handy
//            row = new TableRow(this);
//            row.setLayoutParams(lp);
//            textView = new TextView(this);
//            textView.setTextColor(Color.parseColor("#000000"));
//            textView.setLayoutParams(textParams);
//            textView.append("Food Data Central ID: " + food1.fdcId);
//            row.addView(textView);
//            detailTable.addView(row);
//            

            // Nutrition Tests
            FoodTestActivity test1 = new FoodTestActivity();
            Pair<String, Integer> calorieDensity = test1.calorieDensity(food1.calories, food1.servingSize);
            Pair<String, Integer> totalFatComp = test1.totalFat(food1.calories, food1.fat);
            Pair<String, Integer> satFatComp = test1.saturatedFat(food1.calories, food1.saturatedFat);
            Pair<String, Integer> transFat = test1.transFat(food1.transFat, food1.ingredients);
            Pair<String, Integer> sodium = test1.sodiumContent(food1.calories, food1.sodium, food1.brandedFoodCategory);
            Pair<String, Integer> cholesterol = test1.cholesterolContent(food1.cholesterol, food1.ingredients);
            Pair<String, Integer> fiber = test1.fiberContent(food1.calories, food1.fiber);
            Pair<String, Integer> flour = test1.flours(food1.ingredients);
            Pair<String, Integer> sugar = test1.sugars(food1.ingredients);

            // Calorie Density
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textView.setLayoutParams(textParams);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.append("\nCalorie Density Test");
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append(calorieDensity.getValue0());
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textViewL = new TextView(this);
            textViewR = new TextView(this);
            textViewL.setTextColor(Color.parseColor("#000000"));
            textViewR.setTextColor(Color.parseColor("#000000"));
            textViewL.setLayoutParams(textParams);
            textViewR.setLayoutParams(textParams);
            textViewL.append("Rating:");
            textViewR.append(testRatingDecode(calorieDensity.getValue1()));
            row.addView(textViewL);
            row.addView(textViewR);
            testTable.addView(row);
            


            // Total Fat
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textView.setLayoutParams(textParams);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.append("\nTotal Fat Test");
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append(totalFatComp.getValue0());
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textViewL = new TextView(this);
            textViewR = new TextView(this);
            textViewL.setTextColor(Color.parseColor("#000000"));
            textViewR.setTextColor(Color.parseColor("#000000"));
            textViewL.setLayoutParams(textParams);
            textViewR.setLayoutParams(textParams);
            textViewL.append("Rating:");
            textViewR.append(testRatingDecode(totalFatComp.getValue1()));
            row.addView(textViewL);
            row.addView(textViewR);
            testTable.addView(row);
            

            // Saturated Fat
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textView.setLayoutParams(textParams);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.append("\nSaturated Fat Test");
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append(satFatComp.getValue0());
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textViewL = new TextView(this);
            textViewR = new TextView(this);
            textViewL.setTextColor(Color.parseColor("#000000"));
            textViewR.setTextColor(Color.parseColor("#000000"));
            textViewL.setLayoutParams(textParams);
            textViewR.setLayoutParams(textParams);
            textViewL.append("Rating:");
            textViewR.append(testRatingDecode(satFatComp.getValue1()));
            row.addView(textViewL);
            row.addView(textViewR);
            testTable.addView(row);
            

            // Trans Fat
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textView.setLayoutParams(textParams);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.append("\nTrans Fat Test");
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append(transFat.getValue0());
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textViewL = new TextView(this);
            textViewR = new TextView(this);
            textViewL.setTextColor(Color.parseColor("#000000"));
            textViewR.setTextColor(Color.parseColor("#000000"));
            textViewL.setLayoutParams(textParams);
            textViewR.setLayoutParams(textParams);
            textViewL.append("Rating:");
            textViewR.append(testRatingDecode(transFat.getValue1()));
            row.addView(textViewL);
            row.addView(textViewR);
            testTable.addView(row);
            

            // Sodium
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textView.setLayoutParams(textParams);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.append("\nSodium Test");
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append(sodium.getValue0());
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textViewL = new TextView(this);
            textViewR = new TextView(this);
            textViewL.setTextColor(Color.parseColor("#000000"));
            textViewR.setTextColor(Color.parseColor("#000000"));
            textViewL.setLayoutParams(textParams);
            textViewR.setLayoutParams(textParams);
            textViewL.append("Rating:");
            textViewR.append(testRatingDecode(sodium.getValue1()));
            row.addView(textViewL);
            row.addView(textViewR);
            testTable.addView(row);
            

            // Cholesterol
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textView.setLayoutParams(textParams);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.append("\nCholesterol Test");
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append(cholesterol.getValue0());
            row.addView(textView);
            testTable.addView(row);
            
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textViewL = new TextView(this);
            textViewR = new TextView(this);
            textViewL.setTextColor(Color.parseColor("#000000"));
            textViewR.setTextColor(Color.parseColor("#000000"));
            textViewL.setLayoutParams(textParams);
            textViewR.setLayoutParams(textParams);
            textViewL.append("Rating:");
            textViewR.append(testRatingDecode(cholesterol.getValue1()));
            row.addView(textViewL);
            row.addView(textViewR);
            testTable.addView(row);
            

            // Fiber
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textView.setLayoutParams(textParams);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.append("\nFiber Test");
            row.addView(textView);
            testTable.addView(row);

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append(fiber.getValue0());
            row.addView(textView);
            testTable.addView(row);

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textViewL = new TextView(this);
            textViewR = new TextView(this);
            textViewL.setTextColor(Color.parseColor("#000000"));
            textViewR.setTextColor(Color.parseColor("#000000"));
            textViewL.setLayoutParams(textParams);
            textViewR.setLayoutParams(textParams);
            textViewL.append("Rating:");
            textViewR.append(testRatingDecode(fiber.getValue1()));
            row.addView(textViewL);
            row.addView(textViewR);
            testTable.addView(row);


            // Flours and Grains
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textView.setLayoutParams(textParams);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.append("\nFlour and Grain Test");
            row.addView(textView);
            testTable.addView(row);

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append(flour.getValue0());
            row.addView(textView);
            testTable.addView(row);

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textViewL = new TextView(this);
            textViewR = new TextView(this);
            textViewL.setTextColor(Color.parseColor("#000000"));
            textViewR.setTextColor(Color.parseColor("#000000"));
            textViewL.setLayoutParams(textParams);
            textViewR.setLayoutParams(textParams);
            textViewL.append("Rating:");
            textViewR.append(testRatingDecode(flour.getValue1()));
            row.addView(textViewL);
            row.addView(textViewR);
            testTable.addView(row);


            // Added Sugars
            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textView.setLayoutParams(textParams);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.append("\nAdded Sugars Test");
            row.addView(textView);
            testTable.addView(row);

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);
            textView.append(sugar.getValue0());
            row.addView(textView);
            testTable.addView(row);

            row = new TableRow(this);
            row.setLayoutParams(lp);
            textViewL = new TextView(this);
            textViewR = new TextView(this);
            textViewL.setTextColor(Color.parseColor("#000000"));
            textViewR.setTextColor(Color.parseColor("#000000"));
            textViewL.setLayoutParams(textParams);
            textViewR.setLayoutParams(textParams);
            textViewL.append("Rating:");
            textViewR.append(testRatingDecode(sugar.getValue1()));
            row.addView(textViewL);
            row.addView(textViewR);
            testTable.addView(row);


            // Dialog box to get serving size from the user
            textView = findViewById(servingViewId);
            Double finalDefaultServing = defaultServing;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // If the food is branded, the portions are defaulted to the household serving size
                    if (food1.foodClass.equals("Branded")) {
                        portionWeights.clear();
                        portionDescriptions.clear();
                        portionWeights.add(finalDefaultServing.toString());
                        portionDescriptions.add(food1.householdServingFullText);
                    }
                    servingDialog(portionWeights, portionDescriptions);
                }
            });
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    // Decodes integer rating value given by nutrition test methods
    private String testRatingDecode(Integer rating) {

        String result = "";
        if(rating == 2)
            result = "Best";
        if(rating == 1)
            result = "Acceptable";
        if(rating == 0)
            result = "Bad";
        return result;
    }

    // Creates the servingDialog
    public void servingDialog(ArrayList<String> portionWeights, ArrayList<String> portionDescriptions) {
        ServingDialog servingDialog = new ServingDialog();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("portionWeights", portionWeights);
        bundle.putStringArrayList("portionDescriptions", portionDescriptions);
        servingDialog.setArguments(bundle);
        servingDialog.show(getSupportFragmentManager(), "serving dialog");
    }

    // When the serving size is selected from the servingDialog, the page is updated
    @Override
    public void servingSelection(Double servingSize) {
        servingSelection = servingSize;
        detailParse(fdcId);
    }

    // When the screen is rotated, the scrollView position is saved
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (servingSelection != null) {
            outState.putDouble("servingSelection", servingSelection);
        }
    }

}


