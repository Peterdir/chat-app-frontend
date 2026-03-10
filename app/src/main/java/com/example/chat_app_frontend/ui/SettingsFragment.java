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
import com.example.chat_app_frontend.adapter.SettingsAdapter;
import com.example.chat_app_frontend.model.SettingsItem;
import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

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

    private final SettingsAdapter.OnItemClickListener settingsClickListener = item -> {
        if ("Get Nitro".equals(item.getTitle())) {
            Intent intent = new Intent(getActivity(), NitroActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        } else if ("Shop".equals(item.getTitle())) {
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
        List<SettingsItem> items = new ArrayList<>();
        items.add(new SettingsItem("Get Nitro", R.drawable.ic_nitro_boost));
        items.add(new SettingsItem("Account", R.drawable.ic_person));
        items.add(new SettingsItem("Content & Social", R.drawable.ic_chat_bubble));
        items.add(new SettingsItem("Data & Privacy", R.drawable.ic_settings));
        items.add(new SettingsItem("Family Center", R.drawable.ic_person_add));
        items.add(new SettingsItem("Authorized Apps", R.drawable.ic_store_modern));
        items.add(new SettingsItem("Devices", R.drawable.ic_phone));
        items.add(new SettingsItem("Connections", R.drawable.ic_discord_logo));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SettingsAdapter(items, settingsClickListener));
    }

    private void setupBillingSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvBillingSettings);
        List<SettingsItem> items = new ArrayList<>();
        items.add(new SettingsItem("Shop", R.drawable.ic_store_modern));
        items.add(new SettingsItem("Quests", R.drawable.ic_quest_badge));
        items.add(new SettingsItem("Server Boost", R.drawable.ic_nitro_boost));
        items.add(new SettingsItem("Nitro Gifting", R.drawable.ic_chat_bubble));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SettingsAdapter(items, settingsClickListener));
    }

    private void setupAppSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvAppSettings);
        List<SettingsItem> items = new ArrayList<>();
        items.add(new SettingsItem("Voice", R.drawable.ic_chat_bubble, "Voice Activity"));
        items.add(new SettingsItem("Appearance", R.drawable.ic_settings, "Dark"));
        items.add(new SettingsItem("Accessibility", R.drawable.ic_person));
        items.add(new SettingsItem("Language", R.drawable.ic_search, "English, US"));
        items.add(new SettingsItem("Chat", R.drawable.ic_chat_bubble));
        items.add(new SettingsItem("Web Browser", R.drawable.ic_search));
        items.add(new SettingsItem("Notifications", R.drawable.ic_notifications));
        items.add(new SettingsItem("App Icon", R.drawable.ic_discord_logo, true));
        items.add(new SettingsItem("Advanced", R.drawable.ic_settings));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SettingsAdapter(items, settingsClickListener));
    }

    private void setupSupportSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvSupportSettings);
        List<SettingsItem> items = new ArrayList<>();
        items.add(new SettingsItem("Support", R.drawable.ic_search));
        items.add(new SettingsItem("Upload debug logs to Discord Support", R.drawable.ic_settings));
        items.add(new SettingsItem("Acknowledgements", R.drawable.ic_settings));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SettingsAdapter(items, settingsClickListener));
    }

    private void setupWhatsNewSettings(View view) {
        RecyclerView rv = view.findViewById(R.id.rvWhatsNewSettings);
        List<SettingsItem> items = new ArrayList<>();
        items.add(new SettingsItem("What's New", R.drawable.ic_settings));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SettingsAdapter(items, settingsClickListener));
    }

    private void setupLogoutSetting(View view) {
        RecyclerView rv = view.findViewById(R.id.rvLogoutSetting);
        List<SettingsItem> items = new ArrayList<>();
        items.add(new SettingsItem("Log Out", R.drawable.ic_back_arrow, null, false, true));

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SettingsAdapter(items, settingsClickListener));
    }
}
