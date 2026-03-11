package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.FeaturedProductAdapter;
import com.example.chat_app_frontend.adapter.ShopAdapter;
import com.example.chat_app_frontend.model.ShopItem;
import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView rvHorizontalShop;
    private ShopAdapter shopAdapter;
    private FeaturedProductAdapter featuredAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        initViews();
        setupVerticalGrid();
        setupHorizontalList();
        setupClickListeners();
        loadSampleData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        rvHorizontalShop = findViewById(R.id.rvHorizontalShop);
    }

    private void setupVerticalGrid() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        List<ShopItem> shopItems = new ArrayList<>();
        shopAdapter = new ShopAdapter(shopItems, item -> {
            Toast.makeText(this, "Clicked: " + item.getName(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(shopAdapter);
    }

    private void setupHorizontalList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvHorizontalShop.setLayoutManager(layoutManager);

        List<ShopItem> featuredItems = new ArrayList<>();
        featuredAdapter = new FeaturedProductAdapter(featuredItems);
        rvHorizontalShop.setAdapter(featuredAdapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadSampleData() {
        // 1. Data cho phần cuộn ngang (Mickey & Friends)
        List<ShopItem> featured = new ArrayList<>();
        featured.add(new ShopItem("f1", "Mickey Ear Headb...", R.drawable.mickey_ear_headband1, "0", "circular"));
        featured.add(new ShopItem("f2", "Minnie Ear Headb...", R.drawable.mickey_ear_headband2, "0", "circular"));
        featured.add(new ShopItem("f3", "Mickey Classic", R.drawable.mickey_ear_headband1, "0", "circular"));
        
        rvHorizontalShop.setAdapter(new FeaturedProductAdapter(featured));

        // 2. Data cho phần Popular Picks (Grid dưới)
        List<ShopItem> popular = new ArrayList<>();
        popular.add(new ShopItem("1", "Magic Mists", R.drawable.magic_mists, "3500", "circular"));
        popular.add(new ShopItem("2", "Angry", R.drawable.img, "1200", "circular"));
        popular.add(new ShopItem("3", "Infinite Swirl", R.drawable.infinite_swrirl_bundle, "3500", "circular"));
        popular.add(new ShopItem("4", "Magic Mists", R.drawable.infinite_swirl_bundle2, "8900", "banner"));
        popular.add(new ShopItem("5", "Fallen Angel (Black)", R.drawable.nevermore, "4100", "circular"));
        popular.add(new ShopItem("6", "Nevermore", R.drawable.nevermore, "4100", "banner"));

        // Thêm 8 assets placeholder
        popular.add(new ShopItem("7", "Moon", R.drawable.moon, "0", "circular"));
        popular.add(new ShopItem("8", "HunnyBunnies", R.drawable.hunnybunnies, "0", "circular"));
        popular.add(new ShopItem("9", "DarkRoses", R.drawable.darkroses, "0", "circular"));
        popular.add(new ShopItem("10", "Bonsai", R.drawable.bonsai, "0", "circular"));
        popular.add(new ShopItem("11", "the shame duck", R.drawable.theshameduck, "0", "circular"));
        popular.add(new ShopItem("12", "Aries", R.drawable.aries, "0", "circular"));
        popular.add(new ShopItem("13", "Hologram Dragon", R.drawable.hologram, "0", "circular"));
        popular.add(new ShopItem("14", "Dark Roses Bundle", R.drawable.darkrosebundle, "0", "circular"));

        shopAdapter.updateList(popular);
    }
}
