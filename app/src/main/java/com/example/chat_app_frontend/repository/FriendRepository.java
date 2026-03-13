package com.example.chat_app_frontend.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chat_app_frontend.model.Friend;
import com.example.chat_app_frontend.model.FriendRequest;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.utils.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository quản lý hệ thống bạn bè trên Firebase RTDB.
 *
 * Firebase schema:
 *   friend_requests/{receiverUid}/{senderUid}/
 *       senderId, senderName, senderAvatarUrl, timestamp, status
 *
 *   friends/{uid}/{friendUid}/
 *       friendName, friendAvatarUrl, since
 *
 *   sent_requests/{senderUid}/{receiverUid}/
 *       receiverId, receiverName, receiverAvatarUrl, timestamp
 */
public class FriendRepository {

    private static final String TAG              = "FriendRepository";
    private static final String FRIENDS_PATH     = "friends";
    private static final String REQUESTS_PATH    = "friend_requests";
    private static final String SENT_PATH        = "sent_requests";

    private final DatabaseReference dbRef;
    private static FriendRepository instance;

    private FriendRepository() {
        dbRef = FirebaseManager.getDatabase().getReference();
    }

    public static synchronized FriendRepository getInstance() {
        if (instance == null) {
            instance = new FriendRepository();
        }
        return instance;
    }

