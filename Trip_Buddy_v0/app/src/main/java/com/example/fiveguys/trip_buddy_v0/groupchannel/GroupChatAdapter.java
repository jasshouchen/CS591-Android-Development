package com.example.fiveguys.trip_buddy_v0.groupchannel;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.example.fiveguys.trip_buddy_v0.R;
import com.example.fiveguys.trip_buddy_v0.utils.DateUtils;
import com.example.fiveguys.trip_buddy_v0.utils.FileUtils;
import com.example.fiveguys.trip_buddy_v0.utils.ImageUtils;
import com.example.fiveguys.trip_buddy_v0.utils.TextUtils;
import com.example.fiveguys.trip_buddy_v0.utils.UrlPreviewInfo;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String URL_PREVIEW_CUSTOM_TYPE = "url_preview";

    private static final int VIEW_TYPE_USER_MESSAGE_ME = 10;
    private static final int VIEW_TYPE_USER_MESSAGE_OTHER = 11;

    private static final int VIEW_TYPE_ADMIN_MESSAGE = 30;


    private Context mContext;
    private GroupChannel mChannel;
    private List<BaseMessage> mMessageList;

    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    private ArrayList<String> mFailedMessageIdList = new ArrayList<>();
    private Hashtable<String, Uri> mTempFileMessageUriTable = new Hashtable<>();
    private boolean mIsMessageListLoading;

    interface OnItemLongClickListener {
        void onUserMessageItemLongClick(UserMessage message);

        void onFileMessageItemLongClick(FileMessage message);

        void onAdminMessageItemLongClick(AdminMessage message);
    }

    interface OnItemClickListener {
        void onUserMessageItemClick(UserMessage message);

        void onFileMessageItemClick(FileMessage message);
    }


    GroupChatAdapter(Context context) {
        mMessageList = new ArrayList<>();
        mContext = context;
    }

    public void load(String channelUrl) {
        try {
            File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
            appDir.mkdirs();

            File dataFile = new File(appDir, TextUtils.generateMD5(SendBird.getCurrentUser().getUserId() + channelUrl) + ".data");

            String content = FileUtils.loadFromFile(dataFile);
            String [] dataArray = content.split("\n");

            mChannel = (GroupChannel) GroupChannel.buildFromSerializedData(Base64.decode(dataArray[0], Base64.DEFAULT | Base64.NO_WRAP));

            for(int i = 1; i < dataArray.length; i++) {
                mMessageList.add(BaseMessage.buildFromSerializedData(Base64.decode(dataArray[i], Base64.DEFAULT | Base64.NO_WRAP)));
            }

            notifyDataSetChanged();
        } catch(Exception e) {
            // Nothing to load.
        }
    }

    public void save() {
        try {
            StringBuilder sb = new StringBuilder();
            if (mChannel != null) {
                // Convert current data into string.
                sb.append(Base64.encodeToString(mChannel.serialize(), Base64.DEFAULT | Base64.NO_WRAP));
                BaseMessage message = null;
                for (int i = 0; i < Math.min(mMessageList.size(), 100); i++) {
                    message = mMessageList.get(i);
                    if (!isTempMessage(message)) {
                        sb.append("\n");
                        sb.append(Base64.encodeToString(message.serialize(), Base64.DEFAULT | Base64.NO_WRAP));
                    }
                }

                String data = sb.toString();
                String md5 = TextUtils.generateMD5(data);

                // Save the data into file.
                File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
                appDir.mkdirs();

                File hashFile = new File(appDir, TextUtils.generateMD5(SendBird.getCurrentUser().getUserId() + mChannel.getUrl()) + ".hash");
                File dataFile = new File(appDir, TextUtils.generateMD5(SendBird.getCurrentUser().getUserId() + mChannel.getUrl()) + ".data");

                try {
                    String content = FileUtils.loadFromFile(hashFile);
                    // If data has not been changed, do not save.
                    if(md5.equals(content)) {
                        return;
                    }
                } catch(IOException e) {
                    // File not found. Save the data.
                }

                FileUtils.saveToFile(dataFile, data);
                FileUtils.saveToFile(hashFile, md5);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inflates the correct layout according to the View Type.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                View myUserMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_user_me, parent, false);
                return new MyUserMessageHolder(myUserMsgView);
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                View otherUserMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_user_other, parent, false);
                return new OtherUserMessageHolder(otherUserMsgView);
            case VIEW_TYPE_ADMIN_MESSAGE:
                View adminMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_admin, parent, false);
                return new AdminMessageHolder(adminMsgView);

            default:
                return null;

        }
    }

    /**
     * Binds variables in the BaseMessage to UI components in the ViewHolder.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseMessage message = mMessageList.get(position);
        boolean isContinuous = false;
        boolean isNewDay = false;
        boolean isTempMessage = false;
        boolean isFailedMessage = false;
        Uri tempFileMessageUri = null;

        // If there is at least one item preceding the current one, check the previous message.
        if (position < mMessageList.size() - 1) {
            BaseMessage prevMessage = mMessageList.get(position + 1);

            // If the date of the previous message is different, display the date before the message,
            // and also set isContinuous to false to show information such as the sender's nickname
            // and profile image.
            if (!DateUtils.hasSameDate(message.getCreatedAt(), prevMessage.getCreatedAt())) {
                isNewDay = true;
                isContinuous = false;
            } else {
                isContinuous = isContinuous(message, prevMessage);
            }
        } else if (position == mMessageList.size() - 1) {
            isNewDay = true;
        }

        isTempMessage = isTempMessage(message);
        tempFileMessageUri = getTempFileMessageUri(message);
        isFailedMessage = isFailedMessage(message);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                ((MyUserMessageHolder) holder).bind(mContext, (UserMessage) message, mChannel, isContinuous, isNewDay, isTempMessage, isFailedMessage, mItemClickListener, mItemLongClickListener);
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                ((OtherUserMessageHolder) holder).bind(mContext, (UserMessage) message, mChannel, isNewDay, isContinuous, mItemClickListener, mItemLongClickListener);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                ((AdminMessageHolder) holder).bind(mContext, (AdminMessage) message, mChannel, isNewDay);
                break;
            default:
                break;
        }
    }

    /**
     * Declares the View Type according to the type of message and the sender.
     */
    @Override
    public int getItemViewType(int position) {

        BaseMessage message = mMessageList.get(position);

        if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage) message;
            // If the sender is current user
            if (userMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                return VIEW_TYPE_USER_MESSAGE_ME;
            } else {
                return VIEW_TYPE_USER_MESSAGE_OTHER;
            }
        } else if (message instanceof AdminMessage) {
            return VIEW_TYPE_ADMIN_MESSAGE;
        }

        return -1;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    void setChannel(GroupChannel channel) {
        mChannel = channel;
    }

    public boolean isTempMessage(BaseMessage message) {
        return message.getMessageId() == 0;
    }

    public boolean isFailedMessage(BaseMessage message) {
        if (!isTempMessage(message)) {
            return false;
        }

        if (message instanceof UserMessage) {
            int index = mFailedMessageIdList.indexOf(((UserMessage) message).getRequestId());
            if (index >= 0) {
                return true;
            }
        } else if (message instanceof FileMessage) {
            int index = mFailedMessageIdList.indexOf(((FileMessage) message).getRequestId());
            if (index >= 0) {
                return true;
            }
        }

        return false;
    }


    public Uri getTempFileMessageUri(BaseMessage message) {
        if (!isTempMessage(message)) {
            return null;
        }

        if (!(message instanceof FileMessage)) {
            return null;
        }

        return mTempFileMessageUriTable.get(((FileMessage) message).getRequestId());
    }

    public void markMessageFailed(String requestId) {
        mFailedMessageIdList.add(requestId);
        notifyDataSetChanged();
    }


    public void markMessageSent(BaseMessage message) {
        Object msg = null;

        for (int i = mMessageList.size() - 1; i >= 0; i--) {
            msg = mMessageList.get(i);
            if (message instanceof UserMessage && msg instanceof UserMessage) {
                if (((UserMessage) msg).getRequestId().equals(((UserMessage) message).getRequestId())) {
                    mMessageList.set(i, message);
                    notifyDataSetChanged();
                    return;
                }
            } else if (message instanceof FileMessage && msg instanceof FileMessage) {
                if (((FileMessage) msg).getRequestId().equals(((FileMessage) message).getRequestId())) {
                    mTempFileMessageUriTable.remove(((FileMessage) message).getRequestId());
                    mMessageList.set(i, message);
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }


    void addFirst(BaseMessage message) {
        mMessageList.add(0, message);
        notifyDataSetChanged();
    }

    private synchronized boolean isMessageListLoading() {
        return mIsMessageListLoading;
    }

    private synchronized void setMessageListLoading(boolean tf) {
        mIsMessageListLoading = tf;
    }

    /**
     * Notifies that the user has read all (previously unread) messages in the channel.
     * Typically, this would be called immediately after the user enters the chat and loads
     * its most recent messages.
     */
    public void markAllMessagesAsRead() {
        mChannel.markAsRead();
        notifyDataSetChanged();
    }

     /**
     * Load old message list.
     * @param limit
     * @param handler
     */
    public void loadPreviousMessages(int limit, final BaseChannel.GetMessagesHandler handler) {
        if(isMessageListLoading()) {
            return;
        }

        long oldestMessageCreatedAt = Long.MAX_VALUE;
        if(mMessageList.size() > 0) {
            oldestMessageCreatedAt = mMessageList.get(mMessageList.size() - 1).getCreatedAt();
        }

        setMessageListLoading(true);
        mChannel.getPreviousMessagesByTimestamp(oldestMessageCreatedAt, false, limit, true, BaseChannel.MessageTypeFilter.ALL, null, new BaseChannel.GetMessagesHandler() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                if(handler != null) {
                    handler.onResult(list, e);
                }

                setMessageListLoading(false);
                if(e != null) {
                    e.printStackTrace();
                    return;
                }

                for(BaseMessage message : list) {
                    mMessageList.add(message);
                }

                notifyDataSetChanged();
            }
        });
    }

    /**
     * Replaces current message list with new list.
     * Should be used only on initial load or refresh.
     */
    public void loadLatestMessages(int limit, final BaseChannel.GetMessagesHandler handler) {
        if(isMessageListLoading()) {
            return;
        }

        setMessageListLoading(true);
        mChannel.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, limit, true, BaseChannel.MessageTypeFilter.ALL, null, new BaseChannel.GetMessagesHandler() {
           @Override
           public void onResult(List<BaseMessage> list, SendBirdException e) {
               if(handler != null) {
                   handler.onResult(list, e);
               }

               setMessageListLoading(false);
               if(e != null) {
                   e.printStackTrace();
                   return;
               }

               if(list.size() <= 0) {
                   return;
               }

               for (BaseMessage message : mMessageList) {
                   if (isTempMessage(message) || isFailedMessage(message)) {
                       list.add(0, message);
                   }
               }

               mMessageList.clear();

               for(BaseMessage message : list) {
                   mMessageList.add(message);
               }

               notifyDataSetChanged();
           }
        });
    }

    public void setItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    public void setItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * Checks if the current message was sent by the same person that sent the preceding message.
     * <p>
     * This is done so that redundant UI, such as sender nickname and profile picture,
     * does not have to displayed when not necessary.
     */
    private boolean isContinuous(BaseMessage currentMsg, BaseMessage precedingMsg) {
        // null check
        if (currentMsg == null || precedingMsg == null) {
            return false;
        }

        if (currentMsg instanceof AdminMessage && precedingMsg instanceof AdminMessage) {
            return true;
        }

        User currentUser = null, precedingUser = null;

        if (currentMsg instanceof UserMessage) {
            currentUser = ((UserMessage) currentMsg).getSender();
        } else if (currentMsg instanceof FileMessage) {
            currentUser = ((FileMessage) currentMsg).getSender();
        }

        if (precedingMsg instanceof UserMessage) {
            precedingUser = ((UserMessage) precedingMsg).getSender();
        } else if (precedingMsg instanceof FileMessage) {
            precedingUser = ((FileMessage) precedingMsg).getSender();
        }

        // If admin message or
        return !(currentUser == null || precedingUser == null)
                && currentUser.getUserId().equals(precedingUser.getUserId());


    }


    private static class AdminMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageText, dateText;

        AdminMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_group_chat_message);
            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);
        }

        void bind(Context context, AdminMessage message, GroupChannel channel, boolean isNewDay) {
            messageText.setText(message.getMessage());

            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
            } else {
                dateText.setVisibility(View.GONE);
            }
        }
    }

    private static class MyUserMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, readReceiptText, dateText;
        ViewGroup urlPreviewContainer;
        TextView urlPreviewSiteNameText, urlPreviewTitleText, urlPreviewDescriptionText;
        ImageView urlPreviewMainImageView;
        View padding;

        MyUserMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_group_chat_message);
            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);

            urlPreviewContainer = (ViewGroup) itemView.findViewById(R.id.url_preview_container);
            urlPreviewSiteNameText = (TextView) itemView.findViewById(R.id.text_url_preview_site_name);
            urlPreviewTitleText = (TextView) itemView.findViewById(R.id.text_url_preview_title);
            urlPreviewDescriptionText = (TextView) itemView.findViewById(R.id.text_url_preview_description);
            urlPreviewMainImageView = (ImageView) itemView.findViewById(R.id.image_url_preview_main);

            // Dynamic padding that can be hidden or shown based on whether the message is continuous.
            padding = itemView.findViewById(R.id.view_group_chat_padding);
        }

        void bind(Context context, final UserMessage message, GroupChannel channel, boolean isContinuous, boolean isNewDay, boolean isTempMessage, boolean isFailedMessage, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener) {
            messageText.setText(message.getMessage());
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            if (isFailedMessage) {
                readReceiptText.setText(R.string.message_failed);
                readReceiptText.setVisibility(View.VISIBLE);
            } else if (isTempMessage) {
                readReceiptText.setText(R.string.message_sending);
                readReceiptText.setVisibility(View.VISIBLE);
            } else {

                // Since setChannel is set slightly after adapter is created
                if (channel != null) {
                    int readReceipt = channel.getReadReceipt(message);
                    if (readReceipt > 0) {
                        readReceiptText.setVisibility(View.VISIBLE);
                        readReceiptText.setText(String.valueOf(readReceipt));
                    } else {
                        readReceiptText.setVisibility(View.INVISIBLE);
                    }
                }
            }


            // If continuous from previous message, remove extra padding.
            if (isContinuous) {
                padding.setVisibility(View.GONE);
            } else {
                padding.setVisibility(View.VISIBLE);
            }

            // If the message is sent on a different date than the previous one, display the date.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            urlPreviewContainer.setVisibility(View.GONE);
            if (message.getCustomType().equals(URL_PREVIEW_CUSTOM_TYPE)) {
                try {
                    urlPreviewContainer.setVisibility(View.VISIBLE);
                    final UrlPreviewInfo previewInfo = new UrlPreviewInfo(message.getData());
                    urlPreviewSiteNameText.setText("@" + previewInfo.getSiteName());
                    urlPreviewTitleText.setText(previewInfo.getTitle());
                    urlPreviewDescriptionText.setText(previewInfo.getDescription());
                    ImageUtils.displayImageFromUrl(context, previewInfo.getImageUrl(), urlPreviewMainImageView);
                } catch (JSONException e) {
                    urlPreviewContainer.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }

            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onUserMessageItemClick(message);
                    }
                });
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onUserMessageItemLongClick(message);
                        return true;
                    }
                });
            }
        }

    }

    private static class OtherUserMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, nicknameText, timeText, readReceiptText, dateText;
        ImageView profileImage;

        ViewGroup urlPreviewContainer;
        TextView urlPreviewSiteNameText, urlPreviewTitleText, urlPreviewDescriptionText;
        ImageView urlPreviewMainImageView;

        public OtherUserMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_group_chat_message);
            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            nicknameText = (TextView) itemView.findViewById(R.id.text_group_chat_nickname);
            profileImage = (ImageView) itemView.findViewById(R.id.image_group_chat_profile);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);

            urlPreviewContainer = (ViewGroup) itemView.findViewById(R.id.url_preview_container);
            urlPreviewSiteNameText = (TextView) itemView.findViewById(R.id.text_url_preview_site_name);
            urlPreviewTitleText = (TextView) itemView.findViewById(R.id.text_url_preview_title);
            urlPreviewDescriptionText = (TextView) itemView.findViewById(R.id.text_url_preview_description);
            urlPreviewMainImageView = (ImageView) itemView.findViewById(R.id.image_url_preview_main);
        }


        void bind(Context context, final UserMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener) {

            // Since setChannel is set slightly after adapter is created
            if (channel != null) {
                int readReceipt = channel.getReadReceipt(message);
                if (readReceipt > 0) {
                    readReceiptText.setVisibility(View.VISIBLE);
                    readReceiptText.setText(String.valueOf(readReceipt));
                } else {
                    readReceiptText.setVisibility(View.INVISIBLE);
                }
            }

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
                profileImage.setVisibility(View.INVISIBLE);
                nicknameText.setVisibility(View.GONE);
            } else {
                profileImage.setVisibility(View.VISIBLE);
                ImageUtils.displayRoundImageFromUrl(context, message.getSender().getProfileUrl(), profileImage);

                nicknameText.setVisibility(View.VISIBLE);
                nicknameText.setText(message.getSender().getNickname());
            }

            messageText.setText(message.getMessage());
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            urlPreviewContainer.setVisibility(View.GONE);
            if (message.getCustomType().equals(URL_PREVIEW_CUSTOM_TYPE)) {
                try {
                    urlPreviewContainer.setVisibility(View.VISIBLE);
                    UrlPreviewInfo previewInfo = new UrlPreviewInfo(message.getData());
                    urlPreviewSiteNameText.setText("@" + previewInfo.getSiteName());
                    urlPreviewTitleText.setText(previewInfo.getTitle());
                    urlPreviewDescriptionText.setText(previewInfo.getDescription());
                    ImageUtils.displayImageFromUrl(context, previewInfo.getImageUrl(), urlPreviewMainImageView);
                } catch (JSONException e) {
                    urlPreviewContainer.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }


            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onUserMessageItemClick(message);
                    }
                });
            }
            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onUserMessageItemLongClick(message);
                        return true;
                    }
                });
            }
        }
    }

}



