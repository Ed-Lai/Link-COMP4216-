package comp5216.sydney.edu.au.link;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import comp5216.sydney.edu.au.link.landing.LoginActivity;

public class VenueDetailActivity extends AppCompatActivity {

    private PlacesClient placesClient;
    private String currentUser = "Wizard of Oz";
    private HashMap<String, List<String>> checkinData = new HashMap<>();
    private String currentCheckedInPlace = null;
    private Button checkinButton;
    private FirebaseAuth mAuth;
    private FirebaseUser User;
    private String userId;

    // Save checked-in place in SharedPreferences
    private void saveCheckedInPlace(String placeName) {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putString("currentCheckedInPlace", placeName)
                .apply();
    }

    // Retrieve saved checked-in place from SharedPreferences
    private String getSavedCheckedInPlace() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("currentCheckedInPlace", null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail);

        // Get data passed from the MainActivity
        String placeName = getIntent().getStringExtra("placeName");
        String address = getIntent().getStringExtra("address");
        String openingHours = getIntent().getStringExtra("openingHours");
        PhotoMetadata photoMetadata = getIntent().getParcelableExtra("photoMetadata");

        // Set text fields
        TextView venueNameTextView = findViewById(R.id.venueNameTextView);
        TextView addressTextView = findViewById(R.id.addressTextView);
        TextView openingHoursTextView = findViewById(R.id.openingHoursTextView);
        venueNameTextView.setText(placeName);
        addressTextView.setText("Address: " + address);
        openingHoursTextView.setText("Opening Hours:\n" + openingHours);
        checkinButton = findViewById(R.id.CheckinButton);
        Log.d("VenueDetailActivity", "IN GETTING USER");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(VenueDetailActivity.this, "No user is signed in", Toast.LENGTH_SHORT).show();
        } else {
            userId = currentUser.getUid();
            Log.d("VenueDetailActivity", "Current user ID: " + userId);
        }

        // Display the photo if available
        ImageView venuePhotoImageView = findViewById(R.id.venuePhotoImageView);
        if (photoMetadata != null) {
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(800)
                    .setMaxHeight(600)
                    .build();

            placesClient = Places.createClient(this);
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                venuePhotoImageView.setImageBitmap(fetchPhotoResponse.getBitmap());
            });
        }

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        // Retrieve saved check-in state
        currentCheckedInPlace = getSavedCheckedInPlace();
        updateButtonState(placeName);

        // Check-in/out button listener
        checkinButton.setOnClickListener(view -> {
            if (currentCheckedInPlace == null) {
                promptCheckIn(placeName);
            } else if (currentCheckedInPlace.equals(placeName)) {
                promptCheckOut(placeName);
            } else {
                // Display a message to check out from the current place first
                Toast.makeText(VenueDetailActivity.this, "Already checked in at " +
                        currentCheckedInPlace + ". Please check out first.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Prompt for check-in confirmation
    private void promptCheckIn(String placeName) {
        new AlertDialog.Builder(VenueDetailActivity.this)
                .setTitle("Check In Confirmation")
                .setMessage("Are you sure you want to check in at: " + placeName + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    checkIn(placeName);
                    currentCheckedInPlace = placeName;
                    saveCheckedInPlace(placeName);
                    updateButtonState(placeName);
                    Toast.makeText(VenueDetailActivity.this, "Checked in at: " + placeName, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Prompt for check-out confirmation
    private void promptCheckOut(String placeName) {
        new AlertDialog.Builder(VenueDetailActivity.this)
                .setTitle("Check Out Confirmation")
                .setMessage("Are you sure you want to check out from: " + placeName + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    currentCheckedInPlace = null;
                    saveCheckedInPlace(null);  // Clear the saved checked-in place
                    updateButtonState(placeName);
                    checkOut();
                    Toast.makeText(VenueDetailActivity.this, "Checked out from: " + placeName, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Update button state based on check-in status
    private void updateButtonState(String placeName) {
        if (currentCheckedInPlace == null) {
            // Button is enabled for check-in (green)
            checkinButton.setEnabled(true);
            checkinButton.setText("Check In");
            checkinButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_green_dark)));
        } else if (currentCheckedInPlace.equals(placeName)) {
            // Button is enabled for check-out (red)
            checkinButton.setEnabled(true);
            checkinButton.setText("Check Out");
            checkinButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_red_dark)));
        } else {
            // Button is disabled since checked in at a different place (grey)
            checkinButton.setEnabled(false);
            checkinButton.setText("Already Checked In");
            checkinButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.darker_gray)));
        }
    }

    // Add user to the check-in list
    private void checkIn(String placeName) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Update the location in Firestore
            db.collection("userProfiles").document(userId)
                    .update("location", placeName)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Checked in at: " + placeName);
                        Toast.makeText(VenueDetailActivity.this, "Checked in at: " + placeName, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Log.w("Firestore", "Error updating location", e));
        }
    }

    // Remove user from the check-in list
    private void checkOut() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Update the location in Firestore to "Unknown"
            db.collection("userProfiles").document(userId)
                    .update("location", "Unknown")
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Checked out, location set to Unknown");
                        Toast.makeText(VenueDetailActivity.this, "Checked out", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Log.w("Firestore", "Error updating location", e));
        }
    }
}