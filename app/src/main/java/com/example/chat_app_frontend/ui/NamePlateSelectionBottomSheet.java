package com.example.chat_app_frontend.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.NamePlateAdapter;
import com.example.chat_app_frontend.model.NamePlate;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.NamePlateRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class NamePlateSelectionBottomSheet extends BottomSheetDialogFragment {

    public interface OnNamePlateSelectedListener {
        void onNamePlateSelected(NamePlate plate);
    }

    private OnNamePlateSelectedListener listener;
    private NamePlate selectedPlate;
    private String initialPlateId;

    private ImageView imgPreviewNamePlate, imgPreviewAvatar, imgPreviewDecoration;
    private TextView txtSelectedName, txtSelectedDesc, txtPreviewDisplayName;
    private User user;

    public static NamePlateSelectionBottomSheet newInstance(String currentPlateId, User user) {
        NamePlateSelectionBottomSheet fragment = new NamePlateSelectionBottomSheet();
        Bundle args = new Bundle();
        args.putString("current_plate_id", currentPlateId);
        fragment.setArguments(args);
        fragment.user = user;
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNamePlateSelectedListener) {
            listener = (OnNamePlateSelectedListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_name_plate_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            initialPlateId = getArguments().getString("current_plate_id");
        }

        selectedPlate = NamePlateRepository.getInstance().findNamePlateById(initialPlateId);

        imgPreviewNamePlate = view.findViewById(R.id.img_preview_name_plate);
        imgPreviewAvatar = view.findViewById(R.id.img_preview_avatar);
        imgPreviewDecoration = view.findViewById(R.id.img_preview_decoration);
        txtSelectedName = view.findViewById(R.id.txt_selected_plate_name);
        txtSelectedDesc = view.findViewById(R.id.txt_selected_plate_desc);
        txtPreviewDisplayName = view.findViewById(R.id.txt_preview_display_name);

        loadUserDataIntoPreview();

        updatePreview(selectedPlate);

        setupRecyclerViews(view);

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_apply).setOnClickListener(v -> {
            if (listener != null) {
                listener.onNamePlateSelected(selectedPlate);
            }
            dismiss();
        });
    }

    private void setupRecyclerViews(View view) {
        RecyclerView rvYourPlates = view.findViewById(R.id.rv_your_plates);
        RecyclerView rvShopPlates = view.findViewById(R.id.rv_shop_plates);

        List<NamePlate> yourPlates = NamePlateRepository.getInstance().getYourNamePlates();
        List<NamePlate> shopPlates = NamePlateRepository.getInstance().getShopNamePlates();

        NamePlateAdapter yourAdapter = new NamePlateAdapter(yourPlates, selectedPlate.getId(), plate -> {
            selectedPlate = plate;
            updatePreview(plate);
        });

        NamePlateAdapter shopAdapter = new NamePlateAdapter(shopPlates, selectedPlate.getId(), plate -> {
            // Even if locked, we show preview but maybe with different state
            selectedPlate = plate;
            updatePreview(plate);
        });

        rvYourPlates.setAdapter(yourAdapter);
        rvShopPlates.setAdapter(shopAdapter);
        
        // Ensure only one item selected across both adapters
        yourAdapter.setSelectedId(selectedPlate.getId());
        shopAdapter.setSelectedId(selectedPlate.getId());
    }

    private void loadUserDataIntoPreview() {
        if (user != null) {
            if (txtPreviewDisplayName != null) txtPreviewDisplayName.setText(user.getDisplayNameOrUserName());
            
            if (imgPreviewAvatar != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(this).load(user.getAvatarUrl()).placeholder(R.drawable.img_discord).into(imgPreviewAvatar);
            }

            String decorId = user.getAvatarDecorationId();
            if (decorId != null && !decorId.equals("none") && imgPreviewDecoration != null) {
                com.example.chat_app_frontend.model.Decoration decor = com.example.chat_app_frontend.repository.DecorationRepository.getInstance().findDecorationById(decorId);
                if (decor != null) {
                    imgPreviewDecoration.setVisibility(View.VISIBLE);
                    com.bumptech.glide.Glide.with(this).load(decor.getDrawableResId()).into(imgPreviewDecoration);
                }
            } else if (imgPreviewDecoration != null) {
                imgPreviewDecoration.setVisibility(View.GONE);
            }
        }
    }

    private void updatePreview(NamePlate plate) {
        if (plate == null) return;
        
        if (plate.getType() == NamePlate.Type.NONE) {
            imgPreviewNamePlate.setImageResource(0);
            imgPreviewNamePlate.setBackgroundResource(R.drawable.bg_rounded_card);
            txtSelectedName.setText("Không");
            txtSelectedDesc.setText("Không sử dụng bảng tên");
        } else if (plate.getType() == NamePlate.Type.STORE) {
            // Usually handle shop plates separately
        } else {
            imgPreviewNamePlate.setVisibility(View.VISIBLE);
            com.bumptech.glide.Glide.with(this)
                .load(plate.getDrawableResId())
                .into(imgPreviewNamePlate);
            txtSelectedName.setText(plate.getName());
            
            if (plate.isNitro()) {
                txtSelectedName.setText(plate.getName() + " 💎"); // Nitro indicator
            }

            if (plate.getReceivedDate() != null && !plate.getReceivedDate().isEmpty()) {
                txtSelectedDesc.setText("Đã nhận vào " + plate.getReceivedDate());
            } else {
                txtSelectedDesc.setText(plate.getDescription());
            }
        }
    }
}
