package comp5216.sydney.edu.au.link.Match;

import com.google.firebase.firestore.PropertyName;

import comp5216.sydney.edu.au.link.UserProfile;

public class MatchPerson {
    @PropertyName("name")
    private String matchPersonName;

    @PropertyName("interest")
    private String interest;

    @PropertyName("preference")
    private String preference;

    @PropertyName("photo_url")
    private String photoPath;

    @PropertyName("UserID")
    private String userID;

    @PropertyName("matchRequestId")
    private String matchRequestId;
    public MatchPerson() {}

    private UserProfile userProfile;

    public MatchPerson(String matchPersonName, String interest, String photoPath,String userID, String preference) {
        this.matchPersonName = matchPersonName;
        this.interest = interest;
        this.photoPath = photoPath;
        this.userID = userID;
        this.preference = preference;
    }
    public String getUserID() {
        return userID;
    }

    public String getMatchRequestId() {
        return matchRequestId;
    }

    public void setMatchRequestId(String matchRequestId) {
        this.matchRequestId = matchRequestId;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMatchPersonName() {
        return matchPersonName;
    }
    public void setMatchPersonName(String matchPersonName) {
        this.matchPersonName = matchPersonName;
    }

    public String getPreference(){return preference;}
    public void setPreference(){this.preference = preference;}

    public String getInterest() {
        return interest;
    }
    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getPhotoPath() {
        return photoPath;
    }
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
