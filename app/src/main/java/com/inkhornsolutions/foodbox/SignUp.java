package com.inkhornsolutions.foodbox;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class SignUp extends AppCompatActivity {

    private EditText etNumber;
    private MaterialButton btnSignUp;
    private TextInputLayout textInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        etNumber = (EditText) findViewById(R.id.etNumber);
        btnSignUp = (MaterialButton) findViewById(R.id.btnSignUp);
        textInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignUp.this, etNumber.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });


        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignUp.this, "Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}