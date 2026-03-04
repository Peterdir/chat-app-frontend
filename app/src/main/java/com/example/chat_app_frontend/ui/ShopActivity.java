package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.ShopAdapter;
import com.example.chat_app_frontend.model.ShopItem;
import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ShopAdapter shopAdapter;
    private List<ShopItem> shopItems;
    private TextView tvCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadSampleData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvCurrency = findViewById(R.id.tvCurrency);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        shopItems = new ArrayList<>();
        shopAdapter = new ShopAdapter(shopItems, item -> {
            Toast.makeText(this, "Clicked: " + item.getName(), Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(shopAdapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadSampleData() {
        // Gán ngẫu nhiên các asset từ drawable vào danh sách shop
        shopItems.add(new ShopItem("1", "Magic Mists", R.drawable.magic_mists, "3500", "circular"));
        shopItems.add(new ShopItem("2", "Angry", R.drawable.img, "1200", "circular"));
        shopItems.add(new ShopItem("3", "Infinite Swirl", R.drawable.infinite_swrirl_bundle, "3500", "circular"));
        shopItems.add(new ShopItem("4", "Magic Mists", R.drawable.infinite_swirl_bundle2, "8900", "banner"));
        shopItems.add(new ShopItem("5", "Fallen Angel (Black)", R.drawable.nevermore, "4100", "circular"));
        shopItems.add(new ShopItem("6", "Nevermore", R.drawable.nevermore, "4100", "banner"));
        shopItems.add(new ShopItem("7", "Wolf Moon", R.drawable.nitro_3_days, "1400", "circular"));
        shopItems.add(new ShopItem("8", "Dark Birds", R.drawable.ic_quest_badge, "1200", "circular"));

        shopAdapter.updateList(shopItems);
    }
}
