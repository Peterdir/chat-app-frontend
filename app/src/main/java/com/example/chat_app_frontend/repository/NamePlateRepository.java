package com.example.chat_app_frontend.repository;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.NamePlate;

import java.util.ArrayList;
import java.util.List;

public class NamePlateRepository {
    private static NamePlateRepository instance;
    private final List<NamePlate> allNamePlates;

    private NamePlateRepository() {
        allNamePlates = new ArrayList<>();
        // Metadata
        allNamePlates.add(new NamePlate("none", "Không", 0, "", "", false, NamePlate.Type.NONE));
        allNamePlates.add(new NamePlate("store", "Cửa hàng", 0, "", "", false, NamePlate.Type.STORE, false, true));

        // Real name plates (Unlocked/REGULAR)
        allNamePlates.add(new NamePlate("song_tu", "Song Tử", R.drawable.infinite_swrirl_bundle, "Để lại dấu ấn của bạn", "Tháng 1 năm 2026", true, NamePlate.Type.REGULAR));
        allNamePlates.add(new NamePlate("magic_mist", "Sương Mù", R.drawable.magic_mists, "Huyền ảo và bí ẩn", "Tháng 2 năm 2026", false, NamePlate.Type.REGULAR));
        allNamePlates.add(new NamePlate("infinite_swirl_2", "Vòng Xoáy V2", R.drawable.infinite_swirl_bundle2, "Vẻ đẹp của vũ trụ", "Tháng 3 năm 2026", true, NamePlate.Type.REGULAR));
        allNamePlates.add(new NamePlate("aurora", "Cực Quang", R.drawable.frame_aurora, "Ánh sáng phương Bắc", "Tháng 3 năm 2026", false, NamePlate.Type.REGULAR));
        
        // Shop items (Locked)
        allNamePlates.add(new NamePlate("bunny", "Thỏ Ngọc", R.drawable.avatar1, "Dễ thương và đáng yêu", "", false, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("aespa", "Aespa Fanlight", R.drawable.frame_aespa_fanlight, "Dành cho fan Aespa", "", true, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("angry", "Giận Dữ", R.drawable.frame_angry, "Hiệu ứng rực lửa", "", false, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("black_hole", "Hố Đen", R.drawable.frame_black_hole, "Sức mạnh vô tận", "", true, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("bubble_tea", "Trà Sữa", R.drawable.frame_bubble_tea, "Ngọt ngào từng khoảnh khắc", "", false, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("candle", "Ánh Nến", R.drawable.frame_candlelight, "Ấm áp và lung linh", "", false, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("witch", "Phù Thủy", R.drawable.frame_witch_hat, "Mùa lễ hội hóa trang", "", true, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("valorant", "Valorant 2024", R.drawable.frame_valorant_champions_2024, "Vinh quang nhà vô địch", "", true, NamePlate.Type.REGULAR, true, false));
    }

    public static synchronized NamePlateRepository getInstance() {
        if (instance == null) {
            instance = new NamePlateRepository();
        }
        return instance;
    }

    public List<NamePlate> getAllNamePlates() {
        return allNamePlates;
    }

    public List<NamePlate> getYourNamePlates() {
        List<NamePlate> list = new ArrayList<>();
        for (NamePlate np : allNamePlates) {
            if (!np.isLocked()) {
                list.add(np);
            }
        }
        return list;
    }

    public List<NamePlate> getShopNamePlates() {
        List<NamePlate> list = new ArrayList<>();
        for (NamePlate np : allNamePlates) {
            if (np.isLocked()) {
                list.add(np);
            }
        }
        return list;
    }

    public NamePlate findNamePlateById(String id) {
        if (id == null) return findNamePlateById("none");
        for (NamePlate np : allNamePlates) {
            if (np.getId().equals(id)) {
                return np;
            }
        }
        return findNamePlateById("none");
    }
}
