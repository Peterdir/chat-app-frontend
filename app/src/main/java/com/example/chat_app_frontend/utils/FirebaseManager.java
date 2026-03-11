package com.example.chat_app_frontend.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Firebase Manager - Quản lý kết nối Firebase Database tập trung
 * 
 * Sử dụng Singleton pattern để đảm bảo chỉ có 1 instance duy nhất
 * 
 * Cách sử dụng:
 * - Lấy database instance: FirebaseManager.getDatabase()
 * - Lấy reference: FirebaseManager.getDatabaseReference("path/to/data")
 */
public class FirebaseManager {

    private static FirebaseDatabase database;

    /**
     * Lấy Firebase Database instance
     * Firebase sẽ tự động đọc URL từ google-services.json
     * 
     * @return FirebaseDatabase instance
     */
    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            // Firebase tự động lấy URL từ google-services.json
            // Nếu cần custom URL, dùng: getInstance("your-custom-url")
            database = FirebaseDatabase.getInstance();
            
            // Bật persistence để cache dữ liệu offline
            database.setPersistenceEnabled(true);
        }
        return database;
    }

    /**
     * Lấy DatabaseReference đến một đường dẫn cụ thể
     * 
     * @param path Đường dẫn trong database (VD: "messages", "users/userId")
     * @return DatabaseReference
     */
    public static DatabaseReference getDatabaseReference(String path) {
        return getDatabase().getReference(path);
    }

    /**
     * Lấy reference đến thư mục messages
     */
    public static DatabaseReference getMessagesRef() {
        return getDatabaseReference("messages");
    }

    /**
     * Lấy reference đến thư mục users
     */
    public static DatabaseReference getUsersRef() {
        return getDatabaseReference("users");
    }

    /**
     * Lấy reference đến thư mục servers
     */
    public static DatabaseReference getServersRef() {
        return getDatabaseReference("servers");
    }

    /**
     * Lấy reference đến thư mục test
     */
    public static DatabaseReference getTestMessagesRef() {
        return getDatabaseReference("test_messages");
    }
}
