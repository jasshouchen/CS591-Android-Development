package com.example.fiveguys.trip_buddy_v0.groupchannel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.sendbird.android.*;
import com.example.fiveguys.trip_buddy_v0.R;
import com.example.fiveguys.trip_buddy_v0.utils.*;

import java.util.ArrayList;
import java.util.List;

import static com.example.fiveguys.trip_buddy_v0.groupchannel.GroupChannelListFragment2.matcheddestination;
import static com.example.fiveguys.trip_buddy_v0.groupchannel.GroupChannelListFragment2.matchedusridlist;

// class structure for each individual group chat. Frag2 is construction for side bar chat access.
public class GroupChatFragment2 extends Fragment {
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT";

    private static final String LOG_TAG = GroupChatFragment2.class.getSimpleName();

    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT";
    private static final String STATE_CHANNEL_URL = "STATE_CHANNEL_URL";
    private static final int INTENT_REQUEST_CHOOSE_MEDIA = 301;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 13;
    static final String EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL";

    private RelativeLayout mRootLayout;
    private RecyclerView mRecyclerView;
    private GroupChatAdapter mChatAdapter;
    private LinearLayoutManager mLayoutManager;
    private EditText mMessageEditText;
    private Button mMessageSendButton;
    private View mCurrentEventLayout;
    private TextView mCurrentEventText;

    private GroupChannel mChannel;
    private String mChannelUrl;
    private PreviousMessageListQuery mPrevMessageListQuery;

    private boolean mIsTyping;
    private static Toolbar mToolbar;


    /**
     * To create an instance of this fragment, a Channel URL should be required.
     */
    public static GroupChatFragment2 newInstance(@NonNull String channelUrl) {
        GroupChatFragment2 fragment = new GroupChatFragment2();

        Bundle args = new Bundle();
        args.putString(GroupChannelListFragment2.EXTRA_GROUP_CHANNEL_URL, channelUrl);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_chat_fragment2, container, false);

        setRetainInstance(true);


