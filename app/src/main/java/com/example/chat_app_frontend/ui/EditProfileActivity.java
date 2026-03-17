package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Decoration;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.DecorationRepository;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import android.widget.Toast;

public class EditProfileActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ImageView btnBack;
    private TextView btnSave;
    private View btnNitroPreview;
    private View shimmerNitroPreview;
    private EditText etDisplayName, etPronouns, etAboutMe;
    private TextView txtDecorationNameCurrent;
    private ImageView imgMainAvatarDecoration;
    private User currentUser;
    private String currentDecorationId = "none";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupTabs();
        setupButtons();
        loadUserInfo();
        
        // Bắt đầu hiệu ứng lấp lánh cho nút Nitro
        startShimmerAnimation();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
        btnNitroPreview = findViewById(R.id.btn_nitro_preview);
        shimmerNitroPreview = findViewById(R.id.shimmer_nitro_preview);
        etDisplayName = findViewById(R.id.et_display_name);
        etPronouns = findViewById(R.id.et_pronouns);
        etAboutMe = findViewById(R.id.et_about_me);
        txtDecorationNameCurrent = findViewById(R.id.txt_decoration_name_current);
        imgMainAvatarDecoration = findViewById(R.id.img_main_avatar_decoration);
    }

    private void setupTabs() {
        // Mặc định chọn tab thứ 1: Hồ Sơ Chính (index 0)
        TabLayout.Tab targetTab = tabLayout.getTabAt(0);
        if (targetTab != null) {
            targetTab.select();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    // Chuyển sang màn hình EditServerProfileActivity (Hồ Sơ Theo Máy Chủ)
                    Intent intent = new Intent(EditProfileActivity.this, EditServerProfileActivity.class);
                    startActivity(intent);
                    finish(); // Kết thúc màn hình hiện tại để tránh chồng chất
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupButtons() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveUserInfo());
        }

        // Clear name button
        View btnClearName = findViewById(R.id.btn_clear_name);
        EditText etDisplayName = findViewById(R.id.et_display_name);
        if (btnClearName != null && etDisplayName != null) {
            btnClearName.setOnClickListener(v -> etDisplayName.setText(""));
        }

        if (btnNitroPreview != null) {
            btnNitroPreview.setOnClickListener(v -> {
                Intent intent = new Intent(this, NitroActivity.class);
                startActivity(intent);
            });
        }

        // Decoration item
        View itemAvatarDecoration = findViewById(R.id.item_avatar_decoration);
        TextView txtDecorationNameCurrent = findViewById(R.id.txt_decoration_name_current);
        ImageView imgMainAvatarDecoration = findViewById(R.id.img_main_avatar_decoration);

        if (itemAvatarDecoration != null) {
            itemAvatarDecoration.setOnClickListener(v -> {
                DecorationSelectionBottomSheet bottomSheet = new DecorationSelectionBottomSheet();
                bottomSheet.setInitialDecorationId(currentDecorationId); 
                bottomSheet.setOnDecorationAppliedListener(decoration -> {
                    currentDecorationId = decoration.getId();
                    updateCurrentDecorationUI(decoration);
                });
                bottomSheet.show(getSupportFragmentManager(), "DecorationSelectionBottomSheet");
            });
        }
    }

    private void updateCurrentDecorationUI(Decoration decoration) {
        if (decoration == null || decoration.getType() == Decoration.Type.NONE) {
            imgMainAvatarDecoration.setVisibility(View.GONE);
            if (txtDecorationNameCurrent != null) txtDecorationNameCurrent.setText("Không");
        } else {
            imgMainAvatarDecoration.setVisibility(View.VISIBLE);
            imgMainAvatarDecoration.setImageResource(decoration.getDrawableResId());
            if (txtDecorationNameCurrent != null) txtDecorationNameCurrent.setText(decoration.getName());
        }
    }

    private void loadUserInfo() {
        FirebaseUser fbUser = FirebaseManager.getAuth().getCurrentUser();
        if (fbUser == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        UserRepository.getInstance().getUserByUid(fbUser.getUid(), new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user;
                etDisplayName.setText(user.getDisplayName());
                etPronouns.setText(user.getPronouns());
                etAboutMe.setText(user.getBio());
                
                currentDecorationId = user.getAvatarDecorationId();
                Decoration currentDecor = DecorationRepository.getInstance().findDecorationById(currentDecorationId);
                updateCurrentDecorationUI(currentDecor);
            }

            @Override
            public void onUserNotFound() {
                Toast.makeText(EditProfileActivity.this, "Không tìm thấy thông tin user", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(EditProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo() {
        if (currentUser == null) return;

        currentUser.setDisplayName(etDisplayName.getText().toString());
        currentUser.setPronouns(etPronouns.getText().toString());
        currentUser.setBio(etAboutMe.getText().toString());
        currentUser.setAvatarDecorationId(currentDecorationId);

        UserRepository.getInstance().saveUser(currentUser, new UserRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditProfileActivity.this, "Đã lưu hồ sơ", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(EditProfileActivity.this, "Lưu thất bại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startShimmerAnimation() {
        if (btnNitroPreview != null && shimmerNitroPreview != null) {
            Runnable shimmerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isDestroyed() || isFinishing()) return;

                    shimmerNitroPreview.setVisibility(View.VISIBLE);
                    shimmerNitroPreview.setTranslationX(-150f);

                    btnNitroPreview.post(() -> {
                        float endX = btnNitroPreview.getWidth() + 150f;
                        shimmerNitroPreview.animate()
                                .translationX(endX)
                                .setDuration(1500)
                                .withEndAction(() -> {
                                    shimmerNitroPreview.setVisibility(View.INVISIBLE);
                                    shimmerNitroPreview.setTranslationX(-150f);
                                    shimmerNitroPreview.postDelayed(this, 3000);
                                })
                                .start();
                    });
                }
            };
            shimmerNitroPreview.postDelayed(shimmerRunnable, 1000);
        }
    }
}
