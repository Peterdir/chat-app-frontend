package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Decoration;
import com.example.chat_app_frontend.adapter.DecorationAdapter;
import com.example.chat_app_frontend.repository.DecorationRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class DecorationSelectionBottomSheet extends BottomSheetDialogFragment {

    private ImageView previewDecoration, previewS1, previewS2, previewS3, previewS4;
    private TextView decorationName;
    private RecyclerView rvYourDecorations, rvNitroDecorations;
    private DecorationAdapter yourAdapter, nitroAdapter;
    private View btnSubscribe;
    private Decoration selectedDecoration;
    private String initialDecorationId = "none";

    public interface OnDecorationAppliedListener {
        void onDecorationApplied(Decoration decoration);
    }

    private OnDecorationAppliedListener onDecorationAppliedListener;

    public void setOnDecorationAppliedListener(OnDecorationAppliedListener listener) {
        this.onDecorationAppliedListener = listener;
    }

    public void setInitialDecorationId(String id) {
        this.initialDecorationId = id;
    }

    @Override
    public int getTheme() {
        return com.google.android.material.R.style.Theme_Design_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_change_decoration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewDecoration = view.findViewById(R.id.preview_decoration);
        previewS1 = view.findViewById(R.id.preview_decoration_s1);
        previewS2 = view.findViewById(R.id.preview_decoration_s2);
        previewS3 = view.findViewById(R.id.preview_decoration_s3);
        previewS4 = view.findViewById(R.id.preview_decoration_s4);
        
        decorationName = view.findViewById(R.id.decoration_name);
        rvYourDecorations = view.findViewById(R.id.rv_your_decorations);
        rvNitroDecorations = view.findViewById(R.id.rv_nitro_decorations);
        btnSubscribe = view.findViewById(R.id.btn_subscribe);

        btnSubscribe.setOnClickListener(v -> {
            if (onDecorationAppliedListener != null && selectedDecoration != null) {
                onDecorationAppliedListener.onDecorationApplied(selectedDecoration);
                dismiss();
            }
        });

        setupRecyclerViews();
        initializeInitialPreview();
    }

    private void initializeInitialPreview() {
        Decoration initialDecor = DecorationRepository.getInstance().findDecorationById(initialDecorationId);
        if (initialDecor != null && initialDecor.getType() != Decoration.Type.NONE && initialDecor.getType() != Decoration.Type.STORE) {
            updatePreviewVisibility(View.VISIBLE);
            int resId = initialDecor.getDrawableResId();
            previewDecoration.setImageResource(resId);
            previewS1.setImageResource(resId);
            previewS2.setImageResource(resId);
            previewS3.setImageResource(resId);
            previewS4.setImageResource(resId);
            decorationName.setText(initialDecor.getName());
        } else {
            updatePreviewVisibility(View.GONE);
            decorationName.setText("Chưa chọn trang trí");
        }
    }

    private void setupRecyclerViews() {
        List<Decoration> yourDecorations = DecorationRepository.getInstance().getYourDecorations();
        List<Decoration> nitroDecorations = DecorationRepository.getInstance().getNitroDecorations();

        yourAdapter = new DecorationAdapter(yourDecorations, this::onDecorationSelected);
        nitroAdapter = new DecorationAdapter(nitroDecorations, this::onDecorationSelected);

        yourAdapter = new DecorationAdapter(yourDecorations, this::onDecorationSelected);
        nitroAdapter = new DecorationAdapter(nitroDecorations, this::onDecorationSelected);

        rvYourDecorations.setAdapter(yourAdapter);
        rvNitroDecorations.setAdapter(nitroAdapter);
    }

    private void onDecorationSelected(Decoration decoration) {
        this.selectedDecoration = decoration;
        if (decoration.getType() == Decoration.Type.NONE || decoration.getType() == Decoration.Type.STORE) {
            updatePreviewVisibility(View.GONE);
        } else {
            updatePreviewVisibility(View.VISIBLE);
            int resId = decoration.getDrawableResId();
            previewDecoration.setImageResource(resId);
            previewS1.setImageResource(resId);
            previewS2.setImageResource(resId);
            previewS3.setImageResource(resId);
            previewS4.setImageResource(resId);
        }
        decorationName.setText(decoration.getName());

        handleSubscribeButtonVisibility(decoration);
    }

    private void handleSubscribeButtonVisibility(Decoration decoration) {
        boolean isChanged = !decoration.getId().equals(initialDecorationId);
        if (isChanged && btnSubscribe.getVisibility() != View.VISIBLE) {
            btnSubscribe.setVisibility(View.VISIBLE);
            btnSubscribe.setAlpha(0f);
            btnSubscribe.setTranslationY(50f);
            btnSubscribe.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();
        } else if (!isChanged && btnSubscribe.getVisibility() == View.VISIBLE) {
            btnSubscribe.animate()
                    .alpha(0f)
                    .translationY(50f)
                    .setDuration(200)
                    .withEndAction(() -> btnSubscribe.setVisibility(View.GONE))
                    .start();
        }
    }

    private void updatePreviewVisibility(int visibility) {
        previewDecoration.setVisibility(visibility);
        previewS1.setVisibility(visibility);
        previewS2.setVisibility(visibility);
        previewS3.setVisibility(visibility);
        previewS4.setVisibility(visibility);
    }
}
