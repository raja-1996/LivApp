package com.example.raja.livapp;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDataBase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    EditText username;
    EditText password;

    Button signup;
    Button login;

    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDataBase = FirebaseDatabase.getInstance().getReference();
        mAuth     = FirebaseAuth.getInstance();

        username = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);

        signup = (Button) findViewById(R.id.signIn);
        login  = (Button) findViewById(R.id.logIn);

        mProgress = new ProgressDialog(MainActivity.this);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("");

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {


                Intent in = new Intent(MainActivity.this,ChatActivity.class);
                in.putExtra("userId",mAuth.getCurrentUser().getUid().toString());
                startActivity(in);
                finish();

            }
        };

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgress.setMessage("Loading");
                mProgress.show();

                String user = username.getText().toString();
                String pass = password.getText().toString();

                mAuth.createUserWithEmailAndPassword(user,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        mProgress.dismiss();

                        if(task.isSuccessful()) {

                            Intent in = new Intent(MainActivity.this, DetailsActivity.class);
                            in.putExtra("userId",mAuth.getCurrentUser().getUid().toString());
                            startActivity(in);
                            finish();

                        } else {

                            Toast.makeText(MainActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgress.setMessage("Loading");
                mProgress.show();

                String user = username.getText().toString();
                String pass = password.getText().toString();

                mAuth.signInWithEmailAndPassword(user,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {

                            Intent in = new Intent(MainActivity.this,ChatActivity.class);
                            in.putExtra("userId",mAuth.getCurrentUser().getUid().toString());
                            startActivity(in);

                        }

                    }
                });

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthStateListener);
    }
}
