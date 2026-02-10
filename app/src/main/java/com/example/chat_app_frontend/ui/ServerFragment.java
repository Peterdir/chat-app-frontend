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
import com.example.chat_app_frontend.adapter.ChannelAdapter;
import com.example.chat_app_frontend.model.Channel;

import java.util.ArrayList;
import java.util.List;

public class ServerFragment extends Fragment {

    private static final String ARG_SERVER_NAME = "server_name";
    private String serverName;
    private RecyclerView rvChannels;
    private ChannelAdapter channelAdapter;
    private TextView tvServerName;

    public static ServerFragment newInstance(String serverName) {
        ServerFragment fragment = new ServerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_NAME, serverName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
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

        rvChannels = view.findViewById(R.id.rv_channels);
        rvChannels.setLayoutManager(new LinearLayoutManager(getContext()));

        // Mock Data
        List<Channel> channels = new ArrayList<>();
        channels.add(new Channel("1", "welcome-and-rules", "text"));
        channels.add(new Channel("2", "notes-resources", "text"));
        channels.add(new Channel("3", "general", "text"));
        channels.add(new Channel("4", "planning", "text"));
        channels.add(new Channel("5", "bot-commands", "text"));
        channels.add(new Channel("6", "voice-chat", "voice"));
        channels.add(new Channel("7", "music", "voice"));

        channelAdapter = new ChannelAdapter(channels);
        rvChannels.setAdapter(channelAdapter);
    }
}
