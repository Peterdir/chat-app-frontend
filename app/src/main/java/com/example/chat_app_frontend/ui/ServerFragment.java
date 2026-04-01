package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.CategoryChannelAdapter;
import com.example.chat_app_frontend.model.Category;
import com.example.chat_app_frontend.model.CategoryWithChannels;
import com.example.chat_app_frontend.model.Channel;
import com.example.chat_app_frontend.model.RealtimeChatMessage;
import com.example.chat_app_frontend.repository.ServerRepository;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerFragment extends Fragment {

    private static final String ARG_SERVER_ID = "server_id";
    private static final String ARG_SERVER_NAME = "server_name";

    private String serverId;
    private String serverName;
    private RecyclerView rvChannels;
    private CategoryChannelAdapter categoryChannelAdapter;
    private TextView tvServerName;

    public static ServerFragment newInstance(String serverId, String serverName) {
        ServerFragment fragment = new ServerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_ID, serverId);
        args.putString(ARG_SERVER_NAME, serverName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverId = getArguments().getString(ARG_SERVER_ID);
            serverName = getArguments().getString(ARG_SERVER_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_server_channels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvServerName = view.findViewById(R.id.tv_server_name);
        tvServerName.setText(serverName);

        view.findViewById(R.id.layout_server_header).setOnClickListener(v -> {
            ServerProfileBottomSheet bottomSheet = ServerProfileBottomSheet.newInstance(serverId, serverName);
            bottomSheet.show(getChildFragmentManager(), "ServerProfileBottomSheet");
        });

        // Nút thêm thành viên → mở sheet mời bạn bè
        view.findViewById(R.id.btn_add_member).setOnClickListener(v -> {
            InviteFriendsBottomSheet sheet =
                    InviteFriendsBottomSheet.newInstanceForServer(serverId, serverName);
            sheet.show(getChildFragmentManager(), "InviteFriends");
        });

        // Nút tạo danh mục
        view.findViewById(R.id.btn_create_category).setOnClickListener(v -> {
            CreateCategoryBottomSheet sheet = CreateCategoryBottomSheet.newInstance(serverId);
            // Reload data khi dialog closed
            sheet.setOnDismissListener(this::loadChannels);
            sheet.show(getChildFragmentManager(), "CreateCategory");
        });

        View searchBar = view.findViewById(R.id.tv_server_search);
        if (searchBar != null) {
            searchBar.setOnClickListener(v -> showServerSearchDialog());
        }

        rvChannels = view.findViewById(R.id.rv_channels);
        rvChannels.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryChannelAdapter = new CategoryChannelAdapter(new ArrayList<>(), new ArrayList<>());
        categoryChannelAdapter.setServerId(serverId);
        categoryChannelAdapter.setServerName(serverName);
        categoryChannelAdapter.setOnAddChannelClickListener(this::openCreateChannelForCategory);
        rvChannels.setAdapter(categoryChannelAdapter);

        loadChannels();
    }

    private void loadChannels() {
        if (serverId == null) return;
        ServerRepository.getInstance().removeLegacyDefaultChannels(serverId, () ->
                ServerRepository.getInstance().getServerCategoriesWithChannels(
                        serverId,
                        new ServerRepository.OnCategoriesWithChannelsCallback() {
                            @Override
                            public void onSuccess(List<CategoryWithChannels> categoriesWithChannels, List<Channel> uncategorizedChannels) {
                                categoryChannelAdapter.updateData(categoriesWithChannels, uncategorizedChannels);
                            }

                            @Override
                            public void onFailure(String error) {
                                // Lỗi load — giữ adapter rỗng
                            }
                        })
        );
    }

    private void openCreateChannelForCategory(Category category) {
        if (category == null || category.getId() == null) return;
        CreateChannelBottomSheet sheet = CreateChannelBottomSheet.newInstance(serverId, category.getId());
        sheet.setOnDismissListener(this::loadChannels);
        sheet.show(getChildFragmentManager(), "CreateChannel");
    }

    private void showServerSearchDialog() {
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
        input.setHint("Tìm tin nhắn, kênh chat hoặc kênh thoại");
        input.setBackgroundResource(R.drawable.bg_chat_input);
        input.setTextColor(ContextCompat.getColor(requireContext(), R.color.discord_text_primary));
        input.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.discord_text_secondary));
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        root.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView tvStatus = new TextView(requireContext());
        tvStatus.setText("Nhập từ khóa để bắt đầu tìm");
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.discord_text_secondary));
        tvStatus.setPadding(0, dp(8), 0, dp(8));
        root.addView(tvStatus);

        ListView listView = new ListView(requireContext());
        listView.setDivider(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        listView.setDividerHeight(dp(6));
        LinearLayout.LayoutParams listLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(360)
        );
        root.addView(listView, listLp);

        List<SearchResultItem> resultItems = new ArrayList<>();
        ServerSearchAdapter adapter = new ServerSearchAdapter(resultItems);
        listView.setAdapter(adapter);

        Handler searchHandler = new Handler(Looper.getMainLooper());
        int[] activeSearchToken = {0};
        Runnable[] pendingTask = {null};

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
                String keyword = s != null ? s.toString().trim().toLowerCase(Locale.ROOT) : "";
                if (pendingTask[0] != null) {
                    searchHandler.removeCallbacks(pendingTask[0]);
                    pendingTask[0] = null;
                }

                if (keyword.isEmpty()) {
                    activeSearchToken[0]++;
                    resultItems.clear();
                    adapter.notifyDataSetChanged();
                    tvStatus.setText("Nhập từ khóa để bắt đầu tìm");
                    return;
                }

                tvStatus.setText("Đang tìm \"" + keyword + "\"...");
                int requestToken = ++activeSearchToken[0];
                Runnable delayedSearch = () -> searchInServer(keyword, requestToken, activeSearchToken, resultItems, adapter, tvStatus);
                pendingTask[0] = delayedSearch;
                searchHandler.postDelayed(delayedSearch, 280);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= resultItems.size()) {
                return;
            }
            SearchResultItem item = resultItems.get(position);
            if (item.type == SearchResultItem.TYPE_CHANNEL && item.channel != null) {
                openChannelResult(item.channel);
                dialog.dismiss();
                return;
            }

            if (item.type == SearchResultItem.TYPE_MESSAGE && item.channelId != null) {
                Intent intent = new Intent(requireContext(), ServerChatActivity.class);
                intent.putExtra(ServerChatActivity.EXTRA_SERVER_ID, serverId);
                intent.putExtra(ServerChatActivity.EXTRA_SERVER_NAME, serverName);
                intent.putExtra(ServerChatActivity.EXTRA_CHANNEL_ID, item.channelId);
                intent.putExtra(ServerChatActivity.EXTRA_CHANNEL_NAME, item.channelName);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void searchInServer(String keyword,
                                int requestToken,
                                int[] activeSearchToken,
                                List<SearchResultItem> uiItems,
                                ServerSearchAdapter adapter,
                                TextView tvStatus) {
        ServerRepository.getInstance().getServerChannels(serverId, new ServerRepository.OnChannelListCallback() {
            @Override
            public void onSuccess(List<Channel> channels) {
                if (requestToken != activeSearchToken[0]) {
                    return;
                }

                List<SearchResultItem> channelMatches = new ArrayList<>();
                List<Channel> validChannels = new ArrayList<>();
                for (Channel channel : channels) {
                    if (channel == null || channel.getId() == null || channel.getId().trim().isEmpty()) {
                        continue;
                    }
                    validChannels.add(channel);

                    String channelNameValue = safe(channel.getName());
                    if (channelNameValue.toLowerCase(Locale.ROOT).contains(keyword)) {
                        channelMatches.add(SearchResultItem.channel(channel));
                    }
                }

                if (validChannels.isEmpty()) {
                    uiItems.clear();
                    adapter.notifyDataSetChanged();
                    tvStatus.setText("Server chưa có kênh");
                    return;
                }

                List<SearchResultItem> messageMatches = Collections.synchronizedList(new ArrayList<>());
                AtomicInteger pending = new AtomicInteger(validChannels.size());
                for (Channel channel : validChannels) {
                    DatabaseReference messagesRef = FirebaseManager
                            .getDatabaseReference("chat_messages/server_channels")
                            .child(serverId + "_" + channel.getId())
                            .child("messages");

                    messagesRef
                            .orderByChild("createdAt")
                            .limitToLast(200)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (requestToken != activeSearchToken[0]) {
                                        return;
                                    }

                                    for (DataSnapshot msgNode : snapshot.getChildren()) {
                                        RealtimeChatMessage msg = msgNode.getValue(RealtimeChatMessage.class);
                                        if (msg == null) {
                                            continue;
                                        }

                                        String searchable = (safe(msg.getSenderName()) + " " + safe(msg.getContent()))
                                                .toLowerCase(Locale.ROOT);
                                        if (!searchable.contains(keyword)) {
                                            continue;
                                        }

                                        messageMatches.add(SearchResultItem.message(
                                                channel.getId(),
                                                safe(channel.getName()),
                                                safe(msg.getSenderName()),
                                                safe(msg.getContent()),
                                                msg.getCreatedAt()
                                        ));
                                    }
                                    maybePublishServerSearch(requestToken, activeSearchToken, pending,
                                            channelMatches, messageMatches, uiItems, adapter, tvStatus);
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    maybePublishServerSearch(requestToken, activeSearchToken, pending,
                                            channelMatches, messageMatches, uiItems, adapter, tvStatus);
                                }
                            });
                }
            }

            @Override
            public void onFailure(String error) {
                if (requestToken != activeSearchToken[0]) {
                    return;
                }
                uiItems.clear();
                adapter.notifyDataSetChanged();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không tải được dữ liệu tìm kiếm", Toast.LENGTH_SHORT).show();
                }
                tvStatus.setText("Không tải được dữ liệu tìm kiếm");
            }
        });
    }

    private void maybePublishServerSearch(int requestToken,
                                          int[] activeSearchToken,
                                          AtomicInteger pending,
                                          List<SearchResultItem> channelMatches,
                                          List<SearchResultItem> messageMatches,
                                          List<SearchResultItem> uiItems,
                                          ServerSearchAdapter adapter,
                                          TextView tvStatus) {
        if (pending.decrementAndGet() != 0) {
            return;
        }
        if (requestToken != activeSearchToken[0]) {
            return;
        }

        messageMatches.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));

        uiItems.clear();
        uiItems.addAll(channelMatches);
        uiItems.addAll(messageMatches);
        adapter.notifyDataSetChanged();

        int total = uiItems.size();
        if (total == 0) {
            tvStatus.setText("Không có kết quả phù hợp");
            return;
        }
        tvStatus.setText("Kết quả: " + total + " (kênh + tin nhắn)");
    }

    private void openChannelResult(Channel channel) {
        if (channel == null || getContext() == null) {
            return;
        }
        if ("voice".equals(channel.getType())) {
            if (getActivity() != null) {
                VoiceChannelPreviewBottomSheet bottomSheet = new VoiceChannelPreviewBottomSheet(safe(channel.getName()));
                bottomSheet.show(getActivity().getSupportFragmentManager(), "VoicePreview");
            }
            return;
        }

        Intent intent = new Intent(getContext(), ServerChatActivity.class);
        intent.putExtra(ServerChatActivity.EXTRA_SERVER_ID, serverId);
        intent.putExtra(ServerChatActivity.EXTRA_SERVER_NAME, serverName);
        intent.putExtra(ServerChatActivity.EXTRA_CHANNEL_ID, channel.getId());
        intent.putExtra(ServerChatActivity.EXTRA_CHANNEL_NAME, safe(channel.getName()));
        startActivity(intent);
    }

    private int dp(int value) {
        if (getResources() == null) {
            return value;
        }
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String safe(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "unknown";
        }
        return text.trim();
    }

    private String buildSnippet(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "(tin nhắn rỗng)";
        }
        String normalized = content.replace('\n', ' ').trim();
        if (normalized.length() > 72) {
            return normalized.substring(0, 72) + "...";
        }
        return normalized;
    }

    private static class SearchResultItem {
        static final int TYPE_CHANNEL = 1;
        static final int TYPE_MESSAGE = 2;

        final int type;
        final Channel channel;
        final String channelId;
        final String channelName;
        final String senderName;
        final String content;
        final long createdAt;

        private SearchResultItem(int type,
                                 Channel channel,
                                 String channelId,
                                 String channelName,
                                 String senderName,
                                 String content,
                                 long createdAt) {
            this.type = type;
            this.channel = channel;
            this.channelId = channelId;
            this.channelName = channelName;
            this.senderName = senderName;
            this.content = content;
            this.createdAt = createdAt;
        }

        static SearchResultItem channel(Channel channel) {
            return new SearchResultItem(TYPE_CHANNEL, channel, null, null, null, null, 0L);
        }

        static SearchResultItem message(String channelId,
                                        String channelName,
                                        String senderName,
                                        String content,
                                        long createdAt) {
            return new SearchResultItem(TYPE_MESSAGE, null, channelId, channelName, senderName, content, createdAt);
        }
    }

    private class ServerSearchAdapter extends ArrayAdapter<SearchResultItem> {
        ServerSearchAdapter(List<SearchResultItem> items) {
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

            SearchResultItem item = getItem(position);
            if (item == null) {
                title.setText("(kết quả)");
                subtitle.setText("Không xác định");
                return row;
            }

            if (item.type == SearchResultItem.TYPE_CHANNEL && item.channel != null) {
                String channelType = "voice".equals(item.channel.getType()) ? "Kênh thoại" : "Kênh chat";
                title.setText(("voice".equals(item.channel.getType()) ? "🔊 " : "# ") + safe(item.channel.getName()));
                subtitle.setText(channelType);
                return row;
            }

            title.setText(buildSnippet(item.content));
            subtitle.setText("#" + safe(item.channelName) + " • " + safe(item.senderName));
            return row;
        }
    }
}

