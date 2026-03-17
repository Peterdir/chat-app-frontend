package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.repository.ServerRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CreateCategoryBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_SERVER_ID = "server_id";
    private String serverId;
    private EditText etCategoryName;
    private Button btnCreate, btnCancel;
    private Runnable onDismissListener;

    public static CreateCategoryBottomSheet newInstance(String serverId) {
        CreateCategoryBottomSheet sheet = new CreateCategoryBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_ID, serverId);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverId = getArguments().getString(ARG_SERVER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_create_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCategoryName = view.findViewById(R.id.et_category_name);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCreate = view.findViewById(R.id.btn_create);

        btnCancel.setOnClickListener(v -> dismiss());
        btnCreate.setOnClickListener(v -> createCategory());
    }

    private void createCategory() {
        String categoryName = etCategoryName.getText().toString().trim();
        if (TextUtils.isEmpty(categoryName)) {
            Toast.makeText(getContext(), "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        ServerRepository.getInstance().createCategory(serverId, categoryName,
                new ServerRepository.OnCategoryCallback() {
                    @Override
                    public void onSuccess(com.example.chat_app_frontend.model.Category category) {
                        Toast.makeText(getContext(), "Danh mục được tạo thành công", Toast.LENGTH_SHORT).show();
                        if (onDismissListener != null) {
                            onDismissListener.run();
                        }
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

