package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.example.chat_app_frontend.model.Decoration;
import com.example.chat_app_frontend.model.ProfileEffect;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.DecorationRepository;
import com.example.chat_app_frontend.repository.ProfileEffectRepository;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.model.NamePlate;
import com.example.chat_app_frontend.repository.NamePlateRepository;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;

public class EditServerProfileActivity extends AppCompatActivity implements NamePlateSelectionBottomSheet.OnNamePlateSelectedListener {

    private TabLayout tabLayout;
    private ImageView btnBack;
    private TextView tvSave;
    private View btnGetNitro;
    private View shimmerNitro;
    
    // New Views for synchronization
    private ShapeableImageView imgServerIcon, imgPreviewAvatar;
    private ImageView imgPreviewAvatarDecoration, imgPreviewProfileEffect;
    private TextView txtServerName, txtPreviewDisplayName, txtPreviewUsername;
    private EditText etServerNickname, etServerPronouns;
    private TextView txtDecorationNameCurrent, txtProfileEffectNameCurrent, txtNamePlateNameCurrent;
    private View itemAvatarDecoration, itemProfileEffect, itemNamePlate;
    
    private User currentUser;
    private String currentDecorationId = "none";
    private String currentProfileEffectId = "none";
    private String currentNamePlateId = "none";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_server_profile);

        initViews();
        setupTabs();
        setupButtons();
        loadUserInfo();
        setupTextWatchers();
        
        // Bắt đầu hiệu ứng lấp lánh cho nút Nitro
        startShimmerAnimation();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        btnBack = findViewById(R.id.btn_back);
        tvSave = findViewById(R.id.tv_save);
        btnGetNitro = findViewById(R.id.btn_get_nitro);
        shimmerNitro = findViewById(R.id.shimmer_nitro);
        
        imgServerIcon = findViewById(R.id.img_server_icon);
        txtServerName = findViewById(R.id.txt_server_name);
        imgPreviewAvatar = findViewById(R.id.img_preview_avatar);
        imgPreviewAvatarDecoration = findViewById(R.id.img_preview_avatar_decoration);
        imgPreviewProfileEffect = findViewById(R.id.img_preview_profile_effect);
        txtPreviewDisplayName = findViewById(R.id.txt_preview_display_name);
        txtPreviewUsername = findViewById(R.id.txt_preview_username);
        etServerNickname = findViewById(R.id.et_server_nickname);
        etServerPronouns = findViewById(R.id.et_server_pronouns);
        itemAvatarDecoration = findViewById(R.id.item_avatar_decoration);
        txtDecorationNameCurrent = findViewById(R.id.txt_decoration_name_current);
        itemProfileEffect = findViewById(R.id.item_profile_effect);
        txtProfileEffectNameCurrent = findViewById(R.id.txt_profile_effect_name_current);
        itemNamePlate = findViewById(R.id.item_name_plate);
        txtNamePlateNameCurrent = findViewById(R.id.txt_name_plate_name_current);
        
        updateSaveButtonState(false);
    }

    private void loadUserInfo() {
        FirebaseUser fbUser = FirebaseManager.getAuth().getCurrentUser();
        if (fbUser == null) return;

        UserRepository.getInstance().getUserByUid(fbUser.getUid(), new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user;
                
                // Load global data into server profile fields as defaults
                etServerNickname.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
                etServerPronouns.setText(user.getPronouns() != null ? user.getPronouns() : "");
                
                txtPreviewDisplayName.setText(user.getDisplayNameOrUserName());
                txtPreviewUsername.setText(user.getUserName());
                
                // Avatar loading
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    Glide.with(EditServerProfileActivity.this)
                        .load(user.getAvatarUrl())
                        .placeholder(R.drawable.img_discord)
                        .into(imgPreviewAvatar);
                }
                
                currentDecorationId = user.getAvatarDecorationId() != null ? user.getAvatarDecorationId() : "none";
                updateDecorationUI(currentDecorationId);
                
                currentProfileEffectId = user.getProfileEffectId() != null ? user.getProfileEffectId() : "none";
                updateProfileEffectUI(currentProfileEffectId);

                currentNamePlateId = user.getNamePlateId() != null ? user.getNamePlateId() : "none";
                updateNamePlateUI(currentNamePlateId);

                // Mock server info
                if (txtServerName != null) txtServerName.setText("Máy chủ của " + user.getDisplayNameOrUserName());
            }

            @Override
            public void onUserNotFound() {}

            @Override
            public void onFailure(String error) {}
        });
    }

    private void updateDecorationUI(String decorationId) {
        Decoration decor = DecorationRepository.getInstance().findDecorationById(decorationId);
        if (decor != null && txtDecorationNameCurrent != null) {
            txtDecorationNameCurrent.setText(decor.getName());
            if (imgPreviewAvatarDecoration != null) {
                imgPreviewAvatarDecoration.setVisibility(View.VISIBLE);
                Glide.with(this).load(decor.getDrawableResId()).into(imgPreviewAvatarDecoration);
            }
        } else if (txtDecorationNameCurrent != null) {
            txtDecorationNameCurrent.setText("Sử Dụng Mặc Định");
            if (imgPreviewAvatarDecoration != null) imgPreviewAvatarDecoration.setVisibility(View.GONE);
        }
    }

    private void updateNamePlateUI(String plateId) {
        NamePlate plate = NamePlateRepository.getInstance().findNamePlateById(plateId);
        if (plate != null && txtNamePlateNameCurrent != null) {
            txtNamePlateNameCurrent.setText(plate.getName());
            if (imgPreviewProfileEffect != null) {
                if (plate.getType() == NamePlate.Type.NONE) {
                    imgPreviewProfileEffect.setImageResource(0);
                    imgPreviewProfileEffect.setVisibility(View.GONE);
                } else {
                    imgPreviewProfileEffect.setVisibility(View.VISIBLE);
                    imgPreviewProfileEffect.setImageResource(plate.getDrawableResId());
                }
            }
        } else if (txtNamePlateNameCurrent != null) {
            txtNamePlateNameCurrent.setText("Sử Dụng Mặc Định");
            if (imgPreviewProfileEffect != null) imgPreviewProfileEffect.setVisibility(View.GONE);
        }
    }

    private void updateProfileEffectUI(String effectId) {
        ProfileEffect effect = ProfileEffectRepository.getInstance().findEffectById(effectId);
        if (effect != null && txtProfileEffectNameCurrent != null) {
            txtProfileEffectNameCurrent.setText(effect.getName());
            if (imgPreviewProfileEffect != null) {
                if (effect.getType() == ProfileEffect.Type.NONE || effect.getType() == ProfileEffect.Type.SHOP) {
                    imgPreviewProfileEffect.setVisibility(View.GONE);
                } else {
                    imgPreviewProfileEffect.setVisibility(View.VISIBLE);
                    Glide.with(this).load(effect.getEffectResId()).into(imgPreviewProfileEffect);
                }
            }
        } else if (txtProfileEffectNameCurrent != null) {
            txtProfileEffectNameCurrent.setText("Sử Dụng Mặc Định");
            if (imgPreviewProfileEffect != null) imgPreviewProfileEffect.setVisibility(View.GONE);
        }
    }

    private void setupTabs() {
        TabLayout.Tab targetTab = tabLayout.getTabAt(1);
        if (targetTab != null) targetTab.select();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    Intent intent = new Intent(EditServerProfileActivity.this, EditProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());
        
        if (tvSave != null) {
            tvSave.setOnClickListener(v -> saveUserInfo());
        }
        
        if (btnGetNitro != null) {
            btnGetNitro.setOnClickListener(v -> {
                Intent intent = new Intent(this, NitroActivity.class);
                startActivity(intent);
            });
        }
        
        if (itemAvatarDecoration != null) {
            itemAvatarDecoration.setOnClickListener(v -> {
                DecorationSelectionBottomSheet bottomSheet = new DecorationSelectionBottomSheet();
                bottomSheet.setInitialDecorationId(currentDecorationId);
                bottomSheet.setOnDecorationAppliedListener(decoration -> {
                    currentDecorationId = decoration.getId();
                    updateDecorationUI(currentDecorationId);
                    checkChanges();
                });
                bottomSheet.show(getSupportFragmentManager(), "DecorationSelectionBottomSheet");
            });
        }

        if (itemProfileEffect != null) {
            itemProfileEffect.setOnClickListener(v -> {
                ProfileEffectSelectionBottomSheet bottomSheet = new ProfileEffectSelectionBottomSheet();
                bottomSheet.setInitialEffectId(currentProfileEffectId);
                bottomSheet.setUser(currentUser);
                bottomSheet.setOnEffectAppliedListener(effect -> {
                    currentProfileEffectId = effect.getId();
                    updateProfileEffectUI(currentProfileEffectId);
                    checkChanges();
                });
                bottomSheet.show(getSupportFragmentManager(), "ProfileEffectSelectionBottomSheet");
            });
        }
        
        if (itemNamePlate != null) {
            itemNamePlate.setOnClickListener(v -> {
                NamePlateSelectionBottomSheet bottomSheet = NamePlateSelectionBottomSheet.newInstance(currentNamePlateId);
                bottomSheet.show(getSupportFragmentManager(), "NamePlateSelectionBottomSheet");
            });
        }
    }

    @Override
    public void onNamePlateSelected(NamePlate plate) {
        currentNamePlateId = plate.getId();
        updateNamePlateUI(currentNamePlateId);
        checkChanges();
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkChanges();
                if (etServerNickname.hasFocus()) {
                    String previewName = s.length() > 0 ? s.toString() : (currentUser != null ? currentUser.getUserName() : "");
                    txtPreviewDisplayName.setText(previewName);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        etServerNickname.addTextChangedListener(watcher);
        etServerPronouns.addTextChangedListener(watcher);
    }

    private void checkChanges() {
        if (currentUser == null) return;
        boolean changed = false;
        
        String nick = etServerNickname.getText().toString();
        String pr = etServerPronouns.getText().toString();
        
        if (!nick.equals(currentUser.getDisplayName())) changed = true;
        String currentPr = currentUser.getPronouns() != null ? currentUser.getPronouns() : "";
        if (!pr.equals(currentPr)) changed = true;
        if (!currentDecorationId.equals(currentUser.getAvatarDecorationId())) changed = true;
        if (!currentProfileEffectId.equals(currentUser.getProfileEffectId())) changed = true;
        if (!currentNamePlateId.equals(currentUser.getNamePlateId())) changed = true;
        
        updateSaveButtonState(changed);
    }

    private void updateSaveButtonState(boolean enabled) {
        if (tvSave != null) {
            tvSave.setEnabled(enabled);
            tvSave.setAlpha(enabled ? 1.0f : 0.5f);
            tvSave.setTextColor(enabled ? Color.WHITE : Color.parseColor("#80FFFFFF"));
        }
    }

    private void saveUserInfo() {
        if (currentUser == null) return;
        
        currentUser.setDisplayName(etServerNickname.getText().toString());
        currentUser.setPronouns(etServerPronouns.getText().toString());
        currentUser.setAvatarDecorationId(currentDecorationId);
        currentUser.setProfileEffectId(currentProfileEffectId);
        currentUser.setNamePlateId(currentNamePlateId);
        
        UserRepository.getInstance().saveUser(currentUser, new UserRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditServerProfileActivity.this, "Đã lưu hồ sơ máy chủ", Toast.LENGTH_SHORT).show();
                updateSaveButtonState(false);
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(EditServerProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startShimmerAnimation() {
        if (btnGetNitro != null && shimmerNitro != null) {
            Runnable shimmerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isDestroyed() || isFinishing()) return;
                    shimmerNitro.setVisibility(View.VISIBLE);
                    shimmerNitro.setTranslationX(-150f);
                    btnGetNitro.post(() -> {
                        float endX = btnGetNitro.getWidth() + 150f;
                        shimmerNitro.animate()
                                .translationX(endX)
                                .setDuration(1500)
                                .withEndAction(() -> {
                                    shimmerNitro.setVisibility(View.INVISIBLE);
                                    shimmerNitro.setTranslationX(-150f);
                                    shimmerNitro.postDelayed(this, 3000);
                                })
                                .start();
                    });
                }
            };
            shimmerNitro.postDelayed(shimmerRunnable, 1000);
        }
    }
}
