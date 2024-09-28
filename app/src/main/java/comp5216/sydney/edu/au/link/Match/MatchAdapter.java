package comp5216.sydney.edu.au.link.Match;

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

public class MatchAdapter extends ArrayAdapter<MatchPerson> {
    private List<MatchPerson> matchPersonList;
    private OnPersonDeletedListener listener;

    // for delete listener
    public MatchAdapter(Context context, List<MatchPerson> items, OnPersonDeletedListener listener) {
        super(context, 0, items);
        this.matchPersonList = items;
        this.listener = listener;
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
