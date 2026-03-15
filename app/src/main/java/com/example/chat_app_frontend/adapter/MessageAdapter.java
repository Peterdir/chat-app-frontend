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

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Message;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
        private static final Pattern GIPHY_PAYLOAD_PATTERN =
            Pattern.compile("^\\[GIF\\]\\s+giphy://([A-Za-z0-9]+)$");

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
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
            ((GroupStartViewHolder) holder).bind(msg);
        } else if (holder instanceof ContinuationViewHolder) {
            ((ContinuationViewHolder) holder).bind(msg);
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
        TextView tvAvatarInitial;
        TextView tvSenderName;
        TextView tvTimestamp;
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
            tvAvatarInitial = itemView.findViewById(R.id.tv_avatar_initial);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
            cvImageAttachment = itemView.findViewById(R.id.cv_image_attachment);
            imgAttachment = itemView.findViewById(R.id.img_attachment);
            llReactions = itemView.findViewById(R.id.ll_reactions);
            tvReaction1 = itemView.findViewById(R.id.tv_reaction_1);
            tvReaction2 = itemView.findViewById(R.id.tv_reaction_2);
        }

        void bind(Message msg) {
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

            String gifUrl = extractGiphyGifUrl(msg.getContent());

            // Content
            if (gifUrl == null && msg.getContent() != null && !msg.getContent().isEmpty()) {
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
            } else if (Message.TYPE_IMAGE.equals(msg.getMessageType()) && msg.getImageResId() != 0) {
                cvImageAttachment.setVisibility(View.VISIBLE);
                Glide.with(imgAttachment).clear(imgAttachment);
                imgAttachment.setImageResource(msg.getImageResId());
            } else {
                Glide.with(imgAttachment).clear(imgAttachment);
                cvImageAttachment.setVisibility(View.GONE);
            }

            // Reactions (hidden by default in mock)
            llReactions.setVisibility(View.GONE);
        }
    }

    static class ContinuationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageContent;
        CardView cvImageAttachment;
        ImageView imgAttachment;
        LinearLayout llReactions;
        TextView tvReaction1;
        TextView tvReaction2;

        ContinuationViewHolder(View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
            cvImageAttachment = itemView.findViewById(R.id.cv_image_attachment);
            imgAttachment = itemView.findViewById(R.id.img_attachment);
            llReactions = itemView.findViewById(R.id.ll_reactions);
            tvReaction1 = itemView.findViewById(R.id.tv_reaction_1);
            tvReaction2 = itemView.findViewById(R.id.tv_reaction_2);
        }

        void bind(Message msg) {
            String gifUrl = extractGiphyGifUrl(msg.getContent());

            if (gifUrl == null && msg.getContent() != null && !msg.getContent().isEmpty()) {
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
            } else if (Message.TYPE_IMAGE.equals(msg.getMessageType()) && msg.getImageResId() != 0) {
                cvImageAttachment.setVisibility(View.VISIBLE);
                Glide.with(imgAttachment).clear(imgAttachment);
                imgAttachment.setImageResource(msg.getImageResId());
            } else {
                Glide.with(imgAttachment).clear(imgAttachment);
                cvImageAttachment.setVisibility(View.GONE);
            }

            llReactions.setVisibility(View.GONE);
        }
    }
}
