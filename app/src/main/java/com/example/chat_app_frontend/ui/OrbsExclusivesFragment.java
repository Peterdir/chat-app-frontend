package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.OrbAdapter;
import com.example.chat_app_frontend.model.OrbItem;
import java.util.ArrayList;
import java.util.List;

public class OrbsExclusivesFragment extends Fragment {

    private RecyclerView rvOrbs;
    private OrbAdapter orbAdapter;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orbs_exclusives, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        rvOrbs = view.findViewById(R.id.rvOrbs);

        setupToolbar();
        setupRecyclerView();

        return view;
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    private void setupRecyclerView() {
        List<OrbItem> items = new ArrayList<>();
        
        // Hàng 1
        items.add(new OrbItem("3-Day Nitro Credit", 1400, R.drawable.nitro_3_days));
        items.add(new OrbItem("Orbs Apprentice Badge", 1200, R.drawable.img));
        
        // Hàng 2
        items.add(new OrbItem("Infinite Swirl Bundle", 8900, R.drawable.infinite_swrirl_bundle));
        items.add(new OrbItem("Magic Mists", 3500, R.drawable.magic_mists));
        
        // Hàng 3
        items.add(new OrbItem("Infinite Swirl", 3500, R.drawable.magic_mists));
        items.add(new OrbItem("Pondering Portal", 3500, R.drawable.magic_mists));
        
        // Hàng 4
        items.add(new OrbItem("Infinite Swirl", 3500, R.drawable.infinite_swirl_bundle2));
        items.add(new OrbItem("Magic Mists", 3500, R.drawable.infinite_swirl_bundle2));
        
        // Hàng 5
        items.add(new OrbItem("Nevermore", 4100, R.drawable.nevermore));
        items.add(new OrbItem("Lone Wolf Bundle", 11000, R.drawable.nevermore));

        orbAdapter = new OrbAdapter(items);
        rvOrbs.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvOrbs.setAdapter(orbAdapter);
        rvOrbs.setNestedScrollingEnabled(false);
    }
}
