package com.design.senior;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
    Class used to register a user using a phone number
 */
public class   verifyPhoneActivity extends AppCompatActivity {

    private String verificationId;
    private FirebaseAuth mAuth;     // Firebase Authentication object
    private EditText editText;      // User inputted text field

    @Override
    /**
     * Initializes activity and declares which UI layout is used
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        /* Establish Firebase authenticator and retrieve user inputted code */
        mAuth = FirebaseAuth.getInstance();
        editText = findViewById(R.id.codeeditText);

        /* Retrieves phone number from previous activity's extra and sends code */
        String phonenumber = getIntent().getStringExtra("phonenumber");
        sendVerificationCode(phonenumber);

        /* Once the sign in button is clicked, the 6-digit code is verified */
        findViewById(R.id.signInbutton).setOnClickListener(v -> {
            String code = editText.getText().toString().trim();
            if(code.isEmpty() || code.length()<6){
                editText.setError("Enter code...");
                editText.requestFocus();
                return;
            }
            verifyCode(code);
        });
    }

    private void sendVerificationCode(String number){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                mCallBack
        );
    }
    /**
     * Verifies the user entered 6-digit code with Firebase
     * @param code
     */
    private void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){

                        Intent intent  = new Intent(verifyPhoneActivity.this,ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(verifyPhoneActivity.this, task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }



    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){
                verifyCode(code);
            }

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(verifyPhoneActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    };
}
