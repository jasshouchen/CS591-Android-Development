package com.example.fiveguys.trip_buddy_v0;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fiveguys.trip_buddy_v0.groupchannel.CreateGroupChannelActivity;
import com.example.fiveguys.trip_buddy_v0.groupchannel.GroupChannelListFragment;
import com.example.fiveguys.trip_buddy_v0.groupchannel.GroupChatFragment;
import com.example.fiveguys.trip_buddy_v0.main.ChatActivity;
import com.example.fiveguys.trip_buddy_v0.utils.PreferenceUtils;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareLinkContent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    String username, email, uid, age;
    Uri photoUrl;
    FirebaseDatabase database;
    DatabaseReference myRef;
    StorageReference mStorageRef;
    List<String> urilist, destinations;
    List<String> matches;
    GridView grid;
    HashMap<String, List<String>> hm;
    public static List<List<String>> list = new ArrayList<>();
    public  static List<List<String>> totList;
    private static final String TAG = Main.class.getSimpleName();
    private NavigationView mNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        urilist = new ArrayList<>();
        destinations = new ArrayList<>();
        totList = new ArrayList<>();
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



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewTrip.class);
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

        hm = new HashMap<>();

        myRef.child("users").child(uid).child("trips").addValueEventListener(new ValueEventListener
                                                                                       () {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        urilist.clear();
                        destinations.clear();
                        list.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String destid = snapshot.getKey();
                            for (DataSnapshot sp : snapshot.getChildren()) {
                                snapshot.getKey();
                                String url = sp.child("photoUrl").getValue(String.class);
                                String des = sp.child("destinationName").getValue(String.class);
                                final String strt = sp.child("startAddress").getValue(String.class);
                                String dest = sp.child("destinationAddress").getValue(String.class);
                                DatabaseReference newRef = database.getReference("trips/" + destid + "/" + strt.toString());
                                newRef.addValueEventListener(new ValueEventListener() {
                                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                                        List<String> sublist = new ArrayList<String>();
                                        for (DataSnapshot sp : dataSnapshot.getChildren()) {
                                            System.out.println("++++++++++++++++" + sp.getValue());
                                            if(sp.getValue().equals(true)){
                                                sublist.add(sp.getKey().toString());
                                            }
                                            totList.add(sublist);

                                        }
//                                        Log.d("totalList", Arrays.toString(totList.toArray()));
                                        ImageLoad adapter = new ImageLoad(Main.this, urilist, destinations,totList);
                                        grid=(GridView)findViewById(R.id.gridview);
                                        grid.setAdapter(adapter);
                                        grid.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                Toast.makeText(Main.this, "You Clicked at " +totList.get(i),
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(Main.this, CreateGroupChannelActivity.class);
                                                intent.putStringArrayListExtra("LIST", new ArrayList<String>(totList.get(i)));
                                                startActivity(intent);
                                            }
                                        });


                                    }
                                    @Override public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                                urilist.add(url);
                                destinations.add(des);

                            }
                        }


                    }




                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


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


//        myRef.child("users").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Log.d("HashMap", snapshot.getKey());
//                    for (DataSnapshot sp : snapshot.getChildren()) { // for each user
//
//                        for (DataSnapshot sp2 : sp.getChildren()) {
//                            for (DataSnapshot sp3 : sp2.getChildren()) {
////                                Log.d("EACH TRIP", sp3.getKey());
//                                String start = sp3.child("startAddress").getValue(String.class);
//                                String end = sp3.child("destinationAddress").getValue(String.class);
//                                String key = start+end;
////                                Log.i("KeyNew", key);
//                                System.out.println("Line 175" + key);
//                                if (hm.containsKey(key)) {
//                                    List<String> list = hm.get(key);
//                                    list.add(snapshot.getKey());
//                                    hm.put(key,list);
//                                }
//                            }
//                        }
//                    }
//                }
//                System.out.println("LINE 182" + Arrays.asList(hm)); // method 1
//                int matchNum = hm.size();
//                ImageLoad adapter = new ImageLoad(Main.this, urilist, destinations, matchNum);
//                grid=(GridView)findViewById(R.id.gridview);
//                grid.setAdapter(adapter);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });


        // pass matches to it

        nav_name.setText(username);
        myRef.child("users").child(uid).child("age").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.exists()) {
                    age = dataSnapshot.getValue(String.class);
                    nav_name.setText(username + ", " + age);
                    Log.d(TAG, "Age is: " + age);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        if(email != null)
        nav_email.setText(email);
        if(photoUrl != null) {
            Picasso.with(getApplicationContext()).load(photoUrl.toString()).into(nav_image);
        }
       // nav_image.setImageURI(photoUrl);


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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            Intent intent = new Intent(getApplicationContext(), MyInfo.class);// Handle the camera action
            startActivity(intent);
        } else if (id == R.id.nav_tripHistory) {

        } else if (id == R.id.nav_chat) {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

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
}
