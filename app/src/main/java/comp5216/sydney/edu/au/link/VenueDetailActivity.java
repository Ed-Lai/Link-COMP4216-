package comp5216.sydney.edu.au.link;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

public class VenueDetailActivity extends AppCompatActivity {

    private PlacesClient placesClient;

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

        // Add a back button listener to go back to the previous activity
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> {
            // Simply finish this activity and go back
            finish();
        });
    }
}