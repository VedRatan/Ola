package com.example.android.ola_clone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.example.android.ola_clone.LoginandSignup.Login;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    EditText name;
    String userName,userEmail,userPhone,userImage;
    EditText email;
    EditText phone ;
    TextView choiceText, subtitle, serviceText;
    CircleImageView profileImage;
    Button update;
    private FirebaseAuth auth;
    String currentUser;
    RadioGroup group, serviceGroup;
    Boolean userExist=false;
    RadioButton radioButton;
    private ProgressDialog progressDialog;
    private DatabaseReference RootRef;
    String checker;
    public static final int Gallery_code = 1;
    LinearLayout clickHome, clickHistory, clickLogout, clickSettings;
     Uri resulUri;
    StorageReference UserProfileImg,storageReference;
    ImageView menuButton, close;
    DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Initialize();
        group=findViewById(R.id.radioGroup);
        serviceGroup = findViewById(R.id.serviceGroup);
        serviceText = findViewById(R.id.serviceText);
        menuButton = findViewById(R.id.menuButton);
        close = findViewById(R.id.close);
        drawerLayout = findViewById(R.id.drawer_layout);
        clickHome = findViewById(R.id.clickHome);
        clickHistory = findViewById(R.id.clickHistory);
        clickSettings = findViewById(R.id.clickSettings);
        clickLogout = findViewById(R.id.clickLogout);

        RootRef.child("Users").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("name"))
                {
                    group.setVisibility(View.GONE);
                    choiceText.setVisibility(View.GONE);
                    userExist = true;
                    checker = snapshot.child("class").getValue().toString();
                    userName  = snapshot.child("name").getValue().toString();
                    userEmail  = snapshot.child("email").getValue().toString();
                    userPhone  = snapshot.child("phone").getValue().toString();
                    userName.trim();
                    Log.d("user", userName+" "+userEmail+" "+userPhone);
                    subtitle.setText(userName);
                    name.setText(userName);
                    email.setText(userEmail);
                    phone.setText(userPhone);

                }

                if(snapshot.hasChild("image"))
                {
                    Log.d("image", "profile");
                    userImage = snapshot.getValue().toString();
                    GetImage();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!userExist)
                {
                    updateAndCreateUser();

                }
                else
                {
                    updateUser();
                    if(checker.equals("Driver"))
                    {
                        sendToDriverMapActivity();
                    }
                    else {
                        sendToCustomerMapActivity();
                    }
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                   if(userExist)
                   {
                       if(!TextUtils.isEmpty(name.getText().toString()) && !TextUtils.isEmpty(email.getText().toString()) && !TextUtils.isEmpty(phone.getText().toString()))
                       {
                           Intent intent=new Intent(Intent.ACTION_PICK);
                           intent.setType("image/*");
                           startActivityForResult(intent,Gallery_code);
                       }
                       else
                       {
                            if(!TextUtils.isEmpty(name.getText().toString()))
                            {
                                name.setError("required");
                            }
                            if(!TextUtils.isEmpty(email.getText().toString()))
                            {
                                phone.setError("required");
                            }
                            if(!TextUtils.isEmpty(phone.getText().toString()))
                            {
                                email.setError("required");
                            }
                       }
                   }
                   else
                   {
                       Intent intent=new Intent(Intent.ACTION_PICK);
                       intent.setType("image/*");
                       startActivityForResult(intent,Gallery_code);
                   }
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
                Intent intent = null;
                if(checker.equals("Driver"))
                {
                    intent = new Intent(Profile.this, DriverMapActivity.class);
                }
                else if(checker.equals("Customer"))
                {
                    intent = new Intent(Profile.this, CustomerMapActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        clickHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, HistoryActivity.class);
                Log.d("checker",": "+checker+"s");
                intent.putExtra("checker",checker+"s");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        clickSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  recreate();
            }
        });

        clickLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);

                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        DriverMapActivity d = new DriverMapActivity();
                        d.disconnectDriver();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(Profile.this, Login.class));
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


    private void sendToCustomerMapActivity() {
        startActivity(new Intent(Profile.this,CustomerMapActivity.class));
        Animatoo.animateCard(Profile.this);
    }

    private void sendToDriverMapActivity() {
        startActivity(new Intent(Profile.this, DriverMapActivity.class));
        Animatoo.animateCard(Profile.this);
    }


    private void updateUser() {

        String userName=name.getText().toString();
        String userEmail=email.getText().toString();
        String userPhone=phone.getText().toString();


        if(TextUtils.isEmpty(userName) && TextUtils.isEmpty(userPhone) && TextUtils.isEmpty(userEmail))
        {
            name.setError("required");
            email.setError("required");
            phone.setError("required");
        }

        if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userPhone) && !TextUtils.isEmpty(userEmail))
        {
            Map<String , Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUser);
            profileMap.put("name",userName);
            profileMap.put("email", userEmail);
            profileMap.put("phone", userPhone);
            profileMap.put("class", checker);
            if(userImage!=null)
            {
                profileMap.put("image", currentUser);
            }

            progressDialog.setTitle("Update");
            progressDialog.setMessage("Please wait while we update your data");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            RootRef.child("Users").child(currentUser).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        if(checker.equals("Driver"))
                        {
                            RootRef.child("Drivers").child(currentUser).updateChildren(profileMap);
                        }
                        else
                        {
                            RootRef.child("Customers").child(currentUser).updateChildren(profileMap);
                        }
                        progressDialog.cancel();
                        Toast.makeText(getApplicationContext(), "Data Updated", Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        String message=task.getException().getLocalizedMessage();
                        Toast.makeText(getApplicationContext(), "Error : "+message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


    private void Initialize() {
        auth= FirebaseAuth.getInstance();
        currentUser=auth.getCurrentUser().getUid();
        progressDialog=new ProgressDialog(this);
        RootRef= FirebaseDatabase.getInstance().getReference();
        name=findViewById(R.id.name);
        email=findViewById(R.id.emailid);
        phone=findViewById(R.id.phone);
        update=findViewById(R.id.update);
        choiceText = findViewById(R.id.choiceText);
        subtitle = findViewById(R.id.subtitle);
        profileImage  = findViewById(R.id.profileIcon);
        storageReference=FirebaseStorage.getInstance().getReference().child("Profile Images/"+currentUser+".jpg");
        UserProfileImg= FirebaseStorage.getInstance().getReference().child("Profile Images");

    }

    private void updateAndCreateUser() {
        String userName=name.getText().toString();
        String userEmail=email.getText().toString();
        String userPhone=phone.getText().toString();
        int id=group.getCheckedRadioButtonId();
        radioButton=findViewById(id);
        if(radioButton!=null)
        {
            checker=radioButton.getText().toString();
        }

        if(TextUtils.isEmpty(userName))
        {
            name.setError("required");

        }
        if(TextUtils.isEmpty(userEmail))
        {
            email.setError("required");

        }
        if(TextUtils.isEmpty(userPhone))
        {
            phone.setError("required");

        }
        if(TextUtils.isEmpty(checker))
        {
            Toast.makeText(Profile.this, "Please choose the class i.e, customer or driver", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(userName) && TextUtils.isEmpty(userPhone) && TextUtils.isEmpty(userEmail))
        {
            name.setError("required");
            email.setError("required");
            phone.setError("required");
        }



        if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userPhone) && !TextUtils.isEmpty(userEmail))
        {
            HashMap<String , String> profileMap = new HashMap<>();
            profileMap.put("uid",currentUser);
            profileMap.put("name",userName);
            profileMap.put("email", userEmail);
            profileMap.put("phone", userPhone);
            profileMap.put("class", checker);

            progressDialog.setTitle("Update");
            progressDialog.setMessage("Please wait while we update your data");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            RootRef.child("Users").child(currentUser).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        if(checker.equals("Driver"))
                        {
                            RootRef.child("Drivers").child(currentUser).setValue(profileMap);
                            gotoNextActivity();
                    }
                    else
                    {
                        RootRef.child("Customers").child(currentUser).setValue(profileMap);
                        gotoMainActivity();
                    }
                    progressDialog.cancel();
                    Toast.makeText(getApplicationContext(), "Data Updated", Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        String message=task.getException().getLocalizedMessage();
                        Toast.makeText(getApplicationContext(), "Error : "+message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void gotoNextActivity() {
        Intent intent =new Intent(Profile.this, ServiceActivity.class);
        startActivity(intent);
    }

    private void gotoMainActivity() {
        Intent intent =new Intent(Profile.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Gallery_code && resultCode==RESULT_OK && data!=null)
        {
            CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
        }

            if(requestCode == RESULT_OK)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK)
                {

                    resulUri = result.getUri();
                    profileImage.setImageURI(resulUri);
                    StorageReference filePath = UserProfileImg.child(currentUser + ".jpg");
                    filePath.putFile(resulUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.cancel();
                            GetImage();
                            RootRef.child("Users").child(currentUser).child("image").setValue(currentUser);
                            if (checker.equals("Driver")) {
                                RootRef.child("Drivers").child(currentUser).child("image").setValue(currentUser);
                            } else {
                                RootRef.child("Customers").child(currentUser).child("image").setValue(currentUser);
                            }
                            Toast.makeText(getApplicationContext(), "Profile Image Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

    }

    private void GetImage() {
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(profileImage);
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        progressDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }

}

