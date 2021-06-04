package com.inkhornsolutions.foodbox;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Checkout extends AppCompatActivity {

    private FloatingActionButton backArrow;
    private TextView tvChangeAddress, tvUserName, tvAddress, tvPhone, tvTotalPrice;
    private RadioButton rbDoorDelivery;
    private Button btnCheckOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        backArrow = (FloatingActionButton) findViewById(R.id.backArrow);
        tvChangeAddress = (TextView) findViewById(R.id.tvChangeAddress);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        tvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);
        rbDoorDelivery = (RadioButton) findViewById(R.id.rbDoorDelivery);
        btnCheckOut = (Button) findViewById(R.id.btnCheckOut);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


    }
}