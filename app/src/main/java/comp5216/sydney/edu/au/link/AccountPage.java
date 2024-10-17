package comp5216.sydney.edu.au.link;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import comp5216.sydney.edu.au.link.databinding.AccountPageBinding;
import comp5216.sydney.edu.au.link.landing.LoginActivity;
import comp5216.sydney.edu.au.link.model.UserProfile;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

public class AccountPage extends AppCompatActivity {
    private UserProfile currentUserProfile;

    private TextView usernameTextView, userFullNameTextView;
    private ImageView userPhotoView;
    private Button logOut;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_page);
        if (currentUserProfile == null) {
            currentUserProfile = new UserProfile();
        }
        currentUserProfile = getIntent().getParcelableExtra("userProfile");
        // Initialize TextViews
        usernameTextView = findViewById(R.id.username);
        userFullNameTextView = findViewById(R.id.full_name);
        userPhotoView = findViewById(R.id.profile_image);
        logOut = findViewById(R.id.log_out_button);
        setupNavigationButtons();
        if (currentUserProfile != null) {
            // Pass the userProfile object to setupUI method
            setupUI();
        } else {
            Log.e("AccountPage", "UserProfile is null!");
        }

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();  // Call the logOut method when the button is clicked
            }
        });
    }

    // Method linked to the Edit Profile button (android:onClick="editProfile")
    public void editProfile(View view) {
        // Start the EditProfile activity and expect a result
        Intent intent = new Intent(AccountPage.this, EditProfilePage.class);
        intent.putExtra("userProfile", currentUserProfile);
        startActivity(intent);
        finish();
    }

    // Optional: Method to handle back button
    public void goBack(View view) {

        finish(); // Closes the activity and returns to the previous screen
    }


    private void setupNavigationButtons() {
        // Home button click event
        ImageView homeButton = findViewById(R.id.nav_home);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Notification button click event
        ImageView notificationButton = findViewById(R.id.nav_notification);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                //                startActivity(intent);
            }
        });

        // Profile button click event
        ImageView profileButton = findViewById(R.id.nav_profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void setupUI() {
        usernameTextView.setText(currentUserProfile.getUsername());
        userFullNameTextView.setText(currentUserProfile.getName());
        Glide.with(this)
                .load(currentUserProfile.getProfilePictureUrl())
                .placeholder(R.drawable.default_profile_picture)
                .into(userPhotoView);
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(AccountPage.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}