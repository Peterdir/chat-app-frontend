package com.example.chat_app_frontend.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Firebase Manager - Quản lý kết nối Firebase tập trung (Singleton)
 *
 * Cung cấp:
 *  - FirebaseAuth instance        → getAuth()
 *  - FirebaseDatabase instance    → getDatabase()
 *  - FirebaseStorage instance     → getStorage()
 *  - DatabaseReference shortcuts  → getDatabaseReference(), getUsersRef(), v.v.
 */
public class FirebaseManager {

    private static FirebaseDatabase database;
    private static FirebaseAuth auth;
    private static FirebaseStorage storage;

    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------

    /**
     * Lấy FirebaseAuth instance.
     * Dùng để đăng nhập, đăng ký, đăng xuất, gửi email xác thực / reset mật khẩu.
     */
    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    // -------------------------------------------------------------------------
    // Realtime Database
    // -------------------------------------------------------------------------

    /**
     * Lấy FirebaseDatabase instance.
     * Firebase tự đọc URL từ google-services.json.
     */
    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }
        return database;
    }

    /**
     * Lấy DatabaseReference đến một đường dẫn cụ thể.
     *
     * @param path Đường dẫn trong database (VD: "users", "messages/channelId")
     */
    public static DatabaseReference getDatabaseReference(String path) {
        return getDatabase().getReference(path);
    }

    /** Reference đến node users/ */
    public static DatabaseReference getUsersRef() {
        return getDatabaseReference("users");
    }

    /** Reference đến node messages/ */
    public static DatabaseReference getMessagesRef() {
        return getDatabaseReference("messages");
    }

    /** Reference đến node servers/ */
    public static DatabaseReference getServersRef() {
        return getDatabaseReference("servers");
    }

    /** Reference đến node test_messages/ */
    public static DatabaseReference getTestMessagesRef() {
        return getDatabaseReference("test_messages");
    }

    // -------------------------------------------------------------------------
    // Storage
    // -------------------------------------------------------------------------

    /**
     * Lấy FirebaseStorage instance.
     */
    public static FirebaseStorage getStorage() {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        return storage;
    }

    /**
     * Lấy StorageReference đến một đường dẫn cụ thể.
     */
    public static StorageReference getStorageReference(String path) {
        return getStorage().getReference(path);
    }
}
