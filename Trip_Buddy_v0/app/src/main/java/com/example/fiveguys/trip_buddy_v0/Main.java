package com.example.fiveguys.trip_buddy_v0;
import com.example.fiveguys.trip_buddy_v0.ImageLoad;

import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fiveguys.trip_buddy_v0.groupchannel.GroupChatFragment;
import com.example.fiveguys.trip_buddy_v0.main.Chat2Activity;
import com.example.fiveguys.trip_buddy_v0.main.ChatActivity;
import com.example.fiveguys.trip_buddy_v0.utils.PreferenceUtils;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ImageLoad.EditPlayerAdapterCallback
{

    private static final int INTENT_REQUEST_NEW_GROUP_CHANNEL = 302;
    String username, email, uid, age;
    Uri photoUrl;
    private boolean deleted = false;
    FirebaseDatabase database;
    DatabaseReference myRef;
    StorageReference mStorageRef;
    public static List<String> urilist, destinations;
    public static List<Integer> numberList;
    List<Pair> matches;
    GridView grid;
    public static List<List<String>> list = new ArrayList<>();
    public  static List<List<String>> totList;
    private static final String TAG = Main.class.getSimpleName();
    private NavigationView mNavView;
    public static boolean check;

    /**
     * Main onCreate function for main activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        urilist = new ArrayList<>();
        destinations = new ArrayList<>();
        totList = new ArrayList<>();
        numberList = new ArrayList<>();
        if (user != null) {
            username = user.getDisplayName();
            email = user.getEmail();
            uid = user.getUid();
            photoUrl = user.getPhotoUrl();
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference();
            mStorageRef = FirebaseStorage.getInstance().getReference();
            DatabaseReference Users = myRef.child("users");
            Users.child(uid).child("name").setValue(username);
            Users.child(uid).child("email").setValue(email);

            //connect sendbird
            PreferenceUtils.setUserId(Main.this, uid);
            PreferenceUtils.setNickname(Main.this, username);

            connectToSendBird(uid, username);

        } else {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        final SwipeRefreshLayout refresh = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        refresh.setProgressBackgroundColorSchemeResource(R.color.WordDark);
        refresh.setColorSchemeResources(R.color.cardview_light_background);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshGrid();
                refresh.setRefreshing(false);
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user.isEmailVerified()) {
                    Intent intent = new Intent(getApplicationContext(), NewTrip.class);
                    startActivity(intent);
                }else{
                    alert("Please verify your email before adding trips");
                }
            }
        });

        FloatingActionButton chatFab = (FloatingActionButton) findViewById(R.id.chatFab);
        chatFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
        final TextView nav_name = (TextView) header.findViewById(R.id.nav_name);
        TextView nav_email = (TextView) header.findViewById(R.id.nav_email);
        ImageView nav_image = (ImageView) header.findViewById(R.id.nav_image);




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

        refreshGrid();


        // pass matches to it
        nav_name.setText(username);
        myRef.child("users").child(uid).child("age").addValueEventListener(new ValueEventListener() {
            @Override
            /**
             * Listene to user's age
             */
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.exists()) {
                    age = dataSnapshot.getValue(String.class);
                    nav_name.setText(username + ", " + age);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        if(email != null)
        nav_email.setText(email);
        if(photoUrl != null) {
            Picasso.with(getApplicationContext()).load(photoUrl.toString()).into(nav_image);
        }
        new Thread(new Runnable() {
            public void run() {
                // a potentially  time consuming task
                try{
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (Exception e) {}
                refreshGrid();
            }
        }).start();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * refreshGrid to refresh gridview layout
     */
    public void refreshGrid(){
        matches = new ArrayList<>();
        myRef = FirebaseDatabase.getInstance().getReference();

        myRef.addListenerForSingleValueEvent(new ValueEventListener
                () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                urilist.clear();
                destinations.clear();
                list.clear();
                numberList.clear();
                DataSnapshot tripRef = dataSnapshot.child("users").child(uid).child("trips");
                for (DataSnapshot snapshot : tripRef.getChildren()) {
                    final String destid = snapshot.getKey();
                    for (DataSnapshot sp : snapshot.getChildren()) {
                        snapshot.getKey();
                        String url = sp.child("photoUrl").getValue(String.class);
                        String des = sp.child("destinationName").getValue(String.class);
                        final String strt = sp.child("startAddress").getValue(String.class);
                        // listRef, get reference of trips for specific current user to get start
                        // address
                        DataSnapshot listRef = dataSnapshot.child("trips").child(destid).child(strt.toString());
                        List<String> sublist = new ArrayList<>();
                        for (DataSnapshot subuser : listRef.getChildren()) {
                            if (subuser.getValue().equals(true)) {
                                check = true;
                                sublist.add(subuser.getKey().toString());
                            }
                        }
                        totList.add(sublist); // for each user's matched friend into total list
                        urilist.add(url); // stores image url
                        numberList.add(sublist.size()); // number list store matching number
                        destinations.add(des); // destinations store destination
                        final ImageLoad adapter = new ImageLoad(Main.this, dataSnapshot, urilist, destinations, new ImageLoad.EditPlayerAdapterCallback() {
                            @Override
                            public void deletePressed(int position) {
                                deletePlayer(position);
                                // delete image function implemented from
                                // interface
                                    }
                                });
                                adapter.notifyDataSetChanged();
                                grid = (GridView) findViewById(R.id.gridview);
                                grid.setAdapter(adapter);
                                grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                       // Toast.makeText(Main.this, "You Clicked at " + totList.get(i),
                                         //       Toast.LENGTH_SHORT).show();
                                        //Log.d("TotList", Arrays.toString(totList.toArray()));
                                        Intent intent = new Intent(Main.this, Chat2Activity.class);
                                        intent.putStringArrayListExtra("LIST", new ArrayList<String>(totList.get(i)));
                                        intent.putExtra("DESTINATION", String.valueOf(destinations.get(i)));
                                        intent.putExtra("DEST_PHOTO", urilist.get(i));
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void addnumber(int number) {
        numberList.add(number);
    }

    private void addDesList(String des) {
        destinations.add(des);
    }

    private void deletePlayer(final int position) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main.this);

        // Setting Dialog Title
        alertDialog.setTitle("Delete A Trip");

        // Setting Dialog Message
        alertDialog.setMessage("Are you sure?");

        // Setting Delete Button
        alertDialog.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        uid = user.getUid();
                        database = FirebaseDatabase.getInstance();
                        myRef = database.getReference();
                        myRef.child("users").child(uid).child("trips").addListenerForSingleValueEvent(new ValueEventListener
                                                                                                         () {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String destid = snapshot.getKey();
                                    for (DataSnapshot sp : snapshot.getChildren()) {
                                        final String strt = sp.child("startAddress").getValue(String.class);
                                        Pair pair = new Pair(destid, strt);
                                        matches.add(pair); // add pair of start address and
                                        // destination address saved into matches list
                                        if (position < matches.size()) { // make sure position
                                            String dest1 = matches.get(position).first.toString();
                                            String strt1 = matches.get(position).second.toString();
                                            myRef.child("trips").child(""+dest1).child(strt1).child(uid).setValue(false);

                                            myRef.child("tripHistory").child(uid).child("trips").child(""+dest1).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                       // System.out.println("++++++++++++++++++++" + snapshot);
                                                        snapshot.child("activity").getRef().setValue(false);
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                            myRef.child("users").child(uid).child("trips").child(""+dest1).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        //System.out.println("++++++++++++++++++++" + snapshot);
                                                        snapshot.getRef().removeValue();
                                                    }
                                                    if(position == 0){
                                                        recreate();
                                                    }else
                                                    refreshGrid();
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                        }
                                    }

                                }

                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }

                        });

                    }
                });

        // Setting Cancel Button
        alertDialog.setNeutralButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        // Showing Alert Message
        alertDialog.show();
    }


    public void deletePressed(int position) {
        deletePlayer(position);
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_account) {
            Intent intent = new Intent(getApplicationContext(), MyInfo.class);// Handle the camera action
            startActivity(intent);
        } else if (id == R.id.nav_tripHistory) {
            Intent intent = new Intent(getApplicationContext(), TripHistory.class);
            startActivity(intent);
        } else if (id == R.id.nav_chat) {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {
            LoginManager.getInstance().logOut();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static List<List<String>> getVariable()
    {
        return totList;
    }


    /**
     * Attempts to connect a user to SendBird.
     * @param userId    The unique ID of the user.
     * @param userNickname  The user's nickname, which will be displayed in chats.
     */
    private void connectToSendBird(final String userId, final String userNickname) {

        SendBird.connect(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {

                if (e != null) {
                    // Error!
                    Toast.makeText(
                            Main.this, "" + e.getCode() + ": " + e.getMessage(),
                            Toast.LENGTH_SHORT)
                            .show();

                    PreferenceUtils.setConnected(Main.this, false);
                    return;
                }

                PreferenceUtils.setConnected(Main.this, true);

                // Update the user's nickname
                updateCurrentUserInfo(userNickname);
                updateCurrentUserPushToken();

            }
        });
    }

    /**
     * Update the user's push token.
     */
    private void updateCurrentUserPushToken() {
        // Register Firebase Token
        SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(),
                new SendBird.RegisterPushTokenWithStatusHandler() {
                    @Override
                    public void onRegistered(SendBird.PushTokenRegistrationStatus pushTokenRegistrationStatus, SendBirdException e) {
                        if (e != null) {
                            // Error!
                            Toast.makeText(Main.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //Toast.makeText(Main.this, "Push token registered.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Updates the user's nickname.
     * @param userNickname  The new nickname of the user.
     */
    private void updateCurrentUserInfo(String userNickname) {
        SendBird.updateCurrentUserInfo(username, String.valueOf(photoUrl), new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(
                            Main.this, "" + e.getCode() + ":" + e.getMessage(),
                            Toast.LENGTH_SHORT)
                            .show();


                    return;
                }

            }
        });
    }
    public void alert(String s) {
        Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}