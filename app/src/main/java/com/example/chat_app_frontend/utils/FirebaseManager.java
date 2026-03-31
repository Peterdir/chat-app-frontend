package com.example.chat_app_frontend.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Firebase Manager - Quản lý kết nối Firebase tập trung (Singleton)
 */
public class FirebaseManager {

    private static FirebaseDatabase database;
    private static FirebaseAuth auth;
    private static FirebaseStorage storage;

    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }
        return database;
    }

    public static DatabaseReference getDatabaseReference(String path) {
        return getDatabase().getReference(path);
    }

    public static DatabaseReference getUsersRef() {
        return getDatabaseReference("users");
    }

    public static DatabaseReference getMessagesRef() {
        return getDatabaseReference("messages");
    }

    public static DatabaseReference getServersRef() {
        return getDatabaseReference("servers");
    }

    public static FirebaseStorage getStorage() {
        if (storage == null) {
            // Sử dụng bucket cụ thể để đảm bảo tính nhất quán
            storage = FirebaseStorage.getInstance("gs://discord-clone-android.firebasestorage.app");
        }
        return storage;
    }

    public static StorageReference getStorageReference(String path) {
        return getStorage().getReference(path);
    }
}
