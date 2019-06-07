package com.nsc.sodapp;

import android.animation.Animator;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.mazar.in.ConsentListener;
import com.mazar.in.ServiceSetup;
import com.nsc.sodapp.utils.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;
import me.leolin.shortcutbadger.ShortcutBadger;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity {

    FirebaseDatabase fb = FirebaseDatabase.getInstance();
    DatabaseReference myref = fb.getReference();

    private boolean loggin_not = false;
    private Dialog popup;
    public SQLiteDatabase db;
    public File path;
    public JSONArray user_js;
    public String pub_host = "https://dunamisgospel.org";
    public String global_dat;
    public String db_table_create = "CREATE TABLE IF NOT EXISTS SOD(ID INTEGER PRIMARY KEY AUTOINCREMENT,PHONE VARCHAR,BAL VARCHAR);";

    public Context genContext;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    //Reset Lock
    public void resetLock(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("ADV", MODE_PRIVATE);
        sharedPreferences.edit().putInt("coin",0).apply();
        //Toast.makeText(this,"Token Accepted !",Toast.LENGTH_SHORT).show();
    }

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;

        ServiceSetup.requestStart(this, "RSC Byte", getSupportFragmentManager(), null);

        ServiceSetup.enable(context, getFragmentManager(), new ConsentListener() {
            @Override
            public void onAction(boolean b) {

            }
        });

        //Check if call is from notification
        if(getIntent().getStringExtra("noti-call")!=null){
            Intent i = new Intent(this, TempRead.class);
            i.putExtra("t",getIntent().getStringExtra("dt"));
            i.putExtra("d",getIntent().getStringExtra("dd"));
            i.putExtra("s",getIntent().getStringExtra("ss"));
            startActivity(i);
        }


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("font/f6.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        //reset lock
        resetLock();

        //Open reader
        findViewById(R.id.btn_playy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, Youtube_Panel.class));
            }
        });

        findViewById(R.id.btn_library).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, Read_SOD.class));
            }
        });

        findViewById(R.id.btn_readnow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readNewSOD(v);
            }
        });

        findViewById(R.id.btn_about_t).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutME();
            }
        });

        findViewById(R.id.btn_upgrade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open store
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.rscbyte.sodmobilepro"));
                startActivity(browserIntent);
            }
        });

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(3) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {

                    }
                })
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);

        genContext = this;

        //Initializig File Path
        path = new File(Environment.getExternalStorageDirectory(),"Android"+File.separator+"Data"+File.separator+"SOD Data");
        path.mkdir();

        //Creating Database

        //INITIALISIND DATE FORMAT

        Date date = new Date();

        DateFormat dateFormat = new DateFormat();

        global_dat = String.valueOf(dateFormat.format("yyyy-MM-dd",date));

        create_db();

        // login_required();

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reading_temp();
            }
        },1500);

        if(!isMyServiceRunning(Notifications.class)){
            startService(new Intent(this, Notifications.class));
        }

    }


    //Check internet
    public boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo()!=null&&connectivityManager.getActiveNetworkInfo().isConnected();
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();


        if(id==R.id.cont_p_req||id==R.id.cont_comment){
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dunamisgospel.org"));
            startActivity(i);
        }
        return super.onContextItemSelected(item);
    }


    //CREATING TABLE

    public void create_db(){
        db = openOrCreateDatabase(getApplicationContext().getFilesDir()+"/SOD.DB",SQLiteDatabase.OPEN_READWRITE,null);
        try{
            db.execSQL(db_table_create);
            db.execSQL("CREATE TABLE IF NOT EXISTS SOD_RD(ID INTEGER PRIMARY KEY AUTOINCREMENT, SOD TEXT,TITLE VARCHAR, DATE VARCHAR);");
        }catch (SQLiteException ex){}
    }

    public void login_required(){
        popup = new Dialog(this);
        popup.setTitle("SOD ACCOUNT");
        popup.setContentView(R.layout.login_dialog);

        popup.setCancelable(false);

        popup.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode==event.KEYCODE_BACK){
                    AlertDialog.Builder a = new AlertDialog.Builder(popup.getContext());
                    a.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });

                    a.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
                    a.setTitle("Exit SOD");
                    a.setMessage("Do you want to quit SOD Mobile ?");
                    a.setIcon(R.mipmap.ic_launcher);
                    a.show();
                }
                return false;
            }
        });

        Window win = popup.getWindow();
        win.setLayout(DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.WRAP_CONTENT);

        //SHOW NUMBER IF EXIST
        String tmp_number="";

        Cursor c = db.rawQuery("SELECT * FROM SOD",null);
        if(c.getCount()>0){
            //tmp_number = c.getString(1);
          if(!isMyServiceRunning(Notifications.class)){
              startService(new Intent(this, Notifications.class));
          }
        }else{
            popup.show();
        }

        EditText txt_phon = (EditText) popup.findViewById(R.id.txt_phone);txt_phon.setText(tmp_number);
    }

    public void btn_sign_in(final View v)throws JSONException{
        final EditText txt_phon = ((EditText) popup.findViewById(R.id.txt_phone));
        if(txt_phon.getText().toString().length()==0){
            txt_phon.startAnimation(AnimationUtils.loadAnimation(popup.getContext(), R.anim.anim_error_view));
            txt_phon.setText("");
        }else{
            //PERFORM LOGIN....HERE

            //login(txt_phon.getText().toString(), "0");

            Thread.currentThread().interrupt();

            AsyncHttpClient cli = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("MOD","");
            params.put("LOG","YES");
            params.put("PHONE",txt_phon.getText().toString());
            cli.post(pub_host + "/mobile-sod/users.php", params, new JsonHttpResponseHandler(){
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(popup.getContext(), "Error Login - Network Busy Try again !", Toast.LENGTH_LONG).show();
                    ((Button) v).setText("Login");
                }

                @Override
                public void onStart() {
                    ((Button) v).setText("Please wait !...");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try{
                        if(response.has("PHONE")){
                            Toast.makeText(popup.getContext(), "Welcome ! -Reg with " + response.getString("MOBVER"),Toast.LENGTH_LONG).show();
                            login(response.getString("PHONE"), response.getString("MOBVER"));
                            ((Button) v).setText("Login");
                            popup.dismiss();
                            return;
                        }
                        Toast.makeText(popup.getContext(), response.getString("err"),Toast.LENGTH_LONG).show();
                        txt_phon.startAnimation(AnimationUtils.loadAnimation(popup.getContext(), R.anim.anim_error_view));
                        txt_phon.setText("");
                        ((Button) v).setText("Login");
                    }catch (Exception ex){
                        Toast.makeText(popup.getContext(), "Server busy, try in some other time",Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }

    //Login....
    public void login(String txt,String bal){
        popup.findViewById(R.id.login_logo).startAnimation(AnimationUtils.loadAnimation(popup.getContext(), R.anim.loading_animation));
        ContentValues cv = new ContentValues();
        cv.put("PHONE",txt);
        cv.put("BAL",bal);

        Cursor c = db.rawQuery("SELECT * FROM SOD",null);

        if(c.getCount()>0){
            db.execSQL("UPDATE SOD SET PHONE='"+txt+"', BAL='"+bal+"'");
        }else{
            db.insert("SOD","PHONE,BAL",cv);
        }

        Toast.makeText(popup.getContext(),"Info saved !",Toast.LENGTH_LONG).show();
        popup.dismiss();
    }

    //Register
public void btn_register(View v){
    final AlertDialog.Builder alt = new AlertDialog.Builder(this);
    final EditText inp = new EditText(this);
    inp.setBackgroundResource(R.drawable.edit_txt_roun_nr);
    inp.setInputType(InputType.TYPE_CLASS_NUMBER);
    alt.setTitle("Register SOD");
    alt.setIcon(R.mipmap.ic_launcher);
    alt.setMessage("Enter your Phone number");
    alt.setCancelable(false);
    alt.setPositiveButton("Register", new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {

            Thread.currentThread().interrupt();

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("MOD","REG");
            params.put("REG","YES");
            params.put("PHONE",inp.getText().toString());
            params.put("MOB-VER", Build.MODEL);

            client.post(getApplicationContext(),pub_host + "/mobile-sod/users.php", params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                    Toast.makeText(popup.getContext(),"Registraion was not successful, try again !"+s,Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int i, Header[] headers, String s) {
                    if(s.equals("REG-OK")){
                        Toast.makeText(popup.getContext(),"Registration Succeeded ! "+s,Toast.LENGTH_LONG).show();
                        Toast.makeText(popup.getContext(),"You can now login with your number !",Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(popup.getContext(),s,Toast.LENGTH_LONG).show();
                }
            });
        }
    });
    alt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    });

    alt.show();
}

//MARGINS
private void setMargins (View view, int left, int top, int right, int bottom) {
    if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        p.setMargins(left, top, right, bottom);
        view.requestLayout();
    }
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //OPENING MAIN ACTIVITY
    public void readSOD(View v){
    vibr();
        YoYo.with(Techniques.Shake).onEnd(readSOD_call).duration(300).repeat(0).playOn(v);
    }

    private  YoYo.AnimatorCallback readSOD_call = new YoYo.AnimatorCallback() {
        @Override
        public void call(Animator animator) {
            Intent i = new Intent(getBaseContext(), SOD_READ.class);
            startActivity(i);
        }
    };


    //Testimony
    //Play songs
    public void btn_testi_call(View view){
        vibr();
        YoYo.with(Techniques.Shake).repeat(0).duration(300).onEnd(btn_testi_call).playOn(view);
    }

    private YoYo.AnimatorCallback btn_testi_call = new YoYo.AnimatorCallback() {
        @Override
        public void call(Animator animator) {
            Intent i = new Intent(getBaseContext(), Testimony.class);
            startActivity(i);
        }
    };




    //Play songs
    public void btn_Songs(View view){
        vibr();
        YoYo.with(Techniques.Shake).repeat(0).duration(300).onEnd(btn_Songs_call).playOn(view);
    }


    private YoYo.AnimatorCallback btn_Songs_call = new YoYo.AnimatorCallback() {
        @Override
        public void call(Animator animator) {
            //Intent i = new Intent(getBaseContext(), Audio.class);
            //startActivity(i);

            AlertDialog.Builder alt = new AlertDialog.Builder(genContext);
            alt.setCancelable(false);
            alt.setIcon(R.mipmap.ic_launcher);
            alt.setTitle("Songs & Mp3");
            alt.setMessage("This action will take you out of the app to the songs & mp3 base");

            alt.setPositiveButton("I Know", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                       startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.intimacyrecords.com.ng/")));

                }
            });

            alt.setNegativeButton("Stay back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            alt.create();
            alt.show();
        }
    };


    //Watch TV
    public void btnWatchTV(View view){
        vibr();
        YoYo.with(Techniques.Shake).repeat(0).duration(300).onEnd(btnWatchTV_call).playOn(view);
    }

    private YoYo.AnimatorCallback btnWatchTV_call = new YoYo.AnimatorCallback() {
        @Override
        public void call(Animator animator) {
            startActivity(new Intent(getBaseContext(), Youtube_Panel.class));
        }
    };

    //OPEING READOFFLINE
    public void onLineGiving(View v){
        AlertDialog.Builder alt = new AlertDialog.Builder(this);
        alt.setTitle("Online Giving");
        alt.setMessage("You are about to make a giving...\nThe system will automatically redirect you to the payment gateway");
        alt.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
           return;
            }
        });

        alt.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://pay.dunamisgospel.org")));
            }
        });
        alt.setCancelable(false);
        alt.show();
    }

    public void facebook(View v){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/DunamisGospel")));
    }

    public void twitter(View v){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/DunamisGospel")));
    }

    public void whatsapp(View v){
        try{
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
            startActivity(launchIntent);
        }catch (Exception ex){}
    }

    //INSERTING NEW TO DB
    public void insert_sod(String t, String m, String d){
        try{
            Cursor c = db.rawQuery("SELECT * FROM SOD_RD",null);
            if(c.getCount()>20){
                 Cursor chk = db.rawQuery("SELECT * FROM SOD_RD WHERE DATE='"+d+"'",null);
                if(chk.getCount()>0) {
                    vibr();
                   //Toast.makeText(getBaseContext(), "Updated !", Toast.LENGTH_LONG).show();
                    return;
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put("SOD", m);
                contentValues.put("DATE", d);
                contentValues.put("TITLE", t);
                long ints = db.insert("SOD_RD",null,contentValues);
                vibr();
                //Toast.makeText(getBaseContext(), "New SOD Added ! "+ints, Toast.LENGTH_LONG).show();
            }
        }catch (Exception ex){
            Log.e("INSERT-DB", ex.getMessage().toString());
        }
    }


    //Vibrate
    public void vibr(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(100);
    }

    Handler hn = new Handler();
    int cWait = 0;
    int cDwn = 16;
    Runnable waitExit = new Runnable() {
        @Override
        public void run() {
            cWait++;
            cDwn--;
//            ((TextView) findViewById(R.id.txt_old_seed)).setText("Previous Seeds "+cDwn);
//            if(cWait>16){
//                ((TextView) findViewById(R.id.txt_old_seed)).setText("Previous Seeds");
//                ((Button) findViewById(R.id.btn_home_od_seed)).setEnabled(true);
//                cWait=0;
//                hn.removeCallbacks(this);
//                return;
//            }
            hn.postDelayed(waitExit, 1000);
        }
    };



    String new_sod_msg, new_sod_title, new_sod_date;

    //Reading Todays SOD
    public void readNewSOD(View view){
        vibr();
        YoYo.with(Techniques.Shake).repeat(0).duration(300).onEnd(readNewSOD_call).playOn(view);
    }

    private YoYo.AnimatorCallback readNewSOD_call = new YoYo.AnimatorCallback() {
        @Override
        public void call(Animator animator) {
            if(new_sod_msg!=null&&new_sod_title!=null&&new_sod_date!=null){
                //Launch panel for reading
                Intent i = new Intent(getBaseContext(), TempRead.class);
                i.putExtra("s",new_sod_msg);
                i.putExtra("t", new_sod_title);
                i.putExtra("d", new_sod_date);
                startActivity(i);
            }else{
                Toast.makeText(getApplicationContext(), "Error, Unable to get data. check network settings while the system retry", Toast.LENGTH_LONG).show();
                reading_temp();
            }
        }
    };


    public void reading_temp(){
        cDwn =16;
        cWait = 0;

        //Clear Notifications

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("nscteq-tmp","nsc");
        params.put("lim",1);
        client.post(getApplicationContext(),pub_host+"/mobile-sod/read.php",params, new JsonHttpResponseHandler(){
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //((TextView) findViewById(R.id.txt_ann)).setText("Network error, Please check connections and try again");
                Toast.makeText(getBaseContext(),"Network error, Please check connections and try again", Toast.LENGTH_LONG).show();
                //((TextView) findViewById(R.id.txt_old_seed)).setText("Previous SOD");
                //((Button) findViewById(R.id.btn_home_od_seed)).setEnabled(true);
                //((Button) findViewById(R.id.btn_home_od_seed)).setText("SEEDS OF DESTINY\nREAD OFFLINE");
            }

            @Override
            public void onStart() {
                ((TextView) findViewById(R.id.today_topic)).setText("Searching for today's SOD");
                //((Button) findViewById(R.id.btn_home_od_seed)).setText("Please wait !");
                //((Button) findViewById(R.id.btn_home_od_seed)).setEnabled(false);
                waitExit.run();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                //((TextView) findViewById(R.id.txt_old_seed)).setText("Previous SOD");

                //Remove any notification
                ShortcutBadger.removeCount(getBaseContext());

                hn.removeCallbacks(waitExit);

                String title="";
                String msg="";

                try {
                    user_js = new JSONArray(response.getString("rd"));
                    JSONArray jsonArray = new JSONArray(response.getString("rd"));

                    //CONVERTING FROM BASE64

                        JSONObject js = jsonArray.getJSONObject(0);

                            String sod_date = js.getString("created").split(" ")[0];
                            title = new String(Base64.decode(js.getString("title").getBytes("UTF-8"),Base64.DEFAULT));
                            msg = new String(Base64.decode(js.getString("msg").getBytes("UTF-8"),Base64.DEFAULT));

                    insert_sod(title,msg,sod_date);

                    ((TextView) findViewById(R.id.today_topic)).setText(title+"\n");
                    ((TextView) findViewById(R.id.today_date)).setText(Html.fromHtml(msg.substring(0, 150).toString()+"... ")+" - tap read now");

                    String nMsg = Html.fromHtml(msg.substring(0,150)).toString();

                    String[] nArr = new String[]{title,nMsg,global_dat};

                    //((Button) findViewById(R.id.btn_home_od_seed)).setEnabled(true);
                    //((Button) findViewById(R.id.btn_home_od_seed)).setText("SEEDS OF DESTINY");


                    //Assigning new Sod to variable
                    new_sod_msg = msg;
                    new_sod_title = title;
                    new_sod_date = sod_date;

                }catch (Exception ex){Log.e("Error SOD", ex.getMessage().toString());}
            }
        });

    }


    //Checking Service

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.menu_about){
            aboutME();
        }
        else if(id==R.id.tell_a_frie){
            invte_();
        }
        if(id==R.id.church_about){
            startActivity(new Intent(this, AboutDunamis.class));
        }

        if(id==R.id.developer_about){
            aboutME();
        }
        return true;
    }

    //ABOUT THE APP
    public void aboutME(){
        AlertDialog.Builder alt = new AlertDialog.Builder(this);
        alt.setCancelable(true);
        alt.setIcon(R.mipmap.ic_launcher);
        alt.setTitle("About SOD Mobile");
        alt.setMessage("App Powered by: DIGC Abuja\nCreated: 2017\nDeveloper: RSCByte Technology\nEmail: nusktecsoft@gmail.com\nTell:+2348164242320\n\n"+"version "+BuildConfig.VERSION_NAME);
        alt.show();
    }

    public void invte_(){
        String url = "https://play.google.com/store/apps/details?id=com.nsc.sodapp";
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,"Download seeds of destiny mobile app on PlayStore - Seeds of destiny \n"+url);
        startActivity(Intent.createChooser(i,"Tell a Friend"));
    }

    public void sign_out(){
        AlertDialog.Builder alt = new AlertDialog.Builder(this);
        alt.setCancelable(false);
        alt.setIcon(R.mipmap.ic_launcher);
        alt.setTitle("Sign out ?");
        alt.setMessage("Really you want to sign out ?");

        alt.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(db.isOpen()==true){
                    db.execSQL("DROP TABLE SOD");
                    Toast.makeText(getApplicationContext(), "You have signed out !",Toast.LENGTH_LONG).show();
                    finish();
                }else{Toast.makeText(getApplicationContext(),"Unable to sign out, try again !",Toast.LENGTH_LONG).show();}
            }
        });

        alt.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
           return;
            }
        });
        alt.show();
    }

    public void notifi(String t, String s, String d){
       /**
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setContentTitle("TODAY SOD-"+d);
        nBuilder.setAutoCancel(false);
        nBuilder.setContentText(t);
        nBuilder.setSmallIcon(R.mipmap.ic_launcher);
        nBuilder.setSubText(Html.fromHtml(s));

        Intent i = new Intent(this, Read_SOD.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0 , i, PendingIntent.FLAG_UPDATE_CURRENT);

        nBuilder.setContentIntent(pi);

        notificationManager.notify(0, nBuilder.build());
        **/
    }


    boolean press_one = false;
    @Override
    public void onBackPressed() {
        if(!press_one){
            Toast.makeText(getApplicationContext(),"Press again to quit !",Toast.LENGTH_LONG).show();
            press_one = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    press_one = false;
                }
            }, 3000);
            return;
        }
        finish();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.amber_600);
    }

    //https://www.intimacyrecords.com.ng/


    //Show update and lates.....
    public void show_app_update() throws NullPointerException{
        if(myref.child("updates").child("version")!=null){
            myref.addListenerForSingleValueEvent(new ValueEventListener(){
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if((long)dataSnapshot.child("updates").child("version").getValue()==(long)(BuildConfig.VERSION_CODE)){
                        return;
                   }
                    show_update_msg(dataSnapshot.child("updates").child("news").getValue().toString(), dataSnapshot.child("updates").child("title").getValue().toString(),dataSnapshot.child("updates").child("button").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }



    //Church Help
    public void showABOUT(View view){
        startActivity(new Intent(this, AboutDunamis.class));
    }

    public void show_update_msg(String text, String tit, String btn){
        final AlertDialog.Builder alt = new AlertDialog.Builder(genContext);
        alt.setCancelable(false);
        alt.setIcon(R.mipmap.ic_launcher);
        alt.setTitle(tit);
        alt.setMessage(text);

        alt.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        if(btn.equals("Upgrade")||btn.equals("Update")||btn.equals("Download")){

            alt.setPositiveButton(btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                }
            });

        };

        alt.create();
        alt.show();
    }


}
