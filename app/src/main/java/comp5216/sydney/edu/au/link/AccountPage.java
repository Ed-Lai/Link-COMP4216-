package comp5216.sydney.edu.au.link;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import comp5216.sydney.edu.au.link.Match.MatchPageActivity;
import comp5216.sydney.edu.au.link.landing.LoginActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;


public class AccountPage extends AppCompatActivity {

    private TextView instagramHandleTextView, userFullNameTextView;
    private ImageView userPhotoView;
    private Button logOut;
    private String userName;
    private String fullName;
    private String photoUrl;
    private SharedPreferences userSP;
    private BottomNavigationView bottomNavigationView;
    private Button edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userSP = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);

        loadUserProfileFromSharedPreferences();

        setupNavigationButtons();

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                startActivity(new Intent(AccountPage.this, EditProfilePage.class));
            }
        });

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
        finish();
    }

    // Optional: Method to handle back button
    public void goBack(View view) {
        finish(); // Closes the activity and returns to the previous screen
    }


    private void setupUI() {
        setContentView(R.layout.account_page);
        userFullNameTextView = findViewById(R.id.name);
        instagramHandleTextView = findViewById(R.id.instagram_handle);
        userPhotoView = findViewById(R.id.avatarImage);
        logOut = findViewById(R.id.logout_button);
        edit = findViewById(R.id.edit_button);
        instagramHandleTextView.setText(userName);
        userFullNameTextView.setText(fullName);
        Glide.with(this)
                .load(photoUrl)
                .centerCrop()
                .placeholder(R.drawable.default_profile_picture)
                .into(userPhotoView);
    }

    private void logOut() {

        new AlertDialog.Builder(AccountPage.this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences.Editor editor = userSP.edit();
                        editor.clear();
                        editor.apply();

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(AccountPage.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void loadUserProfileFromSharedPreferences() {
        fullName = userSP.getString("name", "");
        userName = userSP.getString("username", "");
        photoUrl = userSP.getString("photoUrl", "");
        setupUI();

    }

    private void setupNavigationButtons() {
        bottomNavigationView = findViewById(R.id.navBar);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(AccountPage.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                return true;
            } else if (itemId == R.id.navigation_matches) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_matches);
                startActivity(new Intent(AccountPage.this, MatchPageActivity.class));
                return true;
            }
            return false;
        });
    }

}