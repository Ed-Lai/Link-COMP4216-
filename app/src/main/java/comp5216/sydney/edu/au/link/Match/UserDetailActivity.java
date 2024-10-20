package comp5216.sydney.edu.au.link.Match;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.List;

import comp5216.sydney.edu.au.link.R;
import comp5216.sydney.edu.au.link.UserProfile;

public class UserDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_user_detail);

        // Get the UserProfile object passed in the Intent
        Intent intent = getIntent();
        UserProfile userProfile = (UserProfile) intent.getSerializableExtra("userProfile");

        String instagramHandle = intent.getStringExtra("instagram_handle");
        String phone = intent.getStringExtra("phone");

        // Find UI elements in the layout
        ImageView profileImage = findViewById(R.id.profile_image);
        TextView textName = findViewById(R.id.text_name);
        TextView textAge = findViewById(R.id.text_age);
        TextView textInterests = findViewById(R.id.text_interests);
        TextView textGender = findViewById(R.id.text_gender);
        TextView textHometown = findViewById(R.id.text_hometown);
        TextView textRelationshipStatus = findViewById(R.id.text_relationship_status);
        ImageButton backButton = findViewById(R.id.back_button);
        TextView textInstagramHandle = findViewById(R.id.instagram_handle);
        TextView textPhone = findViewById(R.id.phone_number);

        // Set up the back button to finish the activity and return to the previous page
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // End the current activity and return to the previous one
            }
        });

        // Setting the content of a UI element
        if (userProfile != null) {
            textName.setText(userProfile.getName());
            textAge.setText(String.valueOf(userProfile.getAge()));
            List<String> interestsList = userProfile.getInterests();
            if (interestsList != null && !interestsList.isEmpty()) {
                String interests = android.text.TextUtils.join(" ", interestsList);  // 将兴趣用空格连接起来
                textInterests.setText(interests);
            } else {
                textInterests.setText("No interests available");
            }
            textGender.setText(userProfile.getGender());
            textHometown.setText(userProfile.getLocation());
            textRelationshipStatus.setText(userProfile.getRelationshipStatus());
            textInstagramHandle.setText(instagramHandle);
            textPhone.setText(phone);

            // Load the user's avatar
            if (userProfile.getProfilePictureUrl() != null && !userProfile.getProfilePictureUrl().isEmpty()) {
                Glide.with(this).load(userProfile.getProfilePictureUrl()).into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.default_profile_picture);
            }
        }
    }
}
