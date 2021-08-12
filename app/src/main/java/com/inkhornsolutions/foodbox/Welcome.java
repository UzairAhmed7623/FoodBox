package com.inkhornsolutions.foodbox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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

        StartSmartAnimation.startAnimation( tvHello,
                AnimationType.FadeInRight , 2000 , 0 , true,300);

        StartSmartAnimation.startAnimation( tvHello2,
                AnimationType.FadeInLeft , 2000 , 0 , true, 300);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cardView.setVisibility(View.VISIBLE);

                StartSmartAnimation.startAnimation( cardView,
                        AnimationType.RollIn , 2000 , 0 , true, 300);

            }
        },2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnSignUp.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.VISIBLE);

                StartSmartAnimation.startAnimation( btnSignUp,
                        AnimationType.ShakeBand , 2000 , 0 , true, 300);

                StartSmartAnimation.startAnimation( btnLogin,
                        AnimationType.ShakeBand , 2000 , 0 , true, 300);

            }
        },4000);
    }
}