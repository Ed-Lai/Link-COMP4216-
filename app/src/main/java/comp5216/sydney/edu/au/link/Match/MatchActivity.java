package comp5216.sydney.edu.au.link.Match;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.link.R;

public class MatchActivity extends AppCompatActivity implements MatchAdapter.OnPersonDeletedListener {

    private FirebaseFirestore db;
    private ListView listView;
    private MatchAdapter adapter;
    private List<MatchPerson> matchPersonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_matches);

        listView = findViewById(R.id.match_listView);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize data
        matchPersonList = new ArrayList<>();
        adapter = new MatchAdapter(this, matchPersonList, this);  // Pass 'this' for delete listener
        listView.setAdapter(adapter);

        // Load data
        loadMatchData();

//        insertSampleData();
    }

    private void loadMatchData() {
        CollectionReference matchPersonRef = db.collection("matchpersons");

        matchPersonRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                matchPersonList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MatchPerson person = document.toObject(MatchPerson.class);
                    matchPersonList.add(person);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onPersonDeleted(MatchPerson person) {
        // Handle deletion of MatchPerson from Firestore or local list
        db.collection("matchpersons").document(person.getMatchPersonName())
                .delete()
                .addOnSuccessListener(aVoid -> {

                    Log.d("FirestoreDelete", "Successfully deleted document: " + person.getMatchPersonName());
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDelete", "Error deleting document: " + person.getMatchPersonName(), e);
                });
    }
//    Button matchButton = findViewById(R.id.match_matchButton);
//    matchButton.setOnClickListener(v -> {
//        // 改变按钮的文字为 "Matching"
//        matchButton.setText("Matching");
//
//        // 将匹配信息存储到 Firebase
//        saveMatchToFirebase();
//    });

    private void saveMatchToFirebase() {
        // 假设当前用户和匹配的用户都有唯一的ID
        String currentUserId = "currentUserId";
        String matchedUserId = "matchedUserId";  // 匹配的用户的ID

        // 创建一个匹配信息
        Map<String, Object> matchInfo = new HashMap<>();
        matchInfo.put("currentUserId", currentUserId);
        matchInfo.put("matchedUserId", matchedUserId);

        // 将匹配信息保存到 Firestore 的 "matches" 集合
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("matches")
                .add(matchInfo)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Match saved successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving match", e);
                });
    }
    private void loadMatchListForUser() {
        String currentUserId = "currentUserId";  // 当前用户的ID

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("matches")
                .whereEqualTo("matchedUserId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 获取匹配用户信息
                            String matchedUserId = document.getString("currentUserId");

                            // 在UI上显示匹配信息
                            showMatchedUserInfo(matchedUserId);
                        }
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void showMatchedUserInfo(String matchedUserId) {
        // 根据 matchedUserId 显示匹配用户的信息
        TextView matchList = findViewById(R.id.match_start);
        matchList.setText("Matched with user ID: " + matchedUserId);
    }

    private void insertSampleData() {
        // example person
        MatchPerson person1 = new MatchPerson("John Doe", "Soccer", "https://cdn.pixabay.com/photo/2024/03/09/16/59/typewriter-8622984_1280.jpg");
        MatchPerson person2 = new MatchPerson("Jane Smith", "Reading", "https://cdn.pixabay.com/photo/2024/03/09/16/59/typewriter-8622984_1280.jpg");
        MatchPerson person3 = new MatchPerson("Emily Johnson", "Music", "https://cdn.pixabay.com/photo/2024/03/09/16/59/typewriter-8622984_1280.jpg");

        // insert to  Firebase Firestore 的 "matchpersons" 集合
        db.collection("matchpersons").document(person1.getMatchPersonName()).set(person1);
        db.collection("matchpersons").document(person2.getMatchPersonName()).set(person2);
        db.collection("matchpersons").document(person3.getMatchPersonName()).set(person3)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Sample data inserted successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error inserting sample data", e);
                });
    }

}
