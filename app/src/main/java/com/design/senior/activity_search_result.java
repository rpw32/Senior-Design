package com.design.senior;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
public class activity_search_result extends AppCompatActivity {

    private TextView mTextViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        mTextViewResult = findViewById(R.id.text_view_result);
        Button buttonParse = findViewById(R.id.button_parse);

        buttonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchParse();
            }
        });

    }

    private void searchParse() {

        try {
            APIRequestActivity inst1 = new APIRequestActivity();

            JSONObject response;
            response = inst1.searchRequest("767335000821", "", "true", "", "", "");

            Iterator x = response.keys();
            JSONArray arrayResponse = new JSONArray();

            while (x.hasNext()){
                String key = (String) x.next();
                arrayResponse.put(response.get(key));
            }

            String totalHits = arrayResponse.getString(1);
            mTextViewResult.append("total hits: " + totalHits + "\n");
            String currentPage = arrayResponse.getString(2);
            mTextViewResult.append("current page: " + currentPage + "\n");
            String totalPages = arrayResponse.getString(3);
            mTextViewResult.append("total pages: " + totalPages + "\n\n");
            JSONArray foodsArray = arrayResponse.getJSONArray(4);
            mTextViewResult.append("results: \n\n");

            for (int i = 0; i < foodsArray.length(); i++) {
                JSONObject food = foodsArray.getJSONObject(i);

                mTextViewResult.append("search result: " + i + "\n");
                String fdcId = food.getString("fdcId");
                mTextViewResult.append("fdcId: " + fdcId + "\n");
                String description = food.getString("description");
                mTextViewResult.append("description: " + description + "\n");
                String dataType = food.getString("dataType");
                mTextViewResult.append("dataType: " + dataType + "\n");
                String gtinUpc = food.getString("gtinUpc");
                mTextViewResult.append("gtinUpc: " + gtinUpc + "\n");
                String publishedDate = food.getString("publishedDate");
                mTextViewResult.append("published date: " + publishedDate + "\n");
                String brandOwner = food.getString("brandOwner");
                mTextViewResult.append("brand owner: " + brandOwner + "\n");
                String ingredients = food.getString("ingredients");
                mTextViewResult.append("ingredients: " + ingredients + "\n");
                mTextViewResult.append("\n\n\n");


            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }
}
