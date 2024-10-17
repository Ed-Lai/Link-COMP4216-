package comp5216.sydney.edu.au.link;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.VenueViewHolder> {

    private List<Place> placeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Place place);
    }


    public VenueAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public VenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.venue_recycler_item, parent, false);
        return new VenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.venueName.setText(place.getName());
        holder.venueAddress.setText(place.getAddress());
        Log.d("test", holder.venueAddress.getText().toString());
        holder.venueIcon.setImageResource(R.drawable.location_icon);

        holder.itemView.setOnClickListener(v -> {
            // Trigger the click listener and pass the clicked place
            if (listener != null) {
                listener.onItemClick(place);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class VenueViewHolder extends RecyclerView.ViewHolder {

        TextView venueName;
        TextView venueAddress;
        ImageView venueIcon;

        public VenueViewHolder(@NonNull View itemView) {
            super(itemView);
            venueName = itemView.findViewById(R.id.venueName);
            venueAddress = itemView.findViewById(R.id.venueAddress);
            venueIcon = itemView.findViewById(R.id.venueIcon);
        }
    }

    public void filterList(List<Place> filteredVenues) {
        this.placeList = filteredVenues;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}