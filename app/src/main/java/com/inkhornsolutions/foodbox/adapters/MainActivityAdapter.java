package com.inkhornsolutions.foodbox.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inkhornsolutions.foodbox.R;
import com.inkhornsolutions.foodbox.RestaurantItems;
import com.inkhornsolutions.foodbox.SignUp;
import com.inkhornsolutions.foodbox.Welcome;
import com.inkhornsolutions.foodbox.models.RestaurantModelClass;
import com.tombayley.activitycircularreveal.CircularReveal;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

//import per.wsj.library.AndRatingBar;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private Context context;
    private List<RestaurantModelClass> resDetails = new ArrayList<>();
    private int checkedPosition = RecyclerView.NO_POSITION;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    View view;
    Activity activity;
    int lastPosition = -1;

    public MainActivityAdapter(Context context, List<RestaurantModelClass> resDetails, Activity activity) {
        this.context = context;
        this.resDetails = resDetails;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MainActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =LayoutInflater.from(parent.getContext());
        view =inflater.inflate(R.layout.main_activity_adapter_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainActivityAdapter.ViewHolder holder, int position) {
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.recycler_view_animation));

        RestaurantModelClass restaurantModelClass = resDetails.get(position);


            holder.tvRestaurant.setText(restaurantModelClass.getResName());

            Log.d("size", "" + restaurantModelClass.getNoOfOrders());
            if (restaurantModelClass.getNoOfOrders() != null){
                holder.tvNoOrders.setText("(" + restaurantModelClass.getNoOfOrders() + ")");
            }
            else {
                holder.tvNoOrders.setText(""+0);
            }

            if (restaurantModelClass.getResRating() != null){
                holder.ratingBar.setRating(Float.parseFloat(restaurantModelClass.getResRating()));
            }
            else {
                holder.ratingBar.setRating(0);
            }

            RequestOptions reqOpt = RequestOptions
                    .fitCenterTransform()
                    .transform(new RoundedCorners(8))
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // It will cache the image after loaded for first time
                    .override(1024, 768)
                    .priority(Priority.IMMEDIATE)
                    .encodeFormat(Bitmap.CompressFormat.PNG)
                    .format(DecodeFormat.DEFAULT);

            Glide.with(context)
                    .load(restaurantModelClass.getImageUri())
                    .thumbnail(0.25f)
                    .apply(reqOpt)
                    .placeholder(R.drawable.food_placeholder)
                    .into(holder.ivRestaurant);

//        Glide.with(context).load(restaurantModelClass.getImageUri()).placeholder(R.drawable.food_placeholder)
//                .fitCenter().into(holder.ivRestaurant);
//        Picasso.get().load(restaurantModelClass.getImageUri()).placeholder(R.drawable.food_placeholder).fit().centerCrop().into(holder.ivRestaurant);

            holder.itemView.setSelected(checkedPosition == position);

            String status = restaurantModelClass.getStatus();
            String approved = restaurantModelClass.getApproved();

            if (approved.equals("yes")){
                if (status.equals("online")){
                    holder.layout.setEnabled(true);
                    holder.tvOpenClose.setText("Open");
                    holder.tvOpenClose.setTextColor(ContextCompat.getColor(context, R.color.open));
                }
                else {
                    holder.layout.setEnabled(false);
                    holder.layout.setClickable(false);
                    holder.itemView.setEnabled(false);
                    holder.itemView.setClickable(false);
//                    ColorMatrix matrix = new ColorMatrix();
//                    matrix.setSaturation(0);
//                    holder.ivRestaurant.setColorFilter(new ColorMatrixColorFilter(matrix));
                    holder.tvOpenClose.setText("Closed");
                    holder.tvOpenClose.setTextColor(ContextCompat.getColor(context, R.color.closed));
//                Toast.makeText(context, "Restaurant is offline for now, Please try later.", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Log.d("approval", "restaurant not approved yet");
                holder.layout.setEnabled(false);
                holder.layout.setClickable(false);
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                holder.ivRestaurant.setColorFilter(new ColorMatrixColorFilter(matrix));

//            Toast.makeText(context, "Restaurant not approved yet", Toast.LENGTH_LONG).show();
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences("userName", context.MODE_PRIVATE);
            String name = sharedPreferences.getString("name", "User Name");

            String resName = restaurantModelClass.getResName().trim();
            Log.d("TAG1", resName);

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage("Loading");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    if (name.equals("")) {
                        progressDialog.dismiss();

                        Toast.makeText(context, "Please complete your profile first.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        if (approved.equals("yes")) {
                            if (status.equals("online")) {
                                progressDialog.dismiss();

                                Intent intent = new Intent(context, RestaurantItems.class);
                                intent.putExtra("restaurant", resName);
                                intent.putExtra("name", name);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                context.startActivity(intent);

                            }
                            else {
                                progressDialog.dismiss();

                                Toasty.error(context, "Restaurant is offline for now, Please try again later.", Toasty.LENGTH_LONG).show();
                            }
                        }
                        else {
                            progressDialog.dismiss();

                            Toasty.error(context, "This restaurant is not approved yet", Toasty.LENGTH_LONG).show();
                        }
                    }
                }
            });
    }

    @Override
    public int getItemCount() {
        return resDetails.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivRestaurant;
        private final TextView tvRestaurant, tvOpenClose;
        private TextView tvNoOrders;
        private final LinearLayout layout;
        private RatingBar ratingBar;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivRestaurant = itemView.findViewById(R.id.ivRestaurant);
            tvRestaurant = itemView.findViewById(R.id.tvRestaurant);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvNoOrders = itemView.findViewById(R.id.tvNoOrders);
            cardView = itemView.findViewById(R.id.cardView);
            tvOpenClose = itemView.findViewById(R.id.tvOpenClose);

            layout = itemView.findViewById(R.id.layout);

        }
    }
}
