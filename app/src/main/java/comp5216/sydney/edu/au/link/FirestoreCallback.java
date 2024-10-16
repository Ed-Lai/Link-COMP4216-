package comp5216.sydney.edu.au.link;

import java.util.Map;

public interface FirestoreCallback {
    void onCallback(Map<String, Object> data);
}