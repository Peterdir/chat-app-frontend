package com.example.chat_app_frontend.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.model.UserStatus;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Repository để quản lý User với Firebase Realtime Database
 * 
 * Cung cấp các phương thức CRUD:
 * - Create/Update user
 * - Read user (single, list, realtime)
 * - Delete user
 * - Update status, profile
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private static final String USERS_PATH = "users";

    private final DatabaseReference usersRef;

    // Singleton instance
    private static UserRepository instance;

    private UserRepository() {
        usersRef = FirebaseManager.getDatabaseReference(USERS_PATH);
    }

    /**
     * Lấy instance singleton
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    // =========================================================================
    // CREATE / UPDATE
    // =========================================================================

    /**
     * Tạo hoặc cập nhật user
     * 
     * @param user User object cần lưu
     * @param callback Callback khi hoàn thành
     */
    public void saveUser(User user, OnCompleteListener callback) {
        if (user.getId() == null) {
            Log.e(TAG, "User ID không được null!");
            if (callback != null) callback.onFailure("User ID không được null");
            return;
        }

        usersRef.child(user.getId().toString())
                .setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Lưu user thành công: " + user.getUserName());
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lưu user thất bại: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * Tạo user mới với ID tự động
     */
    public void createUserWithAutoId(User user, OnUserCreatedListener callback) {
        String newId = usersRef.push().getKey();
        if (newId == null) {
            if (callback != null) callback.onFailure("Không thể tạo ID");
            return;
        }

        // Convert String ID to Long
        user.setId(Long.parseLong(newId.replaceAll("[^0-9]", "").substring(0, Math.min(10, newId.length()))));
        user.setCreatedAt(System.currentTimeMillis());
        user.setLastActive(System.currentTimeMillis());

        saveUser(user, new OnCompleteListener() {
            @Override
            public void onSuccess() {
                if (callback != null) callback.onUserCreated(user);
            }

            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    // =========================================================================
    // READ (Single value)
    // =========================================================================

    /**
     * Lấy user theo ID (đọc 1 lần)
     */
    public void getUserById(Long userId, OnUserLoadedListener callback) {
        usersRef.child(userId.toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            Log.d(TAG, "Đọc user thành công: " + user.getUserName());
                            if (callback != null) callback.onUserLoaded(user);
                        } else {
                            Log.w(TAG, "Không tìm thấy user với ID: " + userId);
                            if (callback != null) callback.onUserNotFound();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Lỗi khi đọc user: " + error.getMessage());
                        if (callback != null) callback.onFailure(error.getMessage());
                    }
                });
    }

    /**
     * Lấy user theo username (đọc 1 lần)
     */
    public void getUserByUsername(String username, OnUserLoadedListener callback) {
        usersRef.orderByChild("userName")
                .equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                User user = child.getValue(User.class);
                                if (callback != null) callback.onUserLoaded(user);
                                return;
                            }
                        } else {
                            if (callback != null) callback.onUserNotFound();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onFailure(error.getMessage());
                    }
                });
    }

    // =========================================================================
    // READ (Realtime)
    // =========================================================================

    /**
     * Lắng nghe thay đổi realtime của 1 user
     */
    public ValueEventListener observeUser(Long userId, OnUserLoadedListener callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && callback != null) {
                    callback.onUserLoaded(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onFailure(error.getMessage());
            }
        };

        usersRef.child(userId.toString()).addValueEventListener(listener);
        return listener;
    }

    /**
     * Hủy listener
     */
    public void removeListener(Long userId, ValueEventListener listener) {
        if (listener != null) {
            usersRef.child(userId.toString()).removeEventListener(listener);
        }
    }

    // =========================================================================
    // UPDATE (Specific fields)
    // =========================================================================

    /**
     * Cập nhật trạng thái online/offline
     */
    public void updateUserStatus(Long userId, UserStatus status, OnCompleteListener callback) {
        usersRef.child(userId.toString())
                .child("status")
                .setValue(status.name())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật status thành công: " + status);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * Cập nhật lastActive timestamp
     */
    public void updateLastActive(Long userId) {
        usersRef.child(userId.toString())
                .child("lastActive")
                .setValue(System.currentTimeMillis());
    }

    /**
     * Cập nhật avatar URL
     */
    public void updateAvatar(Long userId, String avatarUrl, OnCompleteListener callback) {
        usersRef.child(userId.toString())
                .child("avatarUrl")
                .setValue(avatarUrl)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * Cập nhật bio
     */
    public void updateBio(Long userId, String bio, OnCompleteListener callback) {
        usersRef.child(userId.toString())
                .child("bio")
                .setValue(bio)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    /**
     * Xóa user
     */
    public void deleteUser(Long userId, OnCompleteListener callback) {
        usersRef.child(userId.toString())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Xóa user thành công: " + userId);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Xóa user thất bại: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // =========================================================================
    // CALLBACKS
    // =========================================================================

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onUserNotFound();
        void onFailure(String error);
    }

    public interface OnUserCreatedListener {
        void onUserCreated(User user);
        void onFailure(String error);
    }
}
