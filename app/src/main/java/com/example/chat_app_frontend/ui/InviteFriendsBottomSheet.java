package com.example.chat_app_frontend.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Friend;
import com.example.chat_app_frontend.repository.FriendRepository;
import com.example.chat_app_frontend.repository.ServerInviteRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class InviteFriendsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_SERVER_ID = "arg_server_id";
    private static final String ARG_SERVER_NAME = "arg_server_name";

    private String serverId;
    private String serverName;

    private final List<Friend> allFriends = new ArrayList<>();
    private final List<Friend> filteredFriends = new ArrayList<>();
    private final Set<String> invitedFriendIds = new HashSet<>();

    private InviteFriendAdapter adapter;
    private FriendRepository friendRepository;
    private ServerInviteRepository serverInviteRepository;

    @Deprecated
    public InviteFriendsBottomSheet(String channelName) {
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_NAME, channelName);
        setArguments(args);
    }

    public InviteFriendsBottomSheet() {
        // Required empty constructor for fragment recreation.
    }

    public static InviteFriendsBottomSheet newInstanceForServer(String serverId, String serverName) {
        InviteFriendsBottomSheet sheet = new InviteFriendsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_ID, serverId);
        args.putString(ARG_SERVER_NAME, serverName);
        sheet.setArguments(args);
        return sheet;
    }

    public static InviteFriendsBottomSheet newInstanceForChannel(String channelName) {
        InviteFriendsBottomSheet sheet = new InviteFriendsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_NAME, channelName);
        sheet.setArguments(args);
        return sheet;
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

        friendRepository = FriendRepository.getInstance();
        serverInviteRepository = ServerInviteRepository.getInstance();

        readArgs();
        bindHeader(view);
        bindQuickActions(view);

        RecyclerView rvFriends = view.findViewById(R.id.rv_invite_friends);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new InviteFriendAdapter(filteredFriends, invitedFriendIds,
                (friend, invited) -> {
                    if (invited) {
                        cancelInvite(friend);
                    } else {
                        inviteFriend(friend);
                    }
                });
        rvFriends.setAdapter(adapter);

        setupSearch(view);
        loadFriends();
    }

    private void readArgs() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        serverId = args.getString(ARG_SERVER_ID, "");
        serverName = args.getString(ARG_SERVER_NAME, "Server này");
    }

    private void bindHeader(@NonNull View view) {
        TextView tvTitle = view.findViewById(R.id.tv_invite_title);
        TextView tvInviteNote = view.findViewById(R.id.tv_invite_note);

        String displayName = (serverName == null || serverName.trim().isEmpty()) ? "server này" : serverName;
        tvTitle.setText("Mời bạn bè vào " + displayName);

        if (serverId == null || serverId.trim().isEmpty()) {
            tvInviteNote.setText("Bạn đang mời vào kênh thoại. Lời mời sẽ được gửi nhanh tới bạn bè.");
        }
    }

    private void bindQuickActions(@NonNull View view) {
        View actionShare = view.findViewById(R.id.action_share_invite);
        View actionCopy = view.findViewById(R.id.action_copy_link);

        actionShare.setOnClickListener(v -> {
            String inviteLink = buildInviteLink();
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, inviteLink);
            startActivity(Intent.createChooser(sendIntent, "Chia sẻ lời mời"));
        });

        actionCopy.setOnClickListener(v -> {
            String inviteLink = buildInviteLink();
            ClipboardManager clipboard = (ClipboardManager) requireContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("invite_link", inviteLink));
                Toast.makeText(getContext(), "Đã sao chép link mời", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildInviteLink() {
        String safeServerId = (serverId == null || serverId.trim().isEmpty()) ? "voice-room" : serverId;
        return "https://chat-app.local/invite/" + safeServerId;
    }

    private void setupSearch(@NonNull View view) {
        EditText etSearch = view.findViewById(R.id.et_search_friends);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFriends(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadFriends() {
        friendRepository.getFriends(new FriendRepository.OnFriendListListener() {
            @Override
            public void onLoaded(List<Friend> friends) {
                allFriends.clear();
                allFriends.addAll(friends);
                loadInvitedStateThenRender();
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không tải được danh sách bạn bè: " + error, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private void loadInvitedStateThenRender() {
        if (serverId == null || serverId.trim().isEmpty()) {
            invitedFriendIds.clear();
            filterFriends("");
            return;
        }

        serverInviteRepository.getMyInvitedFriendIds(serverId, new ServerInviteRepository.OnInvitedIdsListener() {
            @Override
            public void onLoaded(Set<String> invitedIds) {
                invitedFriendIds.clear();
                invitedFriendIds.addAll(invitedIds);
                filterFriends("");
            }

            @Override
            public void onFailure(String error) {
                invitedFriendIds.clear();
                filterFriends("");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không tải được trạng thái lời mời", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterFriends(String query) {
        String keyword = query.toLowerCase(Locale.getDefault()).trim();

        filteredFriends.clear();
        for (Friend friend : allFriends) {
            String name = friend.getFriendName() != null ? friend.getFriendName() : "";
            if (keyword.isEmpty() || name.toLowerCase(Locale.getDefault()).contains(keyword)) {
                filteredFriends.add(friend);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void inviteFriend(Friend friend) {
        if (friend == null || friend.getUid() == null) {
            return;
        }

        if (invitedFriendIds.contains(friend.getUid())) {
            Toast.makeText(getContext(), "Bạn đã mời người này rồi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (serverId == null || serverId.trim().isEmpty()) {
            invitedFriendIds.add(friend.getUid());
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            Toast.makeText(getContext(), "Đã mời " + friend.getFriendName(), Toast.LENGTH_SHORT).show();
            return;
        }

        serverInviteRepository.sendServerInvite(serverId, serverName, friend, new ServerInviteRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                invitedFriendIds.add(friend.getUid());
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã mời " + friend.getFriendName() + " vào server", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Mời thất bại: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cancelInvite(Friend friend) {
        if (friend == null || friend.getUid() == null) {
            return;
        }

        if (!invitedFriendIds.contains(friend.getUid())) {
            return;
        }

        if (serverId == null || serverId.trim().isEmpty()) {
            invitedFriendIds.remove(friend.getUid());
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            Toast.makeText(getContext(), "Đã hủy lời mời", Toast.LENGTH_SHORT).show();
            return;
        }

        serverInviteRepository.cancelServerInvite(serverId, friend.getUid(), new ServerInviteRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                invitedFriendIds.remove(friend.getUid());
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã hủy lời mời " + friend.getFriendName(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Hủy lời mời thất bại: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static class InviteFriendAdapter extends RecyclerView.Adapter<InviteFriendAdapter.ViewHolder> {
        private final List<Friend> friends;
        private final Set<String> invitedFriendIds;
        private final OnInviteClickListener listener;

        public interface OnInviteClickListener {
            void onInviteClick(Friend friend, boolean invited);
        }

        public InviteFriendAdapter(List<Friend> friends, Set<String> invitedFriendIds, OnInviteClickListener listener) {
            this.friends = friends;
            this.invitedFriendIds = invitedFriendIds;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite_friend, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Friend friend = friends.get(position);
            String friendName = friend.getFriendName() != null ? friend.getFriendName() : "Không tên";
            holder.tvName.setText(friendName);

            int[] colors = {0xFF5865F2, 0xFF43B581, 0xFFFAA61A, 0xFFF04747};
            holder.ivAvatar.setBackgroundTintList(ColorStateList.valueOf(colors[position % colors.length]));

            boolean invited = friend.getUid() != null && invitedFriendIds.contains(friend.getUid());
            holder.btnInvite.setText(invited ? "Hủy mời" : "Mời");
            holder.btnInvite.setEnabled(true);
            holder.btnInvite.setBackgroundTintList(ColorStateList.valueOf(invited ? 0xFFED4245 : 0xFF5865F2));

            holder.btnInvite.setOnClickListener(v -> listener.onInviteClick(friend, invited));
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar;
            TextView tvName;
            TextView btnInvite;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.iv_friend_avatar);
                tvName = itemView.findViewById(R.id.tv_friend_name);
                btnInvite = itemView.findViewById(R.id.btn_invite);
            }
        }
    }
}
