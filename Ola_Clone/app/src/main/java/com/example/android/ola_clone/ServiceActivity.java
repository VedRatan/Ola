package com.example.android.ola_clone;



import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


public class ServiceActivity extends AppCompatActivity {
    CardView premium, regular;
    String serviceType = "";
    DatabaseReference Driverref;
    String currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        premium = findViewById(R.id.premium);
        regular = findViewById(R.id.regular);
        currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        premium.setOnClickListener(view -> {
            serviceType = "premium";

            Driverref = FirebaseDatabase.getInstance().getReference().child("Drivers").child(currentUser);
            Driverref.child("Service").setValue(serviceType).addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    gotoNextActivity();
                }
            });
            gotoNextActivity();
        });

        regular.setOnClickListener(view -> {
            serviceType = "regular";

            Driverref = FirebaseDatabase.getInstance().getReference().child("Drivers").child(currentUser);
            Driverref.child("Service").setValue(serviceType).addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    gotoNextActivity();
                }
            });

        });



    }

    private void gotoNextActivity()
    {
        Intent intent = new Intent(ServiceActivity.this, DriverMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}