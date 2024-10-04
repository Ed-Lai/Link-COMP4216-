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
    private OnDeleteRequestListener deleteRequestListener;



    // for delete listener
    public MatchAdapter(Context context, List<MatchPerson> items, OnDeleteRequestListener listener) {
        super(context, 0, items);
        this.matchPersonList = items;
        this.deleteRequestListener = listener;
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


//        match.setOnClickListener(v -> {
//            // 设置按钮状态为“Matching”
//            match.setText("Matched");
//            // 这里可以添加更多匹配的逻辑，比如回调到 Activity 更新匹配状态
//        });

        // Set delete button click listener
        delete.setOnClickListener(v -> {
            if (deleteRequestListener != null) {
                deleteRequestListener.onDeleteRequest(item);
            }
        });



        return convertView;
    }

// MatchAdapter 中


    public interface OnDeleteRequestListener {
        void onDeleteRequest(MatchPerson person);
    }

}
