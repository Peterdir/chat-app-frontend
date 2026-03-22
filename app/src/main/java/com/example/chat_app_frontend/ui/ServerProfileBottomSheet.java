package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ServerProfileBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_SERVER_ID = "server_id";
    private static final String ARG_SERVER_NAME = "server_name";

    private String serverId;
    private String serverName;
    private ImageView ivServerAvatar;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    if (ivServerAvatar != null) {
                        ivServerAvatar.setImageURI(uri);
                    }
                    uploadImageUsingBase64(uri);
                } else {
                    Toast.makeText(getContext(), "Đã hủy chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    public static ServerProfileBottomSheet newInstance(String serverId, String serverName) {
        ServerProfileBottomSheet fragment = new ServerProfileBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_ID, serverId);
        args.putString(ARG_SERVER_NAME, serverName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverId = getArguments().getString(ARG_SERVER_ID);
            serverName = getArguments().getString(ARG_SERVER_NAME);
        }
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_server_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvServerName = view.findViewById(R.id.tv_profile_server_name);
        ivServerAvatar = view.findViewById(R.id.iv_server_avatar);

        // === ĐỔI TÊN SERVER: ánh xạ view ===
        TextInputLayout tilServerName = view.findViewById(R.id.til_server_name);
        EditText etServerName = view.findViewById(R.id.et_server_name);
        MaterialButton btnSaveServerName = view.findViewById(R.id.btn_save_server_name);

        if (tvServerName != null && serverName != null) {
            tvServerName.setText(serverName);
        }

        loadServerAvatar();

        // === ĐỔI TÊN SERVER: load tên hiện tại và setup logic ===
        final String[] originalName = {serverName != null ? serverName : ""};

        if (etServerName != null) {
            etServerName.setText(originalName[0]);
        }

        // Mặc định disable nút Save
        if (btnSaveServerName != null) {
            btnSaveServerName.setEnabled(false);
            btnSaveServerName.setAlpha(0.5f);
        }

        // TextWatcher: enable/disable nút khi user gõ
        if (etServerName != null && btnSaveServerName != null) {
            final MaterialButton finalBtn = btnSaveServerName;
            etServerName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String current = s.toString().trim();
                    boolean shouldEnable = current.length() > 0 && !current.equals(originalName[0]);
                    finalBtn.setEnabled(shouldEnable);
                    finalBtn.setAlpha(shouldEnable ? 1.0f : 0.5f);
                    if (tilServerName != null) {
                        tilServerName.setError(null);
                    }
                }
            });
        }

        // Click nút Lưu: cập nhật tên lên Firebase
        if (btnSaveServerName != null && etServerName != null) {
            final EditText finalEt = etServerName;
            final MaterialButton finalBtn2 = btnSaveServerName;
            btnSaveServerName.setOnClickListener(v -> {
                String newName = finalEt.getText().toString().trim();

                if (newName.isEmpty()) {
                    if (tilServerName != null) {
                        tilServerName.setError("Tên Server không được để trống");
                    }
                    return;
                }

                if (serverId == null || serverId.isEmpty()) {
                    Toast.makeText(getContext(), "Lỗi: Không có Server ID!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Disable nút khi đang lưu
                finalBtn2.setEnabled(false);
                finalBtn2.setAlpha(0.5f);

                DatabaseReference nameRef = FirebaseDatabase.getInstance()
                        .getReference("servers").child(serverId).child("name");

                nameRef.setValue(newName)
                        .addOnSuccessListener(aVoid -> {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Đổi tên thành công!", Toast.LENGTH_SHORT).show();
                            }
                            originalName[0] = newName;
                            // Cập nhật tên trên Toolbar
                            if (tvServerName != null) {
                                tvServerName.setText(newName);
                            }
                            serverName = newName;
                            finalBtn2.setEnabled(false);
                            finalBtn2.setAlpha(0.5f);
                        })
                        .addOnFailureListener(e -> {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Lỗi đổi tên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            // Cho phép bấm lại
                            finalBtn2.setEnabled(true);
                            finalBtn2.setAlpha(1.0f);
                        });
            });
        }

        LinearLayout btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);
        if (btnEditAvatar != null) {
            btnEditAvatar.setOnClickListener(v -> mGetContent.launch("image/*"));
        }

        FrameLayout flAvatarContainer = view.findViewById(R.id.fl_avatar_container);
        if (flAvatarContainer != null) {
            flAvatarContainer.setOnClickListener(v -> mGetContent.launch("image/*"));
        }

        View tvEditServerProfile = view.findViewById(R.id.tv_edit_server_profile);
        if (tvEditServerProfile != null) {
            tvEditServerProfile.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EditServerProfileActivity.class);
                intent.putExtra("SERVER_ID", serverId);
                startActivity(intent);
                dismiss();
            });
        }

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.post(() -> {
            View parent = (View) view.getParent();
            if (parent != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight(screenHeight / 2);
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    // Tải ảnh (hoặc chuỗi Base64) cũ từ Database
    private void loadServerAvatar() {
        if (serverId == null || serverId.isEmpty()) return;
        DatabaseReference serverRef = FirebaseDatabase.getInstance().getReference("servers").child(serverId);
        serverRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String iconUrl = snapshot.child("iconUrl").getValue(String.class);
                if (iconUrl != null && !iconUrl.isEmpty() && ivServerAvatar != null && isAdded()) {
                    // Glide tự động biết cách đọc chuỗi Base64
                    Glide.with(this).load(iconUrl).centerCrop().into(ivServerAvatar);
                }
            }
        });
    }

    private void uploadImageUsingBase64(Uri imageUri) {
        if (serverId == null || serverId.isEmpty() || getContext() == null) return;

        Toast.makeText(getContext(), "Đang xử lý ảnh...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                int maxSize = 256;
                float scale = Math.min(((float)maxSize / selectedImage.getWidth()), ((float)maxSize / selectedImage.getHeight()));
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                Bitmap scaledBitmap = Bitmap.createBitmap(selectedImage, 0, 0, selectedImage.getWidth(), selectedImage.getHeight(), matrix, true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Chất lượng 70%
                byte[] b = baos.toByteArray();

                String base64Image = Base64.encodeToString(b, Base64.NO_WRAP);

                String finalImageUrl = "data:image/jpeg;base64," + base64Image;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("servers").child(serverId).child("iconUrl");
                        dbRef.setValue(finalImageUrl)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đổi ảnh thành công!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }
}