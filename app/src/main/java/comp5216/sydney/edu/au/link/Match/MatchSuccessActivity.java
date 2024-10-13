package comp5216.sydney.edu.au.link.Match;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.link.R;
import comp5216.sydney.edu.au.link.model.UserProfile;

public class MatchSuccessActivity extends AppCompatActivity implements MatchSuccessActivityAdapter.OnDeleteRequestListener{

    private FirebaseFirestore db;
    private ListView listView;
    private MatchSuccessActivityAdapter adapter;
    private List<UserProfile> matchPersonList;
    private ImageButton imageButton;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_success);

        db = FirebaseFirestore.getInstance();
        matchPersonList = new ArrayList<>();


        listView = findViewById(R.id.match_success_listview);
        imageButton = findViewById(R.id.match_success_gobackimageButton);
        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MatchSuccessActivity.this, MatchActivity.class);
            startActivity(intent);
        });

        currentUserId = getCurrentUserId();
        //currentUserId = "1";



        adapter = new MatchSuccessActivityAdapter(this, matchPersonList, this);
        listView.setAdapter(adapter);
        loadAllMatchesForCurrentUser();
    }

    private void loadAllMatchesForCurrentUser( ) {
        CollectionReference matchPersonRef = db.collection("matchRequests");

        matchPersonRef.whereEqualTo("requestedId", currentUserId)
                .whereEqualTo("status", "finish")
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
        db.collection("userProfiles")
                .whereEqualTo("userID", requesterID) // 假设 "userId" 是字段名
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            UserProfile person = document.toObject(UserProfile.class);
                            matchPersonList.add(person); // 添加到显示列表
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("MatchActivity", "No MatchPerson document found for requesterId: " + requesterID);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching MatchPerson details", e));
    }
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
    public String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
