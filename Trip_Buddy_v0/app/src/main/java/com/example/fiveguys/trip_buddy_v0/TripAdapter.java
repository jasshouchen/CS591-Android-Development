package com.example.fiveguys.trip_buddy_v0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Victor on 4/30/17.
 */

public class TripAdapter extends BaseAdapter{
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<TripHistory.Trip> mDataSource;

    public TripAdapter(Context context, ArrayList<TripHistory.Trip> trips) {
        mContext = context;
        mDataSource = trips;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    //2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    //3
    @Override
    public long getItemId(int position) {
        return position;
    }

    //4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for row item
        View rowView = mInflater.inflate(R.layout.history_list, parent, false);
        TextView desText = (TextView)rowView.findViewById(R.id.desText);
        TextView sttText = (TextView)rowView.findViewById(R.id.sttText);
        TextView date = (TextView)rowView.findViewById(R.id.dateText);
        TextView active = (TextView)rowView.findViewById(R.id.active);
        TripHistory.Trip trip = (TripHistory.Trip) getItem(position);
        desText.setText(trip.dAddress);
        sttText.setText(trip.sAddress);
        date.setText(trip.date.replace("_", "/"));
        if(!trip.act) active.setText("");
        return rowView;
    }
}
