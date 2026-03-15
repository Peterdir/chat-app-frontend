package com.example.chat_app_frontend.ui;

import android.os.Bundle;
import android.text.Html;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chat_app_frontend.R;

public class StudentHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_student_hub);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        TextView tvDesc = findViewById(R.id.tv_hub_desc);
        String desc = "Gặp gỡ các bạn cùng lớp, khám phá cộng đồng và chia sẻ máy chủ của bạn, đều tập trung tại một chỗ. <font color='#00A8FC'>Tìm hiểu thêm.</font>";
        tvDesc.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY));

        TextView tvPolicy = findViewById(R.id.tv_hub_policy);
        String policy = "Xem lại <font color='#00A8FC'>Điều Khoản Dịch Vụ</font> và <font color='#00A8FC'>Chính Sách Bảo Mật</font> của Discord để tìm hiểu thêm về cách chúng tôi sử dụng dữ liệu của bạn.";
        tvPolicy.setText(Html.fromHtml(policy, Html.FROM_HTML_MODE_LEGACY));

        EditText etEmail = findViewById(R.id.et_school_email);
        findViewById(R.id.btn_join_hub).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if(email.isEmpty() || !email.contains("@")){
                Toast.makeText(this, "Email trường học không hợp lệ!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã gửi yêu cầu tham gia Hub!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}