package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.DMAdapter;
import com.example.chat_app_frontend.model.Friend;
import com.example.chat_app_frontend.model.Server;
import com.example.chat_app_frontend.repository.FriendRepository;
import com.example.chat_app_frontend.repository.ServerRepository;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DMFragment extends Fragment {

    private RecyclerView rvDMList;
    private DMAdapter dmAdapter;
    private final List<Friend> friends = new ArrayList<>();
    private final List<Friend> allFriends = new ArrayList<>();
    private final List<Server> myServers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dm_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvDMList = view.findViewById(R.id.rv_dm_list);
        rvDMList.setLayoutManager(new LinearLayoutManager(getContext()));
        dmAdapter = new DMAdapter(friends);
        rvDMList.setAdapter(dmAdapter);

        View searchBar = view.findViewById(R.id.tv_search_dm);
        if (searchBar != null) {
            searchBar.setOnClickListener(v -> showCombinedSearchDialog());
        }

        View btnAddFriend = view.findViewById(R.id.btn_add_friend);
        if (btnAddFriend != null) {
            btnAddFriend.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddFriendActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        loadFriends();
        loadServers();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFriends();
        loadServers();
    }

    private void loadFriends() {
        FriendRepository.getInstance().getFriends(new FriendRepository.OnFriendListListener() {
            @Override
            public void onLoaded(List<Friend> friendList) {
                allFriends.clear();
                allFriends.addAll(friendList);

                friends.clear();
                friends.addAll(allFriends);
                if (dmAdapter != null) {
                    dmAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadServers() {
        ServerRepository.getInstance().getMyServers(new ServerRepository.OnServerListCallback() {
            @Override
            public void onSuccess(List<Server> servers) {
                myServers.clear();
                for (Server server : servers) {
                    if (server == null || server.getId() == null || server.getId().trim().isEmpty()) {
                        continue;
                    }
                    if ("0".equals(server.getId())) {
                        continue;
                    }
                    myServers.add(server);
                }
            }

            @Override
            public void onFailure(String error) {
                myServers.clear();
            }
        });
    }

    private void showCombinedSearchDialog() {
        if (getContext() == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_search_input);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);

        EditText input = new EditText(requireContext());
        input.setHint("Nhập tên bạn bè hoặc server");
        input.setBackgroundResource(R.drawable.bg_chat_input);
        input.setTextColor(ContextCompat.getColor(requireContext(), R.color.discord_text_primary));
        input.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.discord_text_secondary));
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        root.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView tvStatus = new TextView(requireContext());
        tvStatus.setText("Nhập từ khóa để tìm bạn bè hoặc server");
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.discord_text_secondary));
        tvStatus.setPadding(0, dp(8), 0, dp(8));
        root.addView(tvStatus);

        ListView listView = new ListView(requireContext());
        listView.setDivider(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        listView.setDividerHeight(dp(6));
        LinearLayout.LayoutParams listLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(320)
        );
        root.addView(listView, listLp);

        List<SearchItem> filteredItems = new ArrayList<>();
        CombinedSearchAdapter adapter = new CombinedSearchAdapter(filteredItems);
        listView.setAdapter(adapter);

        Runnable refresh = () -> {
            String q = input.getText() != null ? input.getText().toString().trim().toLowerCase() : "";
            filteredItems.clear();

            if (q.isEmpty()) {
                tvStatus.setText("Nhập từ khóa để tìm bạn bè hoặc server");
                adapter.notifyDataSetChanged();
                return;
            }

            for (Friend friend : allFriends) {
                if (friend == null) {
                    continue;
                }
                String friendName = safe(friend.getName());
                if (friendName.toLowerCase().contains(q)) {
                    filteredItems.add(SearchItem.friend(friend));
                }
            }

            for (Server server : myServers) {
                if (server == null) {
                    continue;
                }
                String serverName = safe(server.getName());
                if (serverName.toLowerCase().contains(q)) {
                    filteredItems.add(SearchItem.server(server));
                }
            }

            if (filteredItems.isEmpty()) {
                tvStatus.setText("Không có kết quả phù hợp");
            } else {
                tvStatus.setText("Kết quả: " + filteredItems.size());
            }
            adapter.notifyDataSetChanged();
        };

        AlertDialog dialog = builder
                .setView(root)
                .setNegativeButton("Đóng", null)
                .create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refresh.run();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= filteredItems.size()) {
                return;
            }
            SearchItem item = filteredItems.get(position);
            if (item.type == SearchItem.TYPE_FRIEND && item.friend != null) {
                openFriendChat(item.friend);
                dialog.dismiss();
                return;
            }
            if (item.type == SearchItem.TYPE_SERVER && item.server != null) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openServerFromSearch(item.server.getId(), item.server.getName());
                }
                dialog.dismiss();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void openFriendChat(Friend friend) {
        if (friend == null || getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), DMChatActivity.class);
        intent.putExtra(DMChatActivity.EXTRA_FRIEND_NAME, friend.getName());
        intent.putExtra(DMChatActivity.EXTRA_FRIEND_STATUS, friend.getStatus());
        intent.putExtra(DMChatActivity.EXTRA_FRIEND_AVATAR_URL, friend.getFriendAvatarUrl());
        intent.putExtra(DMChatActivity.EXTRA_FRIEND_AVATAR, friend.getAvatarResId());
        intent.putExtra(DMChatActivity.EXTRA_FRIEND_UID, friend.getUid());
        startActivity(intent);
    }

    private String safe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "(không tên)";
        }
        return value.trim();
    }

    private int dp(int value) {
        if (getResources() == null) {
            return value;
        }
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private static class SearchItem {
        static final int TYPE_FRIEND = 1;
        static final int TYPE_SERVER = 2;

        final int type;
        final Friend friend;
        final Server server;

        private SearchItem(int type, Friend friend, Server server) {
            this.type = type;
            this.friend = friend;
            this.server = server;
        }

        static SearchItem friend(Friend friend) {
            return new SearchItem(TYPE_FRIEND, friend, null);
        }

        static SearchItem server(Server server) {
            return new SearchItem(TYPE_SERVER, null, server);
        }
    }

    private class CombinedSearchAdapter extends ArrayAdapter<SearchItem> {
        CombinedSearchAdapter(List<SearchItem> items) {
            super(requireContext(), R.layout.item_search_result, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = LayoutInflater.from(getContext()).inflate(R.layout.item_search_result, parent, false);
            }

            TextView title = row.findViewById(R.id.tv_search_title);
            TextView subtitle = row.findViewById(R.id.tv_search_subtitle);

            SearchItem item = getItem(position);
            if (item == null) {
                title.setText("(kết quả)");
                subtitle.setText("Không xác định");
                return row;
            }

            if (item.type == SearchItem.TYPE_FRIEND && item.friend != null) {
                title.setText(safe(item.friend.getName()));
                subtitle.setText("Bạn bè");
                return row;
            }

            if (item.type == SearchItem.TYPE_SERVER && item.server != null) {
                title.setText(safe(item.server.getName()));
                subtitle.setText("Server");
                return row;
            }

            title.setText("(kết quả)");
            subtitle.setText("Không xác định");
            return row;
        }
    }
}
