package com.applications.guilhermeaugusto.eldernote.ArrayAdapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.applications.guilhermeaugusto.eldernote.R;

import java.util.ArrayList;

/**
 * Created by guilhermemartins on 12/1/14.
 */
public class BasicArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> itemsArrayList;
    private int selectedItem = -1;

    public BasicArrayAdapter(Context context, ArrayList<String> itemsArrayList) {
        super(context, R.layout.row_basic, itemsArrayList);
        this.itemsArrayList = itemsArrayList;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.row_basic, parent, false);
        defineBackgroundColor(rowView, position);
        defineTextViewText(rowView, position);
        return rowView;
    }

    private void defineBackgroundColor(View rowView, int position){
        if(selectedItem!= -1 && position == selectedItem)
        {
            rowView.setBackgroundColor(Color.GREEN);
        } else {
            if (position % 2 == 1) { rowView.setBackgroundColor(Color.parseColor("#ffdeddde")); }
            else { rowView.setBackgroundColor(Color.parseColor("#fff2f2f5")); }
        }
    }

    private void defineTextViewText(View rowView, int position) {
        TextView textView = (TextView) rowView.findViewById(R.id.textView);
        textView.setText(itemsArrayList.get(position));
    }

    public void setSelectedItem(int position)
    {
        selectedItem = position;
        notifyDataSetChanged();
    }
}