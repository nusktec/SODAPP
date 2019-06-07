package com.nsc.sodapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class HelpSOD extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_sod);
    }



    //Reset Lock
    public void resetLock(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("ADV", MODE_PRIVATE);
        sharedPreferences.edit().putInt("coin",0).commit();
        Toast.makeText(this,"Token Accepted !",Toast.LENGTH_SHORT).show();
    }


    //I will help
    public void iWillHelp(View v){
        //First reset.............
        resetLock();

        //Perform others
        showAccount();
    }


    //I dont want to help
    public void iDontWant(View v){
        resetLock();
        Toast.makeText(this,"Consider helping and God bless you",Toast.LENGTH_SHORT).show();
    }

    //I have
    public void iHave(View v){
        resetLock();
        Toast.makeText(this,"Thanks for your giving, God bless you",Toast.LENGTH_SHORT).show();
    }

    //Show account details
    String accountUBA = "Bank: UBA\nAcc No.:2058779163\nName: Ameh Friday Onuche\nType: Savings\n";
    String accountZENITH = "Bank: ZENITH\nAcc No.:2177798463\nName: Ameh Friday Onuche\nType: Savings\n";

    public void showAccount(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Developer Account");
        alertDialog.setMessage("Which of the bank do you preferred ?");
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton("UBA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    setAccout(accountUBA,"UBA Account",alertDialog.getContext());
            }
        });
        alertDialog.setNegativeButton("Zenith", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setAccout(accountZENITH,"Zenith Account", alertDialog.getContext());
            }
        });
        alertDialog.create().show();
    }


    public void setAccout(final String acc, String title, Context c){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);
        alertDialog.setTitle(title);
        alertDialog.setMessage(acc);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Copy To DialPad", new DialogInterface.OnClickListener(){
            @Override
                    public void onClick(DialogInterface dialog, int which){
                    //Copy to dialpad filter list
                String acc_no = acc.split("\n")[1].split(":")[1];
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+acc_no));
                startActivity(intent);
            }
        });


        //set negative
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.create().show();
    }
}
