package com.design.senior;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.design.senior.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openCamera(View view){
    Intent intent = new Intent(this, CameraMainActivity.class);
    startActivity(intent);
    }

    public void openSearch(View view) {
    Intent intent = new Intent(this, SearchResultActivity.class);
    startActivity(intent);
    }

    public void openSettings(View view){
        Intent intent = new Intent(this, TestSettingsActivity.class);
        startActivity(intent);
    }
    
}
