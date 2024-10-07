package comp5216.sydney.edu.au.link.Match;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public MatchRequests(String requesterId, String requestedId, String status, UserProfile userProfile) {
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

    public List<MatchPerson> findMatchingUsers(List<MatchPerson> allUsers, MatchPerson currentUser) {
        List<MatchPerson> matchedUsers = new ArrayList<>();

        for (MatchPerson user : allUsers) {
            if (!user.getUserID().equals(currentUser.getUserID())) { // 避免与自己匹配
                int commonInterestCount = calculateCommonInterests(currentUser.getInterest(), user.getInterest());
                if (commonInterestCount > THRESHOLD) { // 定义一个阈值用于匹配
                    matchedUsers.add(user);
                }
            }
        }

        return matchedUsers;
    }

    private int calculateCommonInterests(String interests1, String interests2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(interests1.split(" ")));
        Set<String> set2 = new HashSet<>(Arrays.asList(interests2.split(" ")));
        set1.retainAll(set2);
        return set1.size();
    }

}

