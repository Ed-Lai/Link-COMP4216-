package comp5216.sydney.edu.au.link.Match;

import com.google.firebase.firestore.PropertyName;

public class MatchPerson {
    @PropertyName("name")
    private String matchPersonName;

    @PropertyName("hobby")
    private String hobby;

    @PropertyName("photo_url")
    private String photoPath;


    public MatchPerson() {}

    public MatchPerson(String matchPersonName, String hobby, String photoPath) {
        this.matchPersonName = matchPersonName;
        this.hobby = hobby;
        this.photoPath = photoPath;
    }

    public String getMatchPersonName() {
        return matchPersonName;
    }

    public void setMatchPersonName(String matchPersonName) {
        this.matchPersonName = matchPersonName;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
