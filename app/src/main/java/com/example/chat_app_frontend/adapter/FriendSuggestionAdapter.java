package com.example.chat_app_frontend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.FriendSuggestion;

import java.util.List;

public class FriendSuggestionAdapter extends RecyclerView.Adapter<FriendSuggestionAdapter.ViewHolder> {

    private List<FriendSuggestion> suggestions;

    public FriendSuggestionAdapter(List<FriendSuggestion> suggestions) {
        this.suggestions = suggestions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendSuggestion suggestion = suggestions.get(position);
        holder.avatar.setImageResource(suggestion.getAvatarResId());
        holder.displayName.setText(suggestion.getDisplayName());
        holder.username.setText(suggestion.getUsername());
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView displayName, username, btnAdd;

        ViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_suggestion_avatar);
            displayName = itemView.findViewById(R.id.tv_display_name);
            username = itemView.findViewById(R.id.tv_username);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }
}