package com.example.android.ola_clone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.android.ola_clone.LoginandSignup.LoginByEmail;
import com.example.android.ola_clone.LoginandSignup.SignUp;

public class NoInternet extends AppCompatActivity {
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
        String context = getIntent().getStringExtra("context");
        button = findViewById(R.id.retry);
        button.setOnClickListener(view -> {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo != null)
            {
                if(context.equals("main"))
                {
                    Intent intent = new Intent(NoInternet.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                    Animatoo.animateSlideLeft(NoInternet.this);
                }
                else if(context.equals("drivermap")) {
                    Intent intent = new Intent(NoInternet.this, DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                    Animatoo.animateSlideLeft(NoInternet.this);
                }

                else if(context.equals("loginbyemail")) {
                    Intent intent = new Intent(NoInternet.this, LoginByEmail.class);
                    startActivity(intent);
                    finish();
                    Animatoo.animateSlideLeft(NoInternet.this);
                }

                else if(context.equals("signup")) {
                    Intent intent = new Intent(NoInternet.this, SignUp.class);
                    startActivity(intent);
                    finish();
                    Animatoo.animateSlideLeft(NoInternet.this);
                }
            }
        });
    }
}