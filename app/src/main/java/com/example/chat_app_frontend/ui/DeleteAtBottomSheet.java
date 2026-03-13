package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DeleteAtBottomSheet extends BottomSheetDialogFragment {

    public interface OnTimeSelectedListener {
        void onTimeSelected(String time);
    }
    private OnTimeSelectedListener listener;

    public void setOnTimeSelectedListener(OnTimeSelectedListener listener) {
        this.listener = listener;
    }

    private RadioButton radio24h, radio4h, radio1h, radio30m, radioNever;
    private TextView tv24h, tv4h, tv1h, tv30m, tvNever;

    @Override
    public int getTheme() {
        return com.google.android.material.R.style.Theme_Design_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_delete_at_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ RadioButtons
        radio24h = view.findViewById(R.id.radio_24h);
        radio4h = view.findViewById(R.id.radio_4h);
        radio1h = view.findViewById(R.id.radio_1h);
        radio30m = view.findViewById(R.id.radio_30m);
        radioNever = view.findViewById(R.id.radio_never);

        tv24h = view.findViewById(R.id.tv_text_24h);
        tv4h = view.findViewById(R.id.tv_text_4h);
        tv1h = view.findViewById(R.id.tv_text_1h);
        tv30m = view.findViewById(R.id.tv_text_30m);
        tvNever = view.findViewById(R.id.tv_text_never);

        view.findViewById(R.id.row_24h).setOnClickListener(v -> selectTime(radio24h, tv24h.getText().toString()));
        view.findViewById(R.id.row_4h).setOnClickListener(v -> selectTime(radio4h, tv4h.getText().toString()));
        view.findViewById(R.id.row_1h).setOnClickListener(v -> selectTime(radio1h, tv1h.getText().toString()));
        view.findViewById(R.id.row_30m).setOnClickListener(v -> selectTime(radio30m, tv30m.getText().toString()));
        view.findViewById(R.id.row_never).setOnClickListener(v -> selectTime(radioNever, tvNever.getText().toString()));
    }

    private void selectTime(RadioButton selectedRadio, String timeText) {
        // Tắt hết các nút khác
        radio24h.setChecked(false);
        radio4h.setChecked(false);
        radio1h.setChecked(false);
        radio30m.setChecked(false);
        radioNever.setChecked(false);

        selectedRadio.setChecked(true);

        if (listener != null) {
            listener.onTimeSelected(timeText);
        }
        dismiss();
    }
}