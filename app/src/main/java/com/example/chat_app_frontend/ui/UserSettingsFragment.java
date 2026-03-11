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
        if ("Get Nitro".equals(title) || "Server Boost".equals(title) || "Nitro Gifting".equals(title)) {
            Intent intent = new Intent(getActivity(), NitroActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        } else if ("Shop".equals(title)) {
            Intent intent = new Intent(getActivity(), ShopActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        } else if ("Log Out".equals(item.getTitle())) {
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
        items.add(new UserSettingItem("Get Nitro", R.drawable.ic_nitro_boost));
        items.add(new UserSettingItem("Account", R.drawable.ic_person));
        items.add(new UserSettingItem("Content & Social", R.drawable.ic_chat_bubble));
        items.add(new UserSettingItem("Data & Privacy", R.drawable.ic_settings));
        items.add(new UserSettingItem("Family Center", R.drawable.ic_person_add));
        items.add(new UserSettingItem("Authorized Apps", R.drawable.ic_store_modern));
        items.add(new UserSettingItem("Devices", R.drawable.ic_phone));
        items.add(new UserSettingItem("Connections", R.drawable.ic_discord_logo));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }

    private void setupBillingSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvBillingSettings);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("Shop", R.drawable.ic_store_modern));
        items.add(new UserSettingItem("Quests", R.drawable.ic_quest_badge));
        items.add(new UserSettingItem("Server Boost", R.drawable.ic_nitro_boost));
        items.add(new UserSettingItem("Nitro Gifting", R.drawable.ic_chat_bubble));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }

    private void setupAppSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvAppSettings);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("Voice", R.drawable.ic_chat_bubble, "Voice Activity"));
        items.add(new UserSettingItem("Appearance", R.drawable.ic_settings, "Dark"));
        items.add(new UserSettingItem("Accessibility", R.drawable.ic_person));
        items.add(new UserSettingItem("Language", R.drawable.ic_search, "English, US"));
        items.add(new UserSettingItem("Chat", R.drawable.ic_chat_bubble));
        items.add(new UserSettingItem("Web Browser", R.drawable.ic_search));
        items.add(new UserSettingItem("Notifications", R.drawable.ic_notifications));
        items.add(new UserSettingItem("App Icon", R.drawable.ic_discord_logo, true));
        items.add(new UserSettingItem("Advanced", R.drawable.ic_settings));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }

    private void setupSupportSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvSupportSettings);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("Support", R.drawable.ic_search));
        items.add(new UserSettingItem("Upload debug logs to Discord Support", R.drawable.ic_settings));
        items.add(new UserSettingItem("Acknowledgements", R.drawable.ic_settings));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }

    private void setupWhatsNewSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvWhatsNewSettings);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("What's New", R.drawable.ic_settings));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }

    private void setupLogoutSetting(View view) {
        RecyclerView rv = view.findViewById(R.id.rvLogoutSetting);
        List<UserSettingItem> items = new ArrayList<>();
        items.add(new UserSettingItem("Log Out", R.drawable.ic_back_arrow, null, false, true));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserSettingsAdapter(items, settingsClickListener));
    }
}
