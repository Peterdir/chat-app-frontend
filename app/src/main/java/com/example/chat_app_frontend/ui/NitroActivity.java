package com.example.chat_app_frontend.ui;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.utils.FirebaseManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NitroActivity extends AppCompatActivity {

    private static final String PAYMENT_SERVICE_BASE_URL = "https://chat-app-frontend-vxcr.onrender.com";
    private static final String PACKAGE_NITRO = "NITRO";
    private static final String PACKAGE_BASIC = "BASIC";

    private ViewPager2 viewPagerPerks;
    private LinearLayout layoutDots;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_nitro);

        setupAnimations();
        setupSlider();
        handlePaymentResultIntent(getIntent());
    }

    private void setupAnimations() {
        Animation animHover = AnimationUtils.loadAnimation(this, R.anim.anim_hover);
        Animation animSlideIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in);
        Animation animGlow = AnimationUtils.loadAnimation(this, R.anim.anim_glow);
        Animation animSlideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up);

        findViewById(android.R.id.content).startAnimation(animSlideUp);

        ImageView imgNitro = findViewById(R.id.img_wumpus_nitro);
        if (imgNitro != null) imgNitro.startAnimation(animHover);

        ImageView imgBasic = findViewById(R.id.img_wumpus_basic);
        if (imgBasic != null) {
            animSlideIn.setStartOffset(1000);
            imgBasic.startAnimation(animSlideIn);
        }

        TextView tvTitle = findViewById(R.id.tv_header_nitro);
        if (tvTitle != null) tvTitle.startAnimation(animGlow);

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }


        setupShimmerButton(R.id.btn_get_nitro, R.id.view_shine);

        setupShimmerButton(R.id.btn_get_basic, R.id.view_shine_basic);

        setupShimmerButton(R.id.btn_get_nitro_footer, R.id.view_shine_footer);
        setupPurchaseButtons();
    }

    private void setupShimmerButton(int btnId, int shineId) {
        View btnContainer = findViewById(btnId);
        View viewShine = findViewById(shineId);

        if (btnContainer != null && viewShine != null) {
            Animation animShimmer = AnimationUtils.loadAnimation(this, R.anim.anim_shimmer);
            viewShine.startAnimation(animShimmer);

            btnContainer.setOnClickListener(v -> {
                Animation animClick = AnimationUtils.loadAnimation(NitroActivity.this, R.anim.anim_click);
                v.startAnimation(animClick);
            });
        }
    }

    private void setupPurchaseButtons() {
        View btnNitroTop = findViewById(R.id.btn_get_nitro);
        View btnNitroFooter = findViewById(R.id.btn_get_nitro_footer);
        View btnBasic = findViewById(R.id.btn_get_basic);

        if (btnNitroTop != null) {
            btnNitroTop.setOnClickListener(v -> startVnpayCheckout(PACKAGE_NITRO));
        }
        if (btnNitroFooter != null) {
            btnNitroFooter.setOnClickListener(v -> startVnpayCheckout(PACKAGE_NITRO));
        }
        if (btnBasic != null) {
            btnBasic.setOnClickListener(v -> startVnpayCheckout(PACKAGE_BASIC));
        }
    }

    private void startVnpayCheckout(String packageType) {
        if (FirebaseManager.getAuth().getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để mua Nitro", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang tạo phiên thanh toán...", Toast.LENGTH_SHORT).show();
        FirebaseManager.getAuth().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(result -> createPaymentWithToken(packageType, result.getToken()))
                .addOnFailureListener(error -> Toast.makeText(
                        NitroActivity.this,
                        "Không lấy được token đăng nhập: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void createPaymentWithToken(String packageType, String idToken) {
        networkExecutor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(PAYMENT_SERVICE_BASE_URL + "/vnpay/create-payment");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(12000);
                conn.setReadTimeout(12000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + idToken);

                JSONObject body = new JSONObject();
                body.put("packageType", packageType);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                String response = readAll(conn);
                if (code < 200 || code >= 300) {
                    throw new IllegalStateException("Không thể tạo thanh toán. Mã lỗi: " + code);
                }

                JSONObject json = new JSONObject(response);
                String paymentUrl = json.optString("paymentUrl", "");
                if (paymentUrl.isEmpty()) {
                    throw new IllegalStateException("Không nhận được payment URL");
                }

                mainHandler.post(() -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                    startActivity(browserIntent);
                });
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(
                        NitroActivity.this,
                        "Tạo thanh toán thất bại: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    private String readAll(HttpURLConnection conn) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                StandardCharsets.UTF_8
        ));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handlePaymentResultIntent(intent);
    }

    private void handlePaymentResultIntent(Intent intent) {
        if (intent == null || intent.getData() == null) return;
        Uri data = intent.getData();
        if (!"chatapp".equals(data.getScheme()) || !"nitro-payment".equals(data.getHost())) return;

        String status = data.getQueryParameter("status");
        String packageType = data.getQueryParameter("packageType");
        if ("success".equalsIgnoreCase(status)) {
            Toast.makeText(this, "Thanh toán thành công gói " + (packageType == null ? "Nitro" : packageType), Toast.LENGTH_LONG).show();
        } else {
            String code = data.getQueryParameter("code");
            Toast.makeText(this, "Thanh toán thất bại (mã: " + (code == null ? "?" : code) + ")", Toast.LENGTH_LONG).show();
        }
        intent.setData(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkExecutor.shutdownNow();
    }

    private void setupSlider() {
        viewPagerPerks = findViewById(R.id.viewPagerPerks);
        layoutDots = findViewById(R.id.layout_dots);

        List<PerkItem> list = new ArrayList<>();
        list.add(new PerkItem("Cường điệu, giễu cợt và tạo meme bằng emoji tùy chỉnh.", R.drawable.icons));
        list.add(new PerkItem("Tải lên tập tin khổng lồ 500MB để chia sẻ dữ liệu.", R.drawable.files));
        list.add(new PerkItem("Phát trực tiếp chất lượng HD mượt mà sắc nét.", R.drawable.lives));

        PerkAdapter adapter = new PerkAdapter(list);
        viewPagerPerks.setAdapter(adapter);

        addDots(list.size());
        updateDots(0);

        viewPagerPerks.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void addDots(int count) {
        if (layoutDots == null) return;
        layoutDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
            params.setMargins(10, 0, 10, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(android.R.drawable.radiobutton_off_background);
            layoutDots.addView(dot);
        }
    }

    private void updateDots(int position) {
        if (layoutDots == null) return;
        for (int i = 0; i < layoutDots.getChildCount(); i++) {
            View dot = layoutDots.getChildAt(i);
            if (i == position) {
                dot.setAlpha(1.0f);
                dot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            } else {
                dot.setAlpha(0.4f);
                dot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            }
        }
    }

    static class PerkItem {
        String title;
        int imageRes;
        public PerkItem(String title, int imageRes) {
            this.title = title;
            this.imageRes = imageRes;
        }
    }

    class PerkAdapter extends RecyclerView.Adapter<PerkAdapter.PerkViewHolder> {
        private List<PerkItem> items;
        public PerkAdapter(List<PerkItem> items) { this.items = items; }

        @NonNull
        @Override
        public PerkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nitro_perk, parent, false);
            return new PerkViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PerkViewHolder holder, int position) {
            PerkItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.imgIcon.setImageResource(item.imageRes);
        }

        @Override
        public int getItemCount() { return items.size(); }

        class PerkViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            ImageView imgIcon;
            public PerkViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_perk_title);
                imgIcon = itemView.findViewById(R.id.img_perk_icon);
            }
        }
    }
}