package comp5216.sydney.edu.au.link.Match;
import com.google.firebase.firestore.PropertyName;

import comp5216.sydney.edu.au.link.UserProfile;

public class MatchRequests {
    private static final int THRESHOLD =5;
    @PropertyName("requesterId")
    private String requesterId;

    @PropertyName("requestedId")
    private String requestedId;

    @PropertyName("status")
    private String status;

    private UserProfile userProfile;

    public MatchRequests() {
    }

    public MatchRequests(String requesterId, String requestedId, String status) {
        this.requesterId = requesterId;
        this.requestedId = requestedId;
        this.status = status;
        this.userProfile = userProfile;
    }

    // Getters and setters
    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequestedId() {
        return requestedId;
    }

    public void setRequestedId(String requestedId) {
        this.requestedId = requestedId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



}

