package com.example.android.ola_clone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.android.ola_clone.LoginandSignup.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    DatabaseReference rootRef;
    FirebaseUser currentUser;
    String currentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo == null)
        {
            Intent intent = new Intent(MainActivity.this, NoInternet.class);
            intent.putExtra("context","main");
            startActivity(intent);
            finish();
            Animatoo.animateSlideLeft(this);
        }

        mAuth=FirebaseAuth.getInstance();
       currentUser= mAuth.getCurrentUser();
        rootRef= FirebaseDatabase.getInstance().getReference();
        progressDialog=new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();


        if(currentUser == null)
        {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, Login.class));
        }
        else
        {
            verifyExistence();
        }
    }

    private void verifyExistence(){
        currentID=mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if(!(snapshot.child("name")).exists()){
                    startActivity(new Intent(MainActivity.this, Profile.class));
                }

                if(snapshot.child("class").exists())
                {
                    String checker = snapshot.child("class").getValue().toString();
                    if(checker.equals("Driver"))
                    {
                        startActivity(new Intent(MainActivity.this, DriverMapActivity.class));
                    }

                    if(checker.equals("Customer"))
                    {
                        startActivity(new Intent(MainActivity.this, CustomerMapActivity.class));
                    }

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}