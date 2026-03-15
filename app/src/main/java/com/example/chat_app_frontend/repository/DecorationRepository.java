package com.example.chat_app_frontend.repository;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Decoration;
import java.util.ArrayList;
import java.util.List;

public class DecorationRepository {
    private static DecorationRepository instance;
    private final List<Decoration> allDecorations;

    private DecorationRepository() {
        allDecorations = new ArrayList<>();
        // Metadata decorations
        allDecorations.add(new Decoration("none", "Không", 0, "", false, Decoration.Type.NONE));
        allDecorations.add(new Decoration("store", "Cửa hàng", 0, "", false, Decoration.Type.STORE, false, true));

        // Regular/Nitro decorations
        allDecorations.add(new Decoration("aespa", "Aespa Fanlight", R.drawable.frame_aespa_fanlight, "Hào quang Aespa", false, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("witch", "Mũ Phù Thủy", R.drawable.frame_witch_hat, "Halloween vĩnh cửu", false, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("aurora", "Aurora", R.drawable.frame_aurora, "Cực quang", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("black_hole", "Black Hole", R.drawable.frame_black_hole, "Hố đen", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("cat_onesie", "Cat Onesie", R.drawable.frame_cat_onesie, "Onesie mèo", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("unicorn", "Unicorn", R.drawable.frame_unicorn, "Kỳ lân cầu vồng", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("straw_hat", "Mũ Rơm", R.drawable.frame_straw_hat, "Vua Hải Tặc", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("valorant", "Valorant Champion", R.drawable.frame_valorant_champions_2024, "Nhấn phím F1", true, Decoration.Type.REGULAR));
        
        // Additional frames found
        allDecorations.add(new Decoration("angry", "Angry Express", R.drawable.frame_angry, "Nổi giận", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("bubble_tea", "Bubble Tea", R.drawable.frame_bubble_tea, "Trà sữa", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("candlelight", "Candlelight", R.drawable.frame_candlelight, "Ánh nến", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("cozycat", "Cozy Cat", R.drawable.frame_cozycat, "Mèo ấm áp", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("crystal_elk", "Crystal Elk", R.drawable.frame_crystal_elk, "Tuần lộc pha lê", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("solar_orbit", "Solar Orbit", R.drawable.frame_solar_orbit, "Quỹ đạo mặt trời", true, Decoration.Type.REGULAR));
        allDecorations.add(new Decoration("witch_hat_duo", "Witch Hat Duo", R.drawable.frame_witch_hat, "Bộ đôi phù thủy", true, Decoration.Type.REGULAR));
    }

    public static synchronized DecorationRepository getInstance() {
        if (instance == null) {
            instance = new DecorationRepository();
        }
        return instance;
    }

    public List<Decoration> getAllDecorations() {
        return allDecorations;
    }

    public List<Decoration> getYourDecorations() {
        List<Decoration> list = new ArrayList<>();
        for (Decoration d : allDecorations) {
            if (d.getType() == Decoration.Type.NONE || d.getType() == Decoration.Type.STORE || 
                d.getId().equals("aespa") || d.getId().equals("witch")) {
                list.add(d);
            }
        }
        return list;
    }

    public List<Decoration> getNitroDecorations() {
        List<Decoration> list = new ArrayList<>();
        for (Decoration d : allDecorations) {
            if (d.isNitro()) {
                list.add(d);
            }
        }
        return list;
    }

    public Decoration findDecorationById(String id) {
        if (id == null) return findDecorationById("none");
        for (Decoration d : allDecorations) {
            if (d.getId().equals(id)) {
                return d;
            }
        }
        return findDecorationById("none");
    }
}
