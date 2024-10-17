package comp5216.sydney.edu.au.link;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import comp5216.sydney.edu.au.link.landing.LoginActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;


public class AccountPage extends AppCompatActivity {

    private TextView usernameTextView, userFullNameTextView;
    private ImageView userPhotoView;
    private Button logOut;
    private String userName;
    private String fullName;
    private String photoUrl;
    private SharedPreferences userSP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSP = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);

        loadUserProfileFromSharedPreferences();

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
        startActivity(intent);
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
        setContentView(R.layout.account_page);
        usernameTextView = findViewById(R.id.username);
        userFullNameTextView = findViewById(R.id.full_name);
        userPhotoView = findViewById(R.id.profile_image);
        logOut = findViewById(R.id.log_out_button);
        usernameTextView.setText(userName);
        userFullNameTextView.setText(fullName);
        Glide.with(this)
                .load(photoUrl)
                .centerCrop()
                .placeholder(R.drawable.default_profile_picture)
                .into(userPhotoView);
        setupNavigationButtons();
    }

    private void logOut() {
        SharedPreferences.Editor editor = userSP.edit();
        editor.clear();
        editor.apply();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(AccountPage.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserProfileFromSharedPreferences() {
        fullName = userSP.getString("name", "");
        userName = userSP.getString("username", "");
        photoUrl = userSP.getString("photoUrl", "");
        setupUI();

    }

}