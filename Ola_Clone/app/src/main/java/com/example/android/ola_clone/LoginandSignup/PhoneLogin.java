package com.example.android.ola_clone.LoginandSignup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.android.ola_clone.R;

public class PhoneLogin extends AppCompatActivity {
    EditText phoneNumber;
    Button done;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        phoneNumber = findViewById(R.id.phoneNumber);
        done = findViewById(R.id.done);



        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = phoneNumber.getText().toString();
                Log.d("vedlog", "phonenumber " + phone);
                if (phone.isEmpty()) {
                    phoneNumber.setError("enter the phone number first");
                } else if (phoneNumber.length() != 10) {
                    phoneNumber.setError("invalid phone number");
                } else {
                    Intent intent = new Intent(PhoneLogin.this, OTPVerification.class);
                    intent.putExtra("phoneNumber", phone);
                    startActivity(intent);
                    Animatoo.animateZoom(PhoneLogin.this);
                }
            }
        });
    }
}