package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.CategorySpinnerAdapter;
import com.example.chat_app_frontend.model.Category;
import com.example.chat_app_frontend.repository.ServerRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CreateChannelBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_SERVER_ID = "server_id";
    private static final String ARG_PRESELECTED_CATEGORY_ID = "preselected_category_id";
    private String serverId;
    private String preselectedCategoryId;
    private EditText etChannelName;
    private EditText etChannelDescription;
    private RadioGroup rgChannelType;
    private RadioButton rbText;
    private RadioButton rbVoice;
    private Spinner spinnerCategory;
    private Button btnCancel, btnCreate;
    private LinearLayout layoutTextOption;
    private LinearLayout layoutVoiceOption;
    private CategorySpinnerAdapter categoryAdapter;
    private Runnable onDismissListener;

    public static CreateChannelBottomSheet newInstance(String serverId) {
        CreateChannelBottomSheet sheet = new CreateChannelBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_ID, serverId);
        sheet.setArguments(args);
        return sheet;
    }

    public static CreateChannelBottomSheet newInstance(String serverId, String preselectedCategoryId) {
        CreateChannelBottomSheet sheet = new CreateChannelBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_ID, serverId);
        args.putString(ARG_PRESELECTED_CATEGORY_ID, preselectedCategoryId);
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
            preselectedCategoryId = getArguments().getString(ARG_PRESELECTED_CATEGORY_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_create_channel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etChannelName = view.findViewById(R.id.et_channel_name);
        etChannelDescription = view.findViewById(R.id.et_channel_description);
        rgChannelType = view.findViewById(R.id.rg_channel_type);
        rbText = view.findViewById(R.id.rb_text);
        rbVoice = view.findViewById(R.id.rb_voice);
        layoutTextOption = view.findViewById(R.id.layout_text_option);
        layoutVoiceOption = view.findViewById(R.id.layout_voice_option);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCreate = view.findViewById(R.id.btn_create);

        // Set default channel type to text
        rbText.setChecked(true);
        rbVoice.setChecked(false);

        // RadioGroup does not enforce single selection when RadioButtons are nested.
        // Manually enforce mutual exclusivity and allow tapping whole option rows.
        rbText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) rbVoice.setChecked(false);
        });
        rbVoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) rbText.setChecked(false);
        });
        layoutTextOption.setOnClickListener(v -> {
            rbText.setChecked(true);
            rbVoice.setChecked(false);
        });
        layoutVoiceOption.setOnClickListener(v -> {
            rbVoice.setChecked(true);
            rbText.setChecked(false);
        });

        // Load categories into spinner
        loadCategories();

        btnCancel.setOnClickListener(v -> dismiss());
        btnCreate.setOnClickListener(v -> createChannel());
    }

    private void loadCategories() {
        ServerRepository.getInstance().getServerCategories(serverId,
                new ServerRepository.OnCategoryListCallback() {
                    @Override
                    public void onSuccess(List<Category> categories) {
                        // Add "Không có danh mục" option
                        List<Category> allCategories = new ArrayList<>();
                        Category noneCategory = new Category("none", "Không có danh mục");
                        allCategories.add(noneCategory);
                        allCategories.addAll(categories);

                        categoryAdapter = new CategorySpinnerAdapter(getContext(), allCategories);
                        spinnerCategory.setAdapter(categoryAdapter);

                        if (!TextUtils.isEmpty(preselectedCategoryId)) {
                            for (int i = 0; i < allCategories.size(); i++) {
                                Category item = allCategories.get(i);
                                if (preselectedCategoryId.equals(item.getId())) {
                                    spinnerCategory.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Lỗi tải danh mục: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createChannel() {
        if (TextUtils.isEmpty(serverId)) {
            Toast.makeText(getContext(), "Không tìm thấy server để tạo kênh", Toast.LENGTH_SHORT).show();
            return;
        }

        String channelName = etChannelName.getText().toString().trim();
        if (TextUtils.isEmpty(channelName)) {
            Toast.makeText(getContext(), "Vui lòng nhập tên kênh", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!rbText.isChecked() && !rbVoice.isChecked()) {
            Toast.makeText(getContext(), "Vui lòng chọn loại kênh", Toast.LENGTH_SHORT).show();
            return;
        }

        String channelType = rbVoice.isChecked() ? "voice" : "text";

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        String categoryId = selectedCategory != null && !selectedCategory.getId().equals("none") 
                ? selectedCategory.getId() 
                : null;

        ServerRepository.getInstance().createChannel(serverId, channelName, channelType, categoryId,
                new ServerRepository.OnChannelListCallback() {
                    @Override
                    public void onSuccess(java.util.List<com.example.chat_app_frontend.model.Channel> channels) {
                        Toast.makeText(getContext(), "Kênh được tạo thành công", Toast.LENGTH_SHORT).show();
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

