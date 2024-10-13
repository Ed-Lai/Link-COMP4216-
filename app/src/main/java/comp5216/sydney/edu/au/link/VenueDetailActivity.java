package comp5216.sydney.edu.au.link;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VenueDetailActivity extends AppCompatActivity {

    private PlacesClient placesClient;
    private String currentUser = "Wizard of Oz";
    private HashMap<String, List<String>> checkinData = new HashMap<>();
    private String currentCheckedInPlace = null;
    private Button checkinButton ;



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

        // Display the photo if available
        ImageView venuePhotoImageView = findViewById(R.id.venuePhotoImageView);
        if (photoMetadata != null) {
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(800)  // Optional: set image size
                    .setMaxHeight(600)
                    .build();

            placesClient = Places.createClient(this);
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                venuePhotoImageView.setImageBitmap(fetchPhotoResponse.getBitmap());
            });
        }

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> {
            finish();
        });


        updateButtonState();

        checkinButton.setOnClickListener(view -> {
            if (currentCheckedInPlace == null) {
                new AlertDialog.Builder(VenueDetailActivity.this)
                        .setTitle("Check In Confirmation")
                        .setMessage("Are you sure you want to check in at: " + placeName + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            addCheckinUser(placeName, currentUser);
                            currentCheckedInPlace = placeName;
                            updateButtonState();
                            Toast.makeText(VenueDetailActivity.this, "Checked in at: " + placeName, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            } else if (currentCheckedInPlace.equals(placeName)) {
                new AlertDialog.Builder(VenueDetailActivity.this)
                        .setTitle("Check Out Confirmation")
                        .setMessage("Are you sure you want to check out from: " + placeName + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            currentCheckedInPlace = null;
                            updateButtonState();
                            checkOutUser(placeName, currentUser);
                            Toast.makeText(VenueDetailActivity.this, "Checked out from: " + placeName, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                Toast.makeText(VenueDetailActivity.this, "Please check out from " + currentCheckedInPlace + " before checking into another place.", Toast.LENGTH_LONG).show();
            }
        });
    }
        private void updateButtonState() {
            if (currentCheckedInPlace == null) {
                checkinButton.setText("Check In");
                checkinButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_green_dark))); // Green color for "Check In"
            } else {
                checkinButton.setText("Check Out");
                checkinButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_red_dark))); // Red color for "Check Out"
            }
        }

    private void addCheckinUser(String placeName, String userName) {
        if (currentCheckedInPlace != null && checkinData.containsKey(placeName)) {
            Toast.makeText(VenueDetailActivity.this, "You are already checked into " + currentCheckedInPlace + ". Please check out before checking into another place.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!checkinData.containsKey(placeName)) {
            checkinData.put(placeName, new ArrayList<>());
        }
        List<String> usersCheckedIn = checkinData.get(placeName);
        if (!usersCheckedIn.contains(userName)) {
            usersCheckedIn.add(userName);
            currentCheckedInPlace = placeName;
            Toast.makeText(VenueDetailActivity.this, userName + " checked in at " + placeName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(VenueDetailActivity.this, "You are already checked into " + placeName, Toast.LENGTH_SHORT).show();
        }

        Log.d("VenueDetailActivity", "Check-in data: " + checkinData);
    }

    private void checkOutUser(String placeName, String userName) {
        if (currentCheckedInPlace == null || !currentCheckedInPlace.equals(placeName)) {
            Toast.makeText(VenueDetailActivity.this, "You are not checked into this place.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkinData.containsKey(placeName)) {
            List<String> usersCheckedIn = checkinData.get(placeName);
            usersCheckedIn.remove(userName);

            if (usersCheckedIn.isEmpty()) {
                checkinData.remove(placeName);
                Toast.makeText(VenueDetailActivity.this, "No users left, removed " + placeName + " from check-in data.", Toast.LENGTH_SHORT).show();
            }
            currentCheckedInPlace = null;
            Toast.makeText(VenueDetailActivity.this, userName + " checked out from " + placeName, Toast.LENGTH_SHORT).show();
        }
        Log.d("VenueDetailActivity", "Check-in data after checkout: " + checkinData);
    }


}