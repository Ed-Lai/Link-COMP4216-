package comp5216.sydney.edu.au.link.Match;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class InterestsAdapter extends RecyclerView.Adapter<InterestsAdapter.InterestViewHolder> {
    private ArrayList<String> interestsList;

    public InterestsAdapter(ArrayList<String> interestsList) {
        this.interestsList = interestsList;
    }

    @NonNull
    @Override
    public InterestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new InterestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InterestViewHolder holder, int position) {
        holder.interestTextView.setText(interestsList.get(position));
        holder.interestTextView.setTextColor(Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return interestsList.size();
    }

    public static class InterestViewHolder extends RecyclerView.ViewHolder {
        TextView interestTextView;

        public InterestViewHolder(@NonNull View itemView) {
            super(itemView);
            interestTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
