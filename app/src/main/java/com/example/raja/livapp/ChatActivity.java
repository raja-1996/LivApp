package com.example.raja.livapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

import static com.example.raja.livapp.R.id.name;


class Message {

    String userId;
    String textMessage;
    String time;

    Message() {

    }

    Message(String id,String message,String time) {

        this.userId = id;
        this.textMessage = message;
        this.time = time;
    }

}


public class ChatActivity extends AppCompatActivity {


    private DatabaseReference mDataBase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    SQLiteDatabase myDataBase;
    String sql;
    SQLiteStatement statement;

    String userId;

    EmojiconEditText editText;
    EmojiconTextView textView;
    ImageView emoji,send;
    View rootView;
    EmojIconActions emojIconActions;

    ArrayList<String> userIds;
    ArrayList<String> messages;
    ArrayList<String> time;
    MessageAdapter messageAdapter ;
    ListView list;

    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mDataBase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        Intent in = getIntent();
        userId = in.getExtras().getString("userId");


        rootView = (View) findViewById(R.id.activity_chat);
        editText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        emoji = (ImageView) findViewById(R.id.emoji);
        send = (ImageView) findViewById(R.id.send);
        list = (ListView) findViewById(R.id.list);

        userIds = new ArrayList<String>();
        messages = new ArrayList<String >();
        time = new ArrayList<String>();

        mProgress = new ProgressDialog(ChatActivity.this);

        userIds.clear();
        messages.clear();
        time.clear();
        myDataBase = this.openOrCreateDatabase("CHATS", MODE_PRIVATE, null);
        myDataBase.execSQL("CREATE TABLE IF NOT EXISTS chats (id INTEGER PRIMARY KEY, userTable VARCHAR , chatsTable VARCHAR , timeTable VARCHAR) ");

        messageAdapter = new MessageAdapter(ChatActivity.this,userIds,messages,time);
        list.setAdapter(messageAdapter);

        emojIconActions = new EmojIconActions(ChatActivity.this,rootView,editText,emoji);
        emojIconActions.ShowEmojIcon();

        mDataBase.child("Users").child(userId).child("status").setValue("Online");

        sql = "INSERT INTO  chats (userTable, chatsTable, timeTable) VALUES (?, ?, ?) ";
        statement = myDataBase.compileStatement(sql);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                if(charSequence.length()>0) {
                    mDataBase.child("Users").child(userId).child("status").setValue("typing...");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDataBase.child("Users").child(userId).child("status").setValue("Online");

                final String text = editText.getText().toString();
                editText.setText("");

                DatabaseReference mChat = mDataBase.child("Users").child(userId);

                mChat.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String chatId = dataSnapshot.child("id").getValue().toString();

                        Calendar c = Calendar.getInstance();
                        int h = c.get(Calendar.HOUR);
                        int m = c.get(Calendar.MINUTE);
                        String s1 = Integer.toString(h),s2 = Integer.toString(m);
                        if(m<9){
                            s2 = '0' + Integer.toString(m);
                        }
                        if(h<9) {
                            s1 = '0' + Integer.toString(h);
                        }

                        String time = s1 + ":" + s2;

                        Message message = new Message(userId,text,time);

                        mDataBase.child("Chats").child(chatId).push().setValue(message);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        updateListView();

        DownloadTask task = new DownloadTask();
        task.execute("");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.imageDisplay) {

            Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(in, 2);


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 2 && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            mProgress.setMessage("Uploading Image");
            mProgress.show();

            try {

                StorageReference filepath = mStorage.child("Photos").child(uri.getLastPathSegment());
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        mProgress.dismiss();

                        Toast.makeText(ChatActivity.this, "Uploading Done", Toast.LENGTH_LONG).show();

                        String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        Log.i("download", taskSnapshot.getDownloadUrl().toString());

                        Calendar c = Calendar.getInstance();
                        int h = c.get(Calendar.HOUR);
                        int m = c.get(Calendar.MINUTE);
                        String s1 = Integer.toString(h),s2 = Integer.toString(m);
                        if(m<9){
                            s2 = '0' + Integer.toString(m);
                        }
                        if(h<9) {
                            s1 = '0' + Integer.toString(h);
                        }

                        String time = s1 + ":" + s2;

                        final Message messageText = new Message(userId, downloadUrl,time);

                        DatabaseReference mdb = mDataBase.child("Users");

                        mdb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot data : dataSnapshot.getChildren()) {

                                    if (data.getKey().equals(userId)) {

                                        String chatId = data.child("id").getValue().toString();
                                        mDataBase.child("Chats").child(chatId).push().setValue(messageText);

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mProgress.dismiss();
                        Toast.makeText(ChatActivity.this, "Uploading Failed", Toast.LENGTH_LONG).show();
                    }
                });


            } catch (Exception e) {

                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }


        }
    }

    public void updateListView() {

        try {

            Cursor c = myDataBase.rawQuery("SELECT * from chats", null);

            int chatIndex = c.getColumnIndex("userTable");
            int messageIndex = c.getColumnIndex("chatsTable");
            int timeIndex = c.getColumnIndex("timeTable");

            c.moveToFirst();

            userIds.clear();
            messages.clear();
            time.clear();

            while (c != null) {

                userIds.add(c.getString(chatIndex));
                messages.add(c.getString(messageIndex));
                time.add(c.getString(timeIndex));

                c.moveToNext();
            }

            messageAdapter.notifyDataSetChanged();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }


    public class DownloadTask extends AsyncTask<String ,Void,String > {

        @Override
        protected String doInBackground(String... strings) {

            myDataBase.execSQL("DELETE FROM chats");

            DatabaseReference mref = mDataBase.child("Users").child(userId);

            mref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    final String chatId = dataSnapshot.child("id").getValue().toString();

                    Query mQuery = mDataBase.child("Users").orderByChild("id").equalTo(chatId);
                    mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot partner : dataSnapshot.getChildren()) {

                                if(!partner.getKey().equals(userId)) {

                                    String partnerId = partner.child("id").getValue().toString();
                                    android.support.v7.app.ActionBar ab = getSupportActionBar();
                                    ab.setTitle(partner.child("name").getValue().toString());

                                    try{
                                        if(partner.child("status").getValue().toString() != null) {
                                            ab.setSubtitle(partner.child("status").getValue().toString());
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }




                                    final DatabaseReference mPartner  = mDataBase.child("Users").child(partnerId);
                                    mPartner.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                            android.support.v7.app.ActionBar ab = getSupportActionBar();
                                            ab.setSubtitle(dataSnapshot.getValue().toString());

                                        }

                                        @Override
                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    final DatabaseReference mchat = mDataBase.child("Chats").child(chatId);
                    mchat.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            String user = dataSnapshot.child("userId").getValue().toString();
                            String text = dataSnapshot.child("textMessage").getValue().toString();
                            String t = dataSnapshot.child("time").getValue().toString();

                            if(user.equals(userId)) {
                                userIds.add("1");
                                statement.bindString(1,"1");
                            } else {
                                userIds.add("0");
                                statement.bindString(1,"0");
                            }



                            messages.add(text);
                            statement.bindString(2,text);

                            time.add(t);
                            statement.bindString(3,t);

                            messageAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            return null;
        }
    }



    @Override
    protected void onPause() {
        super.onPause();

        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR);
        int m = c.get(Calendar.MINUTE);
        String s1 = Integer.toString(h),s2 = Integer.toString(m);
        if(m<9){
            s2 = '0' + Integer.toString(m);
        }
        if(h<9) {
            s1 = '0' + Integer.toString(h);
        }

        String time = s1 + ":" + s2;

        mDataBase.child("Users").child(userId).child("status").setValue("Last seen at " + time);

    }
}

