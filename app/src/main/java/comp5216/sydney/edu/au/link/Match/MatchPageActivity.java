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
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import comp5216.sydney.edu.au.link.AccountPage;
import comp5216.sydney.edu.au.link.MainActivity;
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

    private List<UserProfile> matchedPersons;
    private int currentIndex = 0;

    private ImageButton rightPersonButton;
    private ImageButton leftPersonButton;
    private Button receivedRequestsButton;
    private Button acceptedRequestsButton;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_main);

        // Initialize Firebase Firestore with the current user ID
        db = FirebaseFirestore.getInstance();
        currentUserId = getCurrentUserId();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            //Toast.makeText(this, "UserID Logged in user ID: " + userId, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        matchedPersons = new ArrayList<>();

        imageButton = findViewById(R.id.match_gobackimageButton);
        imageButton.setOnClickListener(v -> {
            // Create an Intent to jump to MatchMainActivity
            Intent intent = new Intent(MatchPageActivity.this, MainActivity.class);
            startActivity(intent);
        });

        receivedRequestsButton = findViewById(R.id.receivedRequestsButton);
        receivedRequestsButton.setOnClickListener(v -> {
            // Create an Intent to jump to MatchMainActivity
            Intent intent = new Intent(MatchPageActivity.this, MatchActivity.class);
            startActivity(intent);
        });

        acceptedRequestsButton = findViewById(R.id.acceptedRequestsButton);
        acceptedRequestsButton.setOnClickListener(v -> {
            // Create an Intent to jump to MatchMainActivity
            Intent intent = new Intent(MatchPageActivity.this, MatchSuccessActivity.class);
            startActivity(intent);
        });
        



        // Initializing UI components
        matchName = findViewById(R.id.match_name);
        matchStart = findViewById(R.id.match_start);
        matchUserPhoto = findViewById(R.id.match_userphoto);
        matchButton = findViewById(R.id.match_matchButton);
        rightPersonButton = findViewById(R.id.rightperson);
        leftPersonButton = findViewById(R.id.leftperson);
        genderText = findViewById(R.id.personGenderContent);
        interestView = findViewById(R.id.interestRecyclerView);
        processMatchRequests();

        // Set matching user information
        loadMatchedUsers();

        // Set the matching button click event
        rightPersonButton.setOnClickListener(v -> showNextPerson());
        leftPersonButton.setOnClickListener(v -> showPreviousPerson());
        matchButton.setOnClickListener(v -> sendMatchRequest());


    }

    @Override
    protected void onStart(){
        super.onStart();
        processMatchRequests();
        loadMatchedUsers();
        setupNavigationButtons();
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

                        // Get the UserProfile of the current user
                        db.collection("userProfiles").document(currentUserId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        UserProfile currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                                        if (currentUserProfile != null) {
                                            // Add requestedId to the personInMatch collection of the current user
                                            if (!currentUserProfile.getPersonInMatch().contains(requestedId)) {
                                                currentUserProfile.addPersonInMatch(requestedId);
                                                Toast.makeText(this, "update personInMatch successfully" + requestedId, Toast.LENGTH_SHORT).show();
                                                // Update the current user's personInMatch to the database
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

        db.collection("matchRequests")
                .whereEqualTo("requesterId",currentUserId)
                .whereEqualTo("status","cancel")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        MatchRequests matchRequest = document.toObject(MatchRequests.class);
                        String requestedId = matchRequest.getRequestedId();

                        // Get the UserProfile of the current user
                        db.collection("userProfiles").document(currentUserId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        UserProfile currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                                        if (currentUserProfile != null) {
                                            // Add requestedId to the personInMatch collection of the current user
                                            if (currentUserProfile.getPersonInMatch().contains(requestedId)) {
                                                currentUserProfile.deletePersonInMatch(requestedId);
                                                Toast.makeText(this, "update personInMatch successfully" + requestedId, Toast.LENGTH_SHORT).show();
                                                // Update the current user's personInMatch to the database
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
                });
        db.collection("matchRequests")
                .whereEqualTo("requestedId",currentUserId)
                .whereEqualTo("status","cancel")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        MatchRequests matchRequest = document.toObject(MatchRequests.class);
                        String requestedId = matchRequest.getRequestedId();

                        // Get the UserProfile of the current user
                        db.collection("userProfiles").document(currentUserId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        UserProfile currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                                        if (currentUserProfile != null) {
                                            // Add requestedId to the personInMatch collection of the current user
                                            if (currentUserProfile.getPersonInMatch().contains(requestedId)) {
                                                currentUserProfile.deletePersonInMatch(requestedId);
                                                Toast.makeText(this, "update personInMatch successfully" + requestedId, Toast.LENGTH_SHORT).show();
                                                // Update the current user's personInMatch to the database
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
                });
    }


    private void loadMatchedUsers() {
        if (currentUserId == null) {
            Toast.makeText(this, "Current user ID is null. Cannot load matched users.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the UserProfile of the current user to get the set of matching users
        db.collection("userProfiles").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile currentUserProfile = documentSnapshot.toObject(UserProfile.class);
                        if (currentUserProfile != null) {
                            ArrayList<String> personInMatchSet = currentUserProfile.getPersonInMatch();
                            ArrayList<String> currentUserInterests = currentUserProfile.getInterests();


                            // Get other users' information
                            db.collection("userProfiles")
                                    .whereNotEqualTo("userId", currentUserId) // 确保 userId 是数据库中的字段名
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        matchedPersons.clear();
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            UserProfile person = document.toObject(UserProfile.class);

                                            // Check if the person already exists in the matching set of the current user
                                            if (personInMatchSet == null || !personInMatchSet.contains(person.getUserId())) {

                                                ArrayList<String> otherUserInterests = person.getInterests();
                                                int commonInterestCount = calculateCommonInterests(currentUserInterests, otherUserInterests);

                                                // 只Only when two people have more than one common interest, the user is added to the match list
                                                if (commonInterestCount >= 0) {
                                                    if(Objects.equals(currentUserProfile.getLocation(), person.getLocation()) && !person.getLocation().equals("Unknown")){
                                                        matchedPersons.add(person);
                                                    }

                                                }
                                            }
                                        }
                                        // Show first user
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

        String matchedUserId = matchedPersons.get(currentIndex).getUserId();

        // Query the matchRequests collection to detect whether there is a matching request
        String documentName = currentUserId + "to" + matchedUserId;
        db.collection("matchRequests")
                .document(documentName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        if ("pending".equalsIgnoreCase(status)) {
                            // If the matching request is "pending", set the button state to "Matching"
                            matchButton.setText("Matching");
                        }else {
                            // Other Status
                            matchButton.setText("Match");
                        }
                    } else {
                        // If there is no matching request document, it means the user is not in the matching state
                        matchButton.setText("Match");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking matching status", e);
                    Toast.makeText(MatchPageActivity.this, "Error checking matching status.", Toast.LENGTH_SHORT).show();
                });
    }

    // Show next user
    private void showNextPerson() {
        if (currentIndex < matchedPersons.size() - 1) {
            currentIndex++;
            showPersonAtIndex(currentIndex);
            checkMatchingStatus();
        } else {
            Toast.makeText(this, "No more matches.", Toast.LENGTH_SHORT).show();
        }
    }

    // Show previous user
    private void showPreviousPerson() {
        if (currentIndex > 0) {
            currentIndex--;
            showPersonAtIndex(currentIndex);
            checkMatchingStatus();
        } else {
            Toast.makeText(this, "This is the first match.", Toast.LENGTH_SHORT).show();
        }
    }

    // Display user information by index
    private void showPersonAtIndex(int index) {
        UserProfile person = matchedPersons.get(index);
        matchedUserId = person.getUserId();
        matchName.setText(person.getName());
        genderText.setText(person.getGender());
        matchUserPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MatchPageActivity.this, UserDetailActivity.class);
            intent.putExtra("userProfile", person);
            startActivity(intent);
        });
        // Loading user images using Glide
        if (person.getProfilePictureUrl() != null && !person.getProfilePictureUrl().isEmpty()) {
            Glide.with(this).load(person.getProfilePictureUrl()).into(matchUserPhoto);
        } else {
            matchUserPhoto.setImageResource(R.drawable.default_image);
        }

        ArrayList<String> interestsList = person.getInterests();
        RecyclerView recyclerView = findViewById(R.id.interestRecyclerView);

        // Create and set up the adapter
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

        MatchRequests matchRequest = new MatchRequests(currentUserId, matchedUserId, "pending");
        String documentName = currentUserId +"to"+matchedUserId;
        System.out.println(documentName);
            db.collection("matchRequests")
                .document(documentName)
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

        // If any of them is null or empty, the return value is 0.
        if (interests1 == null || interests1.isEmpty() || interests2 == null || interests2.isEmpty()) {
            return 0;
        }

        // Separate the interest string by space and convert it into a set
        Set<String> set1 = new HashSet<>(interests1);
        Set<String> set2 = new HashSet<>(interests2);

        // Calculate intersection
        set1.retainAll(set2);
        return set1.size();
    }

    private void setupNavigationButtons() {
        bottomNavigationView = findViewById(R.id.navBar);
        bottomNavigationView.setSelectedItemId(R.id.navigation_matches);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(MatchPageActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(MatchPageActivity.this, AccountPage.class));
                return true;
            } else if (itemId == R.id.navigation_matches) {
                return true;
            }
            return false;
        });
    }


}