package comp5216.sydney.edu.au.link.Match;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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

public class MatchActivity extends AppCompatActivity implements MatchAdapter.OnDeleteRequestListener,MatchAdapter.OnMatchRequestListener {
    private FirebaseFirestore db;
    private ListView listView;
    private MatchAdapter adapter;
    private List<MatchPerson> matchPersonList;
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
        currentUserId = "1";


        adapter = new MatchAdapter(this, matchPersonList, this,this);
        listView.setAdapter(adapter);

        // Load data
        //insertSampleData();
        //insertSampleWithInterestAndPreferences();
        loadMRequestData();
        //deleteAllMatchRequests();

    }
    public String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
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



    private void loadMatchPersonDetails(String requesterID) {
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
    }



    // 删除匹配请求
    @Override
    public void onDeleteRequest(MatchPerson person) {
        String documentName = person.getUserID()+"to"+currentUserId;
            db.collection("matchRequests").document(documentName)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        matchPersonList.remove(person);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Match request deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error deleting match request", e));
    }



    @Override
    public void onMatchRequest(MatchPerson person) {
        String documentName = person.getUserID() + "to" + currentUserId;
        db.collection("matchRequests").document(documentName)
                .update("status", "finish")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Match request approved", Toast.LENGTH_SHORT).show();
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

    private void insertSampleWithInterestAndPreferences() {
        // 创建带有兴趣和偏好的示例用户
        String interests1 = "Music Travel";
        String preferences1 = "Running Reading";

        UserProfile person1 = new UserProfile("1", "alice@example.com", "alice123", "Alice Johnson", "Female");
        person1.setInterests(Arrays.asList(interests1.split(" ")));
        person1.setPreferences(Arrays.asList(preferences1.split(" ")));

        String interests2 = "Cooking Photography";
        String preferences2 = "Movies Hiking";

        UserProfile person2 = new UserProfile("user2", "bob@example.com", "bob456", "Bob Smith", "Male");
        person2.setInterests(Arrays.asList(interests2.split(" ")));
        person2.setPreferences(Arrays.asList(preferences2.split(" ")));

        String interests3 = "Gaming Movies";
        String preferences3 = "Concerts Running";

        UserProfile person3 = new UserProfile("user3", "charlie@example.com", "charlie789", "Charlie Brown", "Male");
        person3.setInterests(Arrays.asList(interests3.split(" ")));
        person3.setPreferences(Arrays.asList(preferences3.split(" ")));

        // 将数据插入到Firebase Firestore "matchpersons"
        db.collection("matchpersons").document(person1.getUserId()).set(person1);
        db.collection("matchpersons").document(person2.getUserId()).set(person2);
        db.collection("matchpersons").document(person3.getUserId()).set(person3)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Sample data with interests and preferences inserted successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error inserting sample data with interests and preferences", e);
                });
    }



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
