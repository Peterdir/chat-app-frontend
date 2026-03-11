package com.example.chat_app_frontend.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.model.UserStatus;
import com.example.chat_app_frontend.repository.UserRepository;

/**
 * Authentication Manager - Quản lý đăng nhập, đăng xuất và session
 * 
 * Sử dụng SharedPreferences để lưu thông tin user hiện tại
 * Không dùng Firebase Authentication, chỉ dùng Realtime Database
 */
public class AuthManager {

    private static final String TAG = "AuthManager";
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static AuthManager instance;
    private final SharedPreferences prefs;
    private final UserRepository userRepository;
    private User currentUser;

    private AuthManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        userRepository = UserRepository.getInstance();
    }

    /**
     * Lấy instance singleton
     * Gọi init() trước khi sử dụng
     */
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    /**
     * Đăng nhập bằng email và password
     * 
     * @param email Email hoặc username
     * @param password Mật khẩu (plain text)
     * @param callback Callback khi hoàn thành
     */
    public void login(String email, String password, OnAuthListener callback) {
        Log.d(TAG, "Đang đăng nhập với email: " + email);

        // Tìm user theo email
        userRepository.getUserByUsername(email, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                // Kiểm tra password (lưu ý: trong production nên hash password)
                if (user.getPassword() != null && user.getPassword().equals(password)) {
                    // Đăng nhập thành công
                    loginSuccess(user);
                    
                    // Cập nhật status lên Firebase
                    userRepository.updateUserStatus(user.getId(), UserStatus.ONLINE, null);
                    userRepository.updateLastActive(user.getId());
                    
                    Log.d(TAG, "✅ Đăng nhập thành công: " + user.getUserName());
                    if (callback != null) callback.onSuccess(user);
                } else {
                    Log.w(TAG, "❌ Mật khẩu không đúng");
                    if (callback != null) callback.onFailure("Mật khẩu không đúng");
                }
            }

            @Override
            public void onUserNotFound() {
                Log.w(TAG, "❌ Không tìm thấy tài khoản");
                if (callback != null) callback.onFailure("Tài khoản không tồn tại");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Lỗi khi đăng nhập: " + error);
                if (callback != null) callback.onFailure("Lỗi kết nối: " + error);
            }
        });
    }

    /**
     * Lưu thông tin đăng nhập vào SharedPreferences
     */
    private void loginSuccess(User user) {
        currentUser = user;
        prefs.edit()
                .putLong(KEY_USER_ID, user.getId())
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    // =========================================================================
    // LOGOUT
    // =========================================================================

    /**
     * Đăng xuất
     */
    public void logout(OnLogoutListener callback) {
        if (currentUser != null) {
            // Cập nhật status sang OFFLINE
            userRepository.updateUserStatus(currentUser.getId(), UserStatus.OFFLINE, 
                new UserRepository.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        clearSession();
                        Log.d(TAG, "✅ Đăng xuất thành công");
                        if (callback != null) callback.onLogoutSuccess();
                    }

                    @Override
                    public void onFailure(String error) {
                        // Vẫn đăng xuất local dù Firebase fail
                        clearSession();
                        if (callback != null) callback.onLogoutSuccess();
                    }
                });
        } else {
            clearSession();
            if (callback != null) callback.onLogoutSuccess();
        }
    }

    /**
     * Xóa session local
     */
    private void clearSession() {
        currentUser = null;
        prefs.edit()
                .remove(KEY_USER_ID)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }

    // =========================================================================
    // SESSION MANAGEMENT
    // =========================================================================

    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Lấy user hiện tại (từ cache)
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Load user hiện tại từ Firebase (dùng khi app khởi động)
     */
    public void loadCurrentUser(OnAuthListener callback) {
        if (!isLoggedIn()) {
            if (callback != null) callback.onFailure("Chưa đăng nhập");
            return;
        }

        long userId = prefs.getLong(KEY_USER_ID, -1);
        if (userId == -1) {
            if (callback != null) callback.onFailure("Session không hợp lệ");
            return;
        }

        userRepository.getUserById(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user;
                
                // Cập nhật status lên ONLINE
                userRepository.updateUserStatus(user.getId(), UserStatus.ONLINE, null);
                userRepository.updateLastActive(user.getId());
                
                if (callback != null) callback.onSuccess(user);
            }

            @Override
            public void onUserNotFound() {
                // User đã bị xóa, clear session
                clearSession();
                if (callback != null) callback.onFailure("Tài khoản không tồn tại");
            }

            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    // =========================================================================
    // CALLBACKS
    // =========================================================================

    public interface OnAuthListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnLogoutListener {
        void onLogoutSuccess();
    }
}
