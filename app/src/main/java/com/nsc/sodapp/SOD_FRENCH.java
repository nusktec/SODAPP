package com.nsc.sodapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SOD_FRENCH extends Fragment {

    private View genView;

    //Using Variables

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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {




        View rootView = inflater.inflate(R.layout.activity_sod__french, container, false);

        this.genView = rootView;

       startActions();

        return  rootView;
    }


    public void startActions(){
        DateFormat df = new DateFormat();
        gloDate = df.format("yyyy-MM-dd", new Date()).toString();
        Log.e("DateNow", gloDate);

        //Initializig File Path
        path = new File(Environment.getExternalStorageDirectory(),"Android"+File.separator+"Data"+File.separator+"SOD Data");
        path.mkdir();


        list = (ListView) genView.findViewById(R.id.sod_french_layout);

        final ProgressDialog pb = new ProgressDialog(getContext());
        pb.setTitle("Loading SOD");
        pb.setMessage("Please wait !");
        pb.setCancelable(false);

        pb.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (pagain_ == true) {
                    getActivity().finish();
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



    public void create_db(){
        db = getActivity().openOrCreateDatabase(getContext().getFilesDir()+"/SOD.DB",SQLiteDatabase.OPEN_READWRITE,null);
    }



    public void populate_list(){

        Cursor c = db.rawQuery("SELECT * FROM SOD_RD ORDER BY ID DESC LIMIT 31",null);

        c.moveToFirst();

        do {
            String english_sod = c.getString(c.getColumnIndex("SOD"));

            if(english_sod.contains("SEEDS OF DESTINY IN FRENCH")){

                title.add(english_sod.split("SEEDS OF DESTINY IN FRENCH")[1].split("\n")[1]);

                english_sod = english_sod.split("SEEDS OF DESTINY IN FRENCH")[1];
            }else {
                title.add(c.getString(c.getColumnIndex("TITLE")));
            }


            english_sod = english_sod.replaceAll(".?\\*+.?", "");

            msg.add(english_sod);

            date.add(c.getString(c.getColumnIndex("DATE")));

            smsg.add(english_sod.split("\n")[1]);

        }while (c.moveToNext());


        final String[] itemname = new String[title.size()];
        title.toArray(itemname);

        final String[] sub= new String[smsg.size()];
        smsg.toArray(sub);

        final String[] dat= new String[date.size()];
        date.toArray(dat);



        //CUSTOM LIST VIEW
        CustomListAdapter cl = new CustomListAdapter(getActivity(), itemname,sub,dat);

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

                String ref_date[] = date.get(position).split("-");
                String fin_date = ref_date[2]+"-"+ref_date[1]+"-"+ref_date[0];

                if(dm<=sM){
                    if(dd<=sD||dd>sD&&dm<sM){
                        startActivity(i);
                    }else{Toast.makeText(getContext(), "\n" + "Le sujet Upnext est verrouillé, lisez-le "+fin_date, Toast.LENGTH_LONG).show();}
                }else{
                    Toast.makeText(getContext(), "\n" + "Le sujet Upnext est verrouillé, lisez-le "+fin_date, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

}