    /** Lấy UID của user đang đăng nhập */
    private String currentUid() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }

    // =========================================================================
    // SEND FRIEND REQUEST
    // =========================================================================

    /**
     * Gửi lời mời kết bạn tới targetUser.
     * Ghi vào:
     *   friend_requests/{targetUid}/{myUid}
     *   sent_requests/{myUid}/{targetUid}
     */
    public void sendFriendRequest(User targetUser, OnCompleteListener callback) {
        String myUid = currentUid();
        if (myUid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        String targetUid = targetUser.getFirebaseUid();

        // Đọc profile của người gửi (current user) để lấy đúng tên & avatar
        dbRef.child("users").child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        com.example.chat_app_frontend.model.User me =
                                snapshot.getValue(com.example.chat_app_frontend.model.User.class);
                        String myName   = (me != null) ? me.getDisplayNameOrUserName() : myUid;
                        String myAvatar = (me != null) ? me.getAvatarUrl() : null;

                        Map<String, Object> updates = new HashMap<>();

                        // Dữ liệu ở phía người nhận: senderName = TÊN TÔI (người gửi)
                        updates.put(REQUESTS_PATH + "/" + targetUid + "/" + myUid + "/senderId",        myUid);
                        updates.put(REQUESTS_PATH + "/" + targetUid + "/" + myUid + "/senderName",      myName);
                        updates.put(REQUESTS_PATH + "/" + targetUid + "/" + myUid + "/senderAvatarUrl", myAvatar);
                        updates.put(REQUESTS_PATH + "/" + targetUid + "/" + myUid + "/timestamp",       System.currentTimeMillis());
                        updates.put(REQUESTS_PATH + "/" + targetUid + "/" + myUid + "/status",          "pending");

                        // Dữ liệu ở phía người gửi (tab "Đã gửi"): receiverName = tên người nhận
                        updates.put(SENT_PATH + "/" + myUid + "/" + targetUid + "/receiverId",        targetUid);
                        updates.put(SENT_PATH + "/" + myUid + "/" + targetUid + "/receiverName",      targetUser.getDisplayNameOrUserName());
                        updates.put(SENT_PATH + "/" + myUid + "/" + targetUid + "/receiverAvatarUrl", targetUser.getAvatarUrl());
                        updates.put(SENT_PATH + "/" + myUid + "/" + targetUid + "/timestamp",         System.currentTimeMillis());

                        dbRef.updateChildren(updates)
                                .addOnSuccessListener(v -> {
                                    Log.d(TAG, "Gửi lời mời kết bạn thành công → " + targetUid);
                                    if (callback != null) callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Gửi lời mời thất bại: " + e.getMessage());
                                    if (callback != null) callback.onFailure(e.getMessage());
                                });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onFailure(error.getMessage());
                    }
                });
    }

    // =========================================================================
    // ACCEPT FRIEND REQUEST
    // =========================================================================

    /**
     * Chấp nhận lời mời kết bạn từ senderUid.
     * - Thêm vào friends của cả hai phía
     * - Xóa friend_request và sent_request
     */
    public void acceptFriendRequest(FriendRequest request, User currentUserProfile, OnCompleteListener callback) {
        String myUid     = currentUid();
        String senderUid = request.getSenderId();
        if (myUid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        long now = System.currentTimeMillis();

        Map<String, Object> updates = new HashMap<>();

        // Thêm bạn vào danh sách của tôi
        updates.put(FRIENDS_PATH + "/" + myUid + "/" + senderUid + "/friendName",      request.getSenderName());
        updates.put(FRIENDS_PATH + "/" + myUid + "/" + senderUid + "/friendAvatarUrl", request.getSenderAvatarUrl());
        updates.put(FRIENDS_PATH + "/" + myUid + "/" + senderUid + "/since",           now);

        // Thêm tôi vào danh sách của người kia
        updates.put(FRIENDS_PATH + "/" + senderUid + "/" + myUid + "/friendName",      currentUserProfile.getDisplayNameOrUserName());
        updates.put(FRIENDS_PATH + "/" + senderUid + "/" + myUid + "/friendAvatarUrl", currentUserProfile.getAvatarUrl());
        updates.put(FRIENDS_PATH + "/" + senderUid + "/" + myUid + "/since",           now);

        // Xóa request ở cả hai phía
        updates.put(REQUESTS_PATH + "/" + myUid    + "/" + senderUid, null);
        updates.put(SENT_PATH     + "/" + senderUid + "/" + myUid,    null);

        dbRef.updateChildren(updates)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Chấp nhận kết bạn thành công với " + senderUid);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * Overload tiện lợi dùng trong BroadcastReceiver: chỉ cần senderUid.
     * Tự đọc FriendRequest và profile của mình từ Firebase.
     */
    public void acceptFriendRequest(String senderUid, OnCompleteListener callback) {
        String myUid = currentUid();
        if (myUid == null) { if (callback != null) callback.onFailure("Chưa đăng nhập"); return; }

        // Đọc dữ liệu lời mời
        dbRef.child(REQUESTS_PATH).child(myUid).child(senderUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot requestSnap) {
                        FriendRequest req = requestSnap.getValue(FriendRequest.class);
                        if (req == null) {
                            if (callback != null) callback.onFailure("Không tìm thấy lời mời");
                            return;
                        }
                        if (req.getSenderId() == null) req.setSenderId(senderUid);

                        // Đọc profile của mình để ghi vào friends của người kia
                        dbRef.child("users").child(myUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userSnap) {
                                        com.example.chat_app_frontend.model.User me =
                                                userSnap.getValue(com.example.chat_app_frontend.model.User.class);
                                        if (me == null) {
                                            if (callback != null) callback.onFailure("Không đọc được profile");
                                            return;
                                        }
                                        if (me.getFirebaseUid() == null) me.setFirebaseUid(myUid);
                                        acceptFriendRequest(req, me, callback);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        if (callback != null) callback.onFailure(error.getMessage());
                                    }
                                });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onFailure(error.getMessage());
                    }
                });
    }

    // =========================================================================
    // DECLINE FRIEND REQUEST
    // =========================================================================

    /**
     * Từ chối lời mời kết bạn từ senderUid.
     * Chỉ xóa data, không thêm bạn.
     */
    public void declineFriendRequest(String senderUid, OnCompleteListener callback) {
        String myUid = currentUid();
        if (myUid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        Map<String, Object> updates = new HashMap<>();
        updates.put(REQUESTS_PATH + "/" + myUid     + "/" + senderUid, null);
        updates.put(SENT_PATH     + "/" + senderUid + "/" + myUid,     null);

        dbRef.updateChildren(updates)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Từ chối kết bạn từ " + senderUid);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // =========================================================================
    // CANCEL SENT REQUEST
    // =========================================================================

    /** Hủy lời mời đã gửi. */
    public void cancelSentRequest(String receiverUid, OnCompleteListener callback) {
        String myUid = currentUid();
        if (myUid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        Map<String, Object> updates = new HashMap<>();
        updates.put(SENT_PATH     + "/" + myUid       + "/" + receiverUid, null);
        updates.put(REQUESTS_PATH + "/" + receiverUid + "/" + myUid,       null);

        dbRef.updateChildren(updates)
                .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    // =========================================================================
    // READ - Friends list
    // =========================================================================

    /** Lấy danh sách bạn bè (đọc 1 lần). */
    public void getFriends(OnFriendListListener callback) {
        String myUid = currentUid();
        if (myUid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        dbRef.child(FRIENDS_PATH).child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Friend> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Friend f = child.getValue(Friend.class);
                            if (f != null) {
                                f.setUid(child.getKey()); // gán uid từ key
                                list.add(f);
                            }
                        }
                        if (callback != null) callback.onLoaded(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onFailure(error.getMessage());
                    }
                });
    }

    // =========================================================================
    // READ - Pending requests (lời mời chờ xác nhận)
    // =========================================================================

    /** Lắng nghe real-time lời mời kết bạn. Trả về ValueEventListener để hủy sau. */
    public ValueEventListener observePendingRequests(OnFriendRequestListListener callback) {
        String myUid = currentUid();
        if (myUid == null) return null;

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FriendRequest> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    FriendRequest req = child.getValue(FriendRequest.class);
                    if (req != null) {
                        if (req.getSenderId() == null) req.setSenderId(child.getKey());
                        list.add(req);
                    }
                }
                if (callback != null) callback.onLoaded(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onFailure(error.getMessage());
            }
        };

        dbRef.child(REQUESTS_PATH).child(myUid).addValueEventListener(listener);
        return listener;
    }

    public void removePendingListener(ValueEventListener listener) {
        String myUid = currentUid();
        if (myUid != null && listener != null) {
            dbRef.child(REQUESTS_PATH).child(myUid).removeEventListener(listener);
        }
    }

    // =========================================================================
    // READ - Sent requests (lời mời đã gửi)
    // =========================================================================

    public void getSentRequests(OnFriendRequestListListener callback) {
        String myUid = currentUid();
        if (myUid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        dbRef.child(SENT_PATH).child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<FriendRequest> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            // Chuyển đổi sent_request thành FriendRequest để tái sử dụng model
                            String receiverId      = child.child("receiverId").getValue(String.class);
                            String receiverName    = child.child("receiverName").getValue(String.class);
                            String receiverAvatar  = child.child("receiverAvatarUrl").getValue(String.class);
                            Long   ts              = child.child("timestamp").getValue(Long.class);

                            FriendRequest req = new FriendRequest();
                            req.setSenderId(receiverId);        // reuse senderId để giữ UID
                            req.setSenderName(receiverName);
                            req.setSenderAvatarUrl(receiverAvatar);
                            req.setTimestamp(ts != null ? ts : 0);
                            req.setStatus("sent");
                            list.add(req);
                        }
                        if (callback != null) callback.onLoaded(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onFailure(error.getMessage());
                    }
                });
    }

    // =========================================================================
    // CHECK - Đã là bạn chưa?
    // =========================================================================

    public void checkFriendStatus(String targetUid, OnFriendStatusListener callback) {
        String myUid = currentUid();
        if (myUid == null) { callback.onResult("none"); return; }

        dbRef.child(FRIENDS_PATH).child(myUid).child(targetUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            callback.onResult("friend");
                        } else {
                            // Kiểm tra đã gửi lời mời chưa
                            dbRef.child(SENT_PATH).child(myUid).child(targetUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snap2) {
                                            if (snap2.exists()) callback.onResult("sent");
                                            else callback.onResult("none");
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            callback.onResult("none");
                                        }
                                    });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResult("none");
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

    public interface OnFriendListListener {
        void onLoaded(List<Friend> friends);
        void onFailure(String error);
    }

    public interface OnFriendRequestListListener {
        void onLoaded(List<FriendRequest> requests);
        void onFailure(String error);
    }

    public interface OnFriendStatusListener {
        void onResult(String status); // "friend" | "sent" | "none"
    }
}
