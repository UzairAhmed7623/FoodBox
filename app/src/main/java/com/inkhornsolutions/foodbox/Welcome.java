package com.inkhornsolutions.foodbox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.podcopic.animationlib.library.AnimationType;
import com.podcopic.animationlib.library.StartSmartAnimation;
import com.tombayley.activitycircularreveal.CircularReveal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Welcome extends AppCompatActivity {

    private TextView tvHello, tvHello2;
    private CardView cardView;
    private MaterialButton btnSignUp, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


//        //code for get KeyHash for facebook login
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.inkhornsolutions.foodbox",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures){
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        }
//        catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        tvHello = (TextView) findViewById(R.id.tvHello);
        tvHello2 = (TextView) findViewById(R.id.tvHello2);
        cardView = (CardView) findViewById(R.id.cardView);
        btnSignUp = (MaterialButton) findViewById(R.id.btnSignUp);
        btnLogin = (MaterialButton) findViewById(R.id.btnLogin);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tvHello.setVisibility(View.VISIBLE);
                tvHello2.setVisibility(View.VISIBLE);

                StartSmartAnimation.startAnimation( tvHello,
                        AnimationType.FadeInRight , 1000 , 0 , true,300);

                StartSmartAnimation.startAnimation( tvHello2,
                        AnimationType.FadeInLeft , 1000 , 0 , true, 300);
            }
        }, 500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cardView.setVisibility(View.VISIBLE);
                btnSignUp.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.VISIBLE);

                StartSmartAnimation.startAnimation( btnSignUp,
                        AnimationType.BounceInUp , 1000 , 0 , true, 300);

                StartSmartAnimation.startAnimation( btnLogin,
                        AnimationType.BounceInUp , 1000 , 0 , true, 300);

                StartSmartAnimation.startAnimation( cardView,
                        AnimationType.BounceInDown , 1000 , 0 , true, 300);
            }
        },2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                StartSmartAnimation.startAnimation( btnSignUp,
                        AnimationType.Shake , 1000 , 0 , true, 300);

                StartSmartAnimation.startAnimation( btnLogin,
                        AnimationType.Shake , 1000 , 0 , true, 300);

            }
        },4000);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();

                Intent intent = new Intent(Welcome.this, SignUp.class);
                intent.putExtra("signUpOrLogIn", "signUp");

                CircularReveal.presentActivity(new CircularReveal.Builder(
                        Welcome.this,
                        btnSignUp,
                        intent,
                        800
                ));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();

                Intent intent = new Intent(Welcome.this, SignUp.class);
                intent.putExtra("signUpOrLogIn", "logIn");

                CircularReveal.presentActivity(new CircularReveal.Builder(Welcome.this,
                        btnLogin,
                        intent,
                        700
                ));
            }
        });
    }
}