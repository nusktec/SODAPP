package com.nsc.sodapp;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by NSC on 4/11/2017.
 */

public class Notifications extends Service {

    public SQLiteDatabase db;
    public String db_table_create = "CREATE TABLE IF NOT EXISTS SOD(ID INTEGER PRIMARY KEY AUTOINCREMENT,PHONE VARCHAR,BAL VARCHAR);";

    FirebaseDatabase fb = FirebaseDatabase.getInstance();
    DatabaseReference myref = fb.getReference("updates");

    @Override
    public void onCreate() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Timer timer = new Timer();
        timer.schedule(task, 1000,1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

           try {
               create_db();

               show_app_update();
           }catch (Exception ex){

           }
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void notifi(String t, String s, String d) {

        //Enable barge

        ShortcutBadger.applyCount(this,1);

        /**
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setContentTitle("Seeds Of Destiny Today");
        nBuilder.setAutoCancel(false);
        nBuilder.setContentText(t);
        nBuilder.setSmallIcon(R.mipmap.ic_launcher);
        nBuilder.setSubText(Html.fromHtml(s));
        nBuilder.setLights(6, 17, 255);
        nBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Intent i = new Intent(this, Home.class);
        i.putExtra("dt",t);
        i.putExtra("dd",d);
        i.putExtra("ss",s);
        i.putExtra("noti-call","YES");

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        nBuilder.setContentIntent(pi);

        notificationManager.notify(0, nBuilder.build());
         **/
    }


    public void reading_temp() {

        String pub_host = "http://dunamisgospel.org";

        SyncHttpClient client = new SyncHttpClient();
        client.cancelAllRequests(true);
        RequestParams params = new RequestParams();
        params.put("nscteq-tmp", "nsc");
        params.put("lim", 1);
        client.post(getApplicationContext(), pub_host + "/mobile-sod/read.php", params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                notifi("","No internet connections to get today's seeds of destiny, put on your mobile data and read form menu....","Date");
            }

            @Override
            public void onStart() {

            }


            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {


                String title = "";
                String msg = "";

                try {
                    JSONArray jsonArray = new JSONArray(response.getString("rd"));

                    //CONVERTING FROM BASE64

                    JSONObject js = jsonArray.getJSONObject(0);

                    String sod_date = js.getString("created").split(" ")[0];
                    title = new String(Base64.decode(js.getString("title").getBytes("UTF-8"), Base64.DEFAULT));
                    msg = new String(Base64.decode(js.getString("msg").getBytes("UTF-8"), Base64.DEFAULT));


                    String nMsg = Html.fromHtml(msg).toString();

                    String[] nArr = new String[]{title, nMsg, sod_date};
                    notifi(nArr[0], nArr[1], nArr[2]);


                    insert_sod(title,msg,sod_date);


                } catch (Exception ex) {
                    Log.e("Error SOD", ex.getMessage().toString());
                }
            }
        });

    }



    //TIming
    TimerTask task = new TimerTask(){
        @Override
        public void run(){
            Date d = new Date();
            CharSequence s = DateFormat.format("hh:mm:ss", d.getTime());
            if(s.toString().equals("06:00:00")||s.toString().equals("06:00:01")||s.toString().equals("06:00:02")||s.toString().equals("03:00:00")||s.toString().equals("12:00:00")){
                reading_temp();
            }
        }
    };


    //Read once
   private boolean onces = true;

    private boolean bootted = false;

    //check update
    //Show update and lates.....
    public void show_app_update() throws NullPointerException{
            myref.addValueEventListener(new ValueEventListener(){
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null){
                            if((long)dataSnapshot.child("version").getValue()==(long)(BuildConfig.VERSION_CODE)){
                                return;
                            }
                            show_update_msg(dataSnapshot.child("news").getValue().toString(), dataSnapshot.child("title").getValue().toString(),dataSnapshot.child("button").getValue().toString());
                        }
                     }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        //Tell to join
        DatabaseReference chat = fb.getReference("chat");
        chat.child("message_deprecated").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //DONT SHOW ANYTHING

                if (onces==false){


                    if(dataSnapshot.getValue()!=null){

                        String sname = dataSnapshot.getKey();//dataSnapshot.child(dataSnapshot.getKey()).getValue().toString().split("~`i`~")[1].split(":")[0];

                        SharedPreferences pref = getApplicationContext().getSharedPreferences("",Context.MODE_PRIVATE);

                        if(!pref.getString("room_name","dunamite").equals(sname)&&pref.getBoolean("active",false)==false&&pref.getBoolean("alert",true)==true){


                            String newMsg = dataSnapshot.toString().split("~`i`~")[1].replace("}","").split("~~``ii``~~")[0];

                            //Add msg to keepItems
                            //dEPRECATED FOR FUTURE ADAVCEMENT
                            pref.edit().putString("keep_msg", pref.getString("keep_msg","")+newMsg+"~~KMSG~~").commit();

                            int count_noti = pref.getString("keep_msg","~~KMSG~~").toString().split("~~KMSG~~").length;
                            ShortcutBadger.applyCount(getBaseContext(),count_noti);

                            String extra ="";

                            if(count_noti>2){
                                extra = "You have " + count_noti + " new unread messages";
                            }

                           /* PugNotification.with(getBaseContext())
                                    .load()
                                    .title("SOD Chat")
                                    .message("New Messages: \n" + newMsg)
                                    .bigTextStyle(newMsg + "\n\n" + extra)
                                    .smallIcon(R.mipmap.menu_chat)
                                    .largeIcon(R.mipmap.ic_launcher)
                                    .flags(Notification.DEFAULT_LIGHTS)
                                    .flags(Notification.COLOR_DEFAULT)
                                    .simple()
                                    .build();
                            */
                        }


                    }

                }

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

        onces = false;
    }


    boolean showOnce = false;

    public void show_update_msg(String text, String tit, String btn){
        /**
        PendingIntent i = PendingIntent.getActivity(this,0,new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=com.nsc.sodapp")),PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setContentTitle(tit);
        nBuilder.setAutoCancel(false);
        nBuilder.setContentText(text);
        nBuilder.setSmallIcon(R.mipmap.ic_launcher);
        nBuilder.setSubText("Tap to upgrade now");
        nBuilder.setLights(6, 17, 255);
        nBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        nBuilder.setContentIntent(i);

        notificationManager.notify(0, nBuilder.build());

         **/

    }




    public void create_db(){
        db = openOrCreateDatabase(getApplicationContext().getFilesDir()+"/SOD.DB", SQLiteDatabase.OPEN_READWRITE,null);
        try{
            db.execSQL(db_table_create);
            db.execSQL("CREATE TABLE IF NOT EXISTS SOD_RD(ID INTEGER PRIMARY KEY AUTOINCREMENT, SOD TEXT,TITLE VARCHAR, DATE VARCHAR);");
        }catch (SQLiteException ex){}
    }



    //INSERTING NEW TO DB
    public void insert_sod(String t, String m, String d){
        try{
            Cursor c = db.rawQuery("SELECT * FROM SOD_RD",null);
            if(c.getCount()>20){
                Cursor chk = db.rawQuery("SELECT * FROM SOD_RD WHERE DATE='"+d+"'",null);
                if(chk.getCount()>0) {
                    Toast.makeText(getBaseContext(), "Updated !", Toast.LENGTH_LONG).show();
                    return;
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put("SOD", m);
                contentValues.put("DATE", d);
                contentValues.put("TITLE", t);
                long ints = db.insert("SOD_RD",null,contentValues);
                Toast.makeText(getBaseContext(), "New SOD Added ! "+ints, Toast.LENGTH_LONG).show();
            }
        }catch (Exception ex){
            Log.e("INSERT-DB", ex.getMessage().toString());
        }
    }

}