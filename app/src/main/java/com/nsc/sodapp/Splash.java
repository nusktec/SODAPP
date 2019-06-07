package com.nsc.sodapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class Splash extends AppCompatActivity {
    public String db_table_create = "CREATE TABLE IF NOT EXISTS SOD(ID INTEGER PRIMARY KEY AUTOINCREMENT,PHONE VARCHAR,BAL VARCHAR);";
    private Dialog popup;
    public SQLiteDatabase db;
    public File path;
    public JSONArray user_js;
    public String pub_host = "http://dunamisgospel.org";
    public String global_dat;
    private ProgressDialog pd;

    private int ti = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final Animation ani = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_udwn);
        //findViewById(R.id.splash_logo).startAnimation(ani);

       YoYo.with(Techniques.BounceInUp).repeat(1).duration(1000).playOn(findViewById(R.id.welcom_animation));


        pd = new ProgressDialog(this);

        path = new File(Environment.getExternalStorageDirectory(),"Android"+File.separator+"Data"+File.separator+"SOD Data");
        path.mkdir();

        create_db();

        ani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.splash_text).setVisibility(View.VISIBLE);
                findViewById(R.id.splash_text).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null&&cm.getActiveNetworkInfo().isConnected();
    }

    public void msg(String msg){
        AlertDialog.Builder m = new AlertDialog.Builder(this);
        m.setTitle("Warning !");
        m.setMessage(msg);
        m.setCancelable(false);
        m.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        m.show();
    }

    public void chk_db_fin(){
        Cursor c = db.rawQuery("SELECT * FROM SOD_RD",null);
        if(c.getCount()>=29){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getBaseContext(), Home.class));
                    finish();
                }
            }, 5000);

        }else{
            if(isConnected()){
                getSOD();
            }else {
                msg("You are not connected to the internet !\nPut on mobile data and try again !");
            }
        }
    }

    public void getSOD(){
        final AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        pd.setCancelable(false);
        pd.setTitle("Downloading SOD Data");

        params.put("nscteq-fin","nsc");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Thread.currentThread().interrupt();

                client.post(getApplicationContext(), pub_host+"/mobile-sod/read.php", params, new JsonHttpResponseHandler(){

                    @Override
                    public void onStart() {
                        pd.setMessage("Please wait, While the System is Initializing...\n\nNote: after successful installations, restart your phone to enable daily notifications");
                        pd.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if(keyCode==event.KEYCODE_BACK){
                                    finish();
                                }
                                return false;
                            }
                        });
                        pd.show();
                    }


                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        pd.setMessage("Getting SOD's Content...0/31");
                        pd.setMax(31);
                        downloaded_sod(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        pd.dismiss();
                        msg("Unable to Get SOD Data...\nCheck you have a working data plan and try again !");
                    }
                });

            }
        });
    }

    public void downloaded_sod(JSONObject jsonObject){
        try{
            JSONArray jsonArray =  new JSONArray(jsonObject.getString("rd"));
            for (int i=jsonArray.length()-1; i>-1;i--){

                JSONObject js = jsonArray.getJSONObject(i);
                String date = js.getString("created").split(" ")[0];
                String title = new String(Base64.decode(js.getString("title"),Base64.DEFAULT));
                String msg = new String(Base64.decode(js.getString("msg"),Base64.DEFAULT));

                ContentValues cv = new ContentValues();
                cv.put("SOD", msg);
                cv.put("TITLE", title);
                cv.put("DATE", date);

                db.insert("SOD_RD", null, cv);

            }

          Cursor ck = db.rawQuery("SELECT * FROM SOD_RD",null);
            if(ck.getCount()>20){
                Toast.makeText(getApplicationContext(),"Content Downloaded !",Toast.LENGTH_LONG).show();
                pd.dismiss();
                chk_db_fin();
            }else {
                db.execSQL("DROP TABLE SOD_RD");
                msg("Sorry, SOD Content is incomplete...try again !\nWeak network might cause this...");
            }

        }catch(Exception ex){
            Log.e("DOWNL:ERROR", ex.getMessage().toString());
            msg("An error has occur during downloading...please restart the App with stable network !");
            db.execSQL("DROP TABLE SOD_RD");
        }
    }

    public void create_db(){
        db = openOrCreateDatabase(getApplicationContext().getFilesDir()+"/SOD.DB", SQLiteDatabase.OPEN_READWRITE,null);
        try{
            db.execSQL(db_table_create);
            db.execSQL("CREATE TABLE IF NOT EXISTS SOD_RD(ID INTEGER PRIMARY KEY AUTOINCREMENT, SOD TEXT,TITLE VARCHAR, DATE VARCHAR);");

        }catch (SQLiteException ex){

            msg("An error occur during SOD database creation !\nMake sure you have sufficient space about 5mb then retry !");

            return;}chk_db_fin();
    }

}
