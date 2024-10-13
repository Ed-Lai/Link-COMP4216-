package comp5216.sydney.edu.au.link;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.VenueViewHolder> {

    private List<Place> placeList;

    public VenueAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public VenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.venue_item, parent, false);
        return new VenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.venueName.setText(place.getName());
        holder.venueAddress.setText(place.getAddress());
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class VenueViewHolder extends RecyclerView.ViewHolder {

        TextView venueName;
        TextView venueAddress;

        public VenueViewHolder(@NonNull View itemView) {
            super(itemView);
            venueName = itemView.findViewById(R.id.venueName);
            venueAddress = itemView.findViewById(R.id.venueAddress);
        }
    }
}