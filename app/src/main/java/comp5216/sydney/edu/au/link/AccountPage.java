package comp5216.sydney.edu.au.link;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AccountPage extends AppCompatActivity {

    private TextView usernameTextView, userFullNameTextView;

    // Register the ActivityResultLauncher for the new API
    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the updated values from the result intent
                    Intent data = result.getData();
                    String updatedName = data.getStringExtra("updatedName");
                    String updatedUsername = data.getStringExtra("updatedUsername");

                    // Update the TextViews in AccountPage
                    userFullNameTextView.setText(updatedName);
                    usernameTextView.setText(updatedUsername);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_page); // Load account_page.xml

        // Initialize TextViews
        usernameTextView = findViewById(R.id.usernameTextView);
        userFullNameTextView = findViewById(R.id.userFullNameTextView);
    }

    // Method linked to the Edit Profile button (android:onClick="editProfile")
    public void editProfile(View view) {
        // Start the EditProfile activity and expect a result
        Intent intent = new Intent(AccountPage.this, EditProfilePage.class);
        // Optionally, pass current data if needed
        intent.putExtra("name", userFullNameTextView.getText().toString());
        intent.putExtra("username", usernameTextView.getText().toString());
        editProfileLauncher.launch(intent);
    }

    // Optional: Method to handle back button
    public void goBack(View view) {
        // Code to go back or close the activity
        finish(); // Closes the activity and returns to the previous screen
    }
}
