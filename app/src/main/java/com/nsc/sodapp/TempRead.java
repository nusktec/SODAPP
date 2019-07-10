package com.nsc.sodapp;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nsc.sodapp.databinding.ActivityTempReadBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import cz.msebera.android.httpclient.Header;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TempRead extends AppCompatActivity {
    Spanned res;
    String cnv;
    int default_color;
    boolean night_mode = false;

    ActivityTempReadBinding binding;

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("font/f5.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_temp_read);

        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.sodtool2);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        //lock_unlock
        //lock_unlock();

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




        //INITIALIZING arrayas from calss1
        res = Html.fromHtml(getIntent().getStringExtra("s"));

        char[] chars = new char[res.length()];
        TextUtils.getChars(res, 0, res.length(), chars, 0);
        cnv = new String(chars);

        ((TextView) findViewById(R.id.tmp_topic)).setText(Html.fromHtml(getIntent().getStringExtra("t")));
        ((TextView) findViewById(R.id.tmp_scripture)).setText(cnv);

        //First line quote
        String keep_daddy = cnv.split("\n")[0];

        if(keep_daddy.contains("(")){
            keep_daddy = keep_daddy.replace("(","");
        }
        if(keep_daddy.contains(")")){
            keep_daddy = keep_daddy.replace(")","");
        }

        ((TextView) findViewById(R.id.txt_by_daddy)).setText(keep_daddy);

        ((TextView) findViewById(R.id.bottom_date)).setText("Share Now\n"+getIntent().getStringExtra("d")+" SOD");

        WebView wb = (WebView) findViewById(R.id.wb);
        wb.getSettings().setJavaScriptEnabled(true);
        wb.setWebChromeClient(new WebChromeClient());

        /*
        String start = "<html><head><meta http-equiv='Content-Type' content='text/html' charset='UTF-8' /></head><body>";
        String end = "</body></html>";
        String frm = start + getIntent().getStringExtra("s") + end;
        wb.loadDataWithBaseURL("",frm,"text/html","UTF-8","");
        */


        //Set Default color
        default_color = ((TextView) findViewById(R.id.tmp_scripture)).getCurrentTextColor();


        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour < 6 || hour > 18){
            //isNight = true;

            //Toast.makeText(this,"Hey, It's night !.\nTry our night mode theme from the menu",Toast.LENGTH_LONG).show();

        } else {
            //isNight = false;
        }

        //light_check();
        ((TextView) findViewById(R.id.tmp_scripture)).setTextColor(openSetting());

        //begin load adverts
        if(isNetworkAvailable(this)){
            YoYo.with(Techniques.FadeIn).delay(1000).repeat(0).onEnd(new YoYo.AnimatorCallback() {
                @Override
                public void call(Animator animator) {
                    //It has ended......
                    playAdverts();
                }
            }).playOn(binding.advPanel);
        }else {
            binding.advPanel.setVisibility(View.GONE);
        }
    }



    public void light_check(){

        SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        Sensor LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(LightSensor != null){
            //textLIGHT_available.setText("Sensor.TYPE_LIGHT Available");
            mySensorManager.registerListener(
                    LightSensorListener,
                    LightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

        }else{
            //textLIGHT_available.setText("Sensor.TYPE_LIGHT NOT Available");
        }
    }



    private final SensorEventListener LightSensorListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                //textLIGHT_reading.setText("LIGHT: " + event.values[0]);
                Toast.makeText(getApplicationContext(),"There: " + event.values[0],Toast.LENGTH_LONG).show();
            }
        }

    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void twitter(View v){
        String sod = "Today SOD - "+getIntent().getStringExtra("d")+"\nTOPIC: "+getIntent().getStringExtra("t")+"\n\n"+cnv
                +"\n"+"Read SOD at ease via SOD Mobile \nhttps://play.google.com/store/apps/details?id=com.nsc.sodapp";
        social_share(getApplicationContext(), sod, "Share On Twitter");
    }

    public void whatsapp(View v){
        String sod = "Today SOD - "+getIntent().getStringExtra("d")+"\nTOPIC: "+getIntent().getStringExtra("t")+"\n\n"+cnv
                +"\n"+"Download SOD On Mobile\nhttps://play.google.com/store/apps/details?id=com.nsc.sodapp";
        social_share(getApplicationContext(), sod, "Share On Whatsapp");
    }


    //SHOCIAL......
    public void facebook(View v){
        String sod = "Today SOD - "+getIntent().getStringExtra("d")+"\nTOPIC: "+getIntent().getStringExtra("t")+"\n\n"+cnv
                +"\n"+"Download SOD On Mobile\nhttps://play.google.com/store/apps/details?id=com.nsc.sodapp";
        social_share(getApplicationContext(), sod, "Share On Facebook");
    }

    public void social_share(Context context, String s, String via){
        Intent i = new Intent();
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, s);
        i.setAction(Intent.ACTION_SEND);
        startActivity(Intent.createChooser(i, via));
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.menu_send_as||id==R.id.menu_share){
            String sod = "Today's SOD - "+getIntent().getStringExtra("d")+"\nTOPIC: "+getIntent().getStringExtra("t")+"\n\n"+cnv
                    +"\n"+"Download SOD App on your device\nhttps://play.google.com/store/apps/details?id=com.nsc.sodapp";
            social_share(getApplicationContext(), sod, "Share On");
            return false;
        }

        switch (id){
            case R.id.color_yellow:
                if(night_mode==false){
                    ((TextView) findViewById(R.id.tmp_scripture)).setTextColor(default_color);
                    saveSettings(default_color);
                    return false;
                }
                Toast.makeText(getApplicationContext(),"Disable night mode",Toast.LENGTH_LONG).show();

            case R.id.color_green:
                if(night_mode==false){
                    saveSettings(Color.RED);
                    ((TextView) findViewById(R.id.tmp_scripture)).setTextColor(Color.RED);
                    return false;
                }
                Toast.makeText(getApplicationContext(),"Disable night mode",Toast.LENGTH_LONG).show();

            case R.id.color_white:
                if(night_mode==false){
                    saveSettings(getResources().getColor(R.color.colorPrimaryDark));
                    ((TextView) findViewById(R.id.tmp_scripture)).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    return false;
                }
                Toast.makeText(getApplicationContext(),"Disable night mode",Toast.LENGTH_LONG).show();

                default:

        }



    if(id == R.id.color_night_mode){
        if(night_mode==false){
//            ((TextView) findViewById(R.id.tmp_scripture)).setTextColor(getResources().getColor(R.color.night_fore));
//            ((LinearLayout) findViewById(R.id.temp_redbg)).setBackgroundColor(getResources().getColor(R.color.nigh_bg));
            night_mode=true;
            item.setTitle("Disable Night Mode");
        }else{
            ((TextView) findViewById(R.id.tmp_scripture)).setTextColor(default_color);
            ((LinearLayout) findViewById(R.id.temp_redbg)).setBackgroundColor(getResources().getColor(R.color.white_back));
            night_mode=false;
            item.setTitle("Night Mode");
        }

    }

    if(id!=R.id.color_green||id!=R.id.color_yellow||id!=R.id.color_white||id!=R.id.menu_send_as||id!=R.id.menu_share){

    }
        return super.onOptionsItemSelected(item);
    }

    public void saveSettings(int color){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("color", color);
        editor.commit();
    }

    public int openSetting(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int defaultValue = sharedPref.getInt("color",default_color);
        return defaultValue;
    }


    //commit coins
    public void lock_unlock(){
        int coin = getCoin();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("ADV",Context.MODE_PRIVATE);
        pref.edit().putInt("coin",coin).commit();
    }


    public int getCoin(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("ADV",Context.MODE_PRIVATE);
        int defaultValue = pref.getInt("coin",0);
        return defaultValue;
    }

    //advert methods
    String def_url = "https://play.google.com/store/apps/details?id=com.rscbyte.sodmobilepro";
    void playAdverts(){
        //declarations
        binding.advBtnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((def_url=="")?"https://play.google.com/store/apps/details?id=com.rscbyte.sodmobilepro":def_url));
                startActivity(browserIntent);
            }
        });
        //load from the web
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("cmd","adv");
        client.post("http://api.rscbyte.com/adverts/cmd", params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    def_url = response.getString("url"); //url assigned
                    Glide.with(TempRead.this)
                            .load(response.getString("img"))
                            .into(binding.advImage);
                    binding.advText.setText(String.valueOf(response.getString("text")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
