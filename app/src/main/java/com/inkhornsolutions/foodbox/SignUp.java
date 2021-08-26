package com.inkhornsolutions.foodbox;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tombayley.activitycircularreveal.CircularReveal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class SignUp extends AppCompatActivity {

    private static final int RC_SIGN_IN = 120;
    private static final String TAG = "google";
    private EditText etNumber;
    private MaterialButton btnSignUp, btnGoogleSignIn, btnFacebookSignIn;
    private TextInputLayout textInputLayout;
    private CircularReveal mActivityCircularReveal;
    private ConstraintLayout rootLayout;
    private String phoneNumber;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private CallbackManager callbackManager;
    private LoginButton login_button;
    private LoginManager loginManager;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn
                                .getSignedInAccountFromIntent(result.getData());
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                            firebaseAuthWithGoogle(account.getIdToken());

                        } catch (ApiException e) {
                            // Google Sign In failed, update UI appropriately
                            Log.w(TAG, "Google sign in failed", e);
                            Toasty.error(SignUp.this, Objects.requireNonNull(e.getMessage()),
                                    Toasty.LENGTH_LONG).show();
                        }
                    }

                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        etNumber = (EditText) findViewById(R.id.etNumber);
        btnSignUp = (MaterialButton) findViewById(R.id.btnSignUp);
        textInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout);
        rootLayout = (ConstraintLayout) findViewById(R.id.rootLayout);
        btnGoogleSignIn = (MaterialButton) findViewById(R.id.btnGoogleSignIn);
        btnFacebookSignIn = (MaterialButton) findViewById(R.id.btnFacebookSignIn);
//        login_button = (LoginButton) findViewById(R.id.login_button);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mActivityCircularReveal = new CircularReveal(rootLayout);
        mActivityCircularReveal.onActivityCreate(getIntent());

        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            Toasty.info(SignUp.this, "SignedIn", Toasty.LENGTH_SHORT).show();
        } else {
            Toasty.info(SignUp.this, "SignedOut", Toasty.LENGTH_SHORT).show();
        }

        btnFacebookSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginManager.logInWithReadPermissions(SignUp.this, Arrays.asList("email", "public_profile", "user_friends"));

                loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        AuthCredential authCredential = FacebookAuthProvider
                                .getCredential(loginResult.getAccessToken().getToken());
                        mFirebaseAuth.signInWithCredential(authCredential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                                            updateUI(firebaseUser);
                                        } else {
                                            Log.d("facebook", task.getException().getMessage());
                                            Toasty.error(SignUp.this, task.getException().getMessage(), Toasty.LENGTH_LONG).show();
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("facebook", e.getMessage());
                                Toasty.error(SignUp.this, e.getMessage(), Toasty.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onCancel() {
                        Toasty.warning(SignUp.this, "You already signed in.", Toasty.LENGTH_LONG).show();

                        Intent intent = new Intent(SignUp.this, FirstProfile.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("facebook", error.getMessage());
                        Toasty.error(SignUp.this, error.getMessage(), Toasty.LENGTH_LONG).show();
                    }
                });
            }
        });

        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = "+92" + etNumber.getText().toString().trim();
                if (!TextUtils.isEmpty(phoneNumber) && phoneNumber.length() == 13){

                }
                else {
                    Toasty.error(SignUp.this, "Phone number is not correct.",
                            Toast.LENGTH_LONG, true).show();
                }

            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignUp.this, etNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                loginManager.logOut();
                mFirebaseAuth.signOut();
            }
        });

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("438055928659-mv9a4btuqrgkdcp8tl3oat19kntkd4b3.apps.googleusercontent.com")
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(SignUp.this, gso);

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                launcher.launch(signInIntent);
            }
        });
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            HashMap<String, Object> emailAddress = new HashMap<>();
            emailAddress.put("emailAddress", firebaseUser.getEmail());
            FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.getUid()).set(emailAddress, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(SignUp.this, FirstProfile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(SignUp.this, e.getMessage(), Toasty.LENGTH_LONG).show();
                }
            });

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            if (user != null) {

                                Intent intent = new Intent(SignUp.this, FirstProfile.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }

                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toasty.error(SignUp.this, Objects.requireNonNull(task.getException()
                                    .getMessage()), Toasty.LENGTH_LONG).show();

                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mActivityCircularReveal.unRevealActivity(this);
    }
}