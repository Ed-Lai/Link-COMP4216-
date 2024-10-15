package comp5216.sydney.edu.au.link.Match;
import com.google.firebase.firestore.PropertyName;

import comp5216.sydney.edu.au.link.UserProfile;

public class MatchRequests {
    private static final int THRESHOLD =5;
    @PropertyName("requesterId")
    private String requesterId;  // 发起请求的用户ID

    @PropertyName("requestedId")
    private String requestedId;  // 被请求的用户ID

    @PropertyName("status")
    private String status;       // 请求状态，例如 "pending", "confirmed"

    private UserProfile userProfile;

    // 无参构造函数，用于 Firestore 反射
    public MatchRequests() {
    }

    // 带参构造函数
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

