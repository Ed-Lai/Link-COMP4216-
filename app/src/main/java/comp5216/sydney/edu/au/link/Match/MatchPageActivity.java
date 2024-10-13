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

    private List<MatchPerson> matchedPersons; // 用户数据列表
    private int currentIndex = 0; // 当前显示用户的索引

    private ImageButton rightPersonButton;
    private ImageButton leftPersonButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_main);

        // 初始化 Firebase Firestore 和当前用户ID
        db = FirebaseFirestore.getInstance();
        //currentUserId = getCurrentUserId();
        currentUserId = "1";

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
        db.collection("matchpersons")
                .whereNotEqualTo("userID", currentUserId) // 不包括当前用户
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    matchedPersons.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) { //111111
                        MatchPerson person = document.toObject(MatchPerson.class);
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
        // 获取当前用户的兴趣和偏好
        db.collection("matchpersons").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        MatchPerson currentUser = documentSnapshot.toObject(MatchPerson.class);
                        String currentUserInterests = currentUser.getInterest();
                        String currentUserPreferences = currentUser.getPreference();

                        // 获取其他用户的信息
                        db.collection("matchpersons")
                                .whereNotEqualTo("userID", currentUserId) // 不包括当前用户
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    matchedPersons.clear();
                                    for (QueryDocumentSnapshot document : querySnapshot) {
                                        MatchPerson person = document.toObject(MatchPerson.class);
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
    }

    // 显示下一个用户
    private void showNextPerson() {
        if (currentIndex < matchedPersons.size() - 1) {
            currentIndex++;
            showPersonAtIndex(currentIndex);
        } else {
            Toast.makeText(this, "No more matches.", Toast.LENGTH_SHORT).show();
        }
    }

    // 显示上一个用户
    private void showPreviousPerson() {
        if (currentIndex > 0) {
            currentIndex--;
            showPersonAtIndex(currentIndex);
        } else {
            Toast.makeText(this, "This is the first match.", Toast.LENGTH_SHORT).show();
        }
    }

    // 根据索引显示用户信息
    private void showPersonAtIndex(int index) {
        MatchPerson person = matchedPersons.get(index);
        matchedUserId = person.getUserID();
        matchName.setText(person.getMatchPersonName());

        // 使用 Glide 加载用户图片
        if (person.getPhotoPath() != null && !person.getPhotoPath().isEmpty()) {
            Glide.with(this).load(person.getPhotoPath()).into(matchUserPhoto);
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
        matchButton.setText("Matching");

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
    }

    public List<MatchPerson> findMatchingUsers(List<MatchPerson> allUsers, MatchPerson currentUser) {
        List<MatchPerson> matchedUsers = new ArrayList<>();

        for (MatchPerson user : allUsers) {
            if (!user.getUserID().equals(currentUser.getUserID())) { // 避免与自己匹配
                int commonInterestCount = calculateCommonInterests(currentUser.getInterest(), user.getInterest());
                if (commonInterestCount > THRESHOLD) { // 定义一个阈值用于匹配
                    matchedUsers.add(user);
                }
            }
        }

        return matchedUsers;
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