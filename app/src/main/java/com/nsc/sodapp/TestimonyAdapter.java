package com.nsc.sodapp;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


/**
 * Created by NSC on 4/4/2017.
 */
//CustomListAdapter
public class TestimonyAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] testifiername;
    private final String[] content;
    private final String[] test_date;

    public TestimonyAdapter(Activity context, String[] testifiername, String[] contents, String[] date) {
        super(context, R.layout.cus_list_view, testifiername);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.testifiername = testifiername;
        this.content =contents;
        this.test_date = date;

    }

    public View getView(int position, View view, ViewGroup parent) {

        //Change date

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.testimonylist, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.testifier_title);
        TextView testimony_content = (TextView) rowView.findViewById(R.id.testimony_content);
        TextView testimony_date = (TextView) rowView.findViewById(R.id.testimony_date);

       txtTitle.setText(testifiername[position]);

       testimony_content.setText(content[position]);

       testimony_date.setText(test_date[position]);

        return rowView;
    }
}