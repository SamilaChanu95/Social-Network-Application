 package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

 public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserID;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images"); // storing the profile images here in firebase.

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        CountryName = (EditText) findViewById(R.id.setup_country_name);
        SaveInformationButton = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SaveAccountSetupInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String image = dataSnapshot.child("profileimage").getValue().toString();

                    //we can helping the Picasso library we can display the profile image
                    Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {

             Uri ImageUri = data.getData();//get tha data with original image

             // crop the image
             CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
         }

         if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) { //if press the crop button

             CropImage.ActivityResult result = CropImage.getActivityResult(data);

             if(resultCode == RESULT_OK) {

                 loadingBar.setTitle("Profile Image");
                 loadingBar.setMessage("Please wait, while we are updating your profile image...");
                 loadingBar.show();
                 loadingBar.setCanceledOnTouchOutside(true);

                 Uri resultUri = result.getUri(); // get the last result of cropped image

                 StorageReference filePath = UserProfileImageRef.child(currentUserID + "/" + System.currentTimeMillis() + ".jpg");//set the file path with extention with firebase reference

                 //put tha in to Profile Images folder
                 filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                         if(task.isSuccessful() && task != null) {
                             Toast.makeText(SetupActivity.this, "Profile image stored successfully to Firebase Storage...", Toast.LENGTH_SHORT).show();

                             //then get the url of the object image from the firebase database
                             final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();//get the link with The getDownloadUrl method has been depreceated and then using with ".getMetadata().getReference().getDownloadUrl().toString()"

                             //if task is successful by using the user reference in the firebase database then store the image in to Hashmap profileimage object in the firebase or save the image
                             UsersRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if(task.isComplete()) {
                                         //send the user to setupactivity
                                         Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                         startActivity(selfIntent);

                                         Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase Database successfully...", Toast.LENGTH_SHORT).show();
                                         loadingBar.dismiss();
                                     }
                                     else {
                                         String message = task.getException().getMessage();
                                         Toast.makeText(SetupActivity.this, "Error occured :"  + message, Toast.LENGTH_SHORT).show();
                                         loadingBar.dismiss();
                                     }
                                 }
                             });
                         }
                     }
                 });
             }

             else {
                 //if any error occur then show that message
                 Toast.makeText(this, "Error occured : Image not cropped. Try again...", Toast.LENGTH_SHORT).show();
                 loadingBar.dismiss();
             }

         }

     }

     private void SaveAccountSetupInformation() {

        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if(TextUtils.isEmpty(username)) {
            Toast.makeText(SetupActivity.this, "Please enter username..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullname)) {
            Toast.makeText(SetupActivity.this, "Please enter user full name..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(country)) {
            Toast.makeText(SetupActivity.this, "Please enter user country name..", Toast.LENGTH_SHORT).show();
        }
        else {

            loadingBar.setTitle("Saving information");
            loadingBar.setMessage("Please wait, while we are creating your new account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("country",country);
            userMap.put("status","Hey there i am using Social Network.");
            userMap.put("gender","none");
            userMap.put("dob","none");
            userMap.put("relationshipstatus","none");

            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()) {

                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account is created successfully.", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }
                    else {

                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occured: "+message , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }

    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
