package com.example.caozhongli.tripbuddy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.w3c.dom.Text;

public class Login extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Button btnSignin;
    private Button btnSignup;
    // private Button btnFSignin;
    private Button btnGSignin;
    private Button btnLogout;
    private TextView btnGBpassword;
    private EditText edtUser;

    private EditText edtPassword;
    private static final int RC_SIGN_IN = 9001;
    //private ProgressDialog progressdialog;
    private GoogleApiClient mGoogleApiClient;
    private LoginManager loginManager;
    private LoginButton loginButton;
    private static final String TAG = "Login2";
    private ProgressDialog mProgressDialog;
    private CallbackManager mCallbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
        loginButton = (LoginButton) findViewById(R.id.btnFSignin);
        btnGBpassword = (TextView) findViewById(R.id.btnGBpassword);
        edtUser = (EditText) findViewById(R.id.edtUser);
        mProgressDialog = new ProgressDialog(this);
        //loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);

                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        //btnFSignin = (Button) findViewById(R.id.btnFSignin);
        btnSignin = (Button) findViewById(R.id.btnSignin);

//        btnGSignin = (Button) findViewById(R.id.btnGSignin);
        btnSignup = (Button) findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(this);
        btnSignin.setOnClickListener(this);
        btnGBpassword.setOnClickListener(this);

        //btnFSignin.setOnClickListener(this);
        findViewById(R.id.btnGSignin).setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    public void signIn(){
        edtUser = (EditText) findViewById(R.id.edtUser);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        String user = edtUser.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        if(user == null || user.length() ==0){
            alert("Please Input your user name");

        }else if(password == null || password.length() ==0){
            alert("Please Input your password");

        }else {
            mProgressDialog.setMessage("Signing in ...");
            mProgressDialog.show();
            mAuth.signInWithEmailAndPassword(user, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                alert("No exiting user or wrong password");
                            } else {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if(user.isEmailVerified()) {
                                    alert("Sign in successfully!");
                                }else{
                                    alert("Please verify your email");
                                }
                            }
                            mProgressDialog.dismiss();
                        }
                    });
        }

    }
    public void signUp(){
        Intent intent = new Intent(getApplicationContext(),Register.class);
        startActivity(intent);
    }
    public void GsignIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        //Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase

                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mProgressDialog.setMessage("Authorizing ...");
        mProgressDialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            alert("Sign in successfully!");
                        }
                        mProgressDialog.dismiss();
                        // ...
                    }
                });
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mProgressDialog.setMessage("Authorizing ...");
        mProgressDialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            alert("Authentication failed.");

                        }else{
                            alert("Sign in successfully!");
                        }
                        mProgressDialog.dismiss();
                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
    }
    public void getbackPassword(){
        String email = edtUser.getText().toString();
        if(email.length()==0){
            alert("Please input your email");
        }else {
            mAuth.sendPasswordResetEmail(email);
        }
    }
    public void FsignIn(){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignin:
                signIn();
                break;
            case R.id.btnSignup:
                signUp();
                break;
            case R.id.btnGSignin:
                GsignIn();
                //break;
                //Toast.makeText(getApplicationContext(),"Hi", Toast.LENGTH_LONG).show();
                break;
            case R.id.btnGBpassword:
                getbackPassword();
                break;

        }
    }
    public void alert(String s) {
        Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
