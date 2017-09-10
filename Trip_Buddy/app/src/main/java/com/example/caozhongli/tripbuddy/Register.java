package com.example.caozhongli.tripbuddy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import static android.graphics.Color.RED;

public class Register extends AppCompatActivity {
    private EditText edtEmail;
    private EditText edtUserName;
    private EditText edtPassword1;
    private EditText edtPassword2;
    private FirebaseAuth mAuth;
    private TextView txtUser;
    private TextView txtEmail;
    private TextView txtPassword1;
    private TextView txtPassword2;
    private ProgressDialog progressdilog;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "Login2";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        progressdilog = new ProgressDialog(this);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword1 = (EditText) findViewById(R.id.edtPassword);
        edtPassword2 = (EditText) findViewById(R.id.edtPassword2);
        edtUserName = (EditText) findViewById(R.id.edtUser);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtPassword1 = (TextView) findViewById(R.id.txtPassword1);
        txtPassword2 = (TextView) findViewById(R.id.txtPassword2);
        txtUser = (TextView) findViewById(R.id.txtUser);
        mAuth = FirebaseAuth.getInstance();
    }

    public void signUp(){
        String email = edtEmail.getText().toString();
        String userName = edtUserName.getText().toString();
        String password = edtPassword1.getText().toString();
        String con_password = edtPassword2.getText().toString();
        Boolean status = true;
        if(email == null || email.length() == 0){
            txtEmail.setTextColor(RED);
            status = false;
        }
        if(userName == null || userName.length() == 0){
            txtUser.setTextColor(RED);
            status = false;
        }
        if(password == null || password.length() == 0){
            txtPassword1.setTextColor(RED);
            status = false;
        }
        if(con_password == null || con_password.length() == 0){
            txtPassword2.setTextColor(RED);
            status = false;
        }

        if(!status){
            alert("Please complete the red part of the form");
        }else if(con_password!=null&&!con_password.equals(password)){
            alert("Password must be the same");
        }else{
            progressdilog.setMessage("We are registering you ...");
            progressdilog.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (task.isSuccessful()) {
                                alert("Sign up successfully!");
                                String username = edtUserName.getText().toString();
                                FirebaseUser user = mAuth.getCurrentUser();
                                user.sendEmailVerification();
                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username).build();

                                user.updateProfile(profile)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "User profile updated.");
                                                }
                                            }
                                        });
                                progressdilog.dismiss();
                            }else{
                                alert("Sign Up failed");
                                progressdilog.dismiss();
                            }

                            // ...
                        }
                    });
        }

    }
    public void Cancel(){
        Intent intent = new Intent(getApplicationContext(), Login.class);
        finish();
        startActivity(intent);
    }
    public void onClickCancel(View v){
        Cancel();
    }
    public void onClickConfirm(View v){
        signUp();
    }


    public void alert(String s) {
        Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
