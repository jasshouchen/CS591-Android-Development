package com.example.fiveguys.trip_buddy_v0.groupchannel;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sendbird.android.*;
import com.example.fiveguys.trip_buddy_v0.R;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.fiveguys.trip_buddy_v0.Main.totList;



// Fragment 2 is only serving the chat creating function from the main group
public class GroupChannelListFragment2 extends Fragment {

    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";
    public static final String EXTRA_GROUP_CHANNEL_URL = "GROUP_CHANNEL_URL";
    public static final int INTENT_REQUEST_NEW_GROUP_CHANNEL = 302;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private GroupChannelListAdapter mChannelListAdapter;
    private FloatingActionButton mCreateChannelFab;
    private GroupChannelListQuery mChannelListQuery;
    private SwipeRefreshLayout mSwipeRefresh;
    static List<String> matchedusridlist;
    static String matcheddestphoto;
    static String matcheddestination;

    public static GroupChannelListFragment2 newInstance(List<String> usridlist, String destphoto, String destination) {
        GroupChannelListFragment2 fragment = new GroupChannelListFragment2();
        matchedusridlist = usridlist;
        matcheddestphoto = destphoto;
        matcheddestination = destination;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("LIFECYCLE", "GroupChannelListFragment onCreateView()");

        View rootView = inflater.inflate(R.layout.fragment_group_channel_list_fragment2, container, false);

        setRetainInstance(true);

        // Change action bar title
        ((com.example.fiveguys.trip_buddy_v0.main.Chat2Activity) getActivity()).setActionBarTitle(getResources().getString(R.string.all_group_channels));

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_group_channel_list2);
        mChannelListAdapter = new GroupChannelListAdapter(getActivity());

        mSwipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout_group_channel_list2);

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                refreshChannelList(15);
            }
        });


        Intent intent = new Intent(getContext(), CreateGroupChannelActivity.class);
        intent.putStringArrayListExtra("LIST", new ArrayList<String>(matchedusridlist));
        intent.putExtra("DEST_PHOTO", matcheddestphoto);
//        Log.i("uri", matcheddestphoto);
        intent.putExtra("DESTINATION", matcheddestination);
//        Log.i("destination", matcheddestination);

        startActivityForResult(intent, INTENT_REQUEST_NEW_GROUP_CHANNEL);

        setUpRecyclerView();
        setUpChannelListAdapter();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mChannelListAdapter.load();
    }

    @Override
    public void onStop() {
        super.onStop();
        mChannelListAdapter.save();
    }

    @Override
    public void onResume() {
        Log.d("LIFECYCLE", "GroupChannelListFragment onResume()");

        refreshChannelList(15);

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                mChannelListAdapter.updateOrInsert(baseChannel);
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                mChannelListAdapter.notifyDataSetChanged();
            }
        });

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("LIFECYCLE", "GroupChannelListFragment onPause()");

        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
    }

    @Override
    public void onDetach() {
        Log.d("LIFECYCLE", "GroupChannelListFragment onDetach()");
        super.onDetach();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_REQUEST_NEW_GROUP_CHANNEL) {
            if (resultCode == RESULT_OK) {
                // Channel successfully created
                // Enter the newly created channel.
                Log.d("enterGroupChan", "12333333333wocaonimalegebidezenmezhemeduo");
                String newChannelUrl = data.getStringExtra(CreateGroupChannelActivity.EXTRA_NEW_CHANNEL_URL);
                if (newChannelUrl != null) {
                    enterGroupChannel(newChannelUrl);
                }
            } else {
                Log.d("GrChLIST", "resultCode not STATUS_OK");
            }
        }
    }

    // Sets up recycler view
    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChannelListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        // If user scrolls to bottom of the list, loads more channels.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mChannelListAdapter.getItemCount() - 1) {
                    loadNextChannelList();
                }
            }
        });
    }

    // Sets up channel list adapter
    private void setUpChannelListAdapter() {
        mChannelListAdapter.setOnItemClickListener(new GroupChannelListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupChannel channel) {

                enterGroupChannel(channel);
            }
        });

        mChannelListAdapter.setOnItemLongClickListener(new GroupChannelListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final GroupChannel channel) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Leave channel " + channel.getName() + "?")
                        .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                leaveChannel(channel);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create().show();

            }
        });
    }

    /**
     * Enters a Group Channel. Upon entering, a GroupChatFragment will be inflated
     * to display messages within the channel.
     */
    void enterGroupChannel(GroupChannel channel) {
        final String channelUrl = channel.getUrl();
        Log.i("haha",channelUrl);
        enterGroupChannel(channelUrl);
    }

    /**
     * Enters a Group Channel with a URL.
     */
    void enterGroupChannel(String channelUrl) {
        GroupChatFragment2 fragment = GroupChatFragment2.newInstance(channelUrl);
        getFragmentManager().beginTransaction()
                .replace(R.id.container_main2, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Creates a new query to get the list of the user's Group Channels,
     * then replaces the existing dataset.
     */
    private void refreshChannelList(int numChannels) {
        mChannelListQuery = GroupChannel.createMyGroupChannelListQuery();
        mChannelListQuery.setLimit(numChannels);

        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                mChannelListAdapter.setGroupChannelList(list);
            }
        });

        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    /**
     * Loads the next channels from the current query instance.
     */
    private void loadNextChannelList() {
        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                for (GroupChannel channel : list) {
                    mChannelListAdapter.addLast(channel);
                }
            }
        });
    }

    /**
     * Leaves a Group Channel.
     */
    private void leaveChannel(final GroupChannel channel) {
        channel.leave(new GroupChannel.GroupChannelLeaveHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                // Re-query message list
                refreshChannelList(30);
            }
        });
    }


}
