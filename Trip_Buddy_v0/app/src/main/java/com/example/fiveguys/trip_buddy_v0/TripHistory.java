package com.example.fiveguys.trip_buddy_v0;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TripHistory extends AppCompatActivity {

    private Button backBtn;
    private ListView historyList;
    DatabaseReference User;
    private String uid;
    ArrayList<Trip> activeList;
    ArrayList<Trip> deactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        setContentView(R.layout.activity_trip_history);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("");

        //getSupportActionBar().up
        backBtn = (Button)findViewById(R.id.backBtn);
        historyList = (ListView)findViewById(R.id.historyList);
        User = FirebaseDatabase.getInstance().getReference("/tripHistory/"+uid);
        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View control = view.findViewById(R.id.controlItem);

                // Creating the expand animation for the item
                ExpandAnimation expandAni = new ExpandAnimation(control, 500);

                // Start the animation on the toolbar
                control.startAnimation(expandAni);
            }

        });

        activeList = new ArrayList<>();
        deactList = new ArrayList<>();
        User.child("trips").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String destid = snapshot.getKey();
                    for (DataSnapshot sp : snapshot.getChildren()) {
                        String sAddress = sp.child("startAddress").getValue(String.class);
                        String dAddress = sp.child("destinationAddress").getValue(String.class);
                        String date = sp.getKey();
                        boolean act = sp.child("activity").getValue(Boolean.class);
                        Trip trip = new Trip(destid, sAddress,dAddress,date,act);
                        addList(trip);
                    }
                }
                activeList.addAll(deactList);


                TripAdapter adapter = new TripAdapter(getApplicationContext(), activeList);
                historyList.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void addList(Trip trip){
        if(trip.act) activeList.add(trip);
        else deactList.add(trip);
    }

    public class Trip{
        String placeId, sAddress, dAddress, date;
        boolean act;
        public Trip(String _placeId, String _sAddress, String _dAddress, String _date, boolean _act){
            placeId = _placeId;
            sAddress = _sAddress;
            dAddress = _dAddress;
            date = _date;
            act = _act;
        }

    }

}

 class ExpandAnimation extends Animation {
    private View mAnimatedView;
    private LinearLayout.LayoutParams mViewLayoutParams;
    private int mMarginStart, mMarginEnd;
    private boolean mIsVisibleAfter = false;
    private boolean mWasEndedAlready = false;
    public ExpandAnimation(View view, int duration) {

        setDuration(duration);
        mAnimatedView = view;
        mViewLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();

        // decide to show or hide the view
        mIsVisibleAfter = (view.getVisibility() == View.VISIBLE);

        mMarginStart = mViewLayoutParams.bottomMargin;
        mMarginEnd = (mMarginStart == 0 ? (0- view.getHeight()*110/170) : 0);

        view.setVisibility(View.VISIBLE);
    }
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        if (interpolatedTime < 1.0f) {

            // Calculating the new bottom margin, and setting it
            mViewLayoutParams.bottomMargin = mMarginStart
                    + (int) ((mMarginEnd - mMarginStart) * interpolatedTime);

            // Invalidating the layout, making us seeing the changes we made
            mAnimatedView.requestLayout();
        }else if (!mWasEndedAlready) {
            mViewLayoutParams.bottomMargin = mMarginEnd;
            mAnimatedView.requestLayout();

            mWasEndedAlready = true;

    }
}
}