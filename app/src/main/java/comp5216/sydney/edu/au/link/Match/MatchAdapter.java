package comp5216.sydney.edu.au.link.Match;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comp5216.sydney.edu.au.link.R;

public class MatchAdapter extends ArrayAdapter<MatchPerson> {
    private List<MatchPerson> matchPersonList;
    private OnPersonDeletedListener listener;
    private String currentUserId;
    // for delete listener
    public MatchAdapter(Context context, List<MatchPerson> items, OnPersonDeletedListener listener,String currentUserId) {
        super(context, 0, items);
        this.matchPersonList = items;
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.match_listview, parent, false);
        }

        MatchPerson item = getItem(position);

        ShapeableImageView photo = convertView.findViewById(R.id.match_matches_userphoto);
        TextView name = convertView.findViewById(R.id.match_name);
        Button match = convertView.findViewById(R.id.match_matchButton);
        Button delete = convertView.findViewById(R.id.match_list_deleteButton);

        // Set name
        name.setText(item.getMatchPersonName());

        // Use Glide to load image
        String photoPath = item.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            Glide.with(getContext()).load(photoPath).into(photo);
        } else {
            photo.setImageResource(R.drawable.default_image);
        }

        match.setOnClickListener(v -> {
            // 设置按钮状态为“Matching”
            match.setText("Matched");

            // 保存匹配请求到Firebase
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> matchRequest = new HashMap<>();
            matchRequest.put("currentUserId", currentUserId);
            matchRequest.put("matchedUserId", item.getMatchPersonName());

            db.collection("matchRequests")
                    .add(matchRequest)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Match request saved successfully.");

                        // 更新UI或其他逻辑
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error saving match request", e);
                    });
        });
        // Set delete button click listener
        delete.setOnClickListener(v -> {
            // delete current
            matchPersonList.remove(position);

            // update data
            notifyDataSetChanged();

            // delete  Firebase data
            if (listener != null) {
                listener.onPersonDeleted(item);
            }
        });

        return convertView;
    }


    // interface used to matchActivity
    public interface OnPersonDeletedListener {
        void onPersonDeleted(MatchPerson person);
    }
}
