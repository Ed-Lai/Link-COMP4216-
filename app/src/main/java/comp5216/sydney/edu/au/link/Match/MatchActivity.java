package comp5216.sydney.edu.au.link.Match;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.link.R;

public class MatchActivity extends AppCompatActivity implements MatchAdapter.OnPersonDeletedListener {

    private FirebaseFirestore db;
    private ListView listView;
    private MatchAdapter adapter;
    private List<MatchPerson> matchPersonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_matches);

        listView = findViewById(R.id.match_listView);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize data
        matchPersonList = new ArrayList<>();
        adapter = new MatchAdapter(this, matchPersonList, this);  // Pass 'this' for delete listener
        listView.setAdapter(adapter);

        // Load data
        loadMatchData();
    }

    private void loadMatchData() {
        CollectionReference matchPersonRef = db.collection("matchpersons");

        matchPersonRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                matchPersonList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MatchPerson person = document.toObject(MatchPerson.class);
                    matchPersonList.add(person);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onPersonDeleted(MatchPerson person) {
        // Handle deletion of MatchPerson from Firestore or local list
        db.collection("matchpersons").document(person.getMatchPersonName())
                .delete()
                .addOnSuccessListener(aVoid -> {

                    Log.d("FirestoreDelete", "Successfully deleted document: " + person.getMatchPersonName());
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDelete", "Error deleting document: " + person.getMatchPersonName(), e);
                });
    }
}
