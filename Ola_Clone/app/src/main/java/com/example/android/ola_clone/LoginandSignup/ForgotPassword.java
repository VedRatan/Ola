package com.example.android.ola_clone.LoginandSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ola_clone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.paypal.android.sdk.payments.LoginActivity;

public class ForgotPassword extends AppCompatActivity {
    private Button sendMail;
    private EditText mail;
    private String email;
    private TextView backtologin;
    private FirebaseAuth mAuth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mAuth=FirebaseAuth.getInstance();
        backtologin=findViewById(R.id.tologinpage);
        mail=findViewById(R.id.email);
        sendMail=findViewById(R.id.getmail);

        backtologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPassword.this,LoginByEmail.class));
            }
        });

        sendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePassword();
            }
        });
    }

    private void validatePassword() {

        email=mail.getText().toString();
        if(email.isEmpty())
            mail.setError("required field");
        else if(!email.matches(emailPattern))
        {
            mail.setError("please enter correct email");
        }
        else
        {

            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    boolean check = !task.getResult().getSignInMethods().isEmpty();
                    if(!check)
                    {
                        Toast.makeText(ForgotPassword.this, "Account doesn't exists try with different mail id", Toast.LENGTH_SHORT).show();

                    }

                    else {
                        sendResetPassMail();
                    }

                }
            });
        }
    }

    private void sendResetPassMail() {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(ForgotPassword.this, "please check your mail inbox", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ForgotPassword.this,LoginByEmail.class));
                    finish();
                }
                else
                {
                    Toast.makeText(ForgotPassword.this, "sorry, unable to send the reset mail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}