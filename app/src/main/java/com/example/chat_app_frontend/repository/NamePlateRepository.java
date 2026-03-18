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

        // Real name plates
        allNamePlates.add(new NamePlate("song_tu", "Song Tử", R.drawable.infinite_swrirl_bundle, "Để lại dấu ấn của bạn", "Tháng 1 năm 2026", true, NamePlate.Type.REGULAR));
        
        // Shop items (locked)
        allNamePlates.add(new NamePlate("bunny", "Bunny", R.drawable.avatar1, "Dễ thương", "", false, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("clover", "Clover", R.drawable.frame_aurora, "May mắn", "", false, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("stars", "Stars", R.drawable.frame_black_hole, "Tinh tú", "", false, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("hearts", "Hearts", R.drawable.frame_valorant_champions_2024, "Tình yêu", "", false, NamePlate.Type.REGULAR, true, false));
        allNamePlates.add(new NamePlate("bubbles", "Bubbles", R.drawable.frame_bubble_tea, "Bong bóng", "", false, NamePlate.Type.REGULAR, true, false));
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
