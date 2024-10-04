package comp5216.sydney.edu.au.link.Match;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
        String currentUserId = getCurrentUserId();
        adapter = new MatchAdapter(this, matchPersonList, this,currentUserId);  // Pass 'this' for delete listener
        listView.setAdapter(adapter);

        // Load data
        loadMatchData();

//        insertSampleData();
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

//    private void saveMatchToFirebase() {
//        String currentUserId = "currentUserId";
//        String matchedUserId = "matchedUserId";
//
//        // create a match Information
//        Map<String, Object> matchInfo = new HashMap<>();
//        matchInfo.put("currentUserId", currentUserId);
//        matchInfo.put("matchedUserId", matchedUserId);
//
//        // Store match Information to  Firestore  "matches" set
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("matches")
//                .add(matchInfo)
//                .addOnSuccessListener(documentReference -> {
//                    Log.d("Firestore", "Match saved successfully.");
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "Error saving match", e);
//                });
//    }
//    private void loadMatchListForUser() {
//        String currentUserId = "currentUserId";
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("matches")
//                .whereEqualTo("matchedUserId", currentUserId)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            // get match users Info
//                            String matchedUserId = document.getString("currentUserId");
//
//                            // update in UI
//                            showMatchedUserInfo(matchedUserId);
//                        }
//                    } else {
//                        Log.e("Firestore", "Error getting documents: ", task.getException());
//                    }
//                });
//    }
//
//    private void showMatchedUserInfo(String matchedUserId) {
//        // show the information of users according to  matchedUserId
//        TextView matchList = findViewById(R.id.match_start);
//        matchList.setText("Matched with user ID: " + matchedUserId);
//    }
//
    private void loadMatchRequestsForUser() {
        String currentUserId = "currentUserId";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("matchRequests")
                .whereEqualTo("requestedId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        matchPersonList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String requestorId = document.getString("requestorId");
                            // obtain user Info from requestorId
                            loadMatchPersonData(requestorId);
                        }
                    } else {
                        Log.e("Firestore", "Error getting match requests: ", task.getException());
                    }
                });
    }

    private void loadMatchPersonData(String requestorId) {
        // get user Info from Firestore and store in the list
        db.collection("matchpersons").document(requestorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        MatchPerson person = documentSnapshot.toObject(MatchPerson.class);
                        matchPersonList.add(person);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting match person data", e);
                });
    }


    private void insertSampleData() {
        // example person
        MatchPerson person1 = new MatchPerson("John Doe", "Soccer", "https://cdn.pixabay.com/photo/2024/03/09/16/59/typewriter-8622984_1280.jpg");
        MatchPerson person2 = new MatchPerson("Jane Smith", "Reading", "https://cdn.pixabay.com/photo/2024/03/09/16/59/typewriter-8622984_1280.jpg");
        MatchPerson person3 = new MatchPerson("Emily Johnson", "Music", "https://cdn.pixabay.com/photo/2024/03/09/16/59/typewriter-8622984_1280.jpg");

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

}
