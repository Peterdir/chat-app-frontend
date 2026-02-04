package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class NitroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_nitro);

        Animation animHover = AnimationUtils.loadAnimation(this, R.anim.anim_hover);
        Animation animSlideIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in);
        Animation animGlow = AnimationUtils.loadAnimation(this, R.anim.anim_glow);
        Animation animSlideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up);
//        Animation animClick = AnimationUtils.loadAnimation(this, R.anim.anim_button_click);

        findViewById(android.R.id.content).startAnimation(animSlideUp);

        ImageView imgNitro = findViewById(R.id.img_wumpus_nitro);
        if (imgNitro != null) {
            imgNitro.startAnimation(animHover);
        }

        ImageView imgBasic = findViewById(R.id.img_wumpus_basic);
        if (imgBasic != null) {
            animSlideIn.setStartOffset(1000);
            imgBasic.startAnimation(animSlideIn);
        }


    }
}