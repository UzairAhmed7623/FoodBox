package com.inkhornsolutions.foodbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tombayley.activitycircularreveal.CircularReveal;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class SignUp extends AppCompatActivity {

    private static final int RC_SIGN_IN = 120;
    private static final String TAG = "google";
    private EditText etNumber, etCode;
    private MaterialButton btnSignUp, btnGoogleSignIn, btnFacebookSignIn, backButton;
    private TextInputLayout textInputLayout;
    private CircularReveal mActivityCircularReveal;
    private ConstraintLayout rootLayout;
    private String phoneNumber;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private CallbackManager callbackManager;
    private LoginButton login_button;
    private LoginManager loginManager;
    private String verificationId;
    private ProgressDialog progressDialog;
    private String signUpOrLogIn;
    private TextView tvHello2;
    private TextInputLayout textInputLayout2;

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
        etCode = (EditText) findViewById(R.id.etCode);
        btnSignUp = (MaterialButton) findViewById(R.id.btnSignUp);
        textInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout);
        rootLayout = (ConstraintLayout) findViewById(R.id.rootLayout);
        btnGoogleSignIn = (MaterialButton) findViewById(R.id.btnGoogleSignIn);
        btnFacebookSignIn = (MaterialButton) findViewById(R.id.btnFacebookSignIn);
        backButton = (MaterialButton) findViewById(R.id.backButton);
        tvHello2 = (TextView) findViewById(R.id.tvHello2);
        textInputLayout2 = (TextInputLayout) findViewById(R.id.textInputLayout2);

        signUpOrLogIn = getIntent().getStringExtra("signUpOrLogIn");

        if (signUpOrLogIn.equals("logIn")){
            tvHello2.setText("Log In");
            btnSignUp.setText("Log In");
            textInputLayout2.setHelperText("Please write the received otp code and press log in button.");
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();

        mActivityCircularReveal = new CircularReveal(rootLayout);
        mActivityCircularReveal.onActivityCreate(getIntent());

        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();


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
                        Toasty.warning(SignUp.this, "Cancelled!", Toasty.LENGTH_LONG).show();

//                        Intent intent = new Intent(SignUp.this, FirstProfile.class);
////                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
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
                    sendVerificationCode(phoneNumber);
                    progressDialog = new ProgressDialog(SignUp.this);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
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

                String code = etCode.getText().toString().trim();

                if (code.isEmpty() || code.length() < 6) {

                    etCode.setError("Enter code...");
                    etCode.requestFocus();
                    return;
                }
                progressDialog.dismiss();
                verifyCode(code);

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

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mFirebaseAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(SignUp.this) // Search (for callback binding)
                        .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            progressDialog.dismiss();

            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {

                etCode.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Map<String, Object> ph = new HashMap<>();
                            ph.put("phoneNumber", Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getPhoneNumber());

                            FirebaseFirestore.getInstance().collection("Users").document(mFirebaseAuth.getCurrentUser().getUid())
                                    .set(ph, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Phone", "Phone number has been saved!");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {
                                    Toasty.error(SignUp.this, e.getMessage(), Toasty.LENGTH_LONG).show();
                                }
                            });

                            SharedPreferences signUpPreferences = getSharedPreferences("signUp", MODE_PRIVATE);
                            SharedPreferences.Editor editor = signUpPreferences.edit();
                            editor.putBoolean("registered", true);
                            editor.apply();

                            Intent intent = new Intent(SignUp.this, PhoneVerified.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                        } else {
                            Toasty.error(SignUp.this, task.getException().getMessage(), Toasty.LENGTH_LONG).show();
                        }
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
                    SharedPreferences signUpPreferences = getSharedPreferences("signUp", MODE_PRIVATE);
                    SharedPreferences.Editor editor = signUpPreferences.edit();
                    editor.putBoolean("registered", true);
                    editor.apply();

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

                                SharedPreferences signUpPreferences = getSharedPreferences("signUp", MODE_PRIVATE);
                                SharedPreferences.Editor editor = signUpPreferences.edit();
                                editor.putBoolean("registered", true);
                                editor.apply();

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