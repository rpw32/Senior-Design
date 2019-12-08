package com.design.senior;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openActivitySearch();
            }
        });

        final Button button2 = findViewById(R.id.button4);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openActivityDetail();
            }
        });
    }

    public void openActivitySearch() {
        Intent intent = new Intent(this, activity_search_result.class);
        startActivity(intent);
    }

    public void openActivityDetail() {
        Intent intent = new Intent(this, activity_detail_result.class);
        startActivity(intent);
    }
}
