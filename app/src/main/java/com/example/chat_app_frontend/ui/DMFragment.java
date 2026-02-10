package com.example.chat_app_frontend.ui;

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

import java.util.ArrayList;
import java.util.List;

public class DMFragment extends Fragment {

    private RecyclerView rvDMList;
    private DMAdapter dmAdapter;

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

        // Mock Data
        List<Friend> friends = new ArrayList<>();
        friends.add(new Friend("1", "Phan Vien", "Playing MATH", R.drawable.h1));
        friends.add(new Friend("2", "Nguyen Dong", "Online", R.drawable.h2));
        friends.add(new Friend("3", "Khanh Duy", "Idle", R.drawable.h3));
        friends.add(new Friend("4", "Peter Park", "DND", R.drawable.h4));
        friends.add(new Friend("5", "Persyy", "Online", R.drawable.h5));


        dmAdapter = new DMAdapter(friends);
        rvDMList.setAdapter(dmAdapter);
    }
}
