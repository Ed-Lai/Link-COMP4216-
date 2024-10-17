package comp5216.sydney.edu.au.link.landing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import comp5216.sydney.edu.au.link.MainActivity;
import comp5216.sydney.edu.au.link.R;


public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private View loginButton;
    private View registerButton;
    private View backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        backButton = findViewById(R.id.back_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (validateLogin(email, password)) {
                    signInWithEmailAndPassword(email, password);

                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }

    private boolean validateLogin(String email, String password) {
        // Check if email is empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if email is in a valid format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if password is empty
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if password length is valid (for example, minimum 6 characters)
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }

        // If all checks passed, return true
        return true;
    }

    private void signInWithEmailAndPassword(String email, String password) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in success, navigate to main activity or dashboard
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            loadUserDataFromFirestore(user.getUid());
                            // Navigate to main activity
                        }
                    } else {
                        // Sign-in failed, display a message to the user
                        Toast.makeText(LoginActivity.this, "Invalid Email/Username or Password, Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserDataFromFirestore(String userId) {

        // Show a loading dialog
        ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Loading user data...");
        progressDialog.setCancelable(false);
        progressDialog.show();  // Show the progress dialog

        // Reference to the specific user document in the "userProfiles" collection

        DocumentReference userRef = FirebaseFirestore.getInstance().collection("userProfiles").document(userId);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                // Dismiss the progress dialog when the task completes


                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get data from the Firestore document
                        String name = document.getString("name");
                        String username = document.getString("username");
                        String gender = document.getString("gender");
                        Long age = document.getLong("age");
                        String ageString = age != null ? age.toString() : "";
                        String location = document.getString("location");
                        String relationshipStatus = document.getString("relationshipStatus");
                        boolean visible = document.getBoolean("visible");
                        String visibleString = visible ? "Yes" : "No";  // Convert boolean to Yes/No string
                        String preference = document.getString("preference");
                        String photoUrl = document.getString("profilePictureUrl");
                        String interests = document.getString("interests");

                        // Save the data to local file for later use
                        putDataInSharedPref(userId, name, username, gender, ageString, location,
                                relationshipStatus, visibleString, preference, photoUrl, interests);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
                progressDialog.dismiss();
            }
        });

    }

    private void putDataInSharedPref(String userId, String name, String username, String gender, String age,
                                     String location, String relationshipStatus, String visible,
                                     String preference, String photoUrl, String interests) {
        // Save data to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Store each field into SharedPreferences
        editor.putString("userId", userId);
        editor.putString("name", name);
        editor.putString("username", username);
        editor.putString("gender", gender);
        editor.putString("age", age);  // Store age as a string
        editor.putString("location", location);
        editor.putString("relationshipStatus", relationshipStatus);
        editor.putBoolean("visible", visible.equals("Yes"));  // Store visible as boolean
        editor.putString("preference", preference);
        editor.putString("photoUrl", photoUrl);
        editor.putString("interests", interests);
        editor.apply();

    }

}
