package comp5216.sydney.edu.au.link.Match;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import comp5216.sydney.edu.au.link.R;

public class MatchPageActivity extends AppCompatActivity {

    private TextView matchName;
    private TextView matchStart;
    private com.google.android.material.imageview.ShapeableImageView matchUserPhoto;
    private Button matchButton;
    private ImageButton goBackButton;
    private FirebaseFirestore db;
    private String currentUserId;
    private String matchedUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_main);

        // 初始化 Firebase Firestore 和当前用户ID
        db = FirebaseFirestore.getInstance();
        currentUserId = getCurrentUserId();

        // 如果用户未登录，提醒登录并退出当前页面
        if (currentUserId == null) {
            promptUserToLogin();
            return;
        }

        // 获取 Intent 中传递的匹配用户ID
        matchedUserId = getIntent().getStringExtra("matchedUserId");
        if (matchedUserId == null) {
            Toast.makeText(this, "Error: No matched user provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化 UI 组件
        matchName = findViewById(R.id.match_name);
        matchStart = findViewById(R.id.match_start);
        matchUserPhoto = findViewById(R.id.match_userphoto);
        matchButton = findViewById(R.id.match_matchButton);
        goBackButton = findViewById(R.id.match_gobackimageButton);

        // 设置匹配用户信息
        loadMatchedUserInfo();

        // 设置匹配按钮点击事件
        matchButton.setOnClickListener(v -> {
            // 设置按钮状态为“Matching”
            matchButton.setText("Matching");

            // 保存匹配请求到 Firebase
            Map<String, Object> matchRequest = new HashMap<>();
            matchRequest.put("currentUserId", currentUserId);
            matchRequest.put("matchedUserId", matchedUserId);

            db.collection("matchRequests")
                    .add(matchRequest)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Match request saved successfully.");
                        Toast.makeText(MatchPageActivity.this, "Match request sent!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error saving match request", e);
                        Toast.makeText(MatchPageActivity.this, "Error sending match request.", Toast.LENGTH_SHORT).show();
                    });
        });

        // 设置返回按钮点击事件
        goBackButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadMatchedUserInfo() {
        // 从 Firebase 获取匹配用户信息并更新 UI（这里假设 matchedUserId 可用于查找用户信息）
        db.collection("matchpersons").document(matchedUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String photoUrl = documentSnapshot.getString("photo_url");

                        // 设置匹配用户的姓名和照片
                        matchName.setText(name);
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this).load(photoUrl).into(matchUserPhoto);
                        } else {
                            matchUserPhoto.setImageResource(R.drawable.default_image);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading matched user info", e);
                    Toast.makeText(MatchPageActivity.this, "Error loading user info.", Toast.LENGTH_SHORT).show();
                });
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }
    }

    private void promptUserToLogin() {
        Toast.makeText(this, "Please log in to continue.", Toast.LENGTH_SHORT).show();
        // 这里可以添加跳转到登录页面的逻辑，例如：
        // Intent loginIntent = new Intent(this, LoginActivity.class);
        // startActivity(loginIntent);
        finish();
    }
}