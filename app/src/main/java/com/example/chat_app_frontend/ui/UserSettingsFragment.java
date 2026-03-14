package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.UserSettingsAdapter;
import com.example.chat_app_frontend.model.UserSettingItem;
import java.util.ArrayList;
import java.util.List;

public class UserSettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        setupAccountSettings(view);
        setupBillingSettings(view);
        setupAppSettings(view);
        setupSupportSettings(view);
        setupWhatsNewSettings(view);
        setupLogoutSetting(view);

        return view;
    }

    private final UserSettingsAdapter.OnItemClickListener settingsClickListener = item -> {
        String title = item.getTitle();
        if ("Nhận Nitro".equals(title) || "Tăng cường máy chủ".equals(title) || "Tặng quà Nitro".equals(title)) {
            Intent intent = new Intent(getActivity(), NitroActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        } else if ("Cửa hàng".equals(title)) {
            Intent intent = new Intent(getActivity(), ShopActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        } else if ("Tài khoản".equals(title)) {
            Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        } else if ("Giao diện".equals(title)) {
            Intent intent = new Intent(getActivity(), AppearanceSettingsActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        } else if ("Đăng xuất".equals(item.getTitle())) {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                getActivity().finish();
            }
        }
    };

    private void setupAccountSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvAccountSettings);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("Nhận Nitro", R.drawable.ic_nitro_boost));
        items.add(new UserSettingItem("Tài khoản", R.drawable.ic_person));
        items.add(new UserSettingItem("Nội dung & Xã hội", R.drawable.ic_chat_bubble));
        items.add(new UserSettingItem("Dữ liệu & Quyền riêng tư", R.drawable.ic_settings));
        items.add(new UserSettingItem("Trung tâm Gia đình", R.drawable.ic_person_add));
        items.add(new UserSettingItem("Ứng dụng được ủy quyền", R.drawable.ic_store_modern));
        items.add(new UserSettingItem("Thiết bị", R.drawable.ic_phone));
        items.add(new UserSettingItem("Kết nối", R.drawable.ic_discord_logo));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }

    private void setupBillingSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvBillingSettings);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("Cửa hàng", R.drawable.ic_store_modern));
        items.add(new UserSettingItem("Nhiệm vụ", R.drawable.ic_quest_badge));
        items.add(new UserSettingItem("Tăng cường máy chủ", R.drawable.ic_nitro_boost));
        items.add(new UserSettingItem("Tặng quà Nitro", R.drawable.ic_chat_bubble));


        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }

    private void setupAppSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvAppSettings);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("Giọng nói", R.drawable.ic_chat_bubble, "Hoạt động giọng nói"));
        items.add(new UserSettingItem("Giao diện", R.drawable.ic_settings, "Tối"));
        items.add(new UserSettingItem("Hỗ trợ tiếp cận", R.drawable.ic_person));
        items.add(new UserSettingItem("Ngôn ngữ", R.drawable.ic_search, "Tiếng Việt"));
        items.add(new UserSettingItem("Trò chuyện", R.drawable.ic_chat_bubble));
        items.add(new UserSettingItem("Trình duyệt Web", R.drawable.ic_search));
        items.add(new UserSettingItem("Thông báo", R.drawable.ic_notifications));
        items.add(new UserSettingItem("Biểu tượng ứng dụng", R.drawable.ic_discord_logo, true));
        items.add(new UserSettingItem("Nâng cao", R.drawable.ic_settings));


        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }

    private void setupSupportSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvSupportSettings);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("Hỗ trợ", R.drawable.ic_search));
        items.add(new UserSettingItem("Tải nhật ký gỡ lỗi lên Hỗ trợ Discord", R.drawable.ic_settings));
        items.add(new UserSettingItem("Lời cảm ơn", R.drawable.ic_settings));


        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }

    private void setupWhatsNewSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvWhatsNewSettings);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("Có gì mới", R.drawable.ic_settings));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }

    private void setupLogoutSetting(View view) {
        RecyclerView rv = view.findViewById(R.id.rvLogoutSetting);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("Đăng xuất", R.drawable.ic_back_arrow, null, false, true));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }
}