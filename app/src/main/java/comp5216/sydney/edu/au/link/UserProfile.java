package comp5216.sydney.edu.au.link;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserProfile {

    private String userId;
    private String email;
    private String username;
    private String name;
    private String gender;
    private int age;
    private String profilePictureUrl;
    private String location;
    private List<String> interests;
    private List<String> preferences;
    private boolean isVisible;
    private String relationshipStatus;
    private ArrayList<String> personInMatch;

    private static final String DEFAULT_PROFILE_PICTURE_URL =
            "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y";
    private static final int DEFAULT_AGE = 18;  // Default age
    private static final String DEFAULT_LOCATION = "Unknown";  // Default location
    private static final List<String> DEFAULT_INTERESTS = new ArrayList<>();  // Default empty interests list
    private static final List<String> DEFAULT_PREFERENCES = new ArrayList<>();  // Default empty preferences list
    private static final boolean DEFAULT_VISIBILITY = true;  // By default, user is visible
    private static final String DEFAULT_RELATIONSHIP_STATUS = "Single";  // Default relationship status

    // Default constructor is needed for Firebase/Room and other ORM tools
    public UserProfile() {
        // Required empty constructor
    }

    // Constructor with fields
    public UserProfile(String userId, String email, String username, String name, String gender) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.name = name;
        this.gender = gender;
        this.profilePictureUrl = DEFAULT_PROFILE_PICTURE_URL;
        this.age = DEFAULT_AGE;
        this.location = DEFAULT_LOCATION;
        this.interests = DEFAULT_INTERESTS;
        this.preferences = DEFAULT_PREFERENCES;
        this.isVisible = DEFAULT_VISIBILITY;
        this.relationshipStatus = DEFAULT_RELATIONSHIP_STATUS;
        this.personInMatch = new ArrayList<>();
    }

    // Getters and Setters

    public ArrayList<String> getPersonInMatch(){ return personInMatch;}

    public void addPersonInMatch(String personInMatch){
        this.personInMatch.add(personInMatch);
    }
    public void deletePersonInMatch(String personInMatch){
        this.personInMatch.remove(personInMatch);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public String getRelationshipStatus () {
        return this.relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }

}

