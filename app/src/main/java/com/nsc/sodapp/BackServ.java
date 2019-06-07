package com.nsc.sodapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * Created by NSC on 4/11/2017.
 */

public class BackServ extends Service {

    @Override
    public void onCreate() {
        Timer timer = new Timer();
        timer.schedule(task, 1000,1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        reading_temp();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void notifi(String t, String s, String d) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setContentTitle("TODAY SOD-" + d);
        nBuilder.setAutoCancel(false);
        nBuilder.setContentText(t);
        nBuilder.setSmallIcon(R.drawable.book);
        nBuilder.setSubText(Html.fromHtml(s));

        Intent i = new Intent(this, Read_SOD.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        nBuilder.setContentIntent(pi);

        notificationManager.notify(0, nBuilder.build());
    }


    public void reading_temp() {

        String pub_host = "http://dunamisgospel.org";

        SyncHttpClient client = new SyncHttpClient();
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


                    String nMsg = Html.fromHtml(msg.substring(0, 150)).toString();

                    String[] nArr = new String[]{title, nMsg, sod_date};
                    notifi(nArr[0], nArr[1], nArr[2]);

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
            if(s.toString().equals("06:00:00")||s.toString().equals("06:00:01")||s.toString().equals("06:00:02")||s.toString().equals("03:00:00")){
                    reading_temp();
                }
            }
    };
}
