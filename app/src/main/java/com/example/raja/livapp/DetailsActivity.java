package com.example.raja.livapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

public class DetailsActivity extends AppCompatActivity {


    private DatabaseReference mDataBase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    EditText name;
    EditText number;

    Button submit;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mDataBase = FirebaseDatabase.getInstance().getReference();

        Intent in = getIntent();
        userId =   in.getExtras().getString("userId");

        name = (EditText) findViewById(R.id.name);
        number =(EditText) findViewById(R.id.number);
        submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String pName = name.getText().toString();
                String pNumber = number.getText().toString();

                mDataBase.child("Users").child(userId).child("name").setValue(pName);
                mDataBase.child("Users").child(userId).child("number").setValue(pNumber);

                Intent in = new Intent(DetailsActivity.this,RequestActivity.class);
                in.putExtra("userId",userId);
                startActivity(in);
                finish();

            }
        });


    }
}
