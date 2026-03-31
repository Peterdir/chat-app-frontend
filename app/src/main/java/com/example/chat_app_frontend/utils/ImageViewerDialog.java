package com.example.chat_app_frontend.utils;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.R;

import java.io.OutputStream;

public class ImageViewerDialog {

    public static void show(Context context, String imageUrlOrBase64) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        
        RelativeLayout root = new RelativeLayout(context);
        root.setBackgroundColor(Color.BLACK);

        com.github.chrisbanes.photoview.PhotoView imageView = new com.github.chrisbanes.photoview.PhotoView(context);
        RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(ivParams);
        
        if (imageUrlOrBase64.startsWith("data:image/")) {
            Glide.with(context).load(imageUrlOrBase64).into(imageView);
        } else {
            Glide.with(context).asGif().load(imageUrlOrBase64).into(imageView);
        }
        
        root.addView(imageView);

        ImageView btnClose = new ImageView(context);
        btnClose.setImageResource(R.drawable.ic_close);
        btnClose.setPadding(48, 48, 48, 48);
        btnClose.setColorFilter(Color.WHITE);
        RelativeLayout.LayoutParams closeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        closeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        closeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        btnClose.setLayoutParams(closeParams);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        root.addView(btnClose);

        Button btnDownload = new Button(context);
        btnDownload.setText("TẢI ẢNH VỀ");
        btnDownload.setTextColor(Color.WHITE);
        btnDownload.setBackgroundColor(Color.parseColor("#5865F2"));
        RelativeLayout.LayoutParams downloadParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        downloadParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        downloadParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        downloadParams.bottomMargin = 96;
        btnDownload.setLayoutParams(downloadParams);
        
        btnDownload.setOnClickListener(v -> {
            downloadImage(context, imageUrlOrBase64);
        });
        root.addView(btnDownload);

        dialog.setContentView(root);
        dialog.show();
    }

    private static void downloadImage(Context context, String imageUrlOrBase64) {
        if (imageUrlOrBase64.startsWith("data:image/")) {
            try {
                String base64Data = imageUrlOrBase64.substring(imageUrlOrBase64.indexOf(",") + 1);
                byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "ChatApp_Image_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ChatApp");
                }
                
                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream out = context.getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();
                    Toast.makeText(context, "Đã tải ảnh về bộ sưu tập!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Lỗi không thể lưu ảnh", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Chỉ hỗ trợ tải ảnh tĩnh", Toast.LENGTH_SHORT).show();
        }
    }
}
