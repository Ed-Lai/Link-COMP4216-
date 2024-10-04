package comp5216.sydney.edu.au.link.Match;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.link.R;

public class MatchSuccessActivity extends AppCompatActivity {

    private ListView matchSuccessListView;
    private ArrayAdapter<String> matchAdapter;
    private List<String> matchedUserList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_success);

        db = FirebaseFirestore.getInstance();

        // initial
        matchedUserList = new ArrayList<>();
        matchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, matchedUserList);

        matchSuccessListView = findViewById(R.id.match_success_listview);
        matchSuccessListView.setAdapter(matchAdapter);

        // get userID
        String userAId = getIntent().getStringExtra("userAId");
        String userBId = getIntent().getStringExtra("userBId");

        // add matched person information to the list
        matchedUserList.add(userAId);
        matchedUserList.add(userBId);

        // from Firebase load matched person
        loadAllMatchesForCurrentUser(userAId);

        // update UI
        matchAdapter.notifyDataSetChanged();
    }

    private void loadAllMatchesForCurrentUser(String currentUserId) {
        db.collection("matches")
                .whereEqualTo("userAId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String matchedUser = document.getString("userBId");
                            if (!matchedUserList.contains(matchedUser)) {
                                matchedUserList.add(matchedUser);
                            }
                        }
                        matchAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Error getting matches: ", task.getException());
                    }
                });
    }
}
