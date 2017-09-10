package com.example.fiveguys.trip_buddy_v0.groupchannel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.fiveguys.trip_buddy_v0.Main;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;
import com.example.fiveguys.trip_buddy_v0.R;

import java.util.ArrayList;
import java.util.List;

/**
 * An Activity to create a new Group Channel.
 * First displays a selectable list of users,
 * then shows an option to create a Distinct channel.
 */

public class CreateGroupChannelActivity extends AppCompatActivity
        implements SelectUserFragment.UsersSelectedListener, SelectDistinctFragment.DistinctSelectedListener {

    public static final String EXTRA_NEW_CHANNEL_URL = "EXTRA_NEW_CHANNEL_URL";

    static final int STATE_SELECT_USERS = 0;
    static final int STATE_SELECT_DISTINCT = 1;

    private Button mNextButton, mCreateButton;

    private List<String> mSelectedIds;
    private boolean mIsDistinct;
    private String mChannelName;

    private int mCurrentState;

    private Toolbar mToolbar;
    private List<String> usridlist;
    private String destination;
    private String photoUri;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // passing the user info variables from the last activity
        setContentView(R.layout.activity_create_group_channel);
        usridlist = new ArrayList<>();
        usridlist = getIntent().getStringArrayListExtra("LIST");
        destination = getIntent().getStringExtra("DESTINATION");
        photoUri = getIntent().getStringExtra("DEST_PHOTO");

        Log.d("userid", usridlist.toString());
        mSelectedIds = new ArrayList<>();


        // show all the users that could be selected into caht
        if (savedInstanceState == null) {
            Fragment fragment = SelectUserFragment.newInstance(usridlist);
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container_create_group_channel, fragment)
                    .commit();
        }

        mNextButton = (Button) findViewById(R.id.button_create_group_channel_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentState == STATE_SELECT_USERS) {
                    createGroupChannel(mSelectedIds, true);
                }
            }
        });
        mNextButton.setEnabled(false);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_create_group_channel);
        setSupportActionBar(mToolbar);


    }

    void setState(int state) {
        if (state == STATE_SELECT_USERS) {
            mCurrentState = STATE_SELECT_USERS;
            mNextButton.setVisibility(View.VISIBLE);
        } else if (state == STATE_SELECT_DISTINCT){
            mCurrentState = STATE_SELECT_DISTINCT;
            mNextButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUserSelected(boolean selected, String userId) {
        if (selected) {
            mSelectedIds.add(userId);
        } else {
            mSelectedIds.remove(userId);
        }

        if (mSelectedIds.size() > 0) {
            mNextButton.setEnabled(true);
        } else {
            mNextButton.setEnabled(false);
        }
    }

    @Override
    public void onDistinctSelected(boolean distinct) {
        mIsDistinct = distinct;
    }

    /**
     * Creates a new Group Channel.
     * Note that if you have not included empty channels in your GroupChannelListQuery,
     * the channel will not be shown in the user's channel list until at least one message
     * has been sent inside.
     */
    private void createGroupChannel(List<String> userIds, boolean distinct) {
        // updating the group chat's name and picture with our own pictures that matches our trips
        GroupChannel.createChannelWithUserIds(userIds, true, destination, photoUri, "", new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(EXTRA_NEW_CHANNEL_URL, groupChannel.getUrl());
                setResult(RESULT_OK, intent);
                finish();

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CreateGroupChannelActivity.this, Main.class);
        startActivity(intent);
        finish();
    }
}
