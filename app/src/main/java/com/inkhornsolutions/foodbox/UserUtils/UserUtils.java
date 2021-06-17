package com.inkhornsolutions.foodbox.UserUtils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inkhornsolutions.foodbox.Remote.IFCMService;
import com.inkhornsolutions.foodbox.Remote.RetrofitFCMClient;
import com.inkhornsolutions.foodbox.models.FCMSendData;
import com.inkhornsolutions.foodbox.models.Token;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

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

	public static void sendNewOrderNotificationToKitchen(View view, Context context, String key) {
		CompositeDisposable compositeDisposable = new CompositeDisposable();
		IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

		FirebaseDatabase.getInstance()
				.getReference("Tokens")
				.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				if (snapshot.exists()){
					Token tokenModel = snapshot.getValue(Token.class);

					Map<String, String> notificationdata = new HashMap<>();
					notificationdata.put("title", "NewOrder");
					notificationdata.put("body", "Congrats! You have new order. Tap to see order details.");

					FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationdata);
					compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
							.subscribeOn(Schedulers.newThread())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(fcmResponse -> {
								if (fcmResponse.getSuccess() == 0){
									compositeDisposable.clear();
									Snackbar.make(view, "Order message send failed!", Snackbar.LENGTH_LONG).show();
								}
								else {
									Snackbar.make(view, "Order placed!", Snackbar.LENGTH_LONG).show();
								}

							}, throwable -> {
								compositeDisposable.clear();
								Snackbar.make(view, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
							}));
				}
				else {
					compositeDisposable.clear();
					Snackbar.make(view, "Token not found!", Snackbar.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				compositeDisposable.clear();
				Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
			}
		});
	}


}
