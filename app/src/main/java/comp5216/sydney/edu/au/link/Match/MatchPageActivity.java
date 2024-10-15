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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import comp5216.sydney.edu.au.link.R;
import comp5216.sydney.edu.au.link.UserProfile;
import comp5216.sydney.edu.au.link.landing.LoginActivity;

public class MatchPageActivity extends AppCompatActivity {

    private static final int THRESHOLD = 1;
    private TextView matchName;
    private TextView matchStart;
    private com.google.android.material.imageview.ShapeableImageView matchUserPhoto;
    private Button matchButton;
    private ImageButton goBackButton;
    private FirebaseFirestore db;
    private String currentUserId;
    private String matchedUserId;
    private ImageButton imageButton;

    private List<UserProfile> matchedPersons; // 用户数据列表
    private int currentIndex = 0; // 当前显示用户的索引

    private ImageButton rightPersonButton;
    private ImageButton leftPersonButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_main);

        // 初始化 Firebase Firestore 和当前用户ID
        db = FirebaseFirestore.getInstance();
        currentUserId = getCurrentUserId();
        //currentUserId = "1";

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Toast.makeText(this, "UserID Logged in user ID: " + userId, Toast.LENGTH_SHORT).show();
        } else {
            // 用户未登录，跳转到登录页面
            Toast.makeText(this, "login unsuccessful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        matchedPersons = new ArrayList<>();

        imageButton = findViewById(R.id.match_gobackimageButton);
        imageButton.setOnClickListener(v -> {
            // 创建跳转到 MatchMainActivity 的 Intent
            Intent intent = new Intent(MatchPageActivity.this, MatchSuccessActivity.class);
            startActivity(intent);
        });

        // 初始化 UI 组件
        matchName = findViewById(R.id.match_name);
        matchStart = findViewById(R.id.match_start);
        matchUserPhoto = findViewById(R.id.match_userphoto);
        matchButton = findViewById(R.id.match_matchButton);
        rightPersonButton = findViewById(R.id.rightperson);
        leftPersonButton = findViewById(R.id.leftperson);
        // 设置匹配用户信息
        loadMatchedUsers();

        rightPersonButton.setOnClickListener(v -> showNextPerson());
        leftPersonButton.setOnClickListener(v -> showPreviousPerson());
        // 设置匹配按钮点击事件
        matchButton.setOnClickListener(v -> sendMatchRequest());






    }

    /*private void loadMatchedUsers() {
        if (currentUserId == null) {
            Toast.makeText(this, "Current user ID is null. Cannot load matched users.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("userProfiles")
                .whereNotEqualTo("userId", currentUserId) // 确保 userId 是数据库中的字段名
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    matchedPersons.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        UserProfile person = document.toObject(UserProfile.class);
                        matchedPersons.add(person);
                    }
                    // 显示第一个用户
                    if (!matchedPersons.isEmpty()) {
                        showPersonAtIndex(currentIndex);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading matched users", e);
                    Toast.makeText(this, "Error loading users info.", Toast.LENGTH_SHORT).show();
                });
    }*/

    private void loadMatchedUsers() {
        if (currentUserId == null) {
            Toast.makeText(this, "Current user ID is null. Cannot load matched users.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前用户的 UserProfile 以便获取匹配中的用户集合
        db.collection("userProfiles").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                        if (currentUserProfile != null) {
                            ArrayList<String> personInMatchSet = currentUserProfile.getPersonInMatch();

                            // 获取其他用户的信息
                            db.collection("userProfiles")
                                    .whereNotEqualTo("userId", currentUserId) // 确保 userId 是数据库中的字段名
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        matchedPersons.clear();
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            UserProfile person = document.toObject(UserProfile.class);

                                            // 检查该 person 是否已经存在于当前用户的匹配集合中
                                            if (personInMatchSet == null || !personInMatchSet.contains(person.getUserId())) {
                                                matchedPersons.add(person);
                                            }
                                        }
                                        // 显示第一个用户
                                        if (!matchedPersons.isEmpty()) {
                                            showPersonAtIndex(currentIndex);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error loading matched users", e);
                                        Toast.makeText(this, "Error loading users info.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Failed to load current user profile.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "Current user profile does not exist");
                        Toast.makeText(this, "Current user profile does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching current user profile", e);
                    Toast.makeText(this, "Error loading current user info.", Toast.LENGTH_SHORT).show();
                });
    }


    /*private void loadMatchedUsers() {
        // 获取当前用户的兴趣和偏好
        db.collection("userProfiles").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile currentUser = documentSnapshot.toObject(UserProfile.class);
                        String currentUserInterests = currentUser.getInterest();
                        String currentUserPreferences = currentUser.getPreference();

                        // 获取其他用户的信息
                        db.collection("userProfiles")
                                .whereNotEqualTo("userID", currentUserId) // 不包括当前用户
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    matchedPersons.clear();
                                    for (QueryDocumentSnapshot document : querySnapshot) {
                                        UserProfile person = document.toObject(userProfiles.class);
                                        String otherUserInterests = person.getInterest();
                                        String otherUserPreferences = person.getPreference();

                                        // 计算匹配度
                                        int commonInterestCount = calculateCommonInterests(currentUserInterests, otherUserInterests);
                                        int commonPreferenceCount = calculateCommonInterests(currentUserPreferences, otherUserPreferences);

                                        // 如果匹配度满足条件，则添加到匹配列表
                                        if (commonInterestCount > THRESHOLD && commonPreferenceCount > THRESHOLD) {
                                            matchedPersons.add(person);
                                        }
                                    }
                                    // 显示第一个用户
                                    if (!matchedPersons.isEmpty()) {
                                        showPersonAtIndex(currentIndex);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error loading matched users", e);
                                    Toast.makeText(this, "Error loading users info.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading current user", e);
                    Toast.makeText(this, "Error loading current user info.", Toast.LENGTH_SHORT).show();
                });
    }*/

    private void checkMatchingStatus() {
        if (matchedPersons.isEmpty() || currentIndex < 0 || currentIndex >= matchedPersons.size()) {
            return;
        }

        String matchedUserId = matchedPersons.get(currentIndex).getUserId();  // 获取当前显示用户的 ID

        // 查询 matchRequests 集合以检测是否存在匹配请求
        String documentName = currentUserId + "to" + matchedUserId;
        db.collection("matchRequests")
                .document(documentName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        if ("pending".equalsIgnoreCase(status)) {
                            // 如果匹配请求是 "pending"，则设置按钮状态为 "Matching"
                            matchButton.setText("Matching");
                        } else if ("accepted".equalsIgnoreCase(status)) {
                            // 如果匹配请求被接受，则设置按钮状态为 "Matched"
                            matchButton.setText("Matched");
                        } else {
                            // 其他状态
                            matchButton.setText("Match");
                        }
                    } else {
                        // 如果没有匹配请求文档，表示用户未在匹配状态中
                        matchButton.setText("Available");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking matching status", e);
                    Toast.makeText(MatchPageActivity.this, "Error checking matching status.", Toast.LENGTH_SHORT).show();
                });
    }

    // 显示下一个用户
    private void showNextPerson() {
        if (currentIndex < matchedPersons.size() - 1) {
            currentIndex++;
            showPersonAtIndex(currentIndex);
            checkMatchingStatus();
        } else {
            Toast.makeText(this, "No more matches.", Toast.LENGTH_SHORT).show();
        }
    }

    // 显示上一个用户
    private void showPreviousPerson() {
        if (currentIndex > 0) {
            currentIndex--;
            showPersonAtIndex(currentIndex);
            checkMatchingStatus();
        } else {
            Toast.makeText(this, "This is the first match.", Toast.LENGTH_SHORT).show();
        }
    }

    // 根据索引显示用户信息
    private void showPersonAtIndex(int index) {
        UserProfile person = matchedPersons.get(index);
        matchedUserId = person.getUserId();
        matchName.setText(person.getName());

        // 使用 Glide 加载用户图片
        if (person.getProfilePictureUrl() != null && !person.getProfilePictureUrl().isEmpty()) {
            Glide.with(this).load(person.getProfilePictureUrl()).into(matchUserPhoto);
        } else {
            matchUserPhoto.setImageResource(R.drawable.default_image);
        }
    }
    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }
    }
    private void sendMatchRequest() {
        if (matchedUserId == null) {
            Toast.makeText(this, "No user selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 设置按钮状态为“Matching”
        //matchButton.setText("Matching");

        // 创建并保存匹配请求到 Firebase
        MatchRequests matchRequest = new MatchRequests(currentUserId, matchedUserId, "pending");
        String documentName = currentUserId +"to"+matchedUserId;
        System.out.println(documentName);
            db.collection("matchRequests")
                .document(documentName)  // 使用自定义文档名称
                .set(matchRequest)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Match request saved successfully with document name: " + documentName);
                    Toast.makeText(MatchPageActivity.this, "Match request sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving match request", e);
                    Toast.makeText(MatchPageActivity.this, "Error sending match request.", Toast.LENGTH_SHORT).show();
                });
        checkMatchingStatus();
    }


    private int calculateCommonInterests(String interests1, String interests2) {

        System.out.println("interests1: " + interests1);
        System.out.println("interests2: " + interests2);

        // 如果有任何一个为 null 或为空，返回匹配度为 0
        if (interests1 == null || interests1.isEmpty() || interests2 == null || interests2.isEmpty()) {
            return 0;
        }

        // 将兴趣字符串按空格分隔并转换为集合
        Set<String> set1 = new HashSet<>(Arrays.asList(interests1.split(" ")));
        Set<String> set2 = new HashSet<>(Arrays.asList(interests2.split(" ")));
        System.out.println();

        // 计算交集
        set1.retainAll(set2);
        return set1.size();
    }


}