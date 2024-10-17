package comp5216.sydney.edu.au.link;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import comp5216.sydney.edu.au.link.model.UserProfile;


public class EditProfilePage extends AppCompatActivity {

    private EditText nameText, usernameText, genderText, ageInput, locationInput, inputRelationshipStatus, inputVisible, inputPreference, inputInterets;
    private ImageView backButton, userPhotoView;
    private String userId;
    private UserProfile currentUserProfile;;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);  // Load your edit_profile.xml layout

        // Initialize the EditText fields
        nameText = findViewById(R.id.input_name);
        usernameText = findViewById(R.id.input_username);
        genderText = findViewById(R.id.input_gender);
        backButton = findViewById(R.id.back_button);
        ageInput = findViewById(R.id.input_age);//// Assuming you have a save button in your layout
        locationInput = findViewById(R.id.input_hometown);
        inputRelationshipStatus = findViewById(R.id.input_relationshipStatus);
        inputVisible = findViewById(R.id.input_visible);
        inputPreference = findViewById(R.id.input_preference);
        inputInterets = findViewById(R.id.input_interests);
        userPhotoView = findViewById(R.id.profile_image);

        if (currentUserProfile == null) {
            currentUserProfile = new UserProfile();
        }

        currentUserProfile = getIntent().getParcelableExtra("userProfile");
        userId = currentUserProfile.getUserId();
        setUI();

        locationInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCountryCityDialog(); // Call the method to show the dialog
            }
        });
        ageInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAgeSelectDialog();
            }
        });
        inputRelationshipStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRelationshipStatusDialog(); // Call the method to show the relationship status dialog
            }
        });
        inputVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVisibilityDialog(); // Call the method to show the Yes/No dialog
            }
        });
        inputPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfilePage.this, PrefSetting.class);
                prefSettingLauncher.launch(intent);
            }
        });
        genderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGenderDialog(); // Call the method to show the Yes/No dialog
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to AccountPage
                Intent intent = new Intent(EditProfilePage.this, AccountPage.class);
                intent.putExtra("userProfile", currentUserProfile);
                // Start the AccountPage activity
                startActivity(intent);

                // Optional: Close the current activity
                finish();
            }
        });


    }

    public void backAndSaveChanges(View view) {
        // Show a confirmation dialog before saving
        new AlertDialog.Builder(EditProfilePage.this)
                .setTitle("Save Changes")
                .setMessage("Do you want to save the changes to your profile?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If user confirms, save changes to Firestore
                        saveProfileChanges();
                    }
                })
                .setNegativeButton("No", null)  // Do nothing if user cancels
                .show();

    }

    // Method to save profile changes and send them back to AccountPage
    private void saveProfileChanges() {
        // Create a progress dialog to show saving state
        ProgressDialog progressDialog = new ProgressDialog(EditProfilePage.this);
        progressDialog.setMessage("Saving changes...");
        progressDialog.setCancelable(false);
        progressDialog.show();  // Show the progress dialog

        // Get the text input from EditText fields
        String updatedName = nameText.getText().toString();
        String updatedUsername = usernameText.getText().toString();
        String updatedGender = genderText.getText().toString();
        String updatedAge = ageInput.getText().toString();
        int updatedIntAge = Integer.parseInt(updatedAge);
        String updatedHometown = locationInput.getText().toString();
        String updatedStatus = inputRelationshipStatus.getText().toString();
        String updatedPref = inputPreference.getText().toString();
        String updatedVisible = inputVisible.getText().toString();
        boolean updatedBoolVisible = updatedVisible.equals("Yes");
        String updatedInterests = inputInterets.getText().toString();

        // Get current user's ID (assuming user is logged in)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a map to hold the updated user data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", updatedName);
        updatedData.put("username", updatedUsername);
        updatedData.put("gender", updatedGender);
        updatedData.put("age", updatedIntAge);
        updatedData.put("interests", updatedInterests);
        updatedData.put("hometown", updatedHometown);
        updatedData.put("relationshipStatus", updatedStatus);
        updatedData.put("preferences", updatedPref);
        updatedData.put("isVisible", updatedBoolVisible);

        currentUserProfile.setName(updatedName);
        currentUserProfile.setUsername(updatedUsername);
        currentUserProfile.setGender(updatedGender);
        currentUserProfile.setAge(updatedIntAge);
        currentUserProfile.setInterests(updatedInterests);
        currentUserProfile.setHometown(updatedHometown);
        currentUserProfile.setRelationshipStatus(updatedStatus);
        currentUserProfile.setPreferences(updatedPref);
        currentUserProfile.setVisible(updatedBoolVisible);

        // Reference to the Firestore document
        DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("userProfiles").document(userId);

        // Update the user profile in Firestore
        userDocRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated the user profile in Firestore
                    progressDialog.dismiss();  // Dismiss the progress dialog

                    // Return to AccountPage
                    Toast.makeText(EditProfilePage.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent(EditProfilePage.this, AccountPage.class);
                    resultIntent.putExtra("userProfile", currentUserProfile);
                    startActivity(resultIntent);
                    finish();  // Close the current activity
                })
                .addOnFailureListener(e -> {
                    // Failed to update the user profile in Firestore
                    progressDialog.dismiss();  // Dismiss the progress dialog
                    Toast.makeText(EditProfilePage.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showCountryCityDialog() {
        // First, ask if the user is from Australia
        new AlertDialog.Builder(EditProfilePage.this)
                .setTitle("Are you from Australia?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If yes, show city picker for Australia
                        showAustralianCityPicker();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If no, show country picker only
                        showCountryPicker();
                    }
                })
                .show();
    }

    private void showAustralianCityPicker() {
        // Create an AlertDialog with a custom layout for city selection
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfilePage.this);
        builder.setTitle("Select City");

        // Inflate the custom layout that contains only a Spinner for city selection
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_city_only, null);
        builder.setView(dialogView);

        // Get the city Spinner from the dialog view
        final Spinner citySpinner = dialogView.findViewById(R.id.spinner_city);

        // Define the Australian cities array
        final String[] australianCities = {
                "Adelaide", "Albury", "Alice Springs", "Armidale", "Ballarat", "Bathurst",
                "Bendigo", "Brisbane", "Broken Hill", "Broome", "Bundaberg", "Cairns",
                "Canberra", "Coffs Harbour", "Darwin", "Dubbo", "Geelong", "Geraldton",
                "Gold Coast", "Gosford", "Goulburn", "Hobart", "Kalgoorlie", "Launceston",
                "Lismore", "Mackay", "Maitland", "Melbourne", "Mildura", "Mount Gambier",
                "Newcastle", "Orange", "Perth", "Port Augusta", "Port Hedland", "Rockhampton",
                "Shepparton", "Sunshine Coast", "Sydney", "Tamworth", "Toowoomba",
                "Townsville", "Wagga Wagga", "Wollongong", "Other"
        };


        // Set up the city Spinner
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(EditProfilePage.this, android.R.layout.simple_spinner_item, australianCities);
        citySpinner.setAdapter(cityAdapter);

        // Set the positive button to confirm the selection
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the selected city and set it to the EditText
                String selectedCity = citySpinner.getSelectedItem().toString();
                locationInput.setText(selectedCity + ", Australia");
            }
        });

        // Set the negative button to cancel the dialog
        builder.setNegativeButton("Cancel", null);

        // Show the dialog
        builder.show();
    }

    private void showCountryPicker() {
        // Create an AlertDialog with a custom layout for country selection
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfilePage.this);
        builder.setTitle("Select Country");

        // Inflate the custom layout that contains a Spinner for country selection
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_country_only, null);
        builder.setView(dialogView);

        // Get the country Spinner from the dialog view
        final Spinner countrySpinner = dialogView.findViewById(R.id.spinner_country);

        // Define the country array (excluding Australia)
        final String[] countries = {
                "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda", "Argentina", "Armenia", "Austria",
                "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan",
                "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
                "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Central African Republic", "Chad", "Chile", "China", "Colombia",
                "Comoros", "Congo (Congo-Brazzaville)", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic", "Democratic Republic of the Congo",
                "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea",
                "Estonia", "Eswatini (fmr. Swaziland)", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia", "Georgia",
                "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti",
                "Honduras", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy",
                "Ivory Coast", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos",
                "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Madagascar",
                "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico",
                "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Myanmar (formerly Burma)",
                "Namibia", "Nauru", "Nepal", "Netherlands", "New Zealand", "Nicaragua", "Niger", "Nigeria", "North Korea",
                "North Macedonia", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru",
                "Philippines", "Poland", "Portugal", "Qatar", "Republic of Korea (South Korea)", "Republic of the Congo", "Romania",
                "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa",
                "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone",
                "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Sudan", "Spain", "Sri Lanka",
                "Sudan", "Suriname", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand",
                "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda",
                "Ukraine", "United Arab Emirates", "United Kingdom", "United States of America", "Uruguay", "Uzbekistan",
                "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe", "Other"
        };


        // Set up the country Spinner
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(EditProfilePage.this, android.R.layout.simple_spinner_item, countries);
        countrySpinner.setAdapter(countryAdapter);

        // Set the positive button to confirm the selection
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the selected country and set it to the EditText
                String selectedCountry = countrySpinner.getSelectedItem().toString();
                locationInput.setText(selectedCountry);
            }
        });

        // Set the negative button to cancel the dialog
        builder.setNegativeButton("Cancel", null);

        // Show the dialog
        builder.show();
    }

    private void showAgeSelectDialog() {
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfilePage.this);
        final NumberPicker numberPicker = new NumberPicker(EditProfilePage.this);

        // Set the range for the NumberPicker
        numberPicker.setMinValue(14);
        numberPicker.setMaxValue(100);
        String currentAge = ageInput.getText().toString();
        if (!currentAge.isEmpty()) {
            int age = Integer.parseInt(currentAge);
            numberPicker.setValue(age);
        }


        // Add the NumberPicker to the dialog
        builder.setView(numberPicker);
        builder.setTitle("Select Age");

        // Set the positive button for the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Set the selected age to the EditText
                ageInput.setText(String.valueOf(numberPicker.getValue()));
            }
        });

        // Set the negative button for the dialog
        builder.setNegativeButton("Cancel", null);

        // Show the dialog
        builder.show();
    }

    private void showRelationshipStatusDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfilePage.this);
        builder.setTitle("Select Relationship Status");

        // Define the relationship status options
        final String[] relationshipStatuses = {
                "Single",
                "Seeking a Relationship",
                "In a Relationship",
                "Married",
                "Divorced",
                "Unknown"
        };

        // Set up the dialog to show the options as a list
        builder.setItems(relationshipStatuses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the selected relationship status
                String selectedStatus = relationshipStatuses[which];

                // Set the selected status to the EditText
                inputRelationshipStatus.setText(selectedStatus);
            }
        });

        // Show the dialog
        builder.show();
    }

    private void showVisibilityDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfilePage.this);
        builder.setTitle("Select Visibility");

        // Define the visibility options
        final String[] visibilityOptions = {"Yes", "No"};

        // Set up the dialog to show the options as a list
        builder.setItems(visibilityOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the selected visibility option
                String selectedOption = visibilityOptions[which];

                // Set the selected option to the EditText
                inputVisible.setText(selectedOption);
            }
        });

        // Show the dialog
        builder.show();
    }

    private void showGenderDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfilePage.this);
        builder.setTitle("Select Gender");

        // Define the visibility options
        final String[] genderOptions = {"Male", "Female", "Other"};

        // Set up the dialog to show the options as a list
        builder.setItems(genderOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the selected visibility option
                String selectedOption = genderOptions[which];

                // Set the selected option to the EditText
                genderText.setText(selectedOption);
            }
        });
        // Show the dialog
        builder.show();
    }

    private final ActivityResultLauncher<Intent> prefSettingLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the updated preferences from the result intent
                    String updatedPreference = result.getData().getStringExtra("updatedPreference");

                    // Update the inputPreference EditText with the returned value
                    inputPreference.setText(updatedPreference);
                }
            }
    );



    private void setUI() {

        nameText.setText(currentUserProfile.getName());
        usernameText.setText(currentUserProfile.getUsername());
        genderText.setText(currentUserProfile.getGender());
        String ageString = String.valueOf(currentUserProfile.getAge());
        ageInput.setText(ageString);
        locationInput.setText(currentUserProfile.getHometown());
        inputRelationshipStatus.setText(currentUserProfile.getRelationshipStatus());
        inputVisible.setText(currentUserProfile.isVisible() ? "Yes" : "No");
        inputPreference.setText(currentUserProfile.getPreferences());
        inputInterets.setText(currentUserProfile.getInterests());
        Glide.with(this)
                .load(currentUserProfile.getProfilePictureUrl())
                .placeholder(R.drawable.default_profile_picture)
                .into(userPhotoView);
    }





}
