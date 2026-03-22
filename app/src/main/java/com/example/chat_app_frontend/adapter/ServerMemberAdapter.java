package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.User;
import com.example.chat_app_frontend.repository.UserRepository;
import com.example.chat_app_frontend.utils.ProfileUIUtils;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerMemberAdapter extends RecyclerView.Adapter<ServerMemberAdapter.ViewHolder> {

    private final List<User> members = new ArrayList<>();
    private final Map<ViewHolder, ValueEventListener> listeners = new HashMap<>();
    private final Map<ViewHolder, String> holderToUid = new HashMap<>();

    public void submitList(List<User> data) {
        members.clear();
        if (data != null) {
            members.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_server_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = members.get(position);
        
        // Remove old listener if any
        removeListener(holder);

        // Start observing this user
        String uid = user.getFirebaseUid();
        if (uid != null) {
            holderToUid.put(holder, uid);
            ValueEventListener listener = UserRepository.getInstance().observeUser(uid, new UserRepository.OnUserLoadedListener() {
                @Override
                public void onUserLoaded(User updatedUser) {
                    bindUser(holder, updatedUser);
                }

                @Override
                public void onUserNotFound() {}

                @Override
                public void onFailure(String error) {}
            });
            listeners.put(holder, listener);
        } else {
            // Fallback for null UID
            bindUser(holder, user);
        }
    }

    private void bindUser(ViewHolder holder, User user) {
        String display = user.getDisplayNameOrUserName();
        if (display == null || display.trim().isEmpty()) {
            display = "Unknown";
        }

        holder.tvName.setText(display);

        String userName = user.getUserName();
        holder.tvSubtitle.setText(userName != null && !userName.trim().isEmpty()
                ? "@" + userName
                : "");

        // Use ProfileUIUtils for all profile elements
        ProfileUIUtils.loadUserProfile(holder.itemView.getContext(), user, 
                holder.imgAvatar, holder.imgDecoration, holder.imgNamePlate, null);
        
        // Show initial if avatar failed/missing
        if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            holder.tvInitial.setVisibility(View.VISIBLE);
            holder.tvInitial.setText(display.isEmpty() ? "?" : String.valueOf(display.charAt(0)).toUpperCase());
            
            int idx = (user.getFirebaseUid() != null ? user.getFirebaseUid().hashCode() : 0) & 0x7FFFFFFF;
            int[] palette = {0xFF5865F2, 0xFF23A559, 0xFFEB459E, 0xFFFEE75C, 0xFF57F287, 0xFFED4245};
            holder.cvAvatar.setCardBackgroundColor(palette[idx % palette.length]);
        } else {
            holder.tvInitial.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        removeListener(holder);
    }

    private void removeListener(ViewHolder holder) {
        if (listeners.containsKey(holder) && holderToUid.containsKey(holder)) {
            ValueEventListener oldListener = listeners.get(holder);
            String oldUid = holderToUid.get(holder);
            UserRepository.getInstance().removeListener(oldUid, oldListener);
            listeners.remove(holder);
            holderToUid.remove(holder);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cvAvatar;
        ImageView imgAvatar;
        ImageView imgDecoration;
        ImageView imgNamePlate;
        TextView tvInitial;
        TextView tvName;
        TextView tvSubtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cvAvatar = itemView.findViewById(R.id.cv_member_avatar);
            imgAvatar = itemView.findViewById(R.id.img_member_avatar);
            imgDecoration = itemView.findViewById(R.id.img_member_decoration);
            imgNamePlate = itemView.findViewById(R.id.img_member_name_plate);
            tvInitial = itemView.findViewById(R.id.tv_member_initial);
            tvName = itemView.findViewById(R.id.tv_member_name);
            tvSubtitle = itemView.findViewById(R.id.tv_member_subtitle);
        }
    }
}
