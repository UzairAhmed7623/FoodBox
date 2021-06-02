package com.inkhornsolutions.foodbox;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowItemDetails extends AppCompatActivity {

    private ElegantNumberButton enbNumofOrders;
    private ImageButton backArrow;
    private CircleImageView itemImage;
    private TextView tvItem, tvPrice, tvDescription;
    private Button btnCheckOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_item_details);

        enbNumofOrders = (ElegantNumberButton) findViewById(R.id.enbNumofOrders);
        backArrow = (ImageButton) findViewById(R.id.backArrow);
        itemImage = (CircleImageView) findViewById(R.id.itemImage);
        tvItem = (TextView) findViewById(R.id.tvItem);
        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        btnCheckOut = (Button) findViewById(R.id.btnCheckOut);
        enbNumofOrders = (ElegantNumberButton) findViewById(R.id.enbNumofOrders);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        enbNumofOrders.bringToFront();

        enbNumofOrders.setOnClickListener(new ElegantNumberButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = enbNumofOrders.getNumber();
            }
        });

        enbNumofOrders.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Log.d("TAG", String.format("oldValue: %d   newValue: %d", oldValue, newValue));
            }
        });

    }
}