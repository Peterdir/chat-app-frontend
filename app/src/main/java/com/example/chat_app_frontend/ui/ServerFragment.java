package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.CategoryChannelAdapter;
import com.example.chat_app_frontend.model.Category;
import com.example.chat_app_frontend.model.CategoryWithChannels;
import com.example.chat_app_frontend.model.Channel;
import com.example.chat_app_frontend.repository.ServerRepository;

import java.util.ArrayList;
import java.util.List;

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
}

