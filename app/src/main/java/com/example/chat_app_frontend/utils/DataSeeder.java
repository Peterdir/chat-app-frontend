package com.example.chat_app_frontend.utils;

import android.util.Log;

import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.model.UserStatus;
import com.example.chat_app_frontend.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Data Seeder - Tạo dữ liệu test cho Firebase
 * Chỉ dùng trong development
 */
public class DataSeeder {

    private static final String TAG = "DataSeeder";

    /**
     * Tạo tài khoản admin test
     * 
     * Thông tin đăng nhập:
     * - Email/Username: admin
     * - Password: admin123
     */
    public static void seedAdminUser(OnSeedCompleteListener callback) {
        Log.d(TAG, "Bắt đầu seed admin user...");

        UserRepository userRepo = UserRepository.getInstance();

        // Kiểm tra xem admin đã tồn tại chưa
        userRepo.getUserById(1L, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                Log.d(TAG, "Admin user đã tồn tại, bỏ qua seed");
                if (callback != null) callback.onSeedComplete(false, "Admin đã tồn tại");
            }

            @Override
            public void onUserNotFound() {
                // Tạo admin user
                createAdminUser(callback);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Lỗi khi kiểm tra admin: " + error);
                if (callback != null) callback.onSeedFailed(error);
            }
        });
    }

    /**
     * Tạo admin user mới
     */
    private static void createAdminUser(OnSeedCompleteListener callback) {
        User admin = new User();
        admin.setId(1L);
        admin.setUserName("admin");
        admin.setEmail("admin@chatapp.com");
        admin.setPassword("admin123");  // Plain text (trong production nên hash)
        admin.setDisplayName("Administrator");
        admin.setBio("System Administrator");
        admin.setAvatarUrl("https://ui-avatars.com/api/?name=Admin&background=5865F2&color=fff&size=256");
        admin.setStatus(UserStatus.OFFLINE);
        admin.setActive(true);
        admin.setEmailVerified(true);
        admin.setCreatedAt(System.currentTimeMillis());
        admin.setLastActive(System.currentTimeMillis());
        
        // Set roles
        admin.setRoles(new ArrayList<>(Arrays.asList("USER", "ADMIN")));

        // Lưu vào Firebase
        UserRepository.getInstance().saveUser(admin, new UserRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Tạo admin user thành công!");
                Log.d(TAG, "   Username: admin");
                Log.d(TAG, "   Password: admin123");
                if (callback != null) callback.onSeedComplete(true, "Admin user đã được tạo");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Lỗi khi tạo admin: " + error);
                if (callback != null) callback.onSeedFailed(error);
            }
        });
    }

    /**
     * Seed nhiều user test (tùy chọn)
     */
    public static void seedTestUsers(OnSeedCompleteListener callback) {
        Log.d(TAG, "Bắt đầu seed test users...");
        
        User[] testUsers = {
            createTestUser(2L, "nguyenvana", "nguyenvana@test.com", "Nguyễn Văn A", "user123"),
            createTestUser(3L, "tranthib", "tranthib@test.com", "Trần Thị B", "user123"),
            createTestUser(4L, "levanc", "levanc@test.com", "Lê Văn C", "user123"),
        };

        UserRepository userRepo = UserRepository.getInstance();
        int[] completedCount = {0};
        int totalUsers = testUsers.length;

        for (User user : testUsers) {
            userRepo.saveUser(user, new UserRepository.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    completedCount[0]++;
                    Log.d(TAG, "✅ Tạo user: " + user.getUserName());
                    
                    if (completedCount[0] == totalUsers) {
                        if (callback != null) {
                            callback.onSeedComplete(true, "Đã tạo " + totalUsers + " test users");
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "❌ Lỗi tạo user " + user.getUserName() + ": " + error);
                    completedCount[0]++;
                    
                    if (completedCount[0] == totalUsers) {
                        if (callback != null) {
                            callback.onSeedFailed("Một số user không tạo được");
                        }
                    }
                }
            });
        }
    }

    /**
     * Helper method tạo test user
     */
    private static User createTestUser(Long id, String username, String email, 
                                       String displayName, String password) {
        User user = new User();
        user.setId(id);
        user.setUserName(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setDisplayName(displayName);
        user.setBio("Test user - " + displayName);
        user.setAvatarUrl("https://ui-avatars.com/api/?name=" + displayName.replace(" ", "+") + 
                         "&background=random&size=256");
        user.setStatus(UserStatus.OFFLINE);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setCreatedAt(System.currentTimeMillis());
        user.setLastActive(System.currentTimeMillis());
        user.setRoles(new ArrayList<>(Arrays.asList("USER")));
        
        return user;
    }

    // =========================================================================
    // CALLBACK
    // =========================================================================

    public interface OnSeedCompleteListener {
        void onSeedComplete(boolean success, String message);
        void onSeedFailed(String error);
    }
}