        mRootLayout = (RelativeLayout) rootView.findViewById(R.id.layout_group_chat_root2);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_group_chat2);
        mChatAdapter = new GroupChatAdapter(getActivity());

        mCurrentEventLayout = rootView.findViewById(R.id.layout_group_chat_current_event2);
        mCurrentEventText = (TextView) rootView.findViewById(R.id.text_group_chat_current_event2);

        mMessageEditText = (EditText) rootView.findViewById(R.id.edittext_group_chat_message2);
        mMessageSendButton = (Button) rootView.findViewById(R.id.button_group_chat_send2);

        mMessageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = mMessageEditText.getText().toString();

                if (userInput == null || userInput.length() <= 0) {
                    return;
                }

                sendUserMessage(userInput);
                mMessageEditText.setText("");
            }
        });


        mIsTyping = false;
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mIsTyping) {
                    setTypingStatus(true);
                }

                if (s.length() == 0) {
                    setTypingStatus(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setUpRecyclerView();

        if (savedInstanceState != null) {
            mChannelUrl = savedInstanceState.getString(STATE_CHANNEL_URL);
        } else {
            mChannelUrl = getArguments().getString(GroupChannelListFragment2.EXTRA_GROUP_CHANNEL_URL);
        }

        setHasOptionsMenu(true);

        // Set action bar title to name of channel
        updateActionBarTitle();

        return rootView;
    }

    private void refresh() {
        if (mChannel == null) {
            GroupChannel.getChannel(mChannelUrl, new GroupChannel.GroupChannelGetHandler() {
                @Override
                public void onResult(GroupChannel groupChannel, SendBirdException e) {
                    if (e != null) {
                        // Error!
                        e.printStackTrace();
                        return;
                    }

                    mChannel = groupChannel;
                    mChatAdapter.setChannel(mChannel);
                    mChatAdapter.loadLatestMessages(30, new BaseChannel.GetMessagesHandler() {
                        @Override
                        public void onResult(List<BaseMessage> list, SendBirdException e) {
                            mChatAdapter.markAllMessagesAsRead();
                        }
                    });
                    updateActionBarTitle();
                }
            });
        } else {
            mChannel.refresh(new GroupChannel.GroupChannelRefreshHandler() {
                @Override
                public void onResult(SendBirdException e) {
                    if (e != null) {
                        // Error!
                        e.printStackTrace();
                        return;
                    }

                    mChatAdapter.loadLatestMessages(30, new BaseChannel.GetMessagesHandler() {
                        @Override
                        public void onResult(List<BaseMessage> list, SendBirdException e) {
                            mChatAdapter.markAllMessagesAsRead();
                        }
                    });
                    updateActionBarTitle();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Gets channel from URL user requested

        Log.v(LOG_TAG, mChannelUrl);

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {

                if (baseChannel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.markAllMessagesAsRead();
                    // Add new message to view
                    mChatAdapter.addFirst(baseMessage);
                }
            }

            @Override
            public void onReadReceiptUpdated(GroupChannel channel) {
                if (channel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                if (channel.getUrl().equals(mChannelUrl)) {
                    List<User> typingUsers = channel.getTypingMembers();
                    displayTyping(typingUsers);
                }
            }

        });

        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
            }

            @Override
            public void onReconnectSucceeded() {
                refresh();
            }

            @Override
            public void onReconnectFailed() {
            }
        });

        if (SendBird.getConnectionState() == SendBird.ConnectionState.OPEN) {
            refresh();
        } else {
            if (SendBird.reconnect()) {
                // Will call onReconnectSucceeded()
            } else {
                String userId = PreferenceUtils.getUserId(getActivity());
                if (userId == null) {
                    Toast.makeText(getActivity(), "Require user ID to connect to SendBird.", Toast.LENGTH_LONG).show();
                    return;
                }

                SendBird.connect(userId, new SendBird.ConnectHandler() {
                    @Override
                    public void onConnected(User user, SendBirdException e) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }

                        refresh();
                    }
                });
            }
        }
    }

    @Override
    public void onPause() {
        setTypingStatus(false);

        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        mChatAdapter.load(mChannelUrl);
    }

    @Override
    public void onStop() {
        super.onStop();
        mChatAdapter.save();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_CHANNEL_URL, mChannelUrl);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_group_chat2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_group_channel_invite2) {
            Intent intent = new Intent(getActivity(), InviteMemberActivity.class);
            intent.putStringArrayListExtra("LIST", new ArrayList<String>(matchedusridlist));
            Log.d("list66666", String.valueOf(matchedusridlist));
            intent.putExtra(EXTRA_CHANNEL_URL, mChannelUrl);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_group_channel_view_members2) {
            Intent intent = new Intent(getActivity(), MemberListActivity.class);
            intent.putExtra(EXTRA_CHANNEL_URL, mChannelUrl);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChatAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mChatAdapter.getItemCount() - 1) {
                    mChatAdapter.loadPreviousMessages(30, null);
                }
            }
        });
    }


    /**
     * Display which users are typing.
     * If more than two users are currently typing, this will state that "multiple users" are typing.
     *
     * @param typingUsers The list of currently typing users.
     */
    private void displayTyping(List<User> typingUsers) {

        if (typingUsers.size() > 0) {
            mCurrentEventLayout.setVisibility(View.VISIBLE);
            String string;

            if (typingUsers.size() == 1) {
                string = typingUsers.get(0).getNickname() + " is typing";
            } else if (typingUsers.size() == 2) {
                string = typingUsers.get(0).getNickname() + " " + typingUsers.get(1).getNickname() + " is typing";
            } else {
                string = "Multiple users are typing";
            }
            mCurrentEventText.setText(string);
        } else {
            mCurrentEventLayout.setVisibility(View.GONE);
        }
    }



    private void updateActionBarTitle() {
        String title = "";
        if(mChannel != null) {
           title = mChannel.getName();
        }

        // Set action bar title to name of channel
        ((com.example.fiveguys.trip_buddy_v0.main.Chat2Activity)getActivity()).setActionBarTitle(title);
    }


    private void sendUserMessageWithUrl(final String text, String url) {
        new WebUtils.UrlPreviewAsyncTask() {
            @Override
            protected void onPostExecute(UrlPreviewInfo info) {
                UserMessage tempUserMessage = null;
                BaseChannel.SendUserMessageHandler handler = new BaseChannel.SendUserMessageHandler() {
                    @Override
                    public void onSent(UserMessage userMessage, SendBirdException e) {
                        if (e != null) {
                            // Error!
                            Log.e(LOG_TAG, e.toString());
                            Toast.makeText(
                                    getActivity(),
                                    "Send failed with error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                            mChatAdapter.markMessageFailed(userMessage.getRequestId());
                            return;
                        }

                        // Update a sent message to RecyclerView
                        mChatAdapter.markMessageSent(userMessage);
                    }
                };

                try {
                    // Sending a message with URL preview information and custom type.
                    String jsonString = info.toJsonString();
                    tempUserMessage = mChannel.sendUserMessage(text, jsonString, GroupChatAdapter.URL_PREVIEW_CUSTOM_TYPE, handler);
                } catch (Exception e) {
                    // Sending a message without URL preview information.
                    tempUserMessage = mChannel.sendUserMessage(text, handler);
                }


                // Display a user message to RecyclerView
                mChatAdapter.addFirst(tempUserMessage);
            }
        }.execute(url);
    }

    private void sendUserMessage(String text) {
        List<String> urls = WebUtils.extractUrls(text);
        if (urls.size() > 0) {
            sendUserMessageWithUrl(text, urls.get(0));
            return;
        }

        UserMessage tempUserMessage = mChannel.sendUserMessage(text, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Log.e(LOG_TAG, e.toString());
                    Toast.makeText(
                            getActivity(),
                            "Send failed with error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    mChatAdapter.markMessageFailed(userMessage.getRequestId());
                    return;
                }

                // Update a sent message to RecyclerView
                mChatAdapter.markMessageSent(userMessage);
            }
        });

        // Display a user message to RecyclerView
        mChatAdapter.addFirst(tempUserMessage);
    }

    /**
     * Notify other users whether the current user is typing.
     *
     * @param typing Whether the user is currently typing.
     */
    private void setTypingStatus(boolean typing) {
        if (mChannel == null) {
            return;
        }

        if (typing) {
            mIsTyping = true;
            mChannel.startTyping();
        } else {
            mIsTyping = false;
            mChannel.endTyping();
        }
    }
}
