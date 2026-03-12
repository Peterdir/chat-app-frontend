package com.example.chat_app_frontend.repository;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.model.UserStatus;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Repository quản lý User profile trong Firebase Realtime Database.
 *
 * Cấu trúc RTDB:
 *   users/
 *     {firebaseUid}/
 *       userName, email, displayName, bio, avatarUrl, status, ...
 *
 * Credentials (email/password) do Firebase Authentication quản lý — không lưu ở đây.
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private static final String USERS_PATH = "users";

    private final DatabaseReference usersRef;

    private static UserRepository instance;

    private UserRepository() {
        usersRef = FirebaseManager.getDatabaseReference(USERS_PATH);
    }

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
     * Lưu (tạo mới hoặc cập nhật) profile của user.
     * Key trong RTDB = user.getFirebaseUid()
     */
    public void saveUser(User user, OnCompleteListener callback) {
        if (user.getFirebaseUid() == null || user.getFirebaseUid().isEmpty()) {
            Log.e(TAG, "Firebase UID không được null/rỗng!");
            if (callback != null) callback.onFailure("Firebase UID không được null");
            return;
        }

        usersRef.child(user.getFirebaseUid())
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

    // =========================================================================
    // READ (Single)
    // =========================================================================

    /**
     * Lấy user profile theo Firebase UID (đọc 1 lần).
     */
    public void getUserByUid(String uid, OnUserLoadedListener callback) {
        usersRef.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            // Đảm bảo firebaseUid được gán (có thể bị null nếu field chưa lưu)
                            if (user.getFirebaseUid() == null) user.setFirebaseUid(uid);
                            Log.d(TAG, "Đọc user thành công: " + user.getUserName());
                            if (callback != null) callback.onUserLoaded(user);
                        } else {
                            Log.w(TAG, "Không tìm thấy user với UID: " + uid);
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

    // =========================================================================
    // READ (Realtime)
    // =========================================================================

    /**
     * Lắng nghe thay đổi realtime của 1 user.
     *
     * @return listener – dùng để hủy sau này bằng removeListener()
     */
    public ValueEventListener observeUser(String uid, OnUserLoadedListener callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (user.getFirebaseUid() == null) user.setFirebaseUid(uid);
                    if (callback != null) callback.onUserLoaded(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onFailure(error.getMessage());
            }
        };

        usersRef.child(uid).addValueEventListener(listener);
        return listener;
    }

    /**
     * Hủy listener realtime.
     */
    public void removeListener(String uid, ValueEventListener listener) {
        if (listener != null) {
            usersRef.child(uid).removeEventListener(listener);
        }
    }

    // =========================================================================
    // UPDATE (Specific fields)
    // =========================================================================

    public void updateUserStatus(String uid, UserStatus status, OnCompleteListener callback) {
        usersRef.child(uid).child("status")
                .setValue(status.name())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void updateLastActive(String uid) {
        usersRef.child(uid).child("lastActive").setValue(System.currentTimeMillis());
    }

    public void updateAvatar(String uid, String avatarUrl, OnCompleteListener callback) {
        usersRef.child(uid).child("avatarUrl")
                .setValue(avatarUrl)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void updateBio(String uid, String bio, OnCompleteListener callback) {
        usersRef.child(uid).child("bio")
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

    public void deleteUser(String uid, OnCompleteListener callback) {
        usersRef.child(uid)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Xóa user thành công: " + uid);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Xóa user thất bại: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // =========================================================================
    // SEARCH
    // =========================================================================

    /**
     * Tìm kiếm user theo displayName hoặc userName chứa query (case-insensitive).
     * Firebase RTDB không hỗ trợ full-text search nên filter client-side.
     *
     * @param query   Từ khoá tìm kiếm
     * @param myUid   UID của user hiện tại (để loại khỏi kết quả)
     */
    public void searchUsers(String query, String myUid, OnUserListListener callback) {
        String lowerQuery = query.toLowerCase().trim();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> results = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    if (user == null) continue;
                    if (user.getFirebaseUid() == null) user.setFirebaseUid(child.getKey());
                    // Bỏ qua chính mình
                    if (myUid != null && myUid.equals(user.getFirebaseUid())) continue;
                    // Lọc theo tên
                    String name     = user.getDisplayName()  != null ? user.getDisplayName().toLowerCase()  : "";
                    String userName = user.getUserName()      != null ? user.getUserName().toLowerCase()      : "";
                    if (name.contains(lowerQuery) || userName.contains(lowerQuery)) {
                        results.add(user);
                    }
                }
                if (callback != null) callback.onLoaded(results);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onFailure(error.getMessage());
            }
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

    public interface OnUserListListener {
        void onLoaded(List<User> users);
        void onFailure(String error);
    }
}
