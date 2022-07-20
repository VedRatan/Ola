package com.example.android.ola_clone.LoginandSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.android.ola_clone.MainActivity;
import com.example.android.ola_clone.NoInternet;
import com.example.android.ola_clone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    EditText inputemail;
    EditText inputpassword;
    EditText confirmpassword;
    Button signup;
    TextView alreadyhavingaccount;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference rootRef;
    String checker;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        inputemail=findViewById(R.id.register_email);
        inputpassword=findViewById(R.id.password);
        confirmpassword=findViewById(R.id.confirm_password);
        signup=findViewById(R.id.signup_button);
        alreadyhavingaccount=findViewById(R.id.back_to_login);
        mAuth=FirebaseAuth.getInstance();
        mUser= mAuth.getCurrentUser();
        rootRef= FirebaseDatabase.getInstance().getReference();
        checker=getIntent().getStringExtra("checker");

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();


        if(networkInfo == null)
        {
            Intent intent = new Intent(SignUp.this, NoInternet.class);
            intent.putExtra("context","main");
            startActivity(intent);
            intent.putExtra("context","signup");
            finish();
            Animatoo.animateSlideLeft(this);
        }

        alreadyhavingaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, LoginByEmail.class);
                intent.putExtra("checker",checker);
                startActivity(intent);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInternetAvailable(SignUp.this))
                    authenticate();
            }
        });
    }

    private void authenticate() {
        String email=inputemail.getText().toString().trim();
        String password=inputpassword.getText().toString();
        String confirmation= confirmpassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            inputemail.setError("Please enter email id");
        }

        if(TextUtils.isEmpty(password))
        {
            inputpassword.setError("Please enter password");
        }
        if(TextUtils.isEmpty(email))
        {
            confirmpassword.setError("required");
        }
        if(TextUtils.isEmpty(email)&&TextUtils.isEmpty(password)&&TextUtils.isEmpty(email))
        {
            inputemail.setError("Please enter email id");
            inputpassword.setError("Please enter password");
            confirmpassword.setError("required");
        }
        if(!email.matches(emailPattern))
        {
            inputemail.setError("please enter correct email");
        }
        else if(password.isEmpty() || password.length()<6)
        {
            inputpassword.setError("password must contain atleast 6 characters");
        }
        else if(!password.equals(confirmation))
        {
            confirmpassword.setError("password doesn't match");
        }
        else
        {
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    boolean check = task.getResult().getSignInMethods().isEmpty();
                    if(check)
                    {
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    String currrentUserId=mAuth.getCurrentUser().getUid();
                                    rootRef.child("Users").child(currrentUserId).setValue("");
                                    sendUserToNextActivity();
                                    Toast.makeText(SignUp.this, "Signed Up Successfully",Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(SignUp.this, ""+task.getException(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(SignUp.this, "Account already exists",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }


    private void sendUserToNextActivity() {
        Intent intent = new Intent(SignUp.this, MainActivity.class);
        intent.putExtra("checker",checker);
        startActivity(intent);

        //This will stop to come back to this activity when user successfully signs up
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public boolean isInternetAvailable(Context context)
    {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null)
        {
            Toast.makeText(SignUp.this, "no internet connection",Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            if(info.isConnected())
            {
                // Toast.makeText(RegisterActivity.this, "connection established",Toast.LENGTH_SHORT).show();
                return true;
            }
            else
            {
                Toast.makeText(SignUp.this, "internet connection",Toast.LENGTH_SHORT).show();
                return true;
            }

        }
    }
}