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
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.FriendRepository;
import com.example.chat_app_frontend.manager.AuthManager;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/** Tab 3: Lời mời kết bạn chờ xác nhận. Real-time updates qua Firebase listener. */
public class PendingRequestsFragment extends Fragment {

    private RecyclerView          recyclerView;
    private ProgressBar           progressBar;
    private LinearLayout          layoutEmpty;
    private TextView              tvEmpty;

    private final List<FriendRequest> requests = new ArrayList<>();
    private FriendRequestAdapter      adapter;
    private FriendRepository          friendRepo;
    private ValueEventListener        pendingListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends_tab, container, false);

        recyclerView = v.findViewById(R.id.recycler_view);
        progressBar  = v.findViewById(R.id.progress_bar);
        layoutEmpty  = v.findViewById(R.id.layout_empty);
        tvEmpty      = v.findViewById(R.id.tv_empty);
        friendRepo   = FriendRepository.getInstance();

        adapter = new FriendRequestAdapter(requests,
                FriendRequestAdapter.Mode.PENDING,
                new FriendRequestAdapter.OnActionListener() {
                    @Override
                    public void onAccept(FriendRequest request, int position) {
                        // Lấy profile của mình để ghi vào friends của người kia
                        AuthManager authManager = AuthManager.getInstance(getContext());
                        User myProfile = authManager.getCurrentUser();
                        if (myProfile == null) return;

                        friendRepo.acceptFriendRequest(request, myProfile,
                                new FriendRepository.OnCompleteListener() {
                                    @Override public void onSuccess() {
                                        if (getActivity() == null) return;
                                        getActivity().runOnUiThread(() -> {
                                            adapter.removeItem(position);
                                            checkEmpty();
                                            Toast.makeText(getContext(),
                                                    "Đã chấp nhận lời mời của " + request.getSenderName(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                    @Override public void onFailure(String error) {
                                        if (getActivity() == null) return;
                                        getActivity().runOnUiThread(() ->
                                                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show());
                                    }
                                });
                    }

                    @Override
                    public void onDeclineOrCancel(FriendRequest request, int position) {
                        friendRepo.declineFriendRequest(request.getSenderId(),
                                new FriendRepository.OnCompleteListener() {
                                    @Override public void onSuccess() {
                                        if (getActivity() == null) return;
                                        getActivity().runOnUiThread(() -> {
                                            adapter.removeItem(position);
                                            checkEmpty();
                                        });
                                    }
                                    @Override public void onFailure(String error) {}
                                });
                    }
                });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        startListening();
        return v;
    }

    private void startListening() {
        progressBar.setVisibility(View.VISIBLE);
        pendingListener = friendRepo.observePendingRequests(new FriendRepository.OnFriendRequestListListener() {
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
            tvEmpty.setText("Không có lời mời kết bạn nào");
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy real-time listener khi fragment bị destroy
        if (pendingListener != null) {
            friendRepo.removePendingListener(pendingListener);
        }
    }
}
