package comp5216.sydney.edu.au.link.Match;

import static java.security.AccessController.getContext;

import android.content.Context;
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
import comp5216.sydney.edu.au.link.model.UserProfile;

public class MatchSuccessActivityAdapter extends ArrayAdapter<UserProfile> {
    private List<UserProfile> matchPersonList;
    private OnDeleteRequestListener deleteRequestListener;

    // Modify constructor to accept OnMatchRequestListener
    public MatchSuccessActivityAdapter(Context context, List<UserProfile> items, OnDeleteRequestListener deleteListener) {
        super(context, 0, items);
        this.matchPersonList = items;
        this.deleteRequestListener = deleteListener;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.match_success_list, parent, false);
        }
        UserProfile item = getItem(position);

        ShapeableImageView photo = convertView.findViewById(R.id.match_success_userphoto);
        TextView name = convertView.findViewById(R.id.match_success_name);
        Button delete = convertView.findViewById(R.id.match_success_deleteButton);

        // Set name
        name.setText(item.getName());

        // Use Glide to load image
        String photoPath = item.getProfilePictureUrl();
        if (photoPath != null && !photoPath.isEmpty()) {
            Glide.with(getContext()).load(photoPath).into(photo);
        } else {
            photo.setImageResource(R.drawable.default_image);
        }

        // Delete button click listener
        delete.setOnClickListener(v -> {
            if (deleteRequestListener != null) {
                deleteRequestListener.onDeleteRequest(item);
            }
        });

        return convertView;
    }

    // Listener interfaces
    public interface OnDeleteRequestListener {
        void onDeleteRequest(UserProfile person);
    }

}
