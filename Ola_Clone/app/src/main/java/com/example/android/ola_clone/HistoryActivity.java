package com.example.android.ola_clone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.android.ola_clone.LoginandSignup.Login;
import com.example.android.ola_clone.RecyclerViewHelper.HisotryAdapter;
import com.example.android.ola_clone.RecyclerViewHelper.History;
import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;


public class HistoryActivity extends AppCompatActivity {
    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.LayoutManager mHistoryLayoutManager;
    String checker,userId;
    DatabaseReference userHistoryDatabase;
    ArrayList <History> resultHistory = new ArrayList<History>();
    private HisotryAdapter mHistoryAdapter;
    LinearLayout clickHome, clickHistory, clickLogout, clickSettings;
    ImageView menuButton, close;
    DrawerLayout drawerLayout;
    TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mHistoryRecyclerView = findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);

        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        checker = getIntent().getStringExtra("checker");
        resultHistory =new ArrayList<History>();
        menuButton = findViewById(R.id.menuButton);
        close = findViewById(R.id.close);
        drawerLayout = findViewById(R.id.drawer_layout);
        clickHome = findViewById(R.id.clickHome);
        clickHistory = findViewById(R.id.clickHistory);
        emptyText = findViewById(R.id.empty);
        clickSettings = findViewById(R.id.clickSettings);
        clickLogout = findViewById(R.id.clickLogout);
        userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child(checker).child(userId).child("history");


        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer(drawerLayout);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeDrawer(drawerLayout);
            }
        });

        clickHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if(checker.equals("Drivers"))
              {
                  intent = new Intent(HistoryActivity.this, DriverMapActivity.class);
              }
              else
              {
                  intent = new Intent(HistoryActivity.this, CustomerMapActivity.class);
              }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        clickHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             recreate();
            }
        });

        clickSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(HistoryActivity.this, Profile.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        clickLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);

                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        disconnectDriver();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(HistoryActivity.this, Login.class));
                        finish();
                        return;
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.show();

            }
        });

       getUserHistoryIds();
    }

    private void closeDrawer(DrawerLayout drawerLayout) {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }




    private void getUserHistoryIds() {
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("times","reached inside nDataChange");
                    if(snapshot.exists())
                    {
                        Log.d("times","reached inside if");
                        for(DataSnapshot history: snapshot.getChildren())
                        {
                            Log.d("times","reached inside for loop");
                         //   Log.d("times", ""+ history.getValue(History.class).getTime());
                            resultHistory.add(history.getValue(History.class));
                        }

                        if(resultHistory.isEmpty())
                        {
                            emptyText.setVisibility(View.VISIBLE);
                            mHistoryRecyclerView.setVisibility(View.GONE);
                        }
                        else {
                            emptyText.setVisibility(View.GONE);
                            mHistoryRecyclerView.setVisibility(View.VISIBLE);
                            mHistoryAdapter = new HisotryAdapter(resultHistory, HistoryActivity.this);
                            mHistoryRecyclerView.setAdapter(mHistoryAdapter);
                            Log.d("times", "" + resultHistory.size());
                            mHistoryAdapter.notifyDataSetChanged();
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("times","error"+ error.getMessage());
            }
        });

    }

    private void disconnectDriver() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        GeoFire geoFireAvailable = new GeoFire(ref);
        geoFireAvailable.removeLocation(userId);
    }

}