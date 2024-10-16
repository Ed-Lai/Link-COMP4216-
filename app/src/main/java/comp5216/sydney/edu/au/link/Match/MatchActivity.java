package comp5216.sydney.edu.au.link.Match;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.link.R;
import comp5216.sydney.edu.au.link.UserProfile;
import comp5216.sydney.edu.au.link.landing.LoginActivity;

public class MatchActivity extends AppCompatActivity implements MatchAdapter.OnDeleteRequestListener,MatchAdapter.OnMatchRequestListener {
    private FirebaseFirestore db;
    private ListView listView;
    private MatchAdapter adapter;
    private List<UserProfile> matchPersonList;
    private ImageButton imageButton;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_matches);

        db = FirebaseFirestore.getInstance();
        matchPersonList = new ArrayList<>();


        listView = findViewById(R.id.match_listView);
        imageButton = findViewById(R.id.match_matches_gobackimageButton);
        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MatchActivity.this, MatchPageActivity.class);
            startActivity(intent);
        });

 //       String currentUserId = getCurrentUserId();
        currentUserId = getCurrentUserId();


        adapter = new MatchAdapter(this, matchPersonList, this,this);
        listView.setAdapter(adapter);

        // Load data
        //insertSampleData();
        //insertSampleWithInterestAndPreferences();
        loadMRequestData();
        //deleteAllMatchRequests();\




    }
    public String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            /*Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            return null;*/

            Intent intent = new Intent(MatchActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return null;
        }
    }
    private void loadMRequestData() {
        CollectionReference matchPersonRef = db.collection("matchRequests");

        matchPersonRef.whereEqualTo("requestedId", currentUserId)
                .whereEqualTo("status","pending")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firestore", "Data fetched successfully");
                        matchPersonList.clear();
                        QuerySnapshot result = task.getResult();

                        if (result.isEmpty()) {
                            Log.d("Firestore", "No documents found in MatchRequests");
                        } else {
                            for (QueryDocumentSnapshot document : result) {
                                MatchRequests matchRequest = document.toObject(MatchRequests.class);
                                if (matchRequest.getRequestedId() != null) {
                                    loadMatchPersonDetails(matchRequest.getRequesterId());
                                }
                            }
                            // 确保所有数据加载完成后再刷新适配器
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("Firestore", "Error fetching data", task.getException());
                    }
                });
    }



    /*private void loadMatchPersonDetails(String requesterID) {
        db.collection("matchpersons")
                .whereEqualTo("userID", requesterID) // 假设 "userId" 是字段名
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            MatchPerson person = document.toObject(MatchPerson.class);
                            matchPersonList.add(person); // 添加到显示列表
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("MatchActivity", "No MatchPerson document found for requesterId: " + requesterID);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching MatchPerson details", e));
    }*/

    private void loadMatchPersonDetails(String requesterID) {
        db.collection("userProfiles")
                .whereEqualTo("userId", requesterID) // 假设 "userId" 是字段名
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            UserProfile person = document.toObject(UserProfile.class);
                            matchPersonList.add(person); // 添加到显示列表
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("MatchActivity", "No UserProfile document found for requesterId: " + requesterID);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching UserProfile details", e));
    }





    // 删除匹配请求
    @Override
    public void onDeleteRequest(UserProfile person) {
        String documentName = person.getUserId()+"to"+currentUserId;
            db.collection("matchRequests").document(documentName)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        matchPersonList.remove(person);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Match request deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error deleting match request", e));
    }



    /*@Override
    public void onMatchRequest(UserProfile person) {
        String documentName = person.getUserId() + "to" + currentUserId;
        db.collection("matchRequests").document(documentName)
                .update("status", "finish")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Match request approved", Toast.LENGTH_SHORT).show();
                    matchPersonList.remove(person);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating match request status", e));
    }*/

    @Override
    public void onMatchRequest(UserProfile person) {
        String documentName = person.getUserId() + "to" + currentUserId;

        // 更新匹配请求的状态为 "finish"
        db.collection("matchRequests").document(documentName)
                .update("status", "finish")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Match request approved", Toast.LENGTH_SHORT).show();

                    // 将匹配的用户 ID 添加到当前用户的 personInMatch Set 中
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String currentUserId = currentUser.getUid();

                        // 获取当前用户的 UserProfile
                        db.collection("userProfiles").document(currentUserId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        UserProfile currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                                        if (currentUserProfile != null && person != null) {
                                            // 添加请求者的 ID 到 personInMatch
                                            currentUserProfile.addPersonInMatch(person.getUserId());

                                            // 将更新后的 UserProfile 上传到数据库
                                            db.collection("userProfiles").document(currentUserId)
                                                    .set(currentUserProfile)
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        Log.d("Firestore", "UserProfile updated successfully with new match.");
                                                    })
                                                    .addOnFailureListener(e -> Log.e("Firestore", "Error updating UserProfile", e));
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching current user profile", e));
                    }

                    // 从匹配列表中移除已匹配的用户
                    matchPersonList.remove(person);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating match request status", e));
    }

    /*private void insertSampleData() {
        // example person
        MatchPerson person1 = new MatchPerson("John Doe", "Music", "https://cdn.pixabay.com/photo/2024/03/09/16/59/typewriter-8622984_1280.jpg","1");
        MatchPerson person2 = new MatchPerson("Jane Smith", "Music", "https://cdn.pixabay.com/photo/2024/03/09/16/59/typewriter-8622984_1280.jpg","2");
        MatchPerson person3 = new MatchPerson("Emily Johnson", "Music", "https://cdn.pixabay.com/photo/2024/03/09/16/59/typewriter-8622984_1280.jpg","3");
        // insert to  Firebase Firestore 的 "matchpersons"
        db.collection("matchpersons").document(person1.getMatchPersonName()).set(person1);
        db.collection("matchpersons").document(person2.getMatchPersonName()).set(person2);
        db.collection("matchpersons").document(person3.getMatchPersonName()).set(person3)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Sample data inserted successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error inserting sample data", e);
                });
    }*/



    private int calculateCommonInterests(String interests1, String interests2) {


        // 如果有任何一个为 null 或为空，返回匹配度为 0
        if (interests1 == null || interests1.isEmpty() || interests2 == null || interests2.isEmpty()) {
            return 0;
        }

        // 将兴趣字符串按空格分隔并转换为集合
        Set<String> set1 = new HashSet<>(Arrays.asList(interests1.split(" ")));
        Set<String> set2 = new HashSet<>(Arrays.asList(interests2.split(" ")));

        // 计算交集
        set1.retainAll(set2);
        return set1.size();
    }


}
