package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.adapter.FriendSuggestionAdapter;
import com.example.chat_app_frontend.adapter.NotificationAdapter;
import com.example.chat_app_frontend.model.FriendSuggestion;
import com.example.chat_app_frontend.model.Notification;
import com.example.chat_app_frontend.model.ServerInvite;
import com.example.chat_app_frontend.repository.ServerInviteRepository;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class NotificationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        setupNotifications(view);
        setupServerInvites(view);
        setupFriendSuggestions(view);
        setupMoreButton(view);

        return view;
    }

    private void setupServerInvites(View view) {
        RecyclerView rvInvites = view.findViewById(R.id.rv_server_invites);
        View tvHeader = view.findViewById(R.id.tv_server_invites_header);

        rvInvites.setLayoutManager(new LinearLayoutManager(getContext()));

        ServerInviteRepository.getInstance().getPendingInvitesForMe(
                new ServerInviteRepository.OnInviteListListener() {
                    @Override
                    public void onSuccess(List<ServerInvite> invites) {
                        if (!isAdded()) return;
                        if (invites.isEmpty()) {
                            tvHeader.setVisibility(View.GONE);
                            rvInvites.setVisibility(View.GONE);
                            return;
                        }
                        tvHeader.setVisibility(View.VISIBLE);
                        rvInvites.setVisibility(View.VISIBLE);
                        rvInvites.setAdapter(new ServerInviteAdapter(invites));
                    }

                    @Override
                    public void onFailure(String error) {
                        // Ẩn section nếu lỗi
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Adapter nội bộ cho danh sách lời mời server
    // -------------------------------------------------------------------------

    private class ServerInviteAdapter extends RecyclerView.Adapter<ServerInviteAdapter.VH> {

        private final List<ServerInvite> list;

        ServerInviteAdapter(List<ServerInvite> list) {
            this.list = new ArrayList<>(list);
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_server_invite, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ServerInvite invite = list.get(position);

            // Chữ cái đầu của server
            String name = invite.getServerName() != null ? invite.getServerName() : "?";
            holder.tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
            holder.tvServerName.setText(name);

            String inviter = invite.getInvitedByName() != null ? invite.getInvitedByName() : "Ai đó";
            holder.tvFrom.setText(inviter + " đã mời bạn tham gia");

            holder.btnAccept.setOnClickListener(v -> {
                holder.btnAccept.setEnabled(false);
                holder.btnDecline.setEnabled(false);
                ServerInviteRepository.getInstance().acceptServerInvite(
                        invite.getServerId(),
                        new ServerInviteRepository.OnCompleteListener() {
                            @Override
                            public void onSuccess() {
                                if (!isAdded()) return;
                                Toast.makeText(getContext(),
                                        "Đã tham gia \"" + invite.getServerName() + "\"!",
                                        Toast.LENGTH_SHORT).show();
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).onServerInviteAccepted(invite.getServerId());
                                }
                                removeItem(holder.getAdapterPosition());
                            }

                            @Override
                            public void onFailure(String error) {
                                if (!isAdded()) return;
                                holder.btnAccept.setEnabled(true);
                                holder.btnDecline.setEnabled(true);
                                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            holder.btnDecline.setOnClickListener(v -> {
                holder.btnAccept.setEnabled(false);
                holder.btnDecline.setEnabled(false);
                ServerInviteRepository.getInstance().declineServerInvite(
                        invite.getServerId(),
                        new ServerInviteRepository.OnCompleteListener() {
                            @Override
                            public void onSuccess() {
                                if (!isAdded()) return;
                                removeItem(holder.getAdapterPosition());
                            }

                            @Override
                            public void onFailure(String error) {
                                if (!isAdded()) return;
                                holder.btnAccept.setEnabled(true);
                                holder.btnDecline.setEnabled(true);
                            }
                        });
            });
        }

        private void removeItem(int position) {
            if (position < 0 || position >= list.size()) return;
            list.remove(position);
            notifyItemRemoved(position);
            if (list.isEmpty()) {
                View root = getView();
                if (root != null) {
                    root.findViewById(R.id.rv_server_invites).setVisibility(View.GONE);
                    root.findViewById(R.id.tv_server_invites_header).setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvInitial, tvServerName, tvFrom;
            Button btnAccept, btnDecline;

            VH(@NonNull View itemView) {
                super(itemView);
                tvInitial = itemView.findViewById(R.id.tv_server_initial);
                tvServerName = itemView.findViewById(R.id.tv_invite_server_name);
                tvFrom = itemView.findViewById(R.id.tv_invite_from);
                btnAccept = itemView.findViewById(R.id.btn_accept_invite);
                btnDecline = itemView.findViewById(R.id.btn_decline_invite);
            }
        }
    }

    // -------------------------------------------------------------------------

    private void setupMoreButton(View view) {
        View btnMore = view.findViewById(R.id.btn_more);
        if (btnMore != null) {
            btnMore.setOnClickListener(v -> {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
                View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_notification_settings, null);
                bottomSheetDialog.setContentView(bottomSheetView);
                
                if (bottomSheetDialog.getWindow() != null) {
                    View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                    if (bottomSheet != null) {
                        bottomSheet.setBackgroundResource(android.R.color.transparent);
                    }
                }
                
                View btnSettings = bottomSheetView.findViewById(R.id.btn_notification_settings);
                if (btnSettings != null) {
                    btnSettings.setOnClickListener(v2 -> {
                        bottomSheetDialog.dismiss();
                        startActivity(new android.content.Intent(getContext(), NotificationSettingsActivity.class));
                    });
                }
                
                bottomSheetDialog.show();
            });
        }
    }

    private void setupNotifications(View view) {
        RecyclerView rvNotifications = view.findViewById(R.id.rv_notifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification(
                R.drawable.avatar1,
                "Liên hệ Won của bạn đã tham gia Discord. Hãy gửi cho họ yêu cầu kết bạn!",
                "12ngày",
                "Thêm Bạn"));
        notifications.add(new Notification(
                R.drawable.avatar2,
                "mfun đã chấp nhận yêu cầu kết bạn.",
                "29ngày",
                null));

        rvNotifications.setAdapter(new NotificationAdapter(notifications));
    }

    private void setupFriendSuggestions(View view) {
        RecyclerView rvSuggestions = view.findViewById(R.id.rv_friend_suggestions);
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));

        List<FriendSuggestion> suggestions = new ArrayList<>();
        suggestions.add(new FriendSuggestion(R.drawable.avatar3, "Aster", "Aster"));
        suggestions.add(new FriendSuggestion(R.drawable.avatar4, "Huỳnh Như", "Huỳnh như"));

        rvSuggestions.setAdapter(new FriendSuggestionAdapter(suggestions));
    }
}
