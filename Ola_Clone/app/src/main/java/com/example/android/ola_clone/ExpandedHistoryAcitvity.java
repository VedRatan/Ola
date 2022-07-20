package com.example.android.ola_clone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExpandedHistoryAcitvity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {
    GoogleMap mMap;
    SupportMapFragment mMapFragment;
    TextView rideLocation,rideDistance,rideDate, userName, userPhone, infotitle;
    DatabaseReference rideHistoryInfo, RootRef;
    String rideId, currentUserId, customerId, driverId;
    CircleImageView profileImage;
    Button paymentButton;
    String checker, distance;
    Boolean customerPaid = false;
    Double ridePrice;
    LatLng pickupLatLng, destinationLatLng;
    private static final int[] COLORS = new int[]{R.color.purple_700};
    private List<Polyline> polylines;
    private String driverorcustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_history_acitvity);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        Initialize();
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);


        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild("name"))
                {

                    driverorcustomer = snapshot.child("class").getValue().toString();
                    getRideInformation(driverorcustomer);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void Initialize() {

        mMapFragment.getMapAsync(this);
        rideId = getIntent().getStringExtra("rideId");
        rideLocation = findViewById(R.id.location);
        rideDistance = findViewById(R.id.rideDistance);
        rideDate = findViewById(R.id.rideDate);
        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        paymentButton = findViewById(R.id.paymentButton);
        profileImage = findViewById(R.id.driverImage);
        infotitle = findViewById(R.id.infotitle);
        polylines = new ArrayList<>();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void getRideInformation(String driverorcustomer) {

        rideHistoryInfo = FirebaseDatabase.getInstance().getReference().child(driverorcustomer+"s").child(currentUserId).child("history").child(rideId);
        Log.d("checker1", " outside: "+ driverorcustomer +" id: "+currentUserId );

        rideHistoryInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("location1", "inside ondatachange "+snapshot.exists());
                    for(DataSnapshot child: snapshot.getChildren())
                    {
                        Log.d("location1", "inside for");
                        if(child.getKey().equals("customer"))
                        {
                            customerId = child.getValue().toString();
                            if(!currentUserId.equals(customerId))
                            {
                                checker = "Drivers";
                                getUserInformation("Customers", customerId);
                                infotitle.setText("Customer Details");
                            }
                        }

                        if(child.getKey().equals("driver"))
                        {
                            driverId = child.getValue().toString();
                            if(!currentUserId.equals(driverId))
                            {
                                checker = "Customers";
                                getUserInformation("Drivers", driverId);
                                infotitle.setText("Driver Details");
                                getCustomerRelatedObjects();
                            }
                        }
                        if(child.getKey().equals("time"))
                        {
                            rideDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }

                        if(child.getKey().equals("destination"))
                        {
                            rideLocation.setText(child.getValue().toString());
                        }

                        if(child.getKey().equals("customerPaid"))
                        {
                            customerPaid = true;
                        }

                        if(child.getKey().equals("location"))
                        {
                           pickupLatLng = new LatLng(Double.parseDouble(child.child("from").child("lat").getValue().toString()),
                                   Double.parseDouble(child.child("from").child("lng").getValue().toString()));

                           destinationLatLng = new LatLng(Double.parseDouble(child.child("to").child("lat").getValue().toString()),
                                   Double.parseDouble(child.child("to").child("lng").getValue().toString()));

                           if(destinationLatLng != new LatLng(0, 0))
                           {
                               getRouteToMarker();
                           }

                        }

                        if(child.getKey().equals("distance"))
                        {
                            distance = child.getValue().toString();
                            rideDistance.setText(distance.substring(0, Math.min(distance.length(), 5))+" Km");
                            ridePrice = Double.valueOf(distance) * 0.5;
                        }

                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCustomerRelatedObjects() {
        if(customerPaid)
        {
            paymentButton.setVisibility(View.GONE);
        }

        else
        {
            paymentButton.setVisibility(View.VISIBLE);
            paymentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    payPalPayment();
                }
            });
        }
    }
    private  int PAYMENT_REQUEST_CODE = 1;
    private static PayPalConfiguration config = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);
    private void payPalPayment() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(ridePrice), "USD", "OLA Ride", PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(ExpandedHistoryAcitvity.this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYMENT_REQUEST_CODE);

    }

    private void getUserInformation(String checker1, String Id) {
        DatabaseReference otherUser = FirebaseDatabase.getInstance().getReference().child(checker1).child(Id);
        GetImage(Id);

        otherUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    Map<String ,Object> map = (Map<String, Object>) snapshot.getValue();
                    if(map.get("name")!=null)
                    {
                        userName.setText(map.get("name").toString());
                    }

                    if(map.get("phone")!=null)
                    {
                        userPhone.setText(map.get("phone").toString());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();
        return date;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
    }

    private void GetImage(String user) {
        StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Profile Images/"+user+".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(profileImage);
            }
        });
    }



    private void getRouteToMarker() {

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLatLng, destinationLatLng)
                .key("AIzaSyDMI5yLsAIUidjWqrhPyFbxzdWQSesNGI0")
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e!=null) {
            // Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "something went wrong, try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds =builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width * 0.2);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location"));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("destination"));
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

//            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }

    private  void removePolylines()
    {
        for(Polyline line: polylines)
        {
            line.remove();
        }
        polylines.clear();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PAYMENT_REQUEST_CODE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirmation != null)
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(confirmation.toJSONObject().toString());
                        String paymentResponse = jsonObject.getJSONObject("response").getString("state");
                        if(paymentResponse.equals("approved"))
                        {
                            Toast.makeText(this, "Payment was successful", Toast.LENGTH_SHORT).show();
                            rideHistoryInfo.child("customerPaid").setValue(true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            else
            {
                Toast.makeText(this, "Payment was unsuccessful", Toast.LENGTH_SHORT).show();
            }
        }
    }
}