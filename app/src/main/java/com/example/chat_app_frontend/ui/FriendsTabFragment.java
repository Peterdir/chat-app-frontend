package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.FriendListAdapter;
import com.example.chat_app_frontend.model.Friend;
import com.example.chat_app_frontend.repository.FriendRepository;

import java.util.ArrayList;
import java.util.List;

/** Tab 1: Danh sách bạn bè. */
public class FriendsTabFragment extends Fragment {

    private RecyclerView   recyclerView;
    private ProgressBar    progressBar;
    private LinearLayout   layoutEmpty;
    private TextView       tvEmpty;

    private final List<Friend> friends = new ArrayList<>();
    private FriendListAdapter  adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends_tab, container, false);

        recyclerView = v.findViewById(R.id.recycler_view);
        progressBar  = v.findViewById(R.id.progress_bar);
        layoutEmpty  = v.findViewById(R.id.layout_empty);
        tvEmpty      = v.findViewById(R.id.tv_empty);

        adapter = new FriendListAdapter(friends, friend -> {
            // Mở DMChatActivity khi nhấn vào bạn bè để nhắn tin
            Intent intent = new Intent(getContext(), DMChatActivity.class);
            intent.putExtra(DMChatActivity.EXTRA_FRIEND_NAME, friend.getFriendName());
            intent.putExtra(DMChatActivity.EXTRA_FRIEND_UID, friend.getUid()); // Sử dụng getUid() thay vì getFriendUid()
            intent.putExtra(DMChatActivity.EXTRA_FRIEND_STATUS, friend.getOnlineStatus()); // Sử dụng getOnlineStatus()
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadFriends();
        return v;
    }

    private void loadFriends() {
        progressBar.setVisibility(View.VISIBLE);
        FriendRepository.getInstance().getFriends(new FriendRepository.OnFriendListListener() {
            @Override
            public void onLoaded(List<Friend> list) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    friends.clear();
                    friends.addAll(list);
                    adapter.notifyDataSetChanged();
                    if (friends.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Bạn chưa có bạn bè nào.\nHãy thêm bạn bè!");
                        recyclerView.setVisibility(View.INVISIBLE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }
            @Override
            public void onFailure(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
