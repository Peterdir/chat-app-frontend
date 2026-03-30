package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.utils.FirebaseManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BuyOrbsActivity extends AppCompatActivity {

    private static final String PAYMENT_SERVICE_BASE_URL = "https://chat-app-frontend-vxcr.onrender.com";
    private static final String PACKAGE_100_ORBS = "PACKAGE_100_ORBS";
    private static final String PACKAGE_500_ORBS = "PACKAGE_500_ORBS";
    private static final String PACKAGE_1000_ORBS = "PACKAGE_1000_ORBS";

    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_buy_orbs);

        setupAnimations();
        handlePaymentResultIntent(getIntent());
    }

    private void setupAnimations() {
        Animation animSlideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up);
        findViewById(android.R.id.content).startAnimation(animSlideUp);

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupShimmerButton(R.id.btn_package_100, R.id.view_shine_100);
        setupShimmerButton(R.id.btn_package_500, R.id.view_shine_500);
        setupShimmerButton(R.id.btn_package_1000, R.id.view_shine_1000);

        setupPurchaseButtons();
    }

    private void setupShimmerButton(int btnId, int shineId) {
        View btnContainer = findViewById(btnId);
        View viewShine = findViewById(shineId);

        if (btnContainer != null && viewShine != null) {
            Animation animShimmer = AnimationUtils.loadAnimation(this, R.anim.anim_shimmer);
            viewShine.startAnimation(animShimmer);

            // Using separate click listener for the visual effect so we don't interfere with the main click listener
        }
    }

    private void setupPurchaseButtons() {
        View btn100 = findViewById(R.id.btn_package_100);
        View btn500 = findViewById(R.id.btn_package_500);
        View btn1000 = findViewById(R.id.btn_package_1000);

        if (btn100 != null) btn100.setOnClickListener(v -> handleButtonClick(v, PACKAGE_100_ORBS));
        if (btn500 != null) btn500.setOnClickListener(v -> handleButtonClick(v, PACKAGE_500_ORBS));
        if (btn1000 != null) btn1000.setOnClickListener(v -> handleButtonClick(v, PACKAGE_1000_ORBS));
    }

    private void handleButtonClick(View view, String packageType) {
        Animation animClick = AnimationUtils.loadAnimation(BuyOrbsActivity.this, R.anim.anim_click);
        view.startAnimation(animClick);
        startVnpayCheckout(packageType);
    }

    private void startVnpayCheckout(String packageType) {
        if (FirebaseManager.getAuth().getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để nạp Orbs", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang tạo phiên thanh toán...", Toast.LENGTH_SHORT).show();
        FirebaseManager.getAuth().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(result -> createPaymentWithToken(packageType, result.getToken()))
                .addOnFailureListener(error -> Toast.makeText(
                        BuyOrbsActivity.this,
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
                    String errorMsg = "Mã lỗi: " + code;
                    try {
                        JSONObject errJson = new JSONObject(response);
                        if (errJson.has("message")) {
                            errorMsg = errJson.getString("message");
                        } else if (errJson.has("error")) {
                            errorMsg = errJson.getString("error");
                        }
                    } catch (Exception ignored) {
                        if (!response.isEmpty() && response.length() < 100) {
                            errorMsg = response;
                        }
                    }
                    throw new IllegalStateException(errorMsg);
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
                        BuyOrbsActivity.this,
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
        if (!"chatapp".equals(data.getScheme()) || !"orbs-payment".equals(data.getHost())) return;

        String status = data.getQueryParameter("status");
        String packageType = data.getQueryParameter("packageType");

        String packageName = packageType;
        if (PACKAGE_100_ORBS.equals(packageType)) packageName = "100 Orbs";
        else if (PACKAGE_500_ORBS.equals(packageType)) packageName = "500 Orbs";
        else if (PACKAGE_1000_ORBS.equals(packageType)) packageName = "1000 Orbs";

        if ("success".equalsIgnoreCase(status)) {
            Toast.makeText(this, "Nạp thành công gói " + packageName, Toast.LENGTH_LONG).show();
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
}
