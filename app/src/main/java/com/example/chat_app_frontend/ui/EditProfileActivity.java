package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Decoration;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.DecorationRepository;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.model.ProfileEffect;
import com.example.chat_app_frontend.repository.ProfileEffectRepository;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.example.chat_app_frontend.model.NamePlate;
import com.example.chat_app_frontend.repository.NamePlateRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.badge.BadgeDrawable;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.Color;
import com.bumptech.glide.Glide;

public class EditProfileActivity extends AppCompatActivity implements NamePlateSelectionBottomSheet.OnNamePlateSelectedListener {

    private TabLayout tabLayout;
    private ImageView btnBack;
    private TextView btnSave;
    private View btnNitroPreview;
    private View shimmerNitroPreview;
    private EditText etDisplayName, etPronouns, etAboutMe;
    private TextView txtDecorationNameCurrent, txtProfileEffectNameCurrent, txtNamePlateNameCurrent;
    private TextView txtPreviewDisplayName, txtPreviewUsername, txtPreviewBio, txtPreviewPronouns, txtAboutMeCharCount;
    private ImageView imgMainAvatar, imgMainAvatarDecoration, imgMainProfileEffect, imgMainNamePlate, imgFullEffectAnimation;
    private User currentUser;
    private String currentDecorationId = "none";
    private String currentProfileEffectId = "none";
    private String currentNamePlateId = "none";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupTabs();
        setupButtons();
        loadUserInfo();
        