class  Holder {

    TextView text1;
    TextView text2;
    TextView time1;
    TextView time2;

    ImageView image1;
    ImageView image2;

}


class MessageAdapter extends ArrayAdapter<String> {


    Context context;
    ArrayList<String > userIds;
    ArrayList<String > messages;
    ArrayList<String > time;


    public MessageAdapter(Context context, ArrayList<String > userIds,ArrayList<String > messages, ArrayList<String > time) {
        super(context, R.layout.simple_row,messages);

        this.context = context;
        this.userIds = userIds;
        this.messages = messages;
        this.time     = time;


        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Holder holder;

        if(convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_row, parent, false);

            holder = new Holder();

            holder.text1 = (TextView) convertView.findViewById(R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(R.id.text2);
            holder.time1 = (TextView) convertView.findViewById(R.id.time1);
            holder.time2 = (TextView) convertView.findViewById(R.id.time2);

            holder.image1 = (ImageView) convertView.findViewById(R.id.image1);
            holder.image2 = (ImageView) convertView.findViewById(R.id.image2);

            convertView.setTag(holder);

        } else {

            holder = (Holder) convertView.getTag();

        }

        holder.text1.setVisibility(View.GONE);
        holder.text2.setVisibility(View.GONE);
        holder.time1.setVisibility(View.GONE);
        holder.time2.setVisibility(View.GONE);
        holder.image1.setVisibility(View.GONE);
        holder.image2.setVisibility(View.GONE);

        if(userIds.get(position).equals("1")) {

            if (isURL(messages.get(position))) {

                holder.image2.setVisibility(View.VISIBLE);
                holder.time2.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(messages.get(position), holder.image2);
                holder.time2.setText(time.get(position));


            } else {
                holder.time2.setVisibility(View.VISIBLE);
                holder.text2.setVisibility(View.VISIBLE);
                holder.text2.setText(messages.get(position)+"\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0");
                holder.time2.setText(time.get(position));
            }

        } else {

            if(isURL(messages.get(position))) {

                holder.image1.setVisibility(View.VISIBLE);
                holder.time1.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(messages.get(position), holder.image1);
                holder.time1.setText(time.get(position));

            } else {

                holder.time1.setVisibility(View.VISIBLE);
                holder.text1.setVisibility(View.VISIBLE);
                holder.text1.setText(messages.get(position)+"\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0");
                holder.time1.setText(time.get(position));

            }

        }


        return convertView;

    }

    public boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String downloadImage(Bitmap showedImgae) {


        String extr = Environment.getExternalStorageDirectory().toString();
        File mFolder = new File(extr + "/LivApp");

        if (!mFolder.exists()) {
            mFolder.mkdir();
        }

        String strF = mFolder.getAbsolutePath();
        File mSubFolder = new File(strF + "/Pictures");

        if (!mSubFolder.exists()) {
            mSubFolder.mkdir();
        }


        Random r = new Random();
        int n = r.nextInt(10000);
        String s = "myfile" + Integer.toString(n) + ".png";

        File f = new File(mSubFolder.getAbsolutePath(), s);

        String strMyImagePath = f.getAbsolutePath();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            showedImgae.compress(Bitmap.CompressFormat.PNG, 70, fos);

            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }

        return strMyImagePath;
    }
}
