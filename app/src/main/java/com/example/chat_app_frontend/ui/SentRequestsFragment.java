package com.example.chat_app_frontend.ui;

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
import com.example.chat_app_frontend.adapter.FriendRequestAdapter;
import com.example.chat_app_frontend.model.FriendRequest;
import com.example.chat_app_frontend.repository.FriendRepository;

import java.util.ArrayList;
import java.util.List;

/** Tab 2: Lời mời kết bạn đã gửi. */
public class SentRequestsFragment extends Fragment {

    private RecyclerView          recyclerView;
    private ProgressBar           progressBar;
    private LinearLayout          layoutEmpty;
    private TextView              tvEmpty;

    private final List<FriendRequest> requests = new ArrayList<>();
    private FriendRequestAdapter      adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends_tab, container, false);

        recyclerView = v.findViewById(R.id.recycler_view);
        progressBar  = v.findViewById(R.id.progress_bar);
        layoutEmpty  = v.findViewById(R.id.layout_empty);
        tvEmpty      = v.findViewById(R.id.tv_empty);

        FriendRepository friendRepo = FriendRepository.getInstance();

        adapter = new FriendRequestAdapter(requests,
                FriendRequestAdapter.Mode.SENT,
                new FriendRequestAdapter.OnActionListener() {
                    @Override
                    public void onAccept(FriendRequest request, int position) { /* Not used */ }

                    @Override
                    public void onDeclineOrCancel(FriendRequest request, int position) {
                        // Hủy lời mời đã gửi
                        friendRepo.cancelSentRequest(request.getSenderId(),
                                new FriendRepository.OnCompleteListener() {
                                    @Override public void onSuccess() {
                                        if (getActivity() == null) return;
                                        getActivity().runOnUiThread(() -> {
                                            adapter.removeItem(position);
                                            checkEmpty();
                                            Toast.makeText(getContext(),
                                                    "Đã hủy lời mời", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                    @Override public void onFailure(String error) {}
                                });
                    }
                });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadSentRequests();
        return v;
    }

    private void loadSentRequests() {
        progressBar.setVisibility(View.VISIBLE);
        FriendRepository.getInstance().getSentRequests(new FriendRepository.OnFriendRequestListListener() {
            @Override
            public void onLoaded(List<FriendRequest> list) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    requests.clear();
                    requests.addAll(list);
                    adapter.notifyDataSetChanged();
                    checkEmpty();
                });
            }
            @Override
            public void onFailure(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        });
    }

    private void checkEmpty() {
        if (requests.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Bạn chưa gửi lời mời kết bạn nào");
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
