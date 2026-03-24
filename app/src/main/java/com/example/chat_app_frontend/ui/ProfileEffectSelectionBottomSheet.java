package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.widget.Toast;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.ProfileEffect;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.ProfileEffectRepository;
import com.example.chat_app_frontend.utils.CosmeticsEntitlements;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileEffectSelectionBottomSheet extends BottomSheetDialogFragment {

    public interface OnEffectAppliedListener {
        void onEffectApplied(ProfileEffect effect);
    }

    private OnEffectAppliedListener onEffectAppliedListener;
    private String initialEffectId = "none";
    private User currentUser;
    private ProfileEffect selectedEffect;
    private ProfileEffectsAdapter adapter;

    private ImageView imgPreviewAvatar, imgPreviewDecoration, imgPreviewProfileEffect;
    private TextView txtPreviewDisplayName, txtPreviewUsername, txtPreviewJoinDate;
    private TextView txtSelectedEffectName, txtSelectedEffectDesc;
    private Button btnApply;
    private View btnCancel;

    public void setOnEffectAppliedListener(OnEffectAppliedListener listener) {
        this.onEffectAppliedListener = listener;
    }

    public void setInitialEffectId(String id) {
        this.initialEffectId = id;
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_profile_effect_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView(view);
        
        selectedEffect = ProfileEffectRepository.getInstance().findEffectById(initialEffectId);
        updatePreviewUI(selectedEffect);
        updateUserInfoUI();
    }

    private void initViews(View view) {
        imgPreviewAvatar = view.findViewById(R.id.img_preview_avatar);
        imgPreviewDecoration = view.findViewById(R.id.img_preview_decoration);
        imgPreviewProfileEffect = view.findViewById(R.id.img_preview_profile_effect);
        txtPreviewDisplayName = view.findViewById(R.id.txt_preview_display_name);
        txtPreviewUsername = view.findViewById(R.id.txt_preview_username);
        txtPreviewJoinDate = view.findViewById(R.id.txt_preview_join_date);
        txtSelectedEffectName = view.findViewById(R.id.txt_selected_effect_name);
        txtSelectedEffectDesc = view.findViewById(R.id.txt_selected_effect_desc);
        btnApply = view.findViewById(R.id.btn_apply);
        btnCancel = view.findViewById(R.id.btn_cancel);

        btnApply.setOnClickListener(v -> {
            if (selectedEffect != null && selectedEffect.getType() == ProfileEffect.Type.EFFECT
                    && !CosmeticsEntitlements.canEquipProfileEffect(currentUser, selectedEffect)) {
                Toast.makeText(requireContext(), "Cần Nitro Basic hoặc Nitro để dùng hiệu ứng này", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(requireContext(), NitroActivity.class));
                return;
            }
            if (onEffectAppliedListener != null) {
                onEffectAppliedListener.onEffectApplied(selectedEffect);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_profile_effects);
        List<ProfileEffect> effects = ProfileEffectRepository.getInstance().getAvailableEffects();

        adapter = new ProfileEffectsAdapter(effects, selectedEffect, currentUser, effect -> {
            if (effect.getType() == ProfileEffect.Type.SHOP) {
                return;
            }
            if (effect.getType() == ProfileEffect.Type.EFFECT
                    && !CosmeticsEntitlements.canEquipProfileEffect(currentUser, effect)) {
                Toast.makeText(requireContext(), "Mở khóa với Nitro Basic hoặc Nitro", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(requireContext(), NitroActivity.class));
                return;
            }
            selectedEffect = effect;
            updatePreviewUI(effect);
            adapter.setSelectedEffect(effect);
            adapter.notifyDataSetChanged();
        });
        
        rv.setAdapter(adapter);
    }

    private void updatePreviewUI(ProfileEffect effect) {
        if (effect == null || effect.getType() == ProfileEffect.Type.NONE || effect.getType() == ProfileEffect.Type.SHOP) {
            imgPreviewProfileEffect.setVisibility(View.GONE);
            txtSelectedEffectName.setText(effect != null && effect.getType() == ProfileEffect.Type.SHOP ? "Cửa hàng" : "Không");
            txtSelectedEffectDesc.setText("");
        } else {
            imgPreviewProfileEffect.setVisibility(View.VISIBLE);
            com.bumptech.glide.Glide.with(this)
                .load(effect.getEffectResId())
                .into(imgPreviewProfileEffect);
            txtSelectedEffectName.setText(effect.getName());
            txtSelectedEffectDesc.setText(effect.getDescription());
        }
    }

    private void updateUserInfoUI() {
        if (currentUser == null) return;

        if (txtPreviewDisplayName != null) txtPreviewDisplayName.setText(currentUser.getDisplayNameOrUserName());
        if (txtPreviewUsername != null) txtPreviewUsername.setText("@" + currentUser.getUserName());
        
        if (txtPreviewJoinDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("d 'thg' M, yyyy", new Locale("vi", "VN"));
            String dateStr = sdf.format(new Date(currentUser.getCreatedAt()));
            txtPreviewJoinDate.setText(dateStr);
        }

        // Load Avatar with Glide
        if (imgPreviewAvatar != null && currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(currentUser.getAvatarUrl())
                .placeholder(R.drawable.img_discord)
                .into(imgPreviewAvatar);
        }

        // Load decoration
        if (imgPreviewDecoration != null) {
            com.example.chat_app_frontend.model.Decoration decoration = com.example.chat_app_frontend.repository.DecorationRepository.getInstance().findDecorationById(currentUser.getAvatarDecorationId());
            if (decoration != null && decoration.getType() != com.example.chat_app_frontend.model.Decoration.Type.NONE) {
                imgPreviewDecoration.setVisibility(View.VISIBLE);
                com.bumptech.glide.Glide.with(this)
                    .load(decoration.getDrawableResId())
                    .into(imgPreviewDecoration);
            } else {
                imgPreviewDecoration.setVisibility(View.GONE);
            }
        }
    }

    public interface OnEffectClickListener {
        void onEffectClick(ProfileEffect effect);
    }

    private static class ProfileEffectsAdapter extends RecyclerView.Adapter<ProfileEffectsAdapter.ViewHolder> {
        private final List<ProfileEffect> effects;
        private ProfileEffect currentSelected;
        private final User user;
        private final OnEffectClickListener listener;

        public ProfileEffectsAdapter(
                List<ProfileEffect> effects,
                ProfileEffect currentSelected,
                User user,
                OnEffectClickListener listener
        ) {
            this.effects = effects;
            this.currentSelected = currentSelected;
            this.user = user;
            this.listener = listener;
        }

        public void setSelectedEffect(ProfileEffect effect) {
            this.currentSelected = effect;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_effect_thumbnail, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ProfileEffect effect = effects.get(position);
            holder.bind(effect, effect == currentSelected, user, listener);
        }

        @Override
        public int getItemCount() {
            return effects.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgThumbnail, imgNone, imgShop, imgLock;
            TextView txtEffectName;
            View selectionBorder, badge;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imgThumbnail = itemView.findViewById(R.id.img_effect_thumbnail);
                imgNone = itemView.findViewById(R.id.img_none_icon);
                imgShop = itemView.findViewById(R.id.img_shop_icon);
                imgLock = itemView.findViewById(R.id.img_lock_effect);
                txtEffectName = itemView.findViewById(R.id.txt_effect_name);
                selectionBorder = itemView.findViewById(R.id.view_selection_border);
                badge = itemView.findViewById(R.id.txt_new_badge);
            }

            public void bind(ProfileEffect effect, boolean isSelected, User user, OnEffectClickListener listener) {
                imgThumbnail.setVisibility(View.GONE);
                imgNone.setVisibility(View.GONE);
                imgShop.setVisibility(View.GONE);
                badge.setVisibility(View.GONE);
                imgLock.setVisibility(View.GONE);

                if (effect.getType() == ProfileEffect.Type.NONE) {
                    imgNone.setVisibility(View.VISIBLE);
                    txtEffectName.setVisibility(View.VISIBLE);
                    txtEffectName.setText("Không");
                } else if (effect.getType() == ProfileEffect.Type.SHOP) {
                    imgShop.setVisibility(View.VISIBLE);
                    badge.setVisibility(View.VISIBLE);
                    txtEffectName.setVisibility(View.VISIBLE);
                    txtEffectName.setText("Cửa hàng");
                } else {
                    imgThumbnail.setVisibility(View.VISIBLE);
                    com.bumptech.glide.Glide.with(itemView.getContext())
                        .load(effect.getThumbnailResId())
                        .into(imgThumbnail);
                    txtEffectName.setVisibility(View.GONE);
                    boolean unlocked = CosmeticsEntitlements.canEquipProfileEffect(user, effect);
                    imgLock.setVisibility(unlocked ? View.GONE : View.VISIBLE);
                }

                selectionBorder.setVisibility(isSelected ? View.VISIBLE : View.GONE);
                itemView.setOnClickListener(v -> listener.onEffectClick(effect));
            }
        }
    }
}
