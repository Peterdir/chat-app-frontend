package com.example.chat_app_frontend.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Message;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.utils.ProfileUIUtils;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMessageInteractionListener {
        void onMessageLongPressed(Message message);
        void onReactionChipClicked(Message message, String emoji);
        void onUserClicked(String userId);
        void onImageClicked(String imageUrlOrBase64);
    }

    private static final int VIEW_TYPE_GROUP_START = 0;
    private static final int VIEW_TYPE_CONTINUATION = 1;
    private static final int VIEW_TYPE_DATE = 2;

    // Palette for avatar backgrounds based on sender name hash
    private static final int[] AVATAR_COLORS = {
            0xFF5865F2, // blurple
            0xFF23A559, // green
            0xFFEB459E, // pink
            0xFFFEE75C, // yellow
            0xFF57F287, // light green
            0xFFED4245, // red
    };

    private final List<Message> messages;
    private final OnMessageInteractionListener interactionListener;
    private final String currentUserId;

    private final Map<RecyclerView.ViewHolder, ValueEventListener> listeners = new java.util.HashMap<>();
    private final Map<RecyclerView.ViewHolder, String> holderToUid = new java.util.HashMap<>();

    private static final Pattern GIPHY_PAYLOAD_PATTERN =
            Pattern.compile("^\\[GIF\\]\\s+giphy://([A-Za-z0-9]+)$");

    public MessageAdapter(List<Message> messages) {
        this(messages, null, "");
    }

    public MessageAdapter(List<Message> messages,
                          OnMessageInteractionListener interactionListener,
                          String currentUserId) {
        this.messages = messages;
        this.interactionListener = interactionListener;
        this.currentUserId = currentUserId != null ? currentUserId : "";
    }

    @Override
    public int getItemViewType(int position) {
        String type = messages.get(position).getMessageType();
        if (Message.TYPE_DATE_DIVIDER.equals(type)) return VIEW_TYPE_DATE;
        if (messages.get(position).isFirstInGroup()) return VIEW_TYPE_GROUP_START;
        return VIEW_TYPE_CONTINUATION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_DATE) {
            View v = inflater.inflate(R.layout.item_date_divider, parent, false);
            return new DateViewHolder(v);
        } else if (viewType == VIEW_TYPE_GROUP_START) {
            View v = inflater.inflate(R.layout.item_message_group_start, parent, false);
            return new GroupStartViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_message_continuation, parent, false);
            return new ContinuationViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).bind(msg);
        } else if (holder instanceof GroupStartViewHolder) {
            GroupStartViewHolder groupHolder = (GroupStartViewHolder) holder;
            groupHolder.bind(msg, interactionListener, currentUserId);
            
            removeListener(groupHolder);
            String uid = msg.getSenderId();
            if (uid != null && !uid.trim().isEmpty()) {
                holderToUid.put(groupHolder, uid);
                ValueEventListener listener = UserRepository.getInstance().observeUser(uid, new UserRepository.OnUserLoadedListener() {
                    @Override
                    public void onUserLoaded(User updatedUser) {
                        bindUserToGroupHolder(groupHolder, updatedUser);
                    }
                    @Override
                    public void onUserNotFound() {}
                    @Override
                    public void onFailure(String error) {}
                });
                listeners.put(groupHolder, listener);
            }
        } else if (holder instanceof ContinuationViewHolder) {
            ((ContinuationViewHolder) holder).bind(msg, interactionListener, currentUserId);
        }
    }

    private void bindUserToGroupHolder(GroupStartViewHolder holder, User user) {
        String display = user.getDisplayNameOrUserName();
        if (display == null || display.trim().isEmpty()) {
            display = "Unknown";
        }
        
        ProfileUIUtils.loadUserProfile(holder.itemView.getContext(), user, 
                holder.imgAvatar, holder.imgDecoration, holder.imgNamePlate, null);
                
        if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            holder.imgAvatar.setVisibility(View.GONE);
            holder.tvAvatarInitial.setVisibility(View.VISIBLE);
            holder.tvAvatarInitial.setText(display.isEmpty() ? "?" : String.valueOf(display.charAt(0)).toUpperCase());
            int idx = (user.getFirebaseUid() != null ? user.getFirebaseUid().hashCode() : 0) & 0x7FFFFFFF;
            holder.cvAvatar.setCardBackgroundColor(AVATAR_COLORS[idx % AVATAR_COLORS.length]);
        } else {
            holder.imgAvatar.setVisibility(View.VISIBLE);
            holder.tvAvatarInitial.setVisibility(View.GONE);
        }

        // Handle Avatar Click
        holder.cvAvatar.setOnClickListener(v -> {
            if (interactionListener != null) {
                interactionListener.onUserClicked(user.getFirebaseUid());
            }
        });
        
        holder.tvSenderName.setOnClickListener(v -> {
            if (interactionListener != null) {
                interactionListener.onUserClicked(user.getFirebaseUid());
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        removeListener(holder);
    }

    private void removeListener(RecyclerView.ViewHolder holder) {
        if (listeners.containsKey(holder) && holderToUid.containsKey(holder)) {
            ValueEventListener oldListener = listeners.get(holder);
            String oldUid = holderToUid.get(holder);
            UserRepository.getInstance().removeListener(oldUid, oldListener);
            listeners.remove(holder);
            holderToUid.remove(holder);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private static String extractGiphyGifUrl(String content) {
        if (content == null) return null;
        Matcher matcher = GIPHY_PAYLOAD_PATTERN.matcher(content.trim());
        if (!matcher.matches()) return null;
        String mediaId = matcher.group(1);
        if (mediaId == null || mediaId.isEmpty()) return null;
        return "https://media.giphy.com/media/" + mediaId + "/giphy.gif";
    }

    private static void bindReplyQuote(Message msg,
                                       LinearLayout llReplyQuote,
                                       TextView tvReplySender,
                                       TextView tvReplyContent) {
        String replySender = msg.getReplyToSenderName();
        String replyContent = msg.getReplyToContent();
        if ((replySender == null || replySender.trim().isEmpty())
                && (replyContent == null || replyContent.trim().isEmpty())) {
            llReplyQuote.setVisibility(View.GONE);
            return;
        }

        llReplyQuote.setVisibility(View.VISIBLE);
        tvReplySender.setText(replySender != null && !replySender.trim().isEmpty()
                ? replySender
                : "Tin nhắn được trả lời");
        tvReplyContent.setText(replyContent != null && !replyContent.trim().isEmpty()
                ? replyContent
                : "Tin nhắn gốc không khả dụng");
    }

    private static List<ReactionEntry> buildReactionEntries(Map<String, Map<String, Boolean>> reactions) {
        List<ReactionEntry> entries = new ArrayList<>();
        if (reactions == null) {
            return entries;
        }

        for (Map.Entry<String, Map<String, Boolean>> entry : reactions.entrySet()) {
            String emoji = entry.getKey();
            Map<String, Boolean> users = entry.getValue();
            if (emoji == null || emoji.trim().isEmpty() || users == null || users.isEmpty()) {
                continue;
            }

            int count = 0;
            for (Boolean reacted : users.values()) {
                if (Boolean.TRUE.equals(reacted)) {
                    count++;
                }
            }
            if (count > 0) {
                entries.add(new ReactionEntry(emoji, count, users));
            }
        }

        Collections.sort(entries, Comparator
                .comparingInt(ReactionEntry::getCount)
                .reversed()
                .thenComparing(ReactionEntry::getEmoji));
        return entries;
    }

    private static void bindReactionChips(Message msg,
                                          LinearLayout llReactions,
                                          TextView tvReaction1,
                                          TextView tvReaction2,
                                          OnMessageInteractionListener listener,
                                          String currentUserId) {
        List<ReactionEntry> entries = buildReactionEntries(msg.getReactions());
        if (entries.isEmpty()) {
            llReactions.setVisibility(View.GONE);
            tvReaction1.setVisibility(View.GONE);
            tvReaction2.setVisibility(View.GONE);
            tvReaction1.setOnClickListener(null);
            tvReaction2.setOnClickListener(null);
            return;
        }

        llReactions.setVisibility(View.VISIBLE);
        bindReactionChip(tvReaction1, entries.get(0), msg, listener, currentUserId);

        if (entries.size() > 1) {
            bindReactionChip(tvReaction2, entries.get(1), msg, listener, currentUserId);
        } else {
            tvReaction2.setVisibility(View.GONE);
            tvReaction2.setOnClickListener(null);
        }
    }

    private static void bindReactionChip(TextView chip,
                                         ReactionEntry entry,
                                         Message message,
                                         OnMessageInteractionListener listener,
                                         String currentUserId) {
        chip.setVisibility(View.VISIBLE);
        chip.setText(entry.getEmoji() + " " + entry.getCount());

        boolean reactedByMe = currentUserId != null
                && !currentUserId.trim().isEmpty()
                && Boolean.TRUE.equals(entry.getUsers().get(currentUserId));

        chip.setBackgroundResource(reactedByMe
                ? R.drawable.bg_reaction_chip_selected
                : R.drawable.bg_reaction_chip);
        chip.setTextColor(reactedByMe ? 0xFF5865F2 : 0xFFF2F3F5);

        if (listener == null) {
            chip.setOnClickListener(null);
            return;
        }

        chip.setOnClickListener(v -> listener.onReactionChipClicked(message, entry.getEmoji()));
    }

    private static class ReactionEntry {
        private final String emoji;
        private final int count;
        private final Map<String, Boolean> users;

        ReactionEntry(String emoji, int count, Map<String, Boolean> users) {
            this.emoji = emoji;
            this.count = count;
            this.users = users;
        }

        String getEmoji() {
            return emoji;
        }

        int getCount() {
            return count;
        }

        Map<String, Boolean> getUsers() {
            return users;
        }
    }

    // ---- ViewHolders ----

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateLabel;

        DateViewHolder(View itemView) {
            super(itemView);
            tvDateLabel = itemView.findViewById(R.id.tv_date_label);
        }

        void bind(Message msg) {
            tvDateLabel.setText(msg.getDateLabel());
        }
    }

    static class GroupStartViewHolder extends RecyclerView.ViewHolder {
        CardView cvAvatar;
        ImageView imgAvatar;
        ImageView imgDecoration;
        ImageView imgNamePlate;
        TextView tvAvatarInitial;
        TextView tvSenderName;
        TextView tvTimestamp;
        LinearLayout llReplyQuote;
        TextView tvReplySender;
        TextView tvReplyContent;
        TextView tvMessageContent;
        CardView cvImageAttachment;
        ImageView imgAttachment;
        LinearLayout llReactions;
        TextView tvReaction1;
        TextView tvReaction2;

        GroupStartViewHolder(View itemView) {
            super(itemView);
            cvAvatar = itemView.findViewById(R.id.cv_avatar);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            imgDecoration = itemView.findViewById(R.id.img_decoration);
            imgNamePlate = itemView.findViewById(R.id.img_name_plate);
            tvAvatarInitial = itemView.findViewById(R.id.tv_avatar_initial);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            llReplyQuote = itemView.findViewById(R.id.ll_reply_quote);
            tvReplySender = itemView.findViewById(R.id.tv_reply_sender);
            tvReplyContent = itemView.findViewById(R.id.tv_reply_content);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
            cvImageAttachment = itemView.findViewById(R.id.cv_image_attachment);
            imgAttachment = itemView.findViewById(R.id.img_attachment);
            llReactions = itemView.findViewById(R.id.ll_reactions);
            tvReaction1 = itemView.findViewById(R.id.tv_reaction_1);
            tvReaction2 = itemView.findViewById(R.id.tv_reaction_2);
        }

        void bind(Message msg,
                  OnMessageInteractionListener listener,
                  String currentUserId) {
            // Avatar
            if (msg.getSenderAvatarResId() != 0) {
                imgAvatar.setImageResource(msg.getSenderAvatarResId());
                imgAvatar.setVisibility(View.VISIBLE);
                tvAvatarInitial.setVisibility(View.GONE);
            } else {
                imgAvatar.setVisibility(View.GONE);
                tvAvatarInitial.setVisibility(View.VISIBLE);
                String name = msg.getSenderName();
                tvAvatarInitial.setText(name != null && !name.isEmpty()
                        ? String.valueOf(name.charAt(0)).toUpperCase() : "?");
                int colorIdx = (msg.getSenderId() != null ? msg.getSenderId().hashCode() : 0)
                        & 0x7FFFFFFF;
                cvAvatar.setCardBackgroundColor(AVATAR_COLORS[colorIdx % AVATAR_COLORS.length]);
            }

            // Name color: self = blurple, others = random from palette
            if (msg.isSelf()) {
                tvSenderName.setTextColor(0xFF5865F2);
            } else {
                int idx = (msg.getSenderId() != null ? msg.getSenderId().hashCode() : 0)
                        & 0x7FFFFFFF;
                tvSenderName.setTextColor((int) AVATAR_COLORS[idx % AVATAR_COLORS.length]);
            }
            tvSenderName.setText(msg.getSenderName());
            tvTimestamp.setText(msg.getTimestamp());
            bindReplyQuote(msg, llReplyQuote, tvReplySender, tvReplyContent);

            String gifUrl = extractGiphyGifUrl(msg.getContent());
            boolean isBase64Image = msg.getContent() != null && msg.getContent().startsWith("data:image/");

            // Content
            if (gifUrl == null && !isBase64Image && msg.getContent() != null && !msg.getContent().isEmpty()) {
                tvMessageContent.setText(msg.getContent());
                tvMessageContent.setVisibility(View.VISIBLE);
            } else {
                tvMessageContent.setVisibility(View.GONE);
            }

            // Image / GIF
            if (gifUrl != null) {
                cvImageAttachment.setVisibility(View.VISIBLE);
                Glide.with(imgAttachment)
                        .asGif()
                        .load(gifUrl)
                        .into(imgAttachment);
                imgAttachment.setOnClickListener(v -> { if (listener != null) listener.onImageClicked(gifUrl); });
            } else if (isBase64Image) {
                cvImageAttachment.setVisibility(View.VISIBLE);
                Glide.with(imgAttachment)
                        .load(msg.getContent())
                        .into(imgAttachment);
                imgAttachment.setOnClickListener(v -> { if (listener != null) listener.onImageClicked(msg.getContent()); });
            } else if (Message.TYPE_IMAGE.equals(msg.getMessageType()) && msg.getImageResId() != 0) {
                cvImageAttachment.setVisibility(View.VISIBLE);
                Glide.with(imgAttachment).clear(imgAttachment);
                imgAttachment.setImageResource(msg.getImageResId());
                imgAttachment.setOnClickListener(null);
            } else {
                Glide.with(imgAttachment).clear(imgAttachment);
                cvImageAttachment.setVisibility(View.GONE);
                imgAttachment.setOnClickListener(null);
            }

            bindReactionChips(msg, llReactions, tvReaction1, tvReaction2, listener, currentUserId);

            if (listener != null) {
                itemView.setOnLongClickListener(v -> {
                    listener.onMessageLongPressed(msg);
                    return true;
                });
                
                // Also enable avatar click for initial static data if Firebase hasn't loaded yet
                cvAvatar.setOnClickListener(v -> listener.onUserClicked(msg.getSenderId()));
                tvSenderName.setOnClickListener(v -> listener.onUserClicked(msg.getSenderId()));
            } else {
                itemView.setOnLongClickListener(null);
                cvAvatar.setOnClickListener(null);
                tvSenderName.setOnClickListener(null);
            }
        }
    }

    static class ContinuationViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llReplyQuote;
        TextView tvReplySender;
        TextView tvReplyContent;
        TextView tvMessageContent;
        CardView cvImageAttachment;
        ImageView imgAttachment;
        LinearLayout llReactions;
        TextView tvReaction1;
        TextView tvReaction2;

        ContinuationViewHolder(View itemView) {
            super(itemView);
            llReplyQuote = itemView.findViewById(R.id.ll_reply_quote);
            tvReplySender = itemView.findViewById(R.id.tv_reply_sender);
            tvReplyContent = itemView.findViewById(R.id.tv_reply_content);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
            cvImageAttachment = itemView.findViewById(R.id.cv_image_attachment);
            imgAttachment = itemView.findViewById(R.id.img_attachment);
            llReactions = itemView.findViewById(R.id.ll_reactions);
            tvReaction1 = itemView.findViewById(R.id.tv_reaction_1);
            tvReaction2 = itemView.findViewById(R.id.tv_reaction_2);
        }

        void bind(Message msg,
                  OnMessageInteractionListener listener,
                  String currentUserId) {
            bindReplyQuote(msg, llReplyQuote, tvReplySender, tvReplyContent);
            String gifUrl = extractGiphyGifUrl(msg.getContent());
            boolean isBase64Image = msg.getContent() != null && msg.getContent().startsWith("data:image/");

            if (gifUrl == null && !isBase64Image && msg.getContent() != null && !msg.getContent().isEmpty()) {
                tvMessageContent.setText(msg.getContent());
                tvMessageContent.setVisibility(View.VISIBLE);
            } else {
                tvMessageContent.setVisibility(View.GONE);
            }

            if (gifUrl != null) {
                cvImageAttachment.setVisibility(View.VISIBLE);
                Glide.with(imgAttachment)
                        .asGif()
                        .load(gifUrl)
                        .into(imgAttachment);
                imgAttachment.setOnClickListener(v -> { if (listener != null) listener.onImageClicked(gifUrl); });
            } else if (isBase64Image) {
                cvImageAttachment.setVisibility(View.VISIBLE);
                Glide.with(imgAttachment)
                        .load(msg.getContent())
                        .into(imgAttachment);
                imgAttachment.setOnClickListener(v -> { if (listener != null) listener.onImageClicked(msg.getContent()); });
            } else if (Message.TYPE_IMAGE.equals(msg.getMessageType()) && msg.getImageResId() != 0) {
                cvImageAttachment.setVisibility(View.VISIBLE);
                Glide.with(imgAttachment).clear(imgAttachment);
                imgAttachment.setImageResource(msg.getImageResId());
                imgAttachment.setOnClickListener(null);
            } else {
                Glide.with(imgAttachment).clear(imgAttachment);
                cvImageAttachment.setVisibility(View.GONE);
                imgAttachment.setOnClickListener(null);
            }

            bindReactionChips(msg, llReactions, tvReaction1, tvReaction2, listener, currentUserId);

            if (listener != null) {
                itemView.setOnLongClickListener(v -> {
                    listener.onMessageLongPressed(msg);
                    return true;
                });
            } else {
                itemView.setOnLongClickListener(null);
            }
        }
    }
}
