package comp5216.sydney.edu.au.link.Match;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;
import comp5216.sydney.edu.au.link.R;
import comp5216.sydney.edu.au.link.UserProfile;

public class MatchAdapter extends ArrayAdapter<UserProfile> {
    private List<UserProfile> matchPersonList;
    private OnDeleteRequestListener deleteRequestListener;
    private OnMatchRequestListener matchRequestListener;

    // Modify constructor to accept OnMatchRequestListener
    public MatchAdapter(Context context, List<UserProfile> items, OnDeleteRequestListener deleteListener, OnMatchRequestListener matchListener) {
        super(context, 0, items);
        this.matchPersonList = items;
        this.deleteRequestListener = deleteListener;
        this.matchRequestListener = matchListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.match_listview, parent, false);
        }
        UserProfile item = getItem(position);

        ShapeableImageView photo = convertView.findViewById(R.id.match_matches_userphoto);
        TextView name = convertView.findViewById(R.id.match_name);
        Button match = convertView.findViewById(R.id.match_list_matchButton);
        Button delete = convertView.findViewById(R.id.match_list_deleteButton);

        // Set name
        name.setText(item.getName());

        // Use Glide to load image
        String photoPath = item.getProfilePictureUrl();
        if (photoPath != null && !photoPath.isEmpty()) {
            Glide.with(getContext()).load(photoPath).into(photo);
        } else {
            photo.setImageResource(R.drawable.default_image);
        }

        // Match button click listener
        match.setOnClickListener(v -> {
            if (matchRequestListener != null) {
                matchRequestListener.onMatchRequest(item);
            }
        });

        // Delete button click listener
        delete.setOnClickListener(v -> {
            if (deleteRequestListener != null) {
                deleteRequestListener.onDeleteRequest(item);
            }
        });

        photo.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UserDetailActivity.class);
            intent.putExtra("userProfile", item);

            // Use context to start the activity
            getContext().startActivity(intent);
        });

        return convertView;
    }

    // Listener interfaces
    public interface OnDeleteRequestListener {
        void onDeleteRequest(UserProfile person);
    }

    public interface OnMatchRequestListener {
        void onMatchRequest(UserProfile person);
    }
}
