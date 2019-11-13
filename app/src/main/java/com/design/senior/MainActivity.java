package com.design.senior;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText phoneEditText;
    private Button continuebutton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneEditText = findViewById(R.id.phoneNumeditText);

        findViewById(R.id.continuebutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numberWDash = phoneEditText.getText().toString().trim();
                String number = numberWDash.replace("-","");

                if(number.isEmpty()|| number.length() < 10){
                    phoneEditText.setError("Valid phone number required");
                    phoneEditText.requestFocus();
                    return;
                }

                String phonenumber = "+1" + number;

                Intent intent = new Intent(MainActivity.this, verifyPhoneActivity.class);
                intent.putExtra("phonenumber",phonenumber);
                startActivity(intent);
            }
        });

    }
}
