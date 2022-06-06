package com.abdosalm.serachengine;

import static com.abdosalm.serachengine.Constants.Constants.INTENT_IS_ADMIN;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityLogin extends AppCompatActivity {
    private Button signInButton;
    private Button logInButton;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = findViewById(R.id.signInButton);
        logInButton = findViewById(R.id.logInButton);
        userNameEditText = findViewById(R.id.userNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            goToNextActivity(true);
        }

        signInButton.setOnClickListener(v->{
            goToNextActivity(false);
        });

        logInButton.setOnClickListener(v->{
            String email = userNameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (!email.equals("") && !password.equals("")){
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        goToNextActivity(true);
                    else
                    {
                        Toast.makeText(ActivityLogin.this, "please enter a correct admin data", Toast.LENGTH_LONG).show();
                        userNameEditText.setText("");
                        passwordEditText.setText("");
                    }
                });
            }
        });

    }
    private void goToNextActivity(boolean isAdmin){
        Intent intent = new Intent(ActivityLogin.this, MainActivity.class);
        intent.putExtra(INTENT_IS_ADMIN,isAdmin);
        startActivity(intent);
        finish();
    }
}