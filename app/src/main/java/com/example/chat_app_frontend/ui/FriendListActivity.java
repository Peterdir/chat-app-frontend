package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chat_app_frontend.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Màn hình danh sách bạn bè với 3 tab:
 *   1. Bạn bè
 *   2. Đã gửi
 *   3. Chờ xác nhận
 *
 * Mở từ card "Bạn bè" trong ProfileFragment.
 */
public class FriendListActivity extends AppCompatActivity {

    private static final String[] TAB_TITLES = {"Bạn bè", "Đã gửi", "Chờ xác nhận"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout  tabLayout = findViewById(R.id.tab_layout);

        viewPager.setAdapter(new FriendPagerAdapter(this));
        viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

        // Mặc định mở tab cuối ("Chờ xác nhận") nếu được yêu cầu
        int startTab = getIntent().getIntExtra("start_tab", 0);
        viewPager.setCurrentItem(startTab, false);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Add Friend button ở header → mở AddFriendActivity
        findViewById(R.id.btn_add_friend).setOnClickListener(v -> {
            startActivity(new Intent(this, AddFriendActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // =========================================================================
    // ViewPager2 Adapter
    // =========================================================================

    private static class FriendPagerAdapter extends FragmentStateAdapter {

        FriendPagerAdapter(@NonNull FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1: return new SentRequestsFragment();
                case 2: return new PendingRequestsFragment();
                default: return new FriendsTabFragment();
            }
        }

        @Override
        public int getItemCount() { return 3; }
    }
}
