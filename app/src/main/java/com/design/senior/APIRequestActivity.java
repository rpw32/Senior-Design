package com.design.senior;

import android.os.StrictMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static okhttp3.RequestBody.create;

public class APIRequestActivity {

    public JSONObject searchRequest(String generalSearchInput, String includeDataTypes, String requireAllWords, String pageNumber, String sortField, String sortDirection) throws IOException, JSONException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Set the default search parameters if they are not initialized
        if (generalSearchInput.equals(""))
            return null;
        if (includeDataTypes.equals(""))
            includeDataTypes = "{\"Survey (FNDDS)\":true,\"Foundation\":false,\"Branded\":true\"SR Legacy\":false, \"Experimental\":false}";
        if (requireAllWords.equals(""))
            requireAllWords = "true";
        if (pageNumber.equals(""))
            pageNumber = "1";
        if (sortField.equals(""))
            sortField = "publishedDate";
        if (sortDirection.equals(""))
            sortDirection = "asc";

        // Create the body of the request using the passed parameters
        String requestBody = String.format("{\n\t\"generalSearchInput\":\"%s\",\n\t\"requireAllWords\":\"%s\",\n\t\"pageNumber\":\"%s\",\n\t\"sortField\":\"%s\",\n\t\"sortDirection\":\"%s\"\n}", generalSearchInput, requireAllWords, pageNumber, sortField, sortDirection);

        // Request is sent via OkHttp
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url("https://api.nal.usda.gov/fdc/v1/search?api_key=BL2Ukd4QaZD2OXHr2ZCFZyqEph957r1NoQTWxV6x")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        Response responses = client.newCall(request).execute();


        String jsonData = responses.body().string();
        JSONObject jsonObject = new JSONObject(jsonData);
        return jsonObject;
    }

    public JSONObject detailRequest (String foodId) throws IOException, JSONException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (foodId == null)
            return null;

        String url = String.format("https://api.nal.usda.gov/fdc/v1/%s?api_key=BL2Ukd4QaZD2OXHr2ZCFZyqEph957r1NoQTWxV6x", foodId);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        String jsonData = response.body().string();
        JSONObject jsonObject = new JSONObject(jsonData);
        return jsonObject;
    }

}