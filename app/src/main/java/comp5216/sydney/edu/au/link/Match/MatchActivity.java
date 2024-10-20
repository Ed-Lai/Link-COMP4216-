package comp5216.sydney.edu.au.link.Match;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.link.AccountPage;
import comp5216.sydney.edu.au.link.MainActivity;
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

        currentUserId = getCurrentUserId();


        adapter = new MatchAdapter(this, matchPersonList, this,this);
        listView.setAdapter(adapter);

        loadMRequestData();

    }
    public String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            Intent intent = new Intent(MatchActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return null;
        }
    }
    //Load db about the request
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
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("Firestore", "Error fetching data", task.getException());
                    }
                });
    }


    private void loadMatchPersonDetails(String requesterID) {
        db.collection("userProfiles")
                .whereEqualTo("userId", requesterID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            UserProfile person = document.toObject(UserProfile.class);
                            matchPersonList.add(person);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("MatchActivity", "No UserProfile document found for requesterId: " + requesterID);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching UserProfile details", e));
    }



    // delete the matchRequest in db
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


// match button function
    @Override
    public void onMatchRequest(UserProfile person) {
        String documentName = person.getUserId() + "to" + currentUserId;

        db.collection("matchRequests").document(documentName)
                .update("status", "finish")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Match request approved", Toast.LENGTH_SHORT).show();

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String currentUserId = currentUser.getUid();

                        db.collection("userProfiles").document(currentUserId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        UserProfile currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                                        if (currentUserProfile != null && person != null) {
                                            // add ID that person sent match request into personInMatch
                                            currentUserProfile.addPersonInMatch(person.getUserId());

                                            // update new UserProfile into db
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

                    // Remove matched users from the match list
                    matchPersonList.remove(person);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating match request status", e));
    }



}
