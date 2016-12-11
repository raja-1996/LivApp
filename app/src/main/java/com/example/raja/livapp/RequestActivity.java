
package com.example.raja.livapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

public class RequestActivity extends AppCompatActivity {


    private DatabaseReference mDataBase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    EditText partner;

    Button submit;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        Intent in = getIntent();
        userId =  in.getExtras().getString("userId");


        partner = (EditText) findViewById(R.id.partner);
        submit = (Button) findViewById(R.id.submit);

        mDataBase = FirebaseDatabase.getInstance().getReference();

        final DatabaseReference mRef = mDataBase.child("Users").child(userId);



        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String number1 = dataSnapshot.child("number").getValue().toString();
                        String number2 = partner.getText().toString();

                        if(number1.compareTo(number2) < 0) {

                            String temp = number1;
                            number1 = number2;
                            number2 = temp;

                        }

                        mRef.child("id").setValue(number1+number2);

                        Intent ip = new Intent(RequestActivity.this, ChatActivity.class);
                        ip.putExtra("userId",userId);
                        startActivity(ip);
                        finish();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

    }
}
