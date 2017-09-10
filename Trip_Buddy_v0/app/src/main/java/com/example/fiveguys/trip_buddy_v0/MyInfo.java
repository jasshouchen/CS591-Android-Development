package com.example.fiveguys.trip_buddy_v0;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.R.attr.path;

public class MyInfo extends AppCompatActivity implements View.OnClickListener{
    private String username;
    private String email;
    private String usersex;
    private String userage;
    private String userfav;
    private String uid;
    private StorageReference mStorageRef;
    private String photoUrl;
    private EditText edtUserName;
    private EditText edtUserSex;
    private EditText edtUserFav;
    private EditText edtUserAge;
    private ImageView UserImage;
    private FirebaseUser user;
    private Button btnDelet;
    private TextView txtEdit;
    private ImageView iv_photo;
    private Bitmap head;
    private static String path = "/sdcard/myHead/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        /*
        get user's information from firebase database
         */
        mStorageRef = FirebaseStorage.getInstance().getReference();
        if (user != null) {
            username = user.getDisplayName();
            email = user.getEmail();
            uid = user.getUid();
            if(user.getPhotoUrl()!=null) {
                photoUrl = user.getPhotoUrl().toString();
            }else{
                photoUrl = "";
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            DatabaseReference Users = myRef.child("users");

            Users.child(uid).child("sex").addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //alert("1");
                    if(snapshot.exists()) {

                        usersex = snapshot.getValue(String.class);  //prints "Do you have data? You'll love Firebase."
                        edtUserSex.setText(usersex);

                    }else{
                        usersex = "";
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
            Users.child(uid).child("age").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //alert("1");
                    if(snapshot.exists()) {
                        userage = snapshot.getValue(String.class);  //prints "Do you have data? You'll love Firebase."
                        edtUserAge.setText(userage);
                    }else{
                        userage = "";
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
            Users.child(uid).child("favorite").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //alert("1");
                    if(snapshot.exists()) {
                        userfav = snapshot.getValue(String.class);  //prints "Do you have data? You'll love Firebase."
                        edtUserFav.setText(userfav);
                    }else{
                        userfav = "";
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_my_info);
        edtUserName = (EditText) findViewById(R.id.edtUserName);
        edtUserAge = (EditText) findViewById(R.id.edtUserAge);
        edtUserSex = (EditText) findViewById(R.id.editUserSex);
        edtUserFav = (EditText) findViewById(R.id.edtUserFav);
        UserImage = (ImageView) findViewById(R.id.UserImag);
        txtEdit = (TextView) findViewById(R.id.txtEdit);
        edtUserFav.setEnabled(false);
        edtUserSex.setEnabled(false);
        edtUserAge.setEnabled(false);
        edtUserName.setEnabled(false);
        if(username!=null){
            edtUserName.setText(username);
        }
        if(photoUrl!=null&&photoUrl.length()>0){
            Picasso.with(getApplicationContext()).load(photoUrl.toString()).into(UserImage);
        }

        txtEdit.setOnClickListener(this);
        UserImage.setOnClickListener(this);

    }
    /*
    The following code is for change the user's information
     */

    public void updateProfile(){
        String option = txtEdit.getText().toString();
        if(option.equals("Update your profile")){
            String sex = edtUserSex.getText().toString();
            String username = edtUserName.getText().toString();
            String age = edtUserAge.getText().toString();
            String favorite = edtUserFav.getText().toString();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            DatabaseReference Users = myRef.child("users");
            Users.child(uid).child("name").setValue(username);
            Users.child(uid).child("sex").setValue(sex);
            Users.child(uid).child("age").setValue(age);
            Users.child(uid).child("favorite").setValue(favorite);
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username).build();
            user.updateProfile(profile);
            edtUserName.setBackground(getDrawable(R.color.colorPrimary));
            edtUserFav.setBackground(getDrawable(R.color.colorPrimary));
            edtUserSex.setBackground(getDrawable(R.color.colorPrimary));
            edtUserAge.setBackground(getDrawable(R.color.colorPrimary));
            edtUserName.setEnabled(false);
            edtUserFav.setEnabled(false);
            edtUserSex.setEnabled(false);
            edtUserAge.setEnabled(false);
            txtEdit.setText("edit your profile");
        }else{
            edtUserName.setBackground(getDrawable(R.drawable.roundedttext));
            edtUserFav.setBackground(getDrawable(R.drawable.roundedttext));
            edtUserSex.setBackground(getDrawable(R.drawable.roundedttext));
            edtUserAge.setBackground(getDrawable(R.drawable.roundedttext));
            edtUserName.setEnabled(true);
            edtUserFav.setEnabled(true);
            edtUserSex.setEnabled(true);
            edtUserAge.setEnabled(true);
            txtEdit.setText("Update your profile");
        }
    }
    /*
    The following code is for change the user's profile photo
     */
    public void showTypeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(this, R.layout.photo_selection, null);
        TextView select_Album = (TextView) view.findViewById(R.id.txtFromAlbum);
        TextView select_camera = (TextView) view.findViewById(R.id.txtOpenCamera);
        select_Album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent1, 1);
                dialog.dismiss();
            }
        });
        select_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent2.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "head.jpg")));
                startActivityForResult(intent2, 2);
                dialog.dismiss();
            }
        });
        dialog.setView(view);
        dialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());
                }

                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    File temp = new File(Environment.getExternalStorageDirectory() + "/head.jpg");
                    cropPhoto(Uri.fromFile(temp));
                }

                break;
            case 3:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    head = extras.getParcelable("data");
                    if (head != null) {
                        UserImage.setImageBitmap(head);
                        try {
                            FileOutputStream fos = openFileOutput("Profile.png", Context.MODE_PRIVATE);
                            head.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            String mypath = MyInfo.this.getFilesDir().getAbsolutePath()+"/Profile.png";
                            Uri file = Uri.fromFile(new File(mypath));
                            StorageReference profilesRef = mStorageRef.child("images/Profile.png");

                            profilesRef.putFile(file)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // Get a URL to the uploaded content


                                            @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                                    .setPhotoUri(downloadUrl).build();
                                            user.updateProfile(profile);

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle unsuccessful uploads
                                            // ...
                                            alert("failed");
                                        }
                                    });
                            fos.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//                        setPicToView(head);
//                        iv_photo.setImageBitmap(head);
                    }
                }
                break;
            default:
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     *
     *
     * @param uri
     */
    /*
    the following code is for crop the photo we take or we choose from file
     */
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }
    

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtEdit:
                updateProfile();
                break;
            case R.id.UserImag:
                showTypeDialog();
                break;
        }
    }
    public void alert(String s) {
        Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
