package com.inkhornsolutions.foodbox.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.inkhornsolutions.foodbox.R;
import com.inkhornsolutions.foodbox.models.HistoryModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<HistoryModelClass> history = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private float avgRating;;

    public HistoryAdapter(Context context, List<HistoryModelClass> history) {
        this.context = context;
        this.history = history;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_history_adapter_layout, parent,false);
        return new HistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        HistoryModelClass historyModelClass = history.get(position);

        String resId = historyModelClass.getResId();
        String resName = historyModelClass.getResName();
        String totalPrice = historyModelClass.getTotalPrice();
        String status = historyModelClass.getStatus();
        String date = historyModelClass.getDate();
        String userRating = historyModelClass.getUserRating();

        holder.tvResNameHistory.setText(resName);
        holder.tvGrandTotalHistory.setText("Price: " + totalPrice);
        holder.tvDateHistory.setText("Date: " + date);
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.completed)).into(holder.ivStatusHistory);

        if (userRating != null){
            holder.btnRateAndReview.setVisibility(View.GONE);
            holder.tvShowRating.setVisibility(View.VISIBLE);
            holder.tvShowRating.setText("Your rating " + historyModelClass.getUserRating() + " / " + "5.0");
        }

        if (status.equals("Completed") && userRating == null){
            holder.btnRateAndReview.setVisibility(View.VISIBLE);
        }

        boolean isExpanded = history.get(position).isExpanded();

        holder.expandableLayoutHistory.setVisibility(isExpanded ? View.VISIBLE: View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyModelClass.setExpanded(!historyModelClass.isExpanded());
                notifyItemChanged(holder.getAbsoluteAdapterPosition());
            }
        });

        ArrayList<HistoryModelClass> arrayListMember = new ArrayList<>();

        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                .collection("Cart").document(resId)
                .collection("Orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot1 : task.getResult()){

                        String id = documentSnapshot1.getId();
                        String itemName = documentSnapshot1.getString("title");
                        String price = documentSnapshot1.getString("price");
                        String itemCount = documentSnapshot1.getString("items_count");
                        String finalPrice = documentSnapshot1.getString("final_price");
                        String pId = documentSnapshot1.getString("pId");

                        HistoryModelClass historyModelClass = new HistoryModelClass();

                        historyModelClass.setId(id);
                        historyModelClass.setItemName(itemName);
                        historyModelClass.setPrice(price);
                        historyModelClass.setItems_Count(itemCount);
                        historyModelClass.setFinalPrice(finalPrice);
                        historyModelClass.setpId(pId);

                        Log.d("asdfgh2", ""+id+itemName+price+itemCount+finalPrice+pId);

                        arrayListMember.add(historyModelClass);
                        holder.rvMemberHistory.setAdapter(new MemberHistoryAdapter(arrayListMember));

                    }
                }
            }
        });

        holder.btnRateAndReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder popDialog = new AlertDialog.Builder(context);
                final RatingBar rating = new RatingBar(context);
                rating.setNumStars(5);
                rating.setStepSize(0.1f);
                rating.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                LinearLayout parent = new LinearLayout(context);
                parent.setGravity(Gravity.CENTER);
                parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                parent.addView(rating);

                // popDialog.setIcon(android.R.drawable.btn_star_big_on);
                popDialog.setTitle("Please give us rating.");
                popDialog.setView(parent);

                // Button OK
                popDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Log.d("rating", "" + rating.getRating());

                        holder.btnRateAndReview.setVisibility(View.GONE);
                        holder.tvShowRating.setVisibility(View.VISIBLE);
                        holder.tvShowRating.setText("Your rating " + rating.getRating() + " / " + "5.0");

                        HashMap<String,Object> userRating = new HashMap<>();
                        userRating.put("userRating", String.valueOf(rating.getRating()));

                        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                                .collection("Cart").document(resId)
                                .set(userRating, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("userRating" , String.valueOf(rating.getRating()));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("userRating" , "Rated!");
                            }
                        });

                        firebaseFirestore.collection("Restaurants").document(resName).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()){
                                            if (documentSnapshot.getString("resRating") != null){

                                                String resRating = documentSnapshot.getString("resRating");

                                                if (resRating.equals("0.0")){
                                                    avgRating  = (5 + rating.getRating()) / 2;
                                                }
                                                else {
                                                    avgRating  = (Float.parseFloat(resRating) + rating.getRating()) / 2;
                                                }

                                                HashMap<String,Object> averageRating = new HashMap<>();
                                                averageRating.put("resRating", String.valueOf(avgRating));

                                                firebaseFirestore.collection("Restaurants").document(resName)
                                                        .set(averageRating, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Log.d("resRating" , String.valueOf(avgRating));
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("resRating" , e.getMessage());
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("rating" , e.getMessage());
                            }
                        });


                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                popDialog.create();
                popDialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvResNameHistory, tvDateHistory, tvGrandTotalHistory, tvShowRating;
        private ImageView ivStatusHistory;
        private LinearLayout expandableLayoutHistory;
        private RecyclerView rvMemberHistory;
        private MaterialButton btnRateAndReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseFirestore = FirebaseFirestore.getInstance();

            ivStatusHistory = (ImageView) itemView.findViewById(R.id.ivStatusHistory);
            tvResNameHistory = (TextView) itemView.findViewById(R.id.tvResNameHistory);
            tvDateHistory = (TextView) itemView.findViewById(R.id.tvDateHistory);
            tvShowRating = (TextView) itemView.findViewById(R.id.tvShowRating);
            btnRateAndReview = (MaterialButton) itemView.findViewById(R.id.btnRateAndReview);
            tvGrandTotalHistory = (TextView) itemView.findViewById(R.id.tvGrandTotalHistory);
            expandableLayoutHistory = (LinearLayout) itemView.findViewById(R.id.expandableLayoutHistory);

            rvMemberHistory = (RecyclerView) itemView.findViewById(R.id.rvMemberHistory);
            rvMemberHistory.setLayoutManager(new LinearLayoutManager(context));

        }
    }
}
