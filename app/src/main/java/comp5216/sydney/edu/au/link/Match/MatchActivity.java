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

public class MatchActivity extends AppCompatActivity implements MatchAdapter.OnDeleteRequestListener {
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


        adapter = new MatchAdapter(this, matchPersonList, this);
        listView.setAdapter(adapter);

        // Load data
        //insertSampleData();
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
        System.out.println(documentName);
            db.collection("matchRequests").document(documentName)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        matchPersonList.remove(person);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Match request deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error deleting match request", e));
    }


    private void insertSampleData() {
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
    }
    private void deleteAllMatchRequests() {
        db.collection("matchRequests")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // 遍历查询结果，删除每个文档
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        db.collection("matchRequests").document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d("Firestore", "Document with ID " + document.getId() + " deleted successfully.")
                                )
                                .addOnFailureListener(e ->
                                        Log.e("Firestore", "Error deleting document with ID " + document.getId(), e)
                                );
                    }
                    Toast.makeText(this, "All matchRequests deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error fetching matchRequests for deletion", e)
                );
    }

}