        setupTextWatchers();
        updateSaveButtonState(false);
        
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
        txtProfileEffectNameCurrent = findViewById(R.id.txt_profile_effect_name_current);
        txtNamePlateNameCurrent = findViewById(R.id.txt_name_plate_name_current);
        txtPreviewDisplayName = findViewById(R.id.txt_preview_display_name);
        txtPreviewUsername = findViewById(R.id.txt_preview_username);
        txtPreviewBio = findViewById(R.id.txt_preview_bio);
        txtPreviewPronouns = findViewById(R.id.txt_preview_pronouns);
        txtAboutMeCharCount = findViewById(R.id.txt_about_me_char_count);
        imgMainAvatar = findViewById(R.id.img_main_avatar);
        imgMainAvatarDecoration = findViewById(R.id.img_main_avatar_decoration);
        imgMainProfileEffect = findViewById(R.id.img_main_profile_effect);
        imgMainNamePlate = findViewById(R.id.img_main_name_plate);
        imgFullEffectAnimation = findViewById(R.id.img_full_effect_animation);
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
                    checkChanges();
                });
                bottomSheet.show(getSupportFragmentManager(), "DecorationSelectionBottomSheet");
            });
        }

        // Profile Effect item
        View itemProfileEffect = findViewById(R.id.item_profile_effect);
        if (itemProfileEffect != null) {
            itemProfileEffect.setOnClickListener(v -> {
                ProfileEffectSelectionBottomSheet bottomSheet = new ProfileEffectSelectionBottomSheet();
                bottomSheet.setInitialEffectId(currentProfileEffectId);
                bottomSheet.setUser(currentUser);
                bottomSheet.setOnEffectAppliedListener(effect -> {
                    currentProfileEffectId = effect.getId();
                    updateCurrentProfileEffectUI(effect);
                    playProfileEffectAnimation(effect);
                    checkChanges();
                });
                bottomSheet.show(getSupportFragmentManager(), "ProfileEffectSelectionBottomSheet");
            });
        }
        // Name Plate item
        View itemNamePlate = findViewById(R.id.item_name_plate);
        if (itemNamePlate != null) {
            itemNamePlate.setOnClickListener(v -> {
                NamePlateSelectionBottomSheet bottomSheet = NamePlateSelectionBottomSheet.newInstance(currentNamePlateId, currentUser);
                bottomSheet.show(getSupportFragmentManager(), "NamePlateSelectionBottomSheet");
            });
        }
    }

    @Override
    public void onNamePlateSelected(NamePlate plate) {
        currentNamePlateId = plate.getId();
        updateCurrentNamePlateUI(plate);
        checkChanges();
    }

    private void updateCurrentNamePlateUI(NamePlate plate) {
        if (txtNamePlateNameCurrent != null) {
            txtNamePlateNameCurrent.setText(plate.getName());
        }
        if (imgMainNamePlate != null) {
            if (plate == null || plate.getType() == NamePlate.Type.NONE || plate.getType() == NamePlate.Type.STORE) {
                imgMainNamePlate.setImageResource(0);
                imgMainNamePlate.setVisibility(View.GONE);
            } else {
                imgMainNamePlate.setVisibility(View.VISIBLE);
                Glide.with(this)
                    .load(plate.getDrawableResId())
                    .into(imgMainNamePlate);
            }
        }
    }

    private void updateCurrentProfileEffectUI(ProfileEffect effect) {
        if (effect == null || effect.getType() == ProfileEffect.Type.NONE || effect.getType() == ProfileEffect.Type.SHOP) {
            if (imgMainProfileEffect != null) imgMainProfileEffect.setVisibility(View.GONE);
            if (txtProfileEffectNameCurrent != null) txtProfileEffectNameCurrent.setText("Không");
        } else {
            if (imgMainProfileEffect != null) {
                imgMainProfileEffect.setVisibility(View.VISIBLE);
                Glide.with(this)
                    .load(effect.getEffectResId())
                    .into(imgMainProfileEffect);
            }
            if (txtProfileEffectNameCurrent != null) txtProfileEffectNameCurrent.setText(effect.getName());
        }
    }

    private void updateCurrentDecorationUI(Decoration decoration) {
        if (decoration == null || decoration.getType() == Decoration.Type.NONE) {
            imgMainAvatarDecoration.setVisibility(View.GONE);
            if (txtDecorationNameCurrent != null) txtDecorationNameCurrent.setText("Không");
        } else {
            imgMainAvatarDecoration.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(decoration.getDrawableResId())
                .into(imgMainAvatarDecoration);
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
                etDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
                etPronouns.setText(user.getPronouns() != null ? user.getPronouns() : "");
                etAboutMe.setText(user.getBio() != null ? user.getBio() : "");
                
                currentDecorationId = user.getAvatarDecorationId() != null ? user.getAvatarDecorationId() : "none";
                Decoration currentDecor = DecorationRepository.getInstance().findDecorationById(currentDecorationId);
                updateCurrentDecorationUI(currentDecor);

                currentProfileEffectId = user.getProfileEffectId() != null ? user.getProfileEffectId() : "none";
                ProfileEffect currentEffect = ProfileEffectRepository.getInstance().findEffectById(currentProfileEffectId);
                updateCurrentProfileEffectUI(currentEffect);

                currentNamePlateId = user.getNamePlateId() != null ? user.getNamePlateId() : "none";
                NamePlate currentPlate = NamePlateRepository.getInstance().findNamePlateById(currentNamePlateId);
                updateCurrentNamePlateUI(currentPlate);

                // Update Preview Info
                if (txtPreviewDisplayName != null) {
                    txtPreviewDisplayName.setText(user.getDisplayNameOrUserName());
                }
                if (txtPreviewUsername != null) {
                    txtPreviewUsername.setText(user.getUserName());
                }
                if (txtPreviewBio != null) {
                    txtPreviewBio.setText(user.getBio() != null ? user.getBio() : "");
                }
                if (txtPreviewPronouns != null) {
                    String pr = user.getPronouns();
                    if (pr != null && !pr.isEmpty()) {
                        txtPreviewPronouns.setVisibility(View.VISIBLE);
                        txtPreviewPronouns.setText(pr);
                    } else {
                        txtPreviewPronouns.setVisibility(View.GONE);
                    }
                }
                
                // Avatar loading
                if (imgMainAvatar != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    Glide.with(EditProfileActivity.this)
                        .load(user.getAvatarUrl())
                        .placeholder(R.drawable.img_discord)
                        .into(imgMainAvatar);
                }

                // Initial char count
                updateCharCount(user.getBio());
                
                // Reset checkChanges because we just loaded new data
                updateSaveButtonState(false);
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

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkChanges();
                if (etDisplayName.hasFocus() && txtPreviewDisplayName != null) {
                    String previewName = s.length() > 0 ? s.toString() : (currentUser != null ? currentUser.getUserName() : "");
                    txtPreviewDisplayName.setText(previewName);
                }
                if (etPronouns.hasFocus() && txtPreviewPronouns != null) {
                    if (s.length() > 0) {
                        txtPreviewPronouns.setVisibility(View.VISIBLE);
                        txtPreviewPronouns.setText(s.toString());
                    } else {
                        txtPreviewPronouns.setVisibility(View.GONE);
                    }
                }
                if (etAboutMe.hasFocus()) {
                    updateCharCount(s.toString());
                    if (txtPreviewBio != null) {
                        txtPreviewBio.setText(s.toString());
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etDisplayName.addTextChangedListener(watcher);
        etPronouns.addTextChangedListener(watcher);
        etAboutMe.addTextChangedListener(watcher);
    }

    private void checkChanges() {
        if (currentUser == null) return;

        boolean changed = false;

        String displayName = etDisplayName.getText().toString();
        String pronouns = etPronouns.getText().toString();
        String aboutMe = etAboutMe.getText().toString();

        if (!displayName.equals(currentUser.getDisplayName())) changed = true;
        
        String currentPronounsValue = currentUser.getPronouns() != null ? currentUser.getPronouns() : "";
        if (!pronouns.equals(currentPronounsValue)) changed = true;
        
        String currentBioValue = currentUser.getBio() != null ? currentUser.getBio() : "";
        if (!aboutMe.equals(currentBioValue)) changed = true;
        
        String userDecorationId = currentUser.getAvatarDecorationId() != null ? currentUser.getAvatarDecorationId() : "none";
        if (!currentDecorationId.equals(userDecorationId)) changed = true;
        
        String userEffectId = currentUser.getProfileEffectId() != null ? currentUser.getProfileEffectId() : "none";
        if (!currentProfileEffectId.equals(userEffectId)) changed = true;

        String userNamePlateId = currentUser.getNamePlateId() != null ? currentUser.getNamePlateId() : "none";
        if (!currentNamePlateId.equals(userNamePlateId)) changed = true;

        updateSaveButtonState(changed);
    }

    private void updateSaveButtonState(boolean enabled) {
        if (btnSave != null) {
            btnSave.setEnabled(enabled);
            btnSave.setAlpha(enabled ? 1.0f : 0.5f);
            btnSave.setTextColor(enabled ? Color.WHITE : Color.parseColor("#80FFFFFF"));
        }
    }

    private void updateCharCount(String bio) {
        if (txtAboutMeCharCount != null) {
            int length = bio != null ? bio.length() : 0;
            txtAboutMeCharCount.setText(String.valueOf(190 - length));
        }
    }

    private void saveUserInfo() {
        if (currentUser == null) return;

        currentUser.setDisplayName(etDisplayName.getText().toString());
        currentUser.setPronouns(etPronouns.getText().toString());
        currentUser.setBio(etAboutMe.getText().toString());
        currentUser.setAvatarDecorationId(currentDecorationId);
        currentUser.setProfileEffectId(currentProfileEffectId);
        currentUser.setNamePlateId(currentNamePlateId);

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

    private void playProfileEffectAnimation(ProfileEffect effect) {
        if (effect == null || effect.getType() == ProfileEffect.Type.NONE || effect.getType() == ProfileEffect.Type.SHOP) {
            if (imgFullEffectAnimation != null) {
                imgFullEffectAnimation.animate().cancel();
                imgFullEffectAnimation.setVisibility(View.GONE);
            }
            return;
        }

        if (imgFullEffectAnimation != null) {
            // Cancel any running animation and reset
            imgFullEffectAnimation.animate().cancel();
            imgFullEffectAnimation.setAlpha(0.0f);
            imgFullEffectAnimation.setVisibility(View.VISIBLE);

            Glide.with(this)
                    .load(effect.getEffectResId())
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            if (!isFinishing() && !isDestroyed()) imgFullEffectAnimation.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            // Resource is loaded, now start the perfect animation sequence
                            if (!isFinishing() && !isDestroyed()) {
                                imgFullEffectAnimation.animate()
                                        .alpha(1.0f)
                                        .setDuration(400) // Fade in
                                        .withEndAction(() -> {
                                            // Stay visible for 1.2 seconds while animation plays
                                            imgFullEffectAnimation.animate()
                                                    .alpha(0.0f)
                                                    .setStartDelay(1200)
                                                    .setDuration(800) // Fade out
                                                    .withEndAction(() -> {
                                                        imgFullEffectAnimation.setVisibility(View.GONE);
                                                        imgFullEffectAnimation.setImageDrawable(null);
                                                    })
                                                    .start();
                                        })
                                        .start();
                            }
                            return false;
                        }
                    })
                    .into(imgFullEffectAnimation);
        }
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
