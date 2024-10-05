package comp5216.sydney.edu.au.link;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import android.view.View;
import android.widget.Toast;

import androidx.navigation.ui.AppBarConfiguration;
import comp5216.sydney.edu.au.link.databinding.ActivityMainBinding;
import comp5216.sydney.edu.au.link.landing.LoginActivity;

// FireStore
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;



public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFirestore();
        testDatabase();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is logged in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // User is not logged in, redirect to the LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();  // Close the MainActivity to prevent the user from returning to it
        } else {
            // User is logged in, continue with showing the main content
            Toast.makeText(this, "Welcome back, " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
        }
    }


    private void initFirestore() {
        firestore = FirebaseFirestore.getInstance();

    }

    private void testDatabase() {
        HashMap<String, Object> testData = new HashMap<>();
        testData.put("testKey", "testValue");

        CollectionReference testCollection = firestore.collection("testCollection");
        testCollection.add(testData);
    }
}