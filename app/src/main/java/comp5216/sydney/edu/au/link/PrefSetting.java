package comp5216.sydney.edu.au.link;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;


import androidx.appcompat.app.AppCompatActivity;



public class PrefSetting extends AppCompatActivity {

    private EditText genderText, ageInput1, ageInput2, locationInput, inputRelationshipStatus, inputInterests;
    private ImageView backButton;
    private View doneButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_setting);  // Load your edit_profile.xml layout

        // Initialize the EditText fields
        genderText = findViewById(R.id.input_gender);
        backButton = findViewById(R.id.back_button);
        ageInput1 = findViewById(R.id.input_age1);
        ageInput2 = findViewById(R.id.input_age2);
        locationInput = findViewById(R.id.input_hometown);
        inputRelationshipStatus = findViewById(R.id.input_relationshipStatus);
        inputInterests = findViewById(R.id.input_interests);
        doneButton = findViewById(R.id.Done_button);

        ageInput1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAgeSelectDialog1();
            }
        });
        ageInput2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAgeSelectDialog2();
            }
        });
        inputRelationshipStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRelationshipStatusDialog(); // Call the method to show the relationship status dialog
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backAndSaveChanges();
            }
        });

        genderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGenderDialog(); // Call the method to show the Yes/No dialog
            }
        });

    }

    private void showAgeSelectDialog1() {
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(PrefSetting.this);
        final NumberPicker numberPicker = new NumberPicker(PrefSetting.this);

        // Set the range for the NumberPicker
        numberPicker.setMinValue(14);
        numberPicker.setMaxValue(100);
        String currentAge = ageInput1.getText().toString();
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
                ageInput1.setText(String.valueOf(numberPicker.getValue()));
            }
        });

        // Set the negative button for the dialog
        builder.setNegativeButton("Cancel", null);

        // Show the dialog
        builder.show();
    }
    private void showAgeSelectDialog2() {
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(PrefSetting.this);
        final NumberPicker numberPicker = new NumberPicker(PrefSetting.this);

        // Set the range for the NumberPicker
        String smallerAge = ageInput1.getText().toString();
        if (!smallerAge.isEmpty()) {
            int age = Integer.parseInt(smallerAge);
            numberPicker.setMinValue(age);
        } else {
            numberPicker.setMinValue(14);
        }

        numberPicker.setMaxValue(100);
        String currentAge = ageInput2.getText().toString();
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
                ageInput2.setText(String.valueOf(numberPicker.getValue()));
            }
        });

        // Set the negative button for the dialog
        builder.setNegativeButton("Cancel", null);

        // Show the dialog
        builder.show();
    }

    private void showRelationshipStatusDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(PrefSetting.this);
        builder.setTitle("Select Relationship Status");

        // Define the relationship status options
        final String[] relationshipStatuses = {
                "Single",
                "Seeking a Relationship",
                "In a Relationship",
                "Married",
                "Divorced",
                "Unknown",
                "No Preference"
        };

        // Keep track of selected options
        final boolean[] checkedItems = new boolean[relationshipStatuses.length];

        builder.setMultiChoiceItems(relationshipStatuses, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // If "No Preference" is selected, deselect all others
                if (which == relationshipStatuses.length - 1) { // "No Preference" is the last option
                    if (isChecked) {
                        // Deselect all other options
                        for (int i = 0; i < checkedItems.length - 1; i++) {
                            checkedItems[i] = false;
                            ((AlertDialog) dialog).getListView().setItemChecked(i, false);
                        }
                    }
                } else {
                    // If any other option is selected, deselect "No Preference"
                    if (isChecked) {
                        checkedItems[relationshipStatuses.length - 1] = false; // Deselect "No Preference"
                        ((AlertDialog) dialog).getListView().setItemChecked(relationshipStatuses.length - 1, false);
                    }
                }
            }
        });

        // Add the positive button to confirm the selection
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuilder selectedStatuses = new StringBuilder();

                // Build a string with selected items
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        if (selectedStatuses.length() > 0) {
                            selectedStatuses.append(", ");
                        }
                        selectedStatuses.append(relationshipStatuses[i]);
                    }
                }

                // If no item is selected, show "No Preference" by default
                if (selectedStatuses.length() == 0) {
                    selectedStatuses.append("No Preference");
                }

                // Set the selected statuses to the EditText
                inputRelationshipStatus.setText(selectedStatuses.toString());
            }
        });

        // Add the cancel button
        builder.setNegativeButton("Cancel", null);

        // Show the dialog
        builder.show();
    }


    private void showGenderDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(PrefSetting.this);
        builder.setTitle("Select Gender");

        // Define the visibility options
        final String[] genderOptions = {"Male", "Female", "Other", "No Preference"};

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

    private void goBack() {
        // Create an AlertDialog to confirm exit
        new AlertDialog.Builder(PrefSetting.this)
                .setTitle("Exit Without Saving")
                .setMessage("Are you sure you want to exit? Any changes will not be saved.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User confirmed exit, return to EditProfilePage
                        Intent intent = new Intent(PrefSetting.this, EditProfilePage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Close current activity
                    }
                })
                .setNegativeButton("No", null) // User canceled, do nothing
                .show();
    }

    private void backAndSaveChanges() {
        // Get the values from the input fields
        String gender = genderText.getText().toString();
        String ageRange = ageInput1.getText().toString() + "-" + ageInput2.getText().toString();
        String relationshipStatus = inputRelationshipStatus.getText().toString();
        String location = locationInput.getText().toString();
        String interests = inputInterests.getText().toString();

        // Concatenate the values with commas
        String fullPreferences = ageRange + ", " + gender + ", " + relationshipStatus + ", " + location + ", " + interests;

        // Create a shortened version with ellipsis after the first three values
        String[] preferencesArray = fullPreferences.split(", ");
        StringBuilder shortenedPreferences = new StringBuilder();

        for (int i = 0; i < preferencesArray.length && i < 3; i++) {
            if (i > 0) shortenedPreferences.append(", ");
            shortenedPreferences.append(preferencesArray[i]);
        }
        if (preferencesArray.length > 3) {
            shortenedPreferences.append(", ...");
        }

        // Create an Intent to return the result to EditProfilePage
        Intent resultIntent = new Intent();
        resultIntent.putExtra("showPreference", shortenedPreferences.toString());
        resultIntent.putExtra("updatedPreference", fullPreferences);

        // Set the result to RESULT_OK and finish the activity
        setResult(RESULT_OK, resultIntent);
        finish(); // Close the PrefSetting activity
    }




}
