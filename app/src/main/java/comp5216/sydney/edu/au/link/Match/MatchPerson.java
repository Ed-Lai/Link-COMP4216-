package comp5216.sydney.edu.au.link.Match;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;



@Entity(tableName = "todolist")
public class MatchPerson {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "MatchPersonID")
    private int matchPersonID;

    @ColumnInfo(name = "MatchPersonName")
    private String matchPersonName;

    @ColumnInfo(name = "Hobby")
    private String hobby;

    @ColumnInfo(name = "photo")
    private String photoPath;

    public int getMatchPersonID() {
        return matchPersonID;
    }

    public void setMatchPersonID(int matchPersonID) {
        this.matchPersonID = matchPersonID;
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
