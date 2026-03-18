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

    private ImageView imgPreviewNamePlate;
    private TextView txtSelectedName, txtSelectedDesc;

    public static NamePlateSelectionBottomSheet newInstance(String currentPlateId) {
        NamePlateSelectionBottomSheet fragment = new NamePlateSelectionBottomSheet();
        Bundle args = new Bundle();
        args.putString("current_plate_id", currentPlateId);
        fragment.setArguments(args);
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
        txtSelectedName = view.findViewById(R.id.txt_selected_plate_name);
        txtSelectedDesc = view.findViewById(R.id.txt_selected_plate_desc);

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

    private void updatePreview(NamePlate plate) {
        if (plate.getType() == NamePlate.Type.NONE) {
            imgPreviewNamePlate.setImageResource(0);
            imgPreviewNamePlate.setBackgroundResource(R.drawable.bg_rounded_card);
            txtSelectedName.setText("Không");
            txtSelectedDesc.setText("Không sử dụng bảng tên");
        } else if (plate.getType() == NamePlate.Type.STORE) {
            // Keep current or show something
        } else {
            imgPreviewNamePlate.setImageResource(plate.getDrawableResId());
            txtSelectedName.setText(plate.getName());
            txtSelectedDesc.setText(plate.getDescription());
            if (!plate.getReceivedDate().isEmpty()) {
                txtSelectedDesc.setText("Đã nhận vào " + plate.getReceivedDate());
            }
        }
    }
}
