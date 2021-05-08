package com.inkhornsolutions.foodbox.UserUtils;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inkhornsolutions.foodbox.models.Token;

public class UserUtils {

    public static void updateToken(Context context, String token) {
        Token tokenModel = new Token(token);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");

		if (FirebaseAuth.getInstance().getCurrentUser() != null){
			tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(tokenModel)
					.addOnSuccessListener(aVoid -> {

						Toast.makeText(context, "Token successfully submitted to database!", Toast.LENGTH_SHORT).show();


					}).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
		}
    }
}
