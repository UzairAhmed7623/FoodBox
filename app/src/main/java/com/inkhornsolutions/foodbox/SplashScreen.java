package com.inkhornsolutions.foodbox;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inkhornsolutions.foodbox.Common.Common;

import java.net.HttpURLConnection;
import java.net.URL;

import es.dmoral.toasty.Toasty;

public class SplashScreen extends AppCompatActivity {

    private CircularProgressIndicator progressBar;
    boolean isConnected;
    private AppUpdateManager appUpdateManager;
    private static final int RC_APP_UPDATE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        appUpdateManager = AppUpdateManagerFactory.create(SplashScreen.this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){

                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, SplashScreen.this, RC_APP_UPDATE);
                    }
                    catch (IntentSender.SendIntentException e) {
                        Toasty.error(SplashScreen.this, e.getMessage(), Toasty.LENGTH_LONG).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toasty.error(SplashScreen.this, e.getMessage(), Toasty.LENGTH_LONG).show();
            }
        });

        progressBar = (CircularProgressIndicator) findViewById(R.id.progressBar);

        //internet not connected dialog lagana ha.

        View view = getLayoutInflater().inflate(R.layout.custom_network_dialog, null);
        MaterialButton btnConfirm = (MaterialButton) view.findViewById(R.id.btnConfirm);
        MaterialButton btnCancel = (MaterialButton) view.findViewById(R.id.btnCancel);

//        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//        alertDialog.setTitle("Checking Connection");
//        alertDialog.setMessage("Checking...");
//        alertDialog.setCancelable(false);
//        alertDialog.show();

        progressBar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            public void run() {
                try {

                    URL url = new URL("https://www.google.com.pk/");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(3000);
//                    isConnected = connection.getResponseCode() == HttpURLConnection.HTTP_OK;

                    if (!(connection.getResponseCode() == HttpURLConnection.HTTP_OK)) {
//                    alertDialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
                        builder.setView(view);

                        AlertDialog alertDialog1 = builder.create();
                        alertDialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alertDialog1.setCancelable(false);
                        alertDialog1.show();

                        btnConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }
                        });

                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });
                    }
                    else {
//                    alertDialog.dismiss();

                        FirebaseDatabase.getInstance().getReference("Admin").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String percentage = snapshot.child("percentage").getValue(String.class);
                                    String available = snapshot.child("available").getValue(String.class);

                                    Common.discountAvailable.put("percentage", percentage);
                                    Common.discountAvailable.put("available", available);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toasty.error(SplashScreen.this, error.getMessage(), Toasty.LENGTH_LONG).show();
                            }
                        });


//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                        }
//                    }, 1000);

                        SharedPreferences signUpPreferences = getSharedPreferences("signUp", MODE_PRIVATE);
                        boolean isSignUp = signUpPreferences.getBoolean("registered", false);

                        if (FirebaseAuth.getInstance().getCurrentUser() != null || isSignUp) {
                            Intent intent = new Intent(SplashScreen.this, FirstProfile.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        } else {
                            Intent intent = new Intent(SplashScreen.this, Welcome.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_APP_UPDATE){
            Toasty.warning(SplashScreen.this, "Downloading!", Toasty.LENGTH_SHORT).show();

            if (resultCode != RESULT_OK){
                Toasty.warning(SplashScreen.this, "failed!", Toasty.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
//            @Override
//            public void onSuccess(AppUpdateInfo appUpdateInfo) {
//                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
//                    try {
//                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE,
//                                SplashScreen.this, RC_APP_UPDATE);
//                    }
//                    catch (IntentSender.SendIntentException e) {
//                        Toasty.error(SplashScreen.this, e.getMessage(), Toasty.LENGTH_LONG).show();
//                    }
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(Exception e) {
//                Toasty.error(SplashScreen.this, e.getMessage(), Toasty.LENGTH_LONG).show();
//            }
//        });
    }
}