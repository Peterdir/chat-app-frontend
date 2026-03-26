package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class ShopAllActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_all);

        // Nút đóng màn hình
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        
        // --- SECTION 1: DISNEY MICKEY ---
        setupProductItem(R.id.product_mickey_1, R.drawable.mickey_ear_headband1, "Băng Đô Tai Mickey", "189.000 đ");
        setupProductItem(R.id.product_mickey_2, R.drawable.mickey_ear_headband2, "Băng Đô Tai Minnie", "189.000 đ");

        // --- SECTION 2: FEELING LUCKY ---
        setupProductItem(R.id.product_feeling_1, R.drawable.feeling_asset1, "Gói Thỏ May Mắn", "189.000 đ");
        setupProductItem(R.id.product_feeling_2, R.drawable.feeling_asset2, "Gói Kỷ Nguyên May Mắn", "189.000 đ");

        // --- SECTION 3: YEAR OF THE HORSE ---
        setupProductItem(R.id.product_year_1, R.drawable.yearhorse_asset1, "Gói Năm Ngọ", "189.000 đ");
        setupProductItem(R.id.product_year_2, R.drawable.yearhorse_asset2, "Gói Lễ Hội Đèn Lồng", "189.000 đ");

        // --- SECTION 4: GAMETIME ---
        setupProductItem(R.id.product_game_1, R.drawable.gametime_asset1, "Ngày Thi Đấu", "79.000 đ");
        setupProductItem(R.id.product_game_2, R.drawable.gametime_asset2, "Thể Thao Muôn Năm", "79.000 đ");

        // --- SECTION 5: GOTHICA ---
        setupProductItem(R.id.product_gothica_1, R.drawable.gothica_asset1, "Gói Hoa Hồng Hắc...", "189.000 đ");
        setupProductItem(R.id.product_gothica_2, R.drawable.gothica_asset2, "Gói Tận Thế U Sầu", "189.000 đ");

        // --- SECTION 6: TAROT ---
        setupProductItem(R.id.product_tarot_1, R.drawable.tarot_asset1, "Gói Ảo Thuật Gia", "189.000 đ");
        setupProductItem(R.id.product_tarot_2, R.drawable.tarot_asset2, "Gói Bói Toán", "189.000 đ");

        // --- SECTION 7: FLUX ---
        setupProductItem(R.id.product_flux_1, R.drawable.flux_asset1, "Gói Ánh Neon", "189.000 đ");
        setupProductItem(R.id.product_flux_2, R.drawable.flux_asset2, "Gói Hào Quang Hu...", "189.000 đ");

        // --- SECTION 8: SLUMBER PARTY ---
        setupProductItem(R.id.product_slumber_1, R.drawable.slumberparty_asset1, "Gói Sóng Mộng Mơ", "189.000 đ");
        setupProductItem(R.id.product_slumber_2, R.drawable.slumberparty_asset2, "Gói Đồng Cỏ Ánh T...", "189.000 đ");

        // --- SECTION 9: ZODIAC ---
        setupProductItem(R.id.product_zodiac_1, R.drawable.zodiac_asset1, "Gói Bạch Dương", "189.000 đ");
        setupProductItem(R.id.product_zodiac_2, R.drawable.zodiac_asset2, "Bạch Dương", "79.000 đ");
    }

    /**
     * Hàm hỗ trợ cập nhật dữ liệu cho một ô sản phẩm nhỏ (include)
     * @param includeId ID của thẻ <include> trong XML
     * @param imageRes ID của ảnh trong drawable
     * @param name Tên sản phẩm hiển thị
     * @param price Giá sản phẩm hiển thị
     */
    private void setupProductItem(int includeId, int imageRes, String name, String price) {
        View layout = findViewById(includeId);
        if (layout != null) {
            ImageView img = layout.findViewById(R.id.img_asset);
            TextView txtName = layout.findViewById(R.id.txt_name);
            TextView txtPrice = layout.findViewById(R.id.txt_price);

            if (img != null) img.setImageResource(imageRes);
            if (txtName != null) txtName.setText(name);
            if (txtPrice != null) txtPrice.setText(price);
        }
    }
}
