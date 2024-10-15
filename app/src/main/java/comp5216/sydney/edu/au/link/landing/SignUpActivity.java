package comp5216.sydney.edu.au.link.landing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import comp5216.sydney.edu.au.link.R;
import comp5216.sydney.edu.au.link.UserProfile;

public class SignUpActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button loginButton;
    private Button createAccountButton;

    private TextView inputEmail;
    private TextView inputUsername;
    private TextView inputPassword;
    private TextView inputName;
    private TextView inputGender;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);  // Use the sign-up layout file

        // Initialize views
        backButton = findViewById(R.id.back_button);
        loginButton = findViewById(R.id.register_button);
        createAccountButton = findViewById(R.id.create_account_button);

        inputEmail = findViewById(R.id.input_email);
        inputUsername = findViewById(R.id.input_username);
        inputPassword = findViewById(R.id.input_password);
        inputName = findViewById(R.id.input_name);
        inputGender = findViewById(R.id.input_gender);

        db = FirebaseFirestore.getInstance();

        // Back button click event
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the previous screen
                finish();  // Or start another activity if needed
            }
        });

        // Login button click event
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the login page
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);  // Assuming MainActivity is the login screen
                startActivity(intent);
            }
        });

        // Create account button click event
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle account creation logic, such as validating inputs and submitting data
                String email = inputEmail.getText().toString();
                String username = inputUsername.getText().toString();
                String password = inputPassword.getText().toString();
                String name = inputName.getText().toString();
                String gender = inputGender.getText().toString();

                if (isSignUpValid(email, username, password, name, gender)) {
                    // Call the function to create a new account
                    createNewAccount(email, username, password, name, gender);
                } else {
                    // Show a message to the user explaining what went wrong
                }

            }
        });
    }

    public boolean isSignUpValid(String email, String username, String password, String name, String gender) {

        // Check if any field is empty
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || name.isEmpty() || gender.isEmpty()) {
            // Notify the user to fill out all fields
            return false;
        }

        // Check if email format is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Notify the user about invalid email format
            return false;
        }

        // Check if password length is at least 6 characters
        if (password.length() < 6) {
            // Notify the user that the password should be at least 6 characters
            return false;
        }

        // Optionally, add more rules for username, name, or gender if needed
        if (username.length() < 3) {
            // Notify the user that the username should be at least 3 characters
            return false;
        }

        if (!gender.equalsIgnoreCase("Male") && !gender.equalsIgnoreCase("Female")) {
            // Notify the user that the gender should be Male or Female
            return false;
        }

        // If all checks passed, return true
        return true;
    }


    // Logic to create a new account, can be further implemented later
    private void createNewAccount(String email, String username, String password, String name, String gender) {
        // Use Firebase Authentication to create a new user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the current user's ID
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // Create a UserProfile object with the user's details
                        UserProfile userProfile = new UserProfile(userId, email, username, name, gender);
                        CollectionReference userProfiles = FirebaseFirestore.getInstance().collection("userProfiles");

                        // Save the user profile to Firestore, using userId as the document ID
                        userProfiles.document(userId).set(userProfile)
                                .addOnSuccessListener(aVoid -> {
                                    // Successfully saved the user profile, redirect to the login screen or another page
                                    Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();

                                    // Example: Redirect to login screen
                                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();  // Optional: Close the current activity
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure when saving the user profile to Firestore
                                    Toast.makeText(SignUpActivity.this, "Failed to save user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Handle failure when creating the user account
                        Toast.makeText(SignUpActivity.this, "Failed to create account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
