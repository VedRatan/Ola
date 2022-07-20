package com.example.android.ola_clone;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.android.ola_clone.LoginandSignup.Login;
import com.example.android.ola_clone.databinding.ActivityDriverMapBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {


    LocationRequest mLocationRequest;
    private DatabaseReference refAvailable, refWorking;
    Button  rideStatus;
    Switch myWorkingSwitch;
    ImageView menuButton, close;
    private int status = 0;
    Boolean isloggingout = false, isAvailable=false;
    Double distance = 0.0;
    String driverId, customerId = "0", destination;
    public static LatLng destinationLatLng;
    private LinearLayout mCustomerInfo, clickHome, clickHistory, clickLogout, clickSettings;
    private TextView mCustomerName, mCustomerPhone, mCustomerDestination;
    CircleImageView customerProfileImage;
    DrawerLayout drawerLayout;
    private GoogleMap mMap;
    private ActivityDriverMapBinding binding;
    private Location mLastLocation;
    private List<Polyline> polylines;
    private double rideDistance;
    DatabaseReference assignedCustomerRef;
    private static final int[] COLORS = new int[]{R.color.purple_700};
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        polylines = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        rideStatus = findViewById(R.id.rideStatus);
        myWorkingSwitch = findViewById(R.id.workingSwitch);
        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        menuButton = findViewById(R.id.menuButton);
        close = findViewById(R.id.close);
        drawerLayout = findViewById(R.id.drawer_layout);
        clickHome = findViewById(R.id.clickHome);
        clickHistory = findViewById(R.id.clickHistory);
        clickSettings = findViewById(R.id.clickSettings);
        clickLogout = findViewById(R.id.clickLogout);




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
                recreate();
            }
        });

        clickHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverMapActivity.this, HistoryActivity.class);
                intent.putExtra("checker","Drivers");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        clickSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!customerId.equals("0")) {
                    Toast.makeText(DriverMapActivity.this, "Complete your trip first", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent  = new Intent(DriverMapActivity.this, Profile.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });

        clickLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder  builder = new AlertDialog.Builder(DriverMapActivity.this);

                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isloggingout = true;
                        disconnectDriver();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(DriverMapActivity.this, Login.class));
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


        mCustomerInfo = findViewById(R.id.customerInfo);
        mCustomerName = findViewById(R.id.customerName);
        mCustomerPhone = findViewById(R.id.customerPhone);
        customerProfileImage = findViewById(R.id.customerProfileImg);
        mCustomerDestination = findViewById(R.id.customerDestination);

        Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).
                withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        assert mapFragment != null;
                        mapFragment.getMapAsync(DriverMapActivity.this);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

        getAssignedCustomer();

        rideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (status) {
                    case 1:
                        status = 2;
                        removePolylines();

                        if (destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0) {

                            Log.d("marker", "latitude " + destinationLatLng.latitude);
                            Log.d("marker", "longitude " + destinationLatLng.longitude);
                            getRouteToMarker(destinationLatLng);
                            if (pickupmarker != null) {
                                pickupmarker.remove();
                            }
                        }

                        rideStatus.setText("Drive Completed");

                        break;

                    case 2:
                        recordRide();
                        endRide();
                        break;
                }
            }
        });


        myWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    isAvailable = true;
                            connectDriver();
                } else {
                    isAvailable = false;
                    disconnectDriver();
                }
            }
        });
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

    private void recordRide() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Drivers").child(userId).child("history");
        DatabaseReference customerref = FirebaseDatabase.getInstance().getReference().child("Customers").child(customerId).child("history");
        String requestId = driverref.push().getKey();
        HashMap map = new HashMap();
        map.put("driver", userId);
        map.put("customer", customerId);
        map.put("time", getCurrentTimeStamp());
        map.put("rideId", requestId);
        map.put("destination", destination);
        map.put("location/from/lat", pickuplatlong.latitude);
        map.put("location/from/lng", pickuplatlong.longitude);
        map.put("location/to/lat", destinationLatLng.latitude);
        map.put("location/to/lng", destinationLatLng.longitude);
        map.put("distance", rideDistance);
        driverref.child(requestId).updateChildren(map);
        customerref.child(requestId).updateChildren(map);

    }

    private Long getCurrentTimeStamp() {
        Long timestamp = System.currentTimeMillis() / 1000;
        return timestamp;
    }


    private void endRide() {


        removePolylines();
        DatabaseReference driverref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("customerRequest");
        ref.child(customerId).removeValue();
        customerId = "0";
        if (driverId != null) {
            driverref.child("Drivers").child(driverId).child("customerRequest").removeValue();
        }

        if (pickupmarker != null) {
            pickupmarker.remove();
        }

        if (assignedCustomerPickupLocationRefListener != null) {
            assignedCustomerRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }

        mCustomerInfo.setVisibility(View.GONE);
        rideStatus.setVisibility(View.GONE);
        rideStatus.setText("Pickup Completed");
        mCustomerName.setText("");
        mCustomerPhone.setText("");
        customerProfileImage.setImageResource(R.drawable.profile);
        mCustomerDestination.setText("--");


    }


    private void getAssignedCustomer() {
        assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    status = 1;
                    customerId = snapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerDestination();
                    getAssignedCustomerInfo();
                } else {
                    removePolylines();
                    endRide();
                    customerId = "0";
                    if (pickupmarker != null) {
                        pickupmarker.remove();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getAssignedCustomerInfo() {
        mCustomerInfo.setVisibility(View.VISIBLE);
        rideStatus.setVisibility(View.VISIBLE);
        final String[] userName = new String[1];
        final String[] userPhone = new String[1];
        final String[] userImage = new String[1];
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Users").child(customerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child("name")).exists()) {

                    userName[0] = snapshot.child("name").getValue().toString();
                    userPhone[0] = snapshot.child("phone").getValue().toString();
                    userName[0].trim();
                    mCustomerName.setText(userName[0]);
                    mCustomerPhone.setText(userPhone[0]);

                }

                if (snapshot.hasChild("image")) {
                    Log.d("image", "profile");
                    userImage[0] = snapshot.getValue().toString();
                    GetImage();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    LatLng pickuplatlong;
    Marker pickupmarker;
    DatabaseReference assignedCustomerPickupLocationRef;
    ValueEventListener assignedCustomerPickupLocationRefListener;

    private void getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !customerId.equals("0")) {
                    List<Object> map = (List<Object>) snapshot.getValue();
                    double locationlat = 0;
                    double locationlong = 0;

                    if (map.get(0) != null) {
                        locationlat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationlong = Double.parseDouble(map.get(1).toString());
                    }
                    pickuplatlong = new LatLng(locationlat, locationlong);
                    pickupmarker = mMap.addMarker(new MarkerOptions().position(pickuplatlong).title("Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.pickup1_foreground)));
                    getRouteToMarker(pickuplatlong);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAssignedCustomerDestination() {
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverId).child("customerRequest");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("destination") != null) {
                        destination = map.get("destination").toString();
                        mCustomerDestination.setText(destination);
                    }

                    Double destinationLat = 0.0, destinationLong = 0.0;

                    if (map.get("destinationLat") != null) {
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                        Log.d("maker", "" + destinationLat);
                    }

                    if (map.get("destinationLong") != null) {
                        destinationLong = Double.valueOf(map.get("destinationLong").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLong);
                    }

                } else {
                    mCustomerDestination.setText("--");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mCustomerInfo.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerPhone.setText("");
        customerProfileImage.setImageResource(R.drawable.profile);
    }

    private void getRouteToMarker(LatLng pickuplatlong) {

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickuplatlong)
                .key("AIzaSyDMI5yLsAIUidjWqrhPyFbxzdWQSesNGI0")
                .build();
        routing.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("TAG", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("TAG", "Can't find style. Error: ", e);
        }
    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            for(Location location: locationResult.getLocations())
            {
                if(!customerId.equals("0"))
                {
                    rideDistance+= mLastLocation.distanceTo(location)/1000;
                }


                mLastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
                refWorking = FirebaseDatabase.getInstance().getReference("driverWorking");
                GeoFire geoFireAvailable = new GeoFire(refAvailable);
                GeoFire geoFireWorking = new GeoFire(refWorking);



                Log.d("driverAvailable"," true");
                if(isAvailable)
                {

                    switch (customerId) {
                        case "0":
                            geoFireWorking.removeLocation(driverId);
                            geoFireAvailable.setLocation(driverId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                        default:
                            geoFireAvailable.removeLocation(driverId);
                            geoFireWorking.setLocation(driverId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                    }
                }

            }
        }
    };








    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private void connectDriver() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }
    public void disconnectDriver()
    {
            if(mFusedLocationClient != null)
            {
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            GeoFire geoFireAvailable = new GeoFire(ref);
            geoFireAvailable.removeLocation(userId);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
                if(FirebaseAuth.getInstance().getCurrentUser() != null)
                {
                    disconnectDriver();
                }

    }

    private void GetImage() {
        StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Profile Images/"+customerId+".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(customerProfileImage);
            }
        });
    }

    @Override
    public void onRoutingFailure(RouteException e) {
                if(e!=null) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
//            distance = Double.parseDouble(String.valueOf(route.get(i).getDistanceValue()/1000));
            int j = route.get(i).getDistanceValue();
            distance = (double) (j / 1000);
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

}