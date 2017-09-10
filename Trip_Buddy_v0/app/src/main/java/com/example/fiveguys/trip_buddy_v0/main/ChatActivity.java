package com.example.fiveguys.trip_buddy_v0.main;

import android.content.Intent;
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

// chat1 activity serves the chat history function which can e
public class ChatActivity extends AppCompatActivity {

    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MAIN";

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        // Set up app bar
         mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
         setSupportActionBar(mToolbar);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_main);
        // mNavView = (NavigationView) findViewById(R.id.nav_view_main);

        // setUpNavigationDrawer();
        // setUpDrawerToggle();


        if(savedInstanceState == null) {
            // If started from launcher
            Fragment fragment = GroupChannelListFragment.newInstance();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container_main, fragment)
                    .commit();

            // Visually sets item as checked
            //mNavView.setCheckedItem(R.id.nav_item_group_channels);
        }

        String channelUrl = getIntent().getStringExtra("groupChannelUrl");
        if(channelUrl != null) {
            // If started from notification
            Fragment fragment = GroupChatFragment.newInstance(channelUrl);
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container_main, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String channelUrl = getIntent().getStringExtra("groupChannelUrl");
        if(channelUrl != null) {
            // If started from notification
            Fragment fragment = GroupChatFragment.newInstance(channelUrl);
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container_main, fragment)
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
     * Sets up items in the navigation drawer to in inflate the correct fragments on click.
     */
//    private void setUpNavigationDrawer() {
//        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                Fragment fragment;
//                int id = item.getItemId();
//                Log.i("dd", String.valueOf(id));
//                Log.i("dd", String.valueOf(R.id.nav_item_group_channels));
//                Log.i("dd", String.valueOf(R.id.nav_item_disconnect));
//
//
//                if (id == R.id.nav_item_group_channels) {
//                    fragment = GroupChannelListFragment.newInstance();
//
//                    FragmentManager manager = getSupportFragmentManager();
//                    manager.popBackStack();
//
//                    manager.beginTransaction()
//                            .replace(R.id.container_main, fragment)
//                            .commit();
//
//                } else if (id == R.id.nav_item_disconnect) {
//                    // Unregister push tokens and disconnect
//                    Log.i("d","d");
//                    Log.i("d","d");
//                }
//
//                item.setChecked(true);
//                mDrawerLayout.closeDrawers();
//
//                return false;
//            }
//        });
//    }



    /**
     * Configures the hamburger icon to react to navigation drawer state changes.
     */
    private void setUpDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.main_drawer_open,
                R.string.main_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // Remove hamburger icon if a fragment is added.
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                if(getSupportFragmentManager().getBackStackEntryCount() > 0){
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
                else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    mDrawerToggle.setDrawerIndicatorEnabled(true);
                    mDrawerToggle.syncState();
                }
            }
        });
    }

    /**
     * A method that allows fragments to change the title of this action bar.
     */
    public void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Unregisters all push tokens for the current user so that they do not receive any notifications,
     * then disconnects from SendBird.
     */



}
