package comp5216.sydney.edu.au.link;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import android.view.View;
import androidx.navigation.ui.AppBarConfiguration;
import comp5216.sydney.edu.au.link.databinding.ActivityMainBinding;

// FireStore
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