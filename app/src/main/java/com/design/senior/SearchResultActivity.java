package com.design.senior;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SearchResultActivity extends AppCompatActivity {

    int pageNumber = 1; // Search page defaults at 1
    final boolean[] processClick = {true}; // Boolean variable is used to prevent multiple button presses
    int position[] = {0, 0}; // Used to restore scroll position if screen is rotated
    String search;
    EditText searchInput;
    Button searchButton;
    TableLayout searchTable;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // Link variables to their layout IDs
        searchInput = (EditText) findViewById(R.id.searchInput);
        searchButton = (Button) findViewById(R.id.searchButton);
        searchTable = (TableLayout) findViewById(R.id.searchTable);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        // If upcTitle is bundled, it is set as the searchInput text
        Intent intent = getIntent();
        String upcTitle = intent.getStringExtra("upcTitle");
        if (upcTitle != null) {
            searchInput.setText(upcTitle);
        }

        // If the user hits enter on the keyboard, it is treated as a click for the "Submit" button
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    searchButton.performClick();
                    handled = true;
                }
                return handled;
            }
        });

        // Search button checks to see if the field is empty. If it isn't, call searchParse()
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When processClick[0] is true, the button can be pressed
                if (processClick[0]) {
                    // Check if input field is populated
                    if (searchInput.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Enter a search term", Toast.LENGTH_SHORT).show();
                    } else {
                        // Button is disabled until searchParse() is finished running
                        processClick[0] = false;
                        searchButton.setEnabled(false);
                        searchButton.setClickable(false);
                        search = searchInput.getText().toString(); // User input is stored to search string
                        pageNumber = 1; // Reset pageNumber to 1 when Search button is pressed
                        searchParse();
                    }
                }
            }
        });

    }

    private void searchParse() { // Parse the API Search response

        try {
            // scrollView defaults to 0
            if (position[0] == 0) {
                scrollView.scrollTo(0, 0); // Reset scrollView position to the top
            }

            searchTable.removeAllViews(); // Reset table, make sure its empty before populating it again

            DatabaseHelper mDatabaseHelper = new DatabaseHelper(this);
            JSONObject response;

            // Check local database for response. If it exists, fetch the response locally
            if (mDatabaseHelper.searchCheckAlreadyExist(search)) {
                String databaseResponse = mDatabaseHelper.searchGetData(search);
                response = new JSONObject(databaseResponse); // Responses are converted to JSONObjects so they can be parsed
            }
            // Else, the response is fetched and saved locally
            else {
                // Check for internet connectivity
                InternetCheck internetCheck = new InternetCheck();
                // If there is no internet, searchParse() is not called
                if (!internetCheck.isOnline()) {
                    Context context = getApplicationContext();
                    CharSequence text = "Internet connection not detected.";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }
                else {
                    APIRequestActivity inst1 = new APIRequestActivity();
                    response = inst1.searchRequest(search, "", "true", String.valueOf(pageNumber), "", "");
                    String databaseString = response.toString();
                    mDatabaseHelper.searchAddData(search, databaseString);
                }
            }

            // Search result stats always located at the top
            String totalHits = response.getString("totalHits");
            String currentPage = response.getString("currentPage");
            String totalPages = response.getString("totalPages");
            JSONArray foodsArray = response.getJSONArray("foods");

            TableRow firstRow = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT); // Set parameters for the searchTable rows, so they wrap to content
            TableRow.LayoutParams textParams = new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1f); // Set parameters for the textView
            firstRow.setLayoutParams(lp);
            TextView textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setLayoutParams(textParams);

            textView.append("Total Hits: " + totalHits + "\n");
            textView.append("Current Page: " + currentPage + "\n");
            textView.append("Total Pages: " + totalPages + "\n");

            // Add textView to row, then add row to searchTable
            firstRow.addView(textView);
            searchTable.addView(firstRow, 0);


            // Loop through the array of foods, showing 50 foods per page
            for (int i = 0; i < foodsArray.length(); i++) {
                JSONObject food = foodsArray.getJSONObject(i); // Each food is grabbed as a JSONObject

                // Setup new row and textView
                TableRow row = new TableRow(this);
                row.setLayoutParams(lp);
                textView = new TextView(this);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setLayoutParams(textParams);

                // All search results will have these strings
                String fdcId = food.getString("fdcId");
                String description = food.getString("description");
                String dataType = food.getString("dataType");
                String publishedDate = food.getString("publishedDate");

                // Initialize these strings as empty, since some search results don't have them
                String ingredients = "";
                String gtinUpc = "";
                String brandOwner = "";
                String additionalDescriptions = "";

                // Result number is calculated with what page you are currently on
                int resultNumber = (i + 1) + 50 * (pageNumber - 1);

                // Branded foods will have these strings
                if (dataType.equals("Branded")) {
                    ingredients = food.getString("ingredients");
                    gtinUpc = food.getString("gtinUpc");
                    brandOwner = food.getString("brandOwner");
                }
                // Only certain Survey foods have this, but it is a useful description
                if (!food.isNull("additionalDescriptions")) {
                    additionalDescriptions = food.getString("additionalDescriptions");
                }

                // Print result, fdcId, and description
                textView.append("Result: " + resultNumber + "\n");
                textView.append("FDC ID: " + fdcId + "\n");
                textView.append("Description: " + description + "\n");

                // Check to make sure it isn't null before printing
                if (!food.isNull("additionalDescriptions")) {
                    textView.append("Additional Descriptions: " + additionalDescriptions + "\n");
                }

                textView.append("DataType: " + dataType + "\n");
                textView.append("Published Date: " + publishedDate + "\n");

                // Print Branded exclusive strings
                if (dataType.equals("Branded")) {
//                  textView.append("Ingredients: " + ingredients + "\n");
                    textView.append("UPC: " + gtinUpc + "\n");
                    textView.append("Brand Owner: " + brandOwner + "\n");
                }

                // Add view to row, and row to searchTable
                row.addView(textView);
                searchTable.addView(row, (i+1));

                // When clicked, the row will load the corresponding food detail page
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent i = new Intent(SearchResultActivity.this, DetailResultActivity.class);
                        i.putExtra("fdcId", fdcId);
                        startActivity(i);
                    }
                });
            }

            // Last row is for Previous Page/Next Page buttons
            TableRow row = new TableRow(this);
            row.setLayoutParams(lp);

            Button prevPage = new Button(this);
            prevPage.setText("Previous Page");
            Button nextPage = new Button(this);
            nextPage.setText("Next Page");

            // Previous Page button is only loaded when the page number is above 1
            if (Integer.valueOf(currentPage) > 1) {
                searchTable.addView(prevPage);
                prevPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pageNumber--;
                        searchTable.removeAllViews();
                        overridePendingTransition(0, 0);
                        searchParse();
                        overridePendingTransition(0, 0);
                    }
                });
            }

            // Next Page button is only loaded when the total page number is greater than the current page
            if (Integer.valueOf(totalPages) > Integer.valueOf(currentPage)) {
                searchTable.addView(nextPage);
                nextPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pageNumber++;
                        searchTable.removeAllViews();
                        overridePendingTransition(0, 0);
                        searchParse();
                        overridePendingTransition(0, 0);
                    }
                });
            }

            // Reset search button
            processClick[0] = true;
            searchButton.setEnabled(true);
            searchButton.setClickable(true);

            // Scroll is restored if the screen is rotated
            if (position[0] != 0) {
                scrollView.scrollTo(position[0], position[1]);
                position[0] = 0;
                position[1] = 0;
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    // When the screen is rotated, the scrollView position is saved
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        position[0] = scrollView.getScrollX();
        position[1] = scrollView.getScrollY();
    }

    // After screen rotation, searchParse() is called again
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        searchButton.performClick();
    }
}
