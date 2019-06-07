package com.nsc.sodapp;
import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;


/**
 * Created by NSC on 4/4/2017.
 */
//CustomListAdapter
public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final String[] submenu;
    private final String[] date;


    public CustomListAdapter(Activity context, String[] itemname, String[] sub, String[] date) {
        super(context, R.layout.cus_list_view, itemname);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemname=itemname;
        this.submenu=sub;
        this.date=date;

    }

    public View getView(int position, View view, ViewGroup parent) {

        //Change date
        String ref_date[] = date[position].split("-");
        String fin_date = ref_date[2]+"-"+ref_date[1]+"-"+ref_date[0];

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.cus_list_view, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.title);
        TextView extratxt = (TextView) rowView.findViewById(R.id.scripture);
        TextView sod_date = (TextView) rowView.findViewById(R.id.date);

        txtTitle.setText(Html.fromHtml(itemname[position]));
        sod_date.setText(fin_date);
        extratxt.setText(Html.fromHtml(submenu[position]));

        DateFormat df = new DateFormat();
        String gloDate = df.format("yyyy-MM-dd", new Date()).toString();

        if(date[position].equals(gloDate)){
            sod_date.setTextColor(Color.RED);
        }else{
            sod_date.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
        }


        return rowView;
    }
}