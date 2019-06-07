package com.nsc.sodapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Read_SOD extends AppCompatActivity {
    public JSONObject sod_js;
    public JSONObject usr_js;
    Home home_class = new Home();
    private boolean pagain_ = false;
    private ListView listView;
    private ArrayAdapter<String> listadapter;

    public SQLiteDatabase db;
    public File path;
    public String db_table_create = "CREATE TABLE IF NOT EXISTS SOD(ID INTEGER PRIMARY KEY AUTOINCREMENT,PHONE VARCHAR,BAL VARCHAR);";

    public List<String> title = new ArrayList<String>();
    List<String> msg = new ArrayList<String>();
    List<String> date = new ArrayList<String>();
    List<String> smsg = new ArrayList<String>();

    public String gloDate;

    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read__sod);

        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.sodtool);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Seeds Library");

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        int rnd = new Random().nextInt((3-1)+1)+1;

        /*
        if(rnd==1){
            findViewById(R.id.read_sod_bg).setBackgroundResource(R.drawable.demo1);
        }
        if(rnd==2){
            findViewById(R.id.read_sod_bg).setBackgroundResource(R.drawable.demo2);
        }
        if(rnd==3){
            findViewById(R.id.read_sod_bg).setBackgroundResource(R.drawable.demo3);
        }

*/
        DateFormat df = new DateFormat();
        gloDate = df.format("yyyy-MM-dd", new Date()).toString();
        Log.e("DateNow", gloDate);

        //Initializig File Path
        path = new File(Environment.getExternalStorageDirectory(),"Android"+File.separator+"Data"+File.separator+"SOD Data");
        path.mkdir();


        list = (ListView) findViewById(R.id.online_read);

        final ProgressDialog pb = new ProgressDialog(this);
        pb.setTitle("Loading SOD");
        pb.setMessage("Please wait !");
        pb.setCancelable(false);

        pb.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (pagain_ == true) {
                    finish();
                }
                if (keyCode == event.KEYCODE_BACK && pagain_ == false) {
                    Toast.makeText(pb.getContext(), "Press again to stop !", Toast.LENGTH_LONG).show();
                    pagain_ = true;
                    return false;
                }
                return false;
            }
        });

        //LOADING SOD FROM SERVER

        create_db();

        pb.cancel();

        populate_list();

    }




    //CREATING TABLE

    public void create_db(){
        db = openOrCreateDatabase(getApplicationContext().getFilesDir()+"/SOD.DB",SQLiteDatabase.OPEN_READWRITE,null);
        try{
            db.execSQL(db_table_create);
            db.execSQL("CREATE TABLE IF NOT EXISTS SOD_RD(ID INTEGER PRIMARY KEY AUTOINCREMENT, SOD TEXT,TITLE VARCHAR, DATE VARCHAR);");
        }catch (SQLiteException ex){}
    }


    //CHECKING LOGIN.......
    public boolean chk_login(){
        File path = new File(Environment.getExternalStorageDirectory(),"Android"+File.separator+"Data"+File.separator+"SOD Data"+File.separator+"sod-store.db");

        if(path.exists()==true){
            return true;
        }
    return  false;
    }


    public void populate_list(){

        Cursor c = db.rawQuery("SELECT * FROM SOD_RD ORDER BY ID DESC LIMIT 31",null);

        c.moveToFirst();

        do {
            title.add(c.getString(c.getColumnIndex("TITLE")));
            msg.add(c.getString(c.getColumnIndex("SOD")));
            date.add(c.getString(c.getColumnIndex("DATE")));
            smsg.add(c.getString(c.getColumnIndex("SOD")).substring(0,150)+"...\nClick to read more");
        }while (c.moveToNext());


        final String[] itemname = new String[title.size()];
        title.toArray(itemname);

        final String[] sub= new String[smsg.size()];
        smsg.toArray(sub);

        final String[] dat= new String[date.size()];
        date.toArray(dat);



        //CUSTOM LIST VIEW
        CustomListAdapter cl = new CustomListAdapter(this, itemname,sub,dat);

        list.setAdapter(cl);

        registerForContextMenu(list);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(view.getContext(),TempRead.class);
                i.putExtra("t",title.get(position).toString());
                i.putExtra("d",date.get(position).toString());
                i.putExtra("s",msg.get(position).toString());

                int dm = Integer.parseInt(date.get(position).split("-")[1]);
                int dd = Integer.parseInt(date.get(position).split("-")[2]);

                int sD,sM;
                sD = Integer.parseInt(gloDate.split("-")[2]);
                sM = Integer.parseInt(gloDate.split("-")[1]);

                if(dm<=sM){
                    if(dd<=sD||dd>sD&&dm<sM){
                        startActivity(i);
                    }else{Toast.makeText(getApplicationContext(), "Unable to read future data...Topic is locked, read it on "+date.get(position).toString(), Toast.LENGTH_LONG).show();}
                }else{
                    Toast.makeText(getApplicationContext(), "Unable to read future data...Topic is locked, read it on "+date.get(position).toString(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    //ONcentext Menu


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
       if(v.getId()==R.id.online_read){
           menu.setHeaderIcon(R.drawable.more_18px);
           menu.setHeaderTitle("Quick Action");
           MenuInflater menuInflater = getMenuInflater();
           menuInflater.inflate(R.menu.context, menu);
       }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Toast.makeText(getApplicationContext(),"Read before you share, send or save !",Toast.LENGTH_LONG).show();
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        //Toast.makeText(getApplicationContext(),"Read before you share, send or save !",Toast.LENGTH_LONG).show();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_menu, menu);
        menu.removeItem(R.id.menu_share);
        menu.removeItem(R.id.menu_send_as);
        menu.removeItem(R.id.menu_color);
        return super.onCreateOptionsMenu(menu);
    }
}
