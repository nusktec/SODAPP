package com.nsc.sodapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Testimony extends AppCompatActivity {
    Dialog dialog;

    Context context;

    FirebaseDatabase fb = FirebaseDatabase.getInstance();
    DatabaseReference myref = fb.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testimony);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(Color.WHITE);


        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        myChildToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        myChildToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Preparing your testimony", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                giveTestimony();
            }
        });

        context = this;

        loadTestimony();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.testimony_menu, menu);
        return true;
    }


    ArrayList<String> arr_testimony = new ArrayList<>();
    ArrayList<String> arr_name = new ArrayList<>();
    ArrayList<String> arr_date = new ArrayList<>();

    boolean testimony_exist = false;

    //load new testimony
    public void loadTestimony(){
        myref.child("testimonies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if((boolean)snapshot.child("approval").getValue()==true){
                        arr_name.add(snapshot.getKey());
                        arr_testimony.add(snapshot.child("content").getValue().toString());
                        arr_date.add(snapshot.child("date").getValue().toString());

                        testimony_exist = true;
                    }
                }

                if(testimony_exist){
                    //List array
                    final String[] testinony = new String[arr_testimony.size()];
                    final String[] name = new String[arr_name.size()];
                    final String[] date = new String[arr_date.size()];

                    arr_testimony.toArray(testinony);
                    arr_name.toArray(name);
                    arr_date.toArray(date);

                    Activity activity = (Activity) context;

                   TestimonyAdapter testimonyAdapter = new TestimonyAdapter(activity,name,testinony,date);

                    ((ListView) findViewById(R.id.testimony_list)).setAdapter(testimonyAdapter);

                }else{
                    Toast.makeText(context, "No approved testimony yet !\nBe the first to give",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    //Give testimony
    public void giveTestimony(){
        final EditText test_content;
        final EditText  test_name;

        dialog = new Dialog(this);
        //dialog.setCancelable(false);
        dialog.setContentView(R.layout.new_testimony);
        dialog.setTitle("New Testimony");
        dialog.setCancelable(false);
        Window win = dialog.getWindow();
        win.setLayout(DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.WRAP_CONTENT);


        test_name = ((EditText) dialog.findViewById(R.id.new_testi_name));
        test_content = ((EditText) dialog.findViewById(R.id.new_testi_content));


        dialog.findViewById(R.id.cancel_btn_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        ((AppCompatButton) dialog.findViewById(R.id.submit_new_testi)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(test_content.getText().toString().equals("")||test_name.getText().toString().equals("")){
                    YoYo.with(Techniques.Shake).duration(200).repeat(0).playOn(v);
                    return;
                }

                //Submit.....
                myref.child("testimonies").child(test_name.getText().toString()).child("content").setValue(test_content.getText().toString());
                myref.child("testimonies").child(test_name.getText().toString()).child("date").setValue(DateFormat.getDateTimeInstance().format(new Date()));
                myref.child("testimonies").child(test_name.getText().toString()).child("approval").setValue(false);


                myref.child("testimonies").child(test_name.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        AlertDialog.Builder alt = new AlertDialog.Builder(dialog.getContext());
                        alt.setCancelable(false);
                        alt.setIcon(R.mipmap.ic_launcher);
                        alt.setTitle("Testimony");
                        alt.setMessage("Testimony has been submitted for approval. thank you and remain bless");

                        alt.setPositiveButton("Amen !", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alt.create();
                        alt.show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        dialog.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.write_testimony){
            giveTestimony();
        }
        return super.onOptionsItemSelected(item);
    }
}
