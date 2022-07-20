package com.example.android.ola_clone.LoginandSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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

public class LoginByEmail extends AppCompatActivity {
    TextView register;
    EditText inputemail;
    EditText inputpassword;
    TextView forgotpassword;
    TextView tohome;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    Button login;
    DatabaseReference rootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_by_email);
        inputemail= findViewById(R.id.login_email);
        inputpassword=findViewById(R.id.login_password);
        register=findViewById(R.id.register);
        login = findViewById(R.id.login_button);
        forgotpassword=findViewById(R.id.forgot_password);
        tohome=findViewById(R.id.home_screen);
        mAuth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();
        mUser= mAuth.getCurrentUser();


        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();


        if(networkInfo == null)
        {
            Intent intent = new Intent(LoginByEmail.this, NoInternet.class);
            startActivity(intent);
            intent.putExtra("context","loginbyemail");
            finish();
            Animatoo.animateSlideLeft(this);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    giveAccess();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginByEmail.this,SignUp.class);
                startActivity(intent);
            }
        });


        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginByEmail.this, ForgotPassword.class));
            }
        });

        tohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginByEmail.this, Login.class));
            }
        });

        if(mAuth.getCurrentUser() != null) {
            sendUserToNextActivity();
        }


    }

    private void giveAccess() {
        String email = inputemail.getText().toString().trim();
        String password = inputpassword.getText().toString();
        if (!email.matches(emailPattern)) {
            inputemail.setError("please enter correct email");
        }

        else if (password.isEmpty() || password.length() < 6) {
            inputpassword.setError("password must contain atleast 6 characters");
        }

        else {
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    boolean check = !task.getResult().getSignInMethods().isEmpty();
                    if(!check)
                    {
                        Toast.makeText(LoginByEmail.this, "account doesn't exist please create one", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {

                        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    String currrentUserId=mAuth.getCurrentUser().getUid();

                                    sendUserToNextActivity();
                                    Toast.makeText(LoginByEmail.this, "signed in",Toast.LENGTH_SHORT).show();

                                }
                                else
                                {

                                    Toast.makeText(LoginByEmail.this, "incorrect email or password",Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                }
            });
        }
    }

    private void sendUserToNextActivity() {
        Intent intent= new Intent(LoginByEmail.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


//    public boolean isInternetAvailable(Context context)
//    {
//        NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
//                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
//
//        if (info == null)
//        {
//            Toast.makeText(LoginByEmail.this, "no internet connection",Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        else
//        {
//            if(info.isConnected())
//            {
//                // Toast.makeText(RegisterActivity.this, "connection established",Toast.LENGTH_SHORT).show();
//                return true;
//            }
//            else
//            {
//                Toast.makeText(LoginByEmail.this, "internet connection",Toast.LENGTH_SHORT).show();
//                return true;
//            }
//
//        }
//    }

}
