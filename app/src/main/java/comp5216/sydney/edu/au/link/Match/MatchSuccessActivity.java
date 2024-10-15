package comp5216.sydney.edu.au.link.Match;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.link.R;
import comp5216.sydney.edu.au.link.model.UserProfile;

public class MatchSuccessActivity extends AppCompatActivity implements MatchSuccessActivityAdapter.OnDeleteRequestListener{

    private FirebaseFirestore db;
    private ListView listView;
    private MatchSuccessActivityAdapter adapter;
    private List<UserProfile> matchPersonList;
    private ImageButton imageButton;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_success);

        db = FirebaseFirestore.getInstance();
        matchPersonList = new ArrayList<>();


        listView = findViewById(R.id.match_success_listview);
        imageButton = findViewById(R.id.match_success_gobackimageButton);
        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MatchSuccessActivity.this, MatchActivity.class);
            startActivity(intent);
        });

        currentUserId = getCurrentUserId();
        //currentUserId = "1";



        adapter = new MatchSuccessActivityAdapter(this, matchPersonList, this);
        listView.setAdapter(adapter);
        loadAllMatchesForCurrentUser();
    }

    private void loadAllMatchesForCurrentUser( ) {
        CollectionReference matchPersonRef = db.collection("matchRequests");

        // 查询 requestedId 为 currentUserId 且状态为 finish 的文档
        Task<QuerySnapshot> requestedIdQuery = matchPersonRef.whereEqualTo("requestedId", currentUserId)
                .whereEqualTo("status", "finish")
                .get();

        // 查询 requesterId 为 currentUserId 且状态为 finish 的文档
        Task<QuerySnapshot> requesterIdQuery = matchPersonRef.whereEqualTo("requesterId", currentUserId)
                .whereEqualTo("status", "finish")
                .get();

        // 使用 Tasks.whenAllSuccess 来等待所有查询完成
        Tasks.whenAllSuccess(requestedIdQuery, requesterIdQuery).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Firestore", "Data fetched successfully");

                matchPersonList.clear();  // 清除之前的数据

                // 获取查询结果
                List<Object> queryResults = task.getResult();
                for (Object result : queryResults) {
                    QuerySnapshot querySnapshot = (QuerySnapshot) result;  // 将 Object 转换为 QuerySnapshot

                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            MatchRequests matchRequest = document.toObject(MatchRequests.class);

                            // 无论是 requestedId 还是 requesterId，加载相关用户详情
                            if (matchRequest.getRequestedId() != null) {
                                loadMatchPersonDetails(matchRequest.getRequesterId());
                                Log.d("Firestore", "requesterId"+matchRequest.getRequesterId());
                            }
                            if (matchRequest.getRequesterId() != null) {
                                loadMatchPersonDetails(matchRequest.getRequestedId());
                                Log.d("Firestore", "requestedId"+matchRequest.getRequestedId());
                            }
                        }
                    } else {
                        Log.d("Firestore", "No documents found in MatchRequests");
                    }
                }

                // 确保所有数据加载完成后再刷新适配器
                adapter.notifyDataSetChanged();
            } else {
                Log.e("Firestore", "Error fetching data", task.getException());
            }
        });
    }
    private void loadMatchPersonDetails(String requesterID) {
        Log.d("Firestore", "Loading user profile for requesterID: " + requesterID); // 添加日志
        db.collection("userProfiles")
                .whereEqualTo("userId", requesterID) // 假设 "userId" 是字段名
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("Firestore", "UserProfile query snapshot size: " + querySnapshot.size()); // 检查返回的文档数量
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            UserProfile person = document.toObject(UserProfile.class);
                            Log.d("Firestore", "UserProfile fetched: " + person); // 打印获取到的用户数据
                            matchPersonList.add(person); // 添加到显示列表
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("MatchActivity", "No UserProfile document found for requesterId: " + requesterID);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching UserProfile details", e));
    }

    @Override
    public void onDeleteRequest(UserProfile person) {
        String documentName = person.getUserId()+"to"+currentUserId;
        db.collection("matchRequests").document(documentName)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    matchPersonList.remove(person);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Match request deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting match request", e));
    }
    public String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
