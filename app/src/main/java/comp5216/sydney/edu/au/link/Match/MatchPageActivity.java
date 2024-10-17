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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Objects;
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
    private TextView genderText;
    private RecyclerView interestView;

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
        genderText = findViewById(R.id.personGenderContent);
        interestView = findViewById(R.id.interestRecyclerView);
        processMatchRequests();
        // 设置匹配用户信息
        loadMatchedUsers();

        rightPersonButton.setOnClickListener(v -> showNextPerson());
        leftPersonButton.setOnClickListener(v -> showPreviousPerson());
        // 设置匹配按钮点击事件
        matchButton.setOnClickListener(v -> sendMatchRequest());


    }

    @Override
    protected void onStart(){
        super.onStart();
        processMatchRequests();
        loadMatchedUsers();
    }


    private void processMatchRequests() {
        db.collection("matchRequests")
                .whereEqualTo("requesterId", currentUserId)
                .whereEqualTo("status", "finish")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        MatchRequests matchRequest = document.toObject(MatchRequests.class);
                        String requestedId = matchRequest.getRequestedId();

                        // 获取当前用户的 UserProfile
                        db.collection("userProfiles").document(currentUserId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        UserProfile currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                                        if (currentUserProfile != null) {
                                            // 添加 requestedId 到当前用户的 personInMatch 集合中
                                            if (!currentUserProfile.getPersonInMatch().contains(requestedId)) {
                                                currentUserProfile.addPersonInMatch(requestedId);
                                                Toast.makeText(this, "update personInMatch successfully" + requestedId, Toast.LENGTH_SHORT).show();
                                                // 更新当前用户的 personInMatch 到数据库
                                                db.collection("userProfiles").document(currentUserId)
                                                        .set(currentUserProfile)
                                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Successfully updated personInMatch for current user."))
                                                        .addOnFailureListener(e -> Log.e("Firestore", "Error updating personInMatch for current user", e));
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching current user profile", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching match requests", e));
    }


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
                            ArrayList<String> currentUserInterests = currentUserProfile.getInterests();


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

                                                ArrayList<String> otherUserInterests = person.getInterests();
                                                int commonInterestCount = calculateCommonInterests(currentUserInterests, otherUserInterests);

                                                // 只有在两人有一个以上的共同兴趣时，才将该用户添加到匹配列表
                                                if (commonInterestCount >= 0) {
                                                    if(Objects.equals(currentUserProfile.getLocation(), person.getLocation()) && !person.getLocation().equals("Unknown")){
                                                        matchedPersons.add(person);
                                                    }

                                                }
                                                //matchedPersons.add(person);
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
                        }else {
                            // 其他状态
                            matchButton.setText("Match");
                        }
                    } else {
                        // 如果没有匹配请求文档，表示用户未在匹配状态中
                        matchButton.setText("Match");
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
        genderText.setText(person.getGender());
        // 使用 Glide 加载用户图片
        if (person.getProfilePictureUrl() != null && !person.getProfilePictureUrl().isEmpty()) {
            Glide.with(this).load(person.getProfilePictureUrl()).into(matchUserPhoto);
        } else {
            matchUserPhoto.setImageResource(R.drawable.default_image);
        }

        ArrayList<String> interestsList = person.getInterests();
        RecyclerView recyclerView = findViewById(R.id.interestRecyclerView);

        // 创建并设置适配器
        InterestsAdapter adapter = new InterestsAdapter(interestsList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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


    private int calculateCommonInterests(ArrayList<String> interests1, ArrayList<String> interests2) {

        // 如果有任何一个为 null 或为空，返回匹配度为 0
        if (interests1 == null || interests1.isEmpty() || interests2 == null || interests2.isEmpty()) {
            return 0;
        }

        // 将兴趣字符串按空格分隔并转换为集合
        Set<String> set1 = new HashSet<>(interests1);
        Set<String> set2 = new HashSet<>(interests2);

        // 计算交集
        set1.retainAll(set2);
        return set1.size();
    }


}