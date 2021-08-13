package com.inkhornsolutions.foodbox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.podcopic.animationlib.library.AnimationType;
import com.podcopic.animationlib.library.StartSmartAnimation;

public class Welcome extends AppCompatActivity {

    private TextView tvHello, tvHello2;
    private CardView cardView;
    private MaterialButton btnSignUp, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        tvHello = (TextView) findViewById(R.id.tvHello);
        tvHello2 = (TextView) findViewById(R.id.tvHello2);
        cardView = (CardView) findViewById(R.id.cardView);
        btnSignUp = (MaterialButton) findViewById(R.id.btnSignUp);
        btnLogin = (MaterialButton) findViewById(R.id.btnLogin);

        tvHello.setVisibility(View.VISIBLE);
        tvHello2.setVisibility(View.VISIBLE);

        StartSmartAnimation.startAnimation( tvHello,
                AnimationType.FadeInRight , 1000 , 0 , true,300);

        StartSmartAnimation.startAnimation( tvHello2,
                AnimationType.FadeInLeft , 1000 , 0 , true, 300);

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
                Intent intent = new Intent(Welcome.this, SignUp.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }
}