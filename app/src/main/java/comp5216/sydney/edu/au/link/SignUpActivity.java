package comp5216.sydney.edu.au.link;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button loginButton;
    private Button createAccountButton;

    private TextView inputEmail;
    private TextView inputUsername;
    private TextView inputPassword;
    private TextView inputName;
    private TextView inputGender;

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

                // Call the function to create a new account
                createNewAccount(email, username, password, name, gender);
            }
        });
    }

    // Logic to create a new account, can be further implemented later
    private void createNewAccount(String email, String username, String password, String name, String gender) {
        // Check for valid input (e.g., proper email format, password length)
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || name.isEmpty() || gender.isEmpty()) {
            // Notify the user to fill out all fields
            return;
        }

        // TODO: Send data to the server or database
        // Once the account is successfully created, you can redirect to the login screen or another page
    }
}
