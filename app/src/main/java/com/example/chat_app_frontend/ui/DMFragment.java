package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.DMAdapter;
import com.example.chat_app_frontend.model.Friend;
import com.example.chat_app_frontend.repository.FriendRepository;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DMFragment extends Fragment {

    private RecyclerView rvDMList;
    private DMAdapter dmAdapter;
    private final List<Friend> friends = new ArrayList<>();

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
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFriends();
    }

    private void loadFriends() {
        FriendRepository.getInstance().getFriends(new FriendRepository.OnFriendListListener() {
            @Override
            public void onLoaded(List<Friend> friendList) {
                friends.clear();
                friends.addAll(friendList);
                dmAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
