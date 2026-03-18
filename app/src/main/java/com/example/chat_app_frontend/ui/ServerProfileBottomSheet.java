package com.example.chat_app_frontend.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        if (tvServerName != null && serverName != null) {
            tvServerName.setText(serverName);
        }

        loadServerAvatar();

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