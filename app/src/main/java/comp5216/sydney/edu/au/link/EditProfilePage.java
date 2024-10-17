package comp5216.sydney.edu.au.link;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfilePage extends AppCompatActivity {

    private EditText nameText, usernameText, genderText;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);  // Load your edit_profile.xml layout

        // Initialize the EditText fields
        nameText = findViewById(R.id.nameText);
        usernameText = findViewById(R.id.usernameText);
        genderText = findViewById(R.id.genderText);
        backButton = findViewById(R.id.back_button);  // Assuming you have a save button in your layout

        // Optional: Retrieve any existing data passed from AccountPage (if you want to show existing data)
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String username = intent.getStringExtra("username");
        String gender = intent.getStringExtra("gender");

        // Populate the EditText fields with existing data if available
        nameText.setText(name);
        usernameText.setText(username);
        genderText.setText(gender);

//        // Set up the save button click listener
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                saveProfileChanges();
//            }
//        });
    }

    public void backAndSaveChanges(View view) {
        saveProfileChanges();
    }

    // Method to save profile changes and send them back to AccountPage
    private void saveProfileChanges() {
        // Get the text input from EditText fields
        String updatedName = nameText.getText().toString();
        String updatedUsername = usernameText.getText().toString();
        String updatedGender = genderText.getText().toString();

        // Create an intent to send the data back to AccountPage
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedName", updatedName);
        resultIntent.putExtra("updatedUsername", updatedUsername);
        resultIntent.putExtra("updatedGender", updatedGender);

        // Set the result and finish the activity
        setResult(RESULT_OK, resultIntent);
        finish();  // Closes EditProfilePage and returns to AccountPage
    }
}
