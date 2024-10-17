package comp5216.sydney.edu.au.link;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import comp5216.sydney.edu.au.link.model.UserProfile;


public class EditProfilePage extends AppCompatActivity {

    private EditText nameText, usernameText, genderText, ageInput, locationInput,
            inputRelationshipStatus, inputVisible, inputPreference, inputInterets;
    private ImageView backButton, userPhotoView;
    private Button editPicture;
    private String userId, fullname, username, gender, age, hometown,
            relationshipStatus, preference, photoUrl, interests;
    private String tempImageUrl;
    private boolean isVisible;
    private SharedPreferences userSP;
    private static final int REQUEST_IMAGE_PICK = 1000;
    private static final int REQUEST_STORAGE_PERMISSION = 2000;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSP = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);
        loadUserProfileFromSharedPreferences();
    }

    private void setClickListener() {
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
                new AlertDialog.Builder(EditProfilePage.this)
                        .setTitle("Unsaved Changes")
                        .setMessage("Your changes are not saved. Are you sure you want to exit?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        editPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestStoragePermission();
            }
        });
    }

    private void loadUserProfileFromSharedPreferences() {

        userId = userSP.getString("userId", "");
        fullname = userSP.getString("name", "");
        username = userSP.getString("username", "");
        gender = userSP.getString("gender", "");
        age = userSP.getString("age", "");
        hometown = userSP.getString("location", "");
        relationshipStatus = userSP.getString("relationshipStatus", "");
        isVisible = userSP.getBoolean("visible", true);
        preference = userSP.getString("preference", "");
        photoUrl = userSP.getString("photoUrl", "");
        interests = userSP.getString("interests", "");
        setUI();

    }
    private void setUI() {

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
        editPicture = findViewById(R.id.edit_picture_button);

        setClickListener();

        nameText.setText(fullname);
        usernameText.setText(username);
        genderText.setText(gender);
        String ageString = String.valueOf(age);
        ageInput.setText(ageString);
        locationInput.setText(hometown);
        inputRelationshipStatus.setText(relationshipStatus);
        inputVisible.setText(isVisible ? "Yes" : "No");
        inputPreference.setText(preference);
        inputInterets.setText(interests);
        Glide.with(this)
                .load(photoUrl)
                .centerCrop()
                .placeholder(R.drawable.default_profile_picture)
                .into(userPhotoView);
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
        SharedPreferences.Editor editor = userSP.edit();

        // Get current user's ID (assuming user is logged in)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a map to hold the updated user data
        Map<String, Object> updatedData = new HashMap<>();

        updatedData.put("name", nameText.getText().toString());
        editor.putString("name", nameText.getText().toString());
        updatedData.put("username", usernameText.getText().toString());
        editor.putString("username", usernameText.getText().toString());
        updatedData.put("gender", genderText.getText().toString());
        editor.putString("gender", genderText.getText().toString());
        updatedData.put("age", Integer.parseInt(ageInput.getText().toString()));
        editor.putString("age", ageInput.getText().toString());
        updatedData.put("interests", inputInterets.getText().toString());
        editor.putString("interests", inputInterets.getText().toString());
        updatedData.put("hometown", locationInput.getText().toString());
        editor.putString("hometown", locationInput.getText().toString());
        updatedData.put("relationshipStatus", inputRelationshipStatus.getText().toString());
        editor.putString("relationshipStatus", inputRelationshipStatus.getText().toString());
        updatedData.put("preferences", preference);
        editor.putString("preference", inputPreference.getText().toString());
        updatedData.put("isVisible", inputVisible.getText().toString().equals("Yes"));
        editor.putBoolean("visible", inputVisible.getText().toString().equals("Yes"));
        updatedData.put("profilePictureUrl", tempImageUrl);
        editor.putString("photoUrl", tempImageUrl);
        editor.apply();

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
                    startActivity(resultIntent);
                    finish();  // Close the current activity
                })
                .addOnFailureListener(e -> {
                    // Failed to update the user profile in Firestore
                    progressDialog.dismiss();  // Dismiss the progress dialog
                    Toast.makeText(EditProfilePage.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void requestStoragePermission() {
            openImagePicker();
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            uploadImageToFirebase(selectedImageUri);  // Upload the selected image
        }
    }
    private void uploadImageToFirebase(Uri fileUri) {
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        // Reference to Firebase storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference ref = storageRef.child("UserImages/" + fileUri.getLastPathSegment());
        UploadTask uploadTask = ref.putFile(fileUri);

        // Get the URL once upload completes
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            progressDialog.dismiss();  // Hide the progress dialog
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                tempImageUrl = downloadUri.toString();
                updateImageView(downloadUri);  // Update the image view with the uploaded image
            } else {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateImageView(Uri imageUri) {
        Glide.with(this).load(imageUri).centerCrop().into(userPhotoView);  // Load the image into ImageView
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
                    String showPreference = result.getData().getStringExtra("showPreference");
                    preference = result.getData().getStringExtra("updatedPreference");

                    // Update the inputPreference EditText with the returned value
                    inputPreference.setText(showPreference);
                }
            }
    );









}
