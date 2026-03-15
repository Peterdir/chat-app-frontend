package com.example.chat_app_frontend.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;
import java.util.List;

public class InviteFriendsBottomSheet extends BottomSheetDialogFragment {

    private String channelName;

    public InviteFriendsBottomSheet(String channelName) {
        this.channelName = channelName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invite_friends_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Removed spannable logic for simplicity

        RecyclerView rvFriends = view.findViewById(R.id.rv_invite_friends);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data
        List<String> friends = Arrays.asList(
                "Phan Vien", "Minh thích bóng", "Persyy", "HoangGJinn",
                "hoangqd", "MeowT", "cuong125", "Hiếu Lê", "Kiệt Trương", "ngumoitinh143");

        InviteFriendAdapter adapter = new InviteFriendAdapter(getContext(), friends, name -> {
            Toast.makeText(getContext(), "Đã mời " + name, Toast.LENGTH_SHORT).show();
        });
        rvFriends.setAdapter(adapter);
    }

    // Static Adapter inside
    public static class InviteFriendAdapter extends RecyclerView.Adapter<InviteFriendAdapter.ViewHolder> {
        private Context context;
        private List<String> friends;
        private OnInviteClickListener listener;

        public interface OnInviteClickListener {
            void onInviteClick(String name);
        }

        public InviteFriendAdapter(Context context, List<String> friends, OnInviteClickListener listener) {
            this.context = context;
            this.friends = friends;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_invite_friend, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = friends.get(position);
            holder.tvName.setText(name);

            // Simple tinting for mock avatars based on position
            int[] colors = { 0xFF5865F2, 0xFF43B581, 0xFFFAA61A, 0xFFF04747 };
            holder.ivAvatar.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(colors[position % colors.length]));

            holder.btnInvite.setOnClickListener(v -> {
                listener.onInviteClick(name);
                holder.btnInvite.setText("Đã Mời");
                holder.btnInvite.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF3BA55D)); // Green
            });
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.ImageView ivAvatar;
            TextView tvName;
            TextView btnInvite;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.iv_friend_avatar);
                tvName = itemView.findViewById(R.id.tv_friend_name);
                // I used an unstructured TextView for the button in XML
                btnInvite = (TextView) ((ViewGroup) itemView).getChildAt(2);
            }
        }
    }
}
