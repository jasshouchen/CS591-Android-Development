package com.example.fiveguys.trip_buddy_v0.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fiveguys.trip_buddy_v0.Main;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.example.fiveguys.trip_buddy_v0.R;
import com.example.fiveguys.trip_buddy_v0.groupchannel.*;
import com.example.fiveguys.trip_buddy_v0.utils.*;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.List;

// Fragment 2 only serves the chat creating function from the trip group
public class Chat2Activity extends AppCompatActivity {

    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MAIN";

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<String> usridlist;
    private Button btnBack;

    String destination;
    String photoUrl;



    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        // get matched user list from main
        usridlist = new ArrayList<>();
        usridlist = getIntent().getStringArrayListExtra("LIST");
        destination = getIntent().getStringExtra("DESTINATION");
        photoUrl = getIntent().getStringExtra("DEST_PHOTO");


        // Set up app bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main2);
        setSupportActionBar(mToolbar);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_main2);
        // mNavView = (NavigationView) findViewById(R.id.nav_view_main2);

//        btnBack = (Button) findViewById(R.id.btnBack);
//        btnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Chat2Activity.this, Main.class);
//                startActivity(intent);
//                Log.i("back","succ");
//            }
//        });

        // setUpNavigationDrawer();
        // setUpDrawerToggle();


        if(savedInstanceState == null) {
            // If started from launcher
            Fragment fragment = GroupChannelListFragment2.newInstance(usridlist, photoUrl, destination);
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container_main2, fragment)
                    .commit();

            // Visually sets item as checked
            // mNavView.setCheckedItem(R.id.nav_item_group_channels);
        }

        String channelUrl = getIntent().getStringExtra("groupChannelUrl");
        if(channelUrl != null) {
            // If started from notification
            Fragment fragment = GroupChatFragment2.newInstance(channelUrl);
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container_main2, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
                Log.d("CONNECTION", "onReconnectStarted()");
            }

            @Override
            public void onReconnectSucceeded() {
                Log.d("CONNECTION", "onReconnectSucceeded()");
            }

            @Override
            public void onReconnectFailed() {
                Log.d("CONNECTION", "onReconnectFailed()");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("d","pause");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Chat2Activity.this, Main.class);
        startActivity(intent);
        finish();
    }

    //    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == android.R.id.home) {
//            mDrawerLayout.openDrawer(GravityCompat.START);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return super.onSupportNavigateUp();
    }



    /**
     * A method that allows fragments to change the title of this action bar.
     */
    public void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }



}
