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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import comp5216.sydney.edu.au.link.R;
import comp5216.sydney.edu.au.link.UserProfile;

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



        adapter = new MatchSuccessActivityAdapter(this, matchPersonList, this);
        listView.setAdapter(adapter);
        loadAllMatchesForCurrentUser();
    }

    private void loadAllMatchesForCurrentUser( ) {
        CollectionReference matchPersonRef = db.collection("matchRequests");

        // check requestedId equal currentUserId and status is  finish
        Task<QuerySnapshot> requestedIdQuery = matchPersonRef.whereEqualTo("requestedId", currentUserId)
                .whereEqualTo("status", "finish")
                .get();

        // check requesterId equal currentUserId and status is  finish
        Task<QuerySnapshot> requesterIdQuery = matchPersonRef.whereEqualTo("requesterId", currentUserId)
                .whereEqualTo("status", "finish")
                .get();
        Set<String> loadedUserIds = new HashSet<>();

        //use Tasks.whenAllSuccess to wait all check finished
        Tasks.whenAllSuccess(requestedIdQuery, requesterIdQuery).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Firestore", "Data fetched successfully");

                matchPersonList.clear();

                // result
                List<Object> queryResults = task.getResult();
                for (Object result : queryResults) {
                    QuerySnapshot querySnapshot = (QuerySnapshot) result;

                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            MatchRequests matchRequest = document.toObject(MatchRequests.class);

                            String requesterId = matchRequest.getRequesterId();
                            String requestedId = matchRequest.getRequestedId();
                            // check and load requesterId
                            if (requesterId != null && !requesterId.equals(currentUserId) && !loadedUserIds.contains(requesterId)) {
                                loadMatchPersonDetails(requesterId);
                                Log.d("Firestore", "Loaded requesterId: " + requesterId);
                                loadedUserIds.add(requesterId);
                            }

                            // check and load requestedId
                            if (requestedId != null && !requestedId.equals(currentUserId) && !loadedUserIds.contains(requestedId)) {
                                loadMatchPersonDetails(requestedId);
                                Log.d("Firestore", "Loaded requestedId: " + requestedId);
                                loadedUserIds.add(requestedId);
                            }
                        }
                    } else {
                        Log.d("Firestore", "No documents found in MatchRequests");
                    }
                }

                adapter.notifyDataSetChanged();
            } else {
                Log.e("Firestore", "Error fetching data", task.getException());
            }
        });
    }
    private void loadMatchPersonDetails(String requesterID) {
        Log.d("Firestore", "Loading user profile for requesterID: " + requesterID);
        db.collection("userProfiles")
                .whereEqualTo("userId", requesterID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("Firestore", "UserProfile query snapshot size: " + querySnapshot.size());
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            UserProfile person = document.toObject(UserProfile.class);
                            Log.d("Firestore", "UserProfile fetched: " + person);
                            matchPersonList.add(person);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("MatchActivity", "No UserProfile document found for requesterId: " + requesterID);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching UserProfile details", e));
    }

    @Override
    public void onDeleteRequest(UserProfile person) {
        String documentName1 = person.getUserId() + "to" + currentUserId;
        String documentName2 = currentUserId + "to" + person.getUserId();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // 首先获取当前用户的 userProfile
            db.collection("userProfiles").document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserProfile currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                            if (currentUserProfile != null && person != null) {
                                // 从 personInMatch 列表中移除此用户
                                currentUserProfile.deletePersonInMatch(person.getUserId());

                                // 更新 userProfiles 的 personInMatch 列表
                                db.collection("userProfiles").document(currentUserId)
                                        .set(currentUserProfile)
                                        .addOnSuccessListener(aVoid1 -> {
                                            Log.d("Firestore", "UserProfile updated successfully, person removed from personInMatch.");

                                            // 检查 documentName1 是否存在
                                            db.collection("matchRequests").document(documentName1)
                                                    .get()
                                                    .addOnSuccessListener(documentSnapshot1 -> {
                                                        if (documentSnapshot1.exists()) {
                                                            // 如果 documentName1 存在，更新 status 为 cancel
                                                            updateMatchRequestStatus(documentName1, person);
                                                        } else {
                                                            // 如果 documentName1 不存在，检查 documentName2
                                                            db.collection("matchRequests").document(documentName2)
                                                                    .get()
                                                                    .addOnSuccessListener(documentSnapshot2 -> {
                                                                        if (documentSnapshot2.exists()) {
                                                                            // 如果 documentName2 存在，更新 status 为 cancel
                                                                            updateMatchRequestStatus(documentName2, person);
                                                                        } else {
                                                                            Log.e("Firestore", "Match request document not found.");
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching match request document2", e));
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching match request document1", e));
                                        })
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error updating UserProfile", e));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching current user profile", e));
        }
    }

    private void updateMatchRequestStatus(String documentName, UserProfile person) {
        // 更新 matchRequests 文档的 status 为 cancel
        db.collection("matchRequests").document(documentName)
                .update("status", "cancel")
                .addOnSuccessListener(aVoid2 -> {
                    // 从本地列表中移除 person 并更新 UI
                    matchPersonList.remove(person);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Match request status updated to 'cancel'", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating match request status", e));
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
