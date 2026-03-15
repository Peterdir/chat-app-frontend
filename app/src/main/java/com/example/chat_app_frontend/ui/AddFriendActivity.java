package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.FriendSearchAdapter;
import com.example.chat_app_frontend.manager.AuthManager;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.FriendRepository;
import com.example.chat_app_frontend.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình tìm kiếm và gửi lời mời kết bạn.
 * Mở từ nút Add Friend (xanh lá) trên DM screen.
 */
public class AddFriendActivity extends AppCompatActivity {

    private EditText         etSearch;
    private ImageView        btnClear, btnBack;
    private ProgressBar      progressSearch;
    private RecyclerView     rvResults;
    private LinearLayout     layoutEmpty;
    private TextView         tvEmptyTitle, tvEmptySubtitle;

    private FriendSearchAdapter adapter;
    private final List<User>   resultUsers    = new ArrayList<>();
    private final List<String> resultStatuses = new ArrayList<>();

    private UserRepository   userRepository;
    private FriendRepository friendRepository;
    private String           myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        userRepository   = UserRepository.getInstance();
        friendRepository = FriendRepository.getInstance();
        myUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        bindViews();
        setupRecyclerView();
        setupSearch();
        setupBackButton();
    }

    private void bindViews() {
        btnBack        = findViewById(R.id.btn_back);
        etSearch       = findViewById(R.id.et_search);
        btnClear       = findViewById(R.id.btn_clear);
        progressSearch = findViewById(R.id.progress_search);
        rvResults      = findViewById(R.id.rv_search_results);
        layoutEmpty    = findViewById(R.id.layout_empty);
        tvEmptyTitle   = findViewById(R.id.tv_empty_title);
        tvEmptySubtitle= findViewById(R.id.tv_empty_subtitle);
    }

    private void setupRecyclerView() {
        adapter = new FriendSearchAdapter(resultUsers, resultStatuses, (user, position) -> {
            // Gửi lời mời kết bạn
            friendRepository.sendFriendRequest(user, new FriendRepository.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        adapter.setStatus(position, "sent");
                        Toast.makeText(AddFriendActivity.this,
                                "Đã gửi lời mời kết bạn tới " + user.getDisplayNameOrUserName(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onFailure(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(AddFriendActivity.this,
                                    "Gửi lời mời thất bại: " + error,
                                    Toast.LENGTH_SHORT).show());
                }
            });
        });

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (s.length() >= 2) {
                    performSearch(s.toString());
                } else {
                    showEmptyState("Tìm kiếm tên hoặc username",
                            "Nhập ít nhất 2 ký tự để tìm kiếm");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String q = etSearch.getText().toString().trim();
                if (q.length() >= 2) performSearch(q);
                return true;
            }
            return false;
        });

        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            showEmptyState("Tìm kiếm tên hoặc username",
                    "Nhập ít nhất 2 ký tự để tìm kiếm");
        });
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void performSearch(String query) {
        showLoading();

        userRepository.searchUsers(query, myUid, new UserRepository.OnUserListListener() {
            @Override
            public void onLoaded(List<User> users) {
                runOnUiThread(() -> {
                    if (users.isEmpty()) {
                        showEmptyState("Không tìm thấy người dùng",
                                "Thử tìm bằng tên khác");
                        return;
                    }

                    // Hiện danh sách, kiểm tra trạng thái từng user
                    resultUsers.clear();
                    resultStatuses.clear();
                    resultUsers.addAll(users);
                    for (int i = 0; i < users.size(); i++) resultStatuses.add("none");

                    showResults();
                    adapter.notifyDataSetChanged();

                    // Kiểm tra trạng thái bạn bè cho từng user (async)
                    checkFriendStatuses(users);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> showEmptyState("Đã xảy ra lỗi", error));
            }
        });
    }

    private void checkFriendStatuses(List<User> users) {
        for (int i = 0; i < users.size(); i++) {
            final int idx = i;
            friendRepository.checkFriendStatus(users.get(i).getFirebaseUid(),
                    status -> runOnUiThread(() -> adapter.setStatus(idx, status)));
        }
    }

    private void showLoading() {
        progressSearch.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvResults.setVisibility(View.GONE);
    }

    private void showResults() {
        progressSearch.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        rvResults.setVisibility(View.VISIBLE);
    }

    private void showEmptyState(String title, String subtitle) {
        progressSearch.setVisibility(View.GONE);
        rvResults.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(title);
        tvEmptySubtitle.setText(subtitle);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
