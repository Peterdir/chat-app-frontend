package com.example.chat_app_frontend.manager;

import android.content.Context;
import android.util.Log;

import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.model.UserStatus;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collections;

/**
 * Authentication Manager – quản lý đăng nhập, đăng ký, đăng xuất và session.
 *
 * Sử dụng Firebase Authentication để xác thực và lưu trữ session.
 * Profile mở rộng (userName, bio, avatarUrl, ...) được lưu trong Firebase RTDB
 * tại users/{firebaseUid}.
 */
public class AuthManager {

    private static final String TAG = "AuthManager";

    private static AuthManager instance;

    private final FirebaseAuth auth;
    private final UserRepository userRepository;
    private User currentUser;   // Cache profile của user hiện tại

    private AuthManager(Context context) {
        auth = FirebaseManager.getAuth();
        userRepository = UserRepository.getInstance();
    }

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
     * Đăng nhập bằng email và password.
     * Firebase Authentication xác thực credentials, sau đó profile được load từ RTDB.
     */
    public void login(String email, String password, OnAuthListener callback) {
        Log.d(TAG, "Đang đăng nhập: " + email);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = authResult.getUser();
                    if (fbUser == null) {
                        if (callback != null) callback.onFailure("Đăng nhập thất bại");
                        return;
                    }

                    // Chặn user chưa xác minh email
                    if (!fbUser.isEmailVerified()) {
                        auth.signOut(); // Xóa session, không cho vào app
                        // Gửi lại email xác minh để tiện cho user
                        fbUser.sendEmailVerification();
                        if (callback != null) callback.onFailure(
                                "Email chưa được xác minh.\n" +
                                "Vui lòng kiểm tra hộp thư và click vào link xác minh.\n" +
                                "(Đã gửi lại email xác minh)");
                        return;
                    }

                    Log.d(TAG, "Firebase Auth OK, UID: " + fbUser.getUid());
                    loadProfileAfterAuth(fbUser, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Đăng nhập thất bại: " + e.getMessage());
                    if (callback != null) callback.onFailure(mapAuthError(e.getMessage()));
                });
    }

    // =========================================================================
    // REGISTER
    // =========================================================================

    /**
     * Đăng ký tài khoản mới.
     * 1. Tạo tài khoản Firebase Auth (email + password)
     * 2. Gửi email xác minh
     * 3. Lưu profile (không có password) vào RTDB tại users/{uid}
     */
    public void register(String email, String password,
                         String username, String displayName,
                         OnAuthListener callback) {
        Log.d(TAG, "Đang đăng ký: " + email);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = authResult.getUser();
                    if (fbUser == null) {
                        if (callback != null) callback.onFailure("Đăng ký thất bại");
                        return;
                    }

                    // Gửi email xác minh (không bắt buộc phải verify để dùng app)
                    fbUser.sendEmailVerification()
                          .addOnCompleteListener(task ->
                              Log.d(TAG, "Gửi email xác minh: " + (task.isSuccessful() ? "OK" : task.getException())));

                    // Tạo profile trong RTDB
                    String uid = fbUser.getUid();
                    User user = new User(uid, username, email, displayName);
                    user.setAvatarUrl("https://ui-avatars.com/api/?name="
                            + displayName.replace(" ", "+")
                            + "&background=5865F2&color=fff&size=256");
                    user.setRoles(Collections.singletonList("USER"));
                    user.setCreatedAt(System.currentTimeMillis());
                    user.setLastActive(System.currentTimeMillis());
                    user.setEmailVerified(fbUser.isEmailVerified());

                    userRepository.saveUser(user, new UserRepository.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            currentUser = user;
                            Log.d(TAG, "✅ Đăng ký thành công: " + username);
                            if (callback != null) callback.onSuccess(user);
                        }

                        @Override
                        public void onFailure(String error) {
                            // Profile không lưu được, xóa tài khoản Auth để tránh orphan
                            fbUser.delete();
                            Log.e(TAG, "❌ Lưu profile thất bại: " + error);
                            if (callback != null) callback.onFailure("Lưu profile thất bại: " + error);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Đăng ký thất bại: " + e.getMessage());
                    if (callback != null) callback.onFailure(mapAuthError(e.getMessage()));
                });
    }

    // =========================================================================
    // LOGOUT
    // =========================================================================

    /**
     * Đăng xuất – cập nhật status OFFLINE rồi sign out khỏi Firebase Auth.
     */
    public void logout(OnLogoutListener callback) {
        if (currentUser != null && currentUser.getFirebaseUid() != null) {
            String uid = currentUser.getFirebaseUid();
            userRepository.updateUserStatus(
                    uid, UserStatus.OFFLINE,
                    new UserRepository.OnCompleteListener() {
                        @Override public void onSuccess() { clearTokenAndSignOut(uid, callback); }
                        @Override public void onFailure(String error) { clearTokenAndSignOut(uid, callback); }
                    });
        } else {
            doSignOut(callback);
        }
    }

    private void clearTokenAndSignOut(String uid, OnLogoutListener callback) {
        FirebaseMessaging.getInstance().getToken()
            .addOnSuccessListener(token -> {
                if (token != null && !token.trim().isEmpty()) {
                    FirebaseManager.getDatabaseReference("user_fcm_tokens")
                        .child(uid)
                        .child(token)
                        .removeValue();
                }
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
                    doSignOut(callback);
                });
            })
            .addOnFailureListener(e -> {
                doSignOut(callback);
            });
    }

    private void doSignOut(OnLogoutListener callback) {
        currentUser = null;
        auth.signOut();
        Log.d(TAG, "✅ Đã đăng xuất");
        if (callback != null) callback.onLogoutSuccess();
    }

    // =========================================================================
    // FORGOT PASSWORD
    // =========================================================================

    /**
     * Gửi email đặt lại mật khẩu.
     * Firebase sẽ gửi link trực tiếp đến email người dùng – không cần OTP thủ công.
     */
    public void sendPasswordResetEmail(String email, OnCompleteListener callback) {
        Log.d(TAG, "Gửi email reset password: " + email);
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Đã gửi email reset password");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Gửi email thất bại: " + e.getMessage());
                    if (callback != null) callback.onFailure(mapAuthError(e.getMessage()));
                });
    }

    // =========================================================================
    // SESSION
    // =========================================================================

    /**
     * Kiểm tra user đã đăng nhập chưa (dựa vào Firebase Auth session – tự persist).
     */
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    /**
     * Lấy user profile đang cache (có thể null nếu chưa load).
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Lấy Firebase UID hiện tại trực tiếp từ FirebaseAuth.
     */
    public String getUid() {
        FirebaseUser fbUser = auth.getCurrentUser();
        return (fbUser != null) ? fbUser.getUid() : null;
    }

    /**
     * Load profile user hiện tại từ RTDB khi app khởi động.
     * Dùng sau khi kiểm tra isLoggedIn() == true.
     */
    public void loadCurrentUser(OnAuthListener callback) {
        FirebaseUser fbUser = auth.getCurrentUser();
        if (fbUser == null) {
            if (callback != null) callback.onFailure("Chưa đăng nhập");
            return;
        }
        loadProfileAfterAuth(fbUser, callback);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    /**
     * Load profile từ RTDB sau khi Firebase Auth xác thực thành công.
     * Nếu profile chưa có (user mới) → tạo profile cơ bản.
     */
    private void loadProfileAfterAuth(FirebaseUser fbUser, OnAuthListener callback) {
        String uid = fbUser.getUid();

        userRepository.getUserByUid(uid, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user;
                // Cập nhật trạng thái online
                userRepository.updateUserStatus(uid, UserStatus.ONLINE, null);
                userRepository.updateLastActive(uid);
                if (callback != null) callback.onSuccess(user);
            }

            @Override
            public void onUserNotFound() {
                // Profile chưa có (sign-in lần đầu với tài khoản cũ)
                // Tạo profile tối thiểu từ thông tin Firebase Auth
                String email = fbUser.getEmail() != null ? fbUser.getEmail() : "";
                String name = fbUser.getDisplayName() != null ? fbUser.getDisplayName() : email;
                User fallback = new User(uid, email, email, name);
                fallback.setEmailVerified(fbUser.isEmailVerified());
                fallback.setCreatedAt(System.currentTimeMillis());
                fallback.setLastActive(System.currentTimeMillis());

                userRepository.saveUser(fallback, new UserRepository.OnCompleteListener() {
                    @Override public void onSuccess() {
                        currentUser = fallback;
                        if (callback != null) callback.onSuccess(fallback);
                    }
                    @Override public void onFailure(String error) {
                        if (callback != null) callback.onFailure(error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    /**
     * Chuyển lỗi Firebase Auth sang tiếng Việt thân thiện.
     */
    private String mapAuthError(String error) {
        if (error == null) return "Đã xảy ra lỗi";
        if (error.contains("EMAIL_NOT_FOUND") || error.contains("no user record") || error.contains("INVALID_LOGIN_CREDENTIALS"))
            return "Email không tồn tại hoặc mật khẩu sai";
        if (error.contains("INVALID_PASSWORD") || error.contains("WRONG_PASSWORD"))
            return "Mật khẩu không đúng";
        if (error.contains("EMAIL_EXISTS") || error.contains("email address is already in use"))
            return "Email này đã được sử dụng";
        if (error.contains("WEAK_PASSWORD") || error.contains("password should be at least"))
            return "Mật khẩu quá yếu (tối thiểu 6 ký tự)";
        if (error.contains("INVALID_EMAIL") || error.contains("badly formatted"))
            return "Email không hợp lệ";
        if (error.contains("TOO_MANY_REQUESTS") || error.contains("too many"))
            return "Quá nhiều lần thử, vui lòng thử lại sau";
        if (error.contains("network") || error.contains("NETWORK_ERROR"))
            return "Lỗi kết nối mạng";
        return "Đăng nhập / đăng ký thất bại. Vui lòng thử lại";
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

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}
