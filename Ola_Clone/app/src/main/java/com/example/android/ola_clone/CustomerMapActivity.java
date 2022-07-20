package com.example.android.ola_clone;

import static com.example.android.ola_clone.R.id;
import static com.example.android.ola_clone.R.mipmap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.example.android.ola_clone.LoginandSignup.Login;
import com.example.android.ola_clone.databinding.ActivityCustomerMapBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback{


        FusedLocationProviderClient client;
        GoogleApiClient mGoogleApiClient;
        LocationRequest mLocationRequest;
        private DatabaseReference RootRef;
        private int radius = 1;
        private Boolean driverFound = false;
        private String driverId;
        Button request;
        GeoQuery geoQuery;
        String userId,destination;
        RadioGroup group;
        LatLng pickupLocation;
        Boolean requestbol = false;
        Marker mPickUpMarker;
        private GoogleMap mMap;
        private ActivityCustomerMapBinding binding;
        private Location mLastLocation;
        private FusedLocationProviderClient mFusedLocationClient;
        private static int AUTOCOMPLETE_REQUEST_CODE = 1;
        String service;
        LatLng destinationLatLng;
        private LinearLayout mDriverInfo;
        private TextView mDriverName, mDriverPhone;
        View regularHighlight, premiumihilight;
        CircleImageView driverProfileImage;
         LinearLayout mCustomerInfo, clickHome, clickHistory, clickLogout, clickSettings, premium , local;
        DrawerLayout drawerLayout;
        ImageView menuButton, close;
        LinearLayout bottomSheetLayout;
        BottomSheetBehavior bottomSheetBehavior;
        String serviceDriver;
        private AutoCompleteTextView autoCompleteTextView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                binding = ActivityCustomerMapBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                request = findViewById(id.request);



                destinationLatLng = new LatLng(0.0,0.0);

                mDriverInfo = findViewById(id.driverInfo);
                mDriverName = findViewById(id.driverName);
                mDriverPhone = findViewById(id.driverPhone);
                driverProfileImage = findViewById(id.driverProfileImg);
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                menuButton = findViewById(R.id.menuButton);
                close = findViewById(R.id.close);
                drawerLayout = findViewById(R.id.drawer_layout);
                clickHome = findViewById(R.id.clickHome);
                clickHistory = findViewById(R.id.clickHistory);
                clickSettings = findViewById(R.id.clickSettings);
                clickLogout = findViewById(R.id.clickLogout);
                premium = findViewById(id.premium);
                local = findViewById(id.regular);
                group = findViewById(id.radioGroup);
                regularHighlight = findViewById(id.regularhighlight);
                premiumihilight = findViewById(id.premiumhighlight);
                bottomSheetLayout = findViewById(id.bottom_sheet);

                Window window = getWindow();
                window.setStatusBarColor(Color.TRANSPARENT);


                Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).
                        withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                        assert mapFragment != null;
                                        mapFragment.getMapAsync(CustomerMapActivity.this);
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                        permissionToken.continuePermissionRequest();
                                }
                        }).check();

                Places.initialize(getApplicationContext(), "AIzaSyDMI5yLsAIUidjWqrhPyFbxzdWQSesNGI0");

                // Initialize the AutocompleteSupportFragment.
                AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                        getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

                // Specify the types of place data to return.
                autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

                // Set up a PlaceSelectionListener to handle the response.
                autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                        @Override
                        public void onPlaceSelected(@NonNull Place place) {
                                // TODO: Get info about the selected place.
                                destination = place.getName();
                                Log.d("destination",""+destination);
                                destinationLatLng = place.getLatLng();
                                Log.d("destination",""+destinationLatLng);

                        }


                        @Override
                        public void onError(@NonNull Status status) {
                                // TODO: Handle the error.
                                Log.d("Places", "An error occurred: " + status);
                        }
                });


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
                                Intent intent = new Intent(CustomerMapActivity.this, HistoryActivity.class);
                                intent.putExtra("checker","Customers");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                        }
                });

                clickSettings.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                                        Intent intent  = new Intent(CustomerMapActivity.this, Profile.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                        }
                });

                clickLogout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                AlertDialog.Builder  builder = new AlertDialog.Builder(CustomerMapActivity.this);

                                builder.setTitle("Logout");
                                builder.setMessage("Are you sure you want to logout ?");
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                FirebaseAuth.getInstance().signOut();
                                                startActivity(new Intent(CustomerMapActivity.this, Login.class));
                                                Animatoo.animateSwipeRight(CustomerMapActivity.this);
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


                local.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                service = "regular";
                                regularHighlight.setVisibility(View.VISIBLE);
                                premiumihilight.setVisibility(View.GONE);
                                request.setVisibility(View.VISIBLE);
                        }
                });

                premium.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                service = "premium";
                                regularHighlight.setVisibility(View.GONE);
                                premiumihilight.setVisibility(View.VISIBLE);
                                request.setVisibility(View.VISIBLE);
                        }
                });

                request.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                                if(requestbol)
                                {
                                        endRide();
                                        premium.setVisibility(View.VISIBLE);
                                        premiumihilight.setVisibility(View.GONE);
                                        local.setVisibility(View.VISIBLE);
                                        regularHighlight.setVisibility(View.GONE);
                                        request.setVisibility(View.GONE);

                                }
                                else
                                {

                                        requestbol = true;
                                        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        RootRef = FirebaseDatabase.getInstance().getReference("customerRequest");
                                        GeoFire geoFire = new GeoFire(RootRef);
                                        geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                                        pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                        mPickUpMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(mipmap.pickup1_foreground)));

                                        request.setText("Getting your driver click to cancel request");
                                        premium.setVisibility(View.GONE);
                                        premiumihilight.setVisibility(View.GONE);
                                        local.setVisibility(View.GONE);
                                        regularHighlight.setVisibility(View.GONE);
                                        getClosestDriver();
                                }

                        }
                });

                mDriverInfo.setVisibility(View.GONE);
                mDriverName.setText("");
                mDriverPhone.setText("");
                driverProfileImage.setImageResource(R.drawable.profile);
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

        private void endRide() {

                requestbol = false;
                radius = 1;
                geoQuery.removeAllListeners();
                if(driverlocationref!=null)
                {
                        driverlocationref.removeEventListener(driverlocationrefListener);
                }
                if(driveHasEndedRef!=null)
                {
                        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);
                }
                String userId = FirebaseAuth.getInstance().getUid();
                DatabaseReference driverref = FirebaseDatabase.getInstance().getReference();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("customerRequest");
                ref.child(userId).removeValue();
                if(driverId != null)
                {
                        driverref.child("Drivers").child(driverId).child("customerRequest").removeValue();
                        driverId = null;
                }
                driverFound = false;

                if(mPickUpMarker!=null)
                {
                        mPickUpMarker.remove();
                }
                if(mDriverMarker!= null)
                {
                        mDriverMarker.remove();
                }
                mDriverInfo.setVisibility(View.GONE);
                mDriverName.setText("");
                mDriverPhone.setText("");
                driverProfileImage.setImageResource(R.drawable.profile);
                request.setText("Book Ola Cab");

        }

        private void getClosestDriver() {
                DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
                GeoFire geoFire = new GeoFire(driverLocation);
                geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
                geoQuery.removeAllListeners();
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                                        if(!driverFound && requestbol)
                                        {

                                                driverId = key;
                                                Log.d("driverfound", ""+key+" "+driverFound);
                                                DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverId).child("customerRequest");
                                                DatabaseReference serviceDatabase = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverId);

                                                serviceDatabase.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                serviceDriver = snapshot.child("Service").getValue().toString();
                                                                if(service.equals(serviceDriver))
                                                                {
                                                                        driverFound =true;
                                                                        String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                                        HashMap map = new HashMap();
                                                                        map.put("customerRideId", customerId);
                                                                        map.put("destination", destination);
                                                                        map.put("destinationLat", destinationLatLng.latitude);
                                                                        map.put("destinationLong", destinationLatLng.longitude);
                                                                        driverref.updateChildren(map);

                                                                        getDriverLocation();
                                                                        getAssignedDriverInfo();
                                                                        getHasRideEnded();
                                                                        request.setText("Looking for driver location");
                                                                }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                });

                                        }
                        }


                        @Override
                        public void onKeyExited(String key) {

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                                if(!driverFound)
                                {
                                        radius = radius + 1;
                                        Log.d("radius", ": "+radius);
                                        if(radius > 100)
                                        {
                                                Toast.makeText(CustomerMapActivity.this, "No driver available", Toast.LENGTH_SHORT).show();
                                                return;
                                        }
                                        getClosestDriver();
                                }

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                });
        }

        private void getAssignedDriverInfo() {
                mDriverInfo.setVisibility(View.VISIBLE);
                final String[] userName = new String[1];
                final String[] userPhone = new String[1];
                final String[] userImage = new String[1];
                DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
                RootRef.child("Users").child(driverId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if((snapshot.child("name")).exists())
                                {

                                        userName[0] = snapshot.child("name").getValue().toString();
                                        userPhone[0] = snapshot.child("phone").getValue().toString();
                                        userName[0].trim();
                                        mDriverName.setText(userName[0]);
                                        mDriverPhone.setText(userPhone[0]);

                                }

                                if(snapshot.hasChild("image"))
                                {
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

        Marker mDriverMarker;
        DatabaseReference driverlocationref;
        ValueEventListener driverlocationrefListener;
        private void getDriverLocation() {
                driverlocationref = FirebaseDatabase.getInstance().getReference().child("driverWorking").child(driverId).child("l");
                driverlocationrefListener=driverlocationref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                        List<Object> map = (List<Object>)snapshot.getValue();
                                        double locationlat = 0;
                                        double locationlong = 0;
                                        request.setText("Driver Found");

                                        if(map.get(0) != null)
                                        {
                                                locationlat  = Double.parseDouble(map.get(0).toString());
                                        }
                                        if(map.get(1)!=null)
                                        {
                                                locationlong  = Double.parseDouble(map.get(1).toString());
                                        }
                                         LatLng driverlatlong = new LatLng(locationlat, locationlong);
                                         if(mDriverMarker != null)
                                         {
                                                 mDriverMarker.remove();
                                         }

                                         Location loc1 = new Location("");
                                         loc1.setLatitude(pickupLocation.latitude);
                                         loc1.setLongitude(pickupLocation.longitude);

                                        Location loc2 = new Location("");
                                        loc2.setLatitude(driverlatlong.latitude);
                                        loc2.setLongitude(driverlatlong.longitude);

                                        float distance = loc1.distanceTo(loc2);
                                        if(distance<100)
                                        {
                                                request.setText("Driver has arrived click to cancel the ride");
                                        }
                                        else {
                                                request.setText("Driver Found" + String.valueOf(distance));
                                        }
                                        mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverlatlong).title("Your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver_foreground)));

                                }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                });
        }

        DatabaseReference driveHasEndedRef;
        private ValueEventListener driveHasEndedRefListener;
        private void getHasRideEnded() {
                 driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
                driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {

                                }
                                else
                                {
                                        endRide();
                                }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                });
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
//        buildGoogleApiClient();
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);


        }


        static int count = 0;
        LocationCallback mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {

                        super.onLocationResult(locationResult);
                        Location mLocation = null;
                        mLocation=locationResult.getLastLocation();
                        count+=1;
                        for(Location location: locationResult.getLocations())
                        {
                                mLastLocation = location;
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                Log.d("location", location.getLatitude() + " " + location.getLongitude());
                        }
                        if(count<=1)
                        {
                                LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                        }
                        Log.d("customerAvailable"," true");
                        if(!getDriversAroundStarted)
                        {
                                getDriversAround();

                        }
                }
        };




        @Override
        public void onPointerCaptureChanged(boolean hasCapture) {
                super.onPointerCaptureChanged(hasCapture);
        }

        @Override
        protected void onStop() {
                super.onStop();
        }

        private void GetImage() {
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Profile Images/"+driverId+".jpg");
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                                Glide.with(getApplicationContext()).load(uri).into(driverProfileImage);
                        }
                });
        }
        List <Marker> markerList = new ArrayList<Marker>();
        boolean getDriversAroundStarted = false;
        private void getDriversAround()
        {
                getDriversAroundStarted = true;
                DatabaseReference driversLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
                GeoFire geoFire = new GeoFire(driversLocation);
                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 20);
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                                for(Marker marker: markerList)
                                {
                                        if(marker.getTag().equals(key))
                                        {

                                        }
                                }
                                LatLng driverLocation =  new LatLng(location.latitude, location.longitude);
                                Marker mdriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver_foreground)));
                                mdriverMarker.setTag(key);
                                markerList.add(mdriverMarker);

                        }

                        @Override
                        public void onKeyExited(String key) {
                                for(Marker marker: markerList)
                                {
                                        if (marker.getTag().equals(key)) {
                                                marker.remove();
                                                markerList.remove(marker);
                                                return;
                                        }
                                }
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {
                                for(Marker marker: markerList)
                                {
                                        if (marker.getTag().equals(key)) {
                                                marker.setPosition(new LatLng(location.latitude, location.longitude));
                                        }
                                }
                        }

                        @Override
                        public void onGeoQueryReady() {

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                });

        }
}