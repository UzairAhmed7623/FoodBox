package com.inkhornsolutions.foodbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.shivtechs.maplocationpicker.LocationPickerActivity;
import com.shivtechs.maplocationpicker.MapUtility;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class FirstProfile extends AppCompatActivity {

    private static final int ADDRESS_PICKER_REQUEST = 1001;
    public static final int REQUEST_IMAGE = 1002;
    private TextView tvFirstName, tvLastName, tvEmailAddress;
    private TextView tvName, tvMobile, tvEmail, tvAddress, tvDateOfBirth;
    private CircleImageView ivProfile;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Toolbar toolbar;
    private CoordinatorLayout rootLayout;
    private DatePickerDialog datePickerDialog;
    private ProgressDialog progressDialog;
    private RelativeLayout layoutUserName,layoutUserEmail,layoutUserAddress,layoutUserDOB;
    private FloatingActionButton btnCompleteProfile;
    private String imageUri = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        tvFirstName = (TextView) findViewById(R.id.tvFirstName);
        tvLastName = (TextView) findViewById(R.id.tvLastName);
        tvEmailAddress = (TextView) findViewById(R.id.tvEmailAddress);

        tvName = (TextView) findViewById(R.id.tvName);
        tvMobile = (TextView) findViewById(R.id.tvMobile);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvDateOfBirth = (TextView) findViewById(R.id.tvDateOfBirth);
        ivProfile = (CircleImageView) findViewById(R.id.ivProfile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);
        layoutUserName = (RelativeLayout) findViewById(R.id.layoutUserName);
        layoutUserEmail = (RelativeLayout) findViewById(R.id.layoutUserEmail);
        layoutUserAddress = (RelativeLayout) findViewById(R.id.layoutUserAddress);
        layoutUserDOB = (RelativeLayout) findViewById(R.id.layoutUserDOB);

        btnCompleteProfile = (FloatingActionButton) findViewById(R.id.btnCompleteProfile);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String displayName = "", email  = "", pic  = "", phone  = "";

        if (firebaseUser.getDisplayName() != null){
            displayName = firebaseUser.getDisplayName();
        }
        if (firebaseUser.getEmail() != null){
            email = firebaseUser.getEmail();
        }
        if (firebaseUser.getPhotoUrl() != null){
            pic = firebaseUser.getPhotoUrl().toString();
        }
        if (firebaseUser.getPhoneNumber() != null){
            phone = firebaseUser.getPhoneNumber();
        }

        String finalDisplayName = displayName;
        String finalEmail = email;
        String finalPhone = phone;
        String finalPic = pic;
        firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {

                            if (documentSnapshot.getString("firstName") != null &&
                                    documentSnapshot.getString("lastName") != null) {
                                String fName = documentSnapshot.getString("firstName");
                                String lName = documentSnapshot.getString("lastName");
                                tvFirstName.setText(fName);
                                tvLastName.setText(lName);
                                tvName.setText(fName + " " + lName);
                            }

                            else if (!TextUtils.isEmpty(finalDisplayName)){

                                tvFirstName.setText(finalDisplayName);
                                tvLastName.setText(finalDisplayName);
                                tvName.setText(finalDisplayName + " " + finalDisplayName);

                                Map<String, Object> addData = new HashMap<>();
                                addData.put("firstName", finalDisplayName);
                                addData.put("lastName", finalDisplayName);

                                firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout, "Your name is saved in database successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                                    }
                                });
                            }

                            if (documentSnapshot.getString("emailAddress") != null) {
                                String email = documentSnapshot.getString("emailAddress");
                                tvEmailAddress.setText(email);
                                tvEmail.setText(email);
                            }
                            else if (!TextUtils.isEmpty(finalEmail)){
                                tvEmailAddress.setText(finalEmail);
                                tvEmail.setText(finalEmail);

                                Map<String, Object> addData = new HashMap<>();
                                addData.put("emailAddress", finalEmail);

                                firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout, "Your email is saved in database successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                                    }
                                });
                            }

                            if (documentSnapshot.getString("phoneNumber") != null) {
                                String mobile = documentSnapshot.getString("phoneNumber");
                                tvMobile.setText(mobile);
                            }
                            else if (!TextUtils.isEmpty(finalPhone)){
                                tvMobile.setText(finalPhone);

                                Map<String, Object> addData = new HashMap<>();
                                addData.put("phoneNumber", finalPhone);

                                firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout, "Your phone number is saved in database successfully!", Snackbar.LENGTH_LONG).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            }

                            if (documentSnapshot.getString("address") != null) {
                                String address = documentSnapshot.getString("address");
                                tvAddress.setText(address);
                            }

                            if (documentSnapshot.getString("DOB") != null) {
                                String dob = documentSnapshot.getString("DOB");
                                tvDateOfBirth.setText(dob);
                            }

                            if (documentSnapshot.getString("UsersImageProfile") != null) {

                                if (isValidContextForGlide(FirstProfile.this)){

                                    String userImage = documentSnapshot.getString("UsersImageProfile");
                                    imageUri = userImage;
                                    Glide.with(FirstProfile.this).load(userImage).placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.user)).into(ivProfile);
                                }
                            }
                            else if (!TextUtils.isEmpty(finalPic)){
                                if (isValidContextForGlide(FirstProfile.this)) {

                                    Glide.with(FirstProfile.this).load(finalPic).placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.user)).into(ivProfile);

                                    HashMap<String, Object> img = new HashMap<>();
                                    img.put("UsersImageProfile", finalPic);

                                    firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(img, SetOptions.merge())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    Snackbar.make(rootLayout, "Image saved in database successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                                                }
                                            });
                                }
                            }

                        }
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                        progressDialog.dismiss();
                    }
                });

        View view = LayoutInflater.from(this).inflate(R.layout.edit_details, null);
        EditText editText = (EditText) view.findViewById(R.id.editText);
        EditText editText2 = (EditText) view.findViewById(R.id.editText2);
        TextInputLayout TextInputLayout2 = (TextInputLayout) view.findViewById(R.id.TextInputLayout2);
        TextView btnAdd = (TextView) view.findViewById(R.id.tvSave);

        AlertDialog.Builder builder = new AlertDialog.Builder(FirstProfile.this);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        layoutUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.show();

                TextInputLayout2.setVisibility(View.VISIBLE);
                editText2.setVisibility(View.VISIBLE);

                editText.setHint("Write your First Name");
                editText2.setHint("Write your Last Name");

                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText2.setInputType(InputType.TYPE_CLASS_TEXT);

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String firstName = editText.getText().toString();
                        String lastName = editText2.getText().toString();

                        if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)){
                            editText.setError("Please write your first name");
                            editText2.setError("Please write your last name");
                        }
                        else {

                            tvFirstName.setText(firstName);
                            tvLastName.setText(lastName);
                            tvName.setText(firstName+" "+lastName);

                            Map<String, Object> addData = new HashMap<>();
                            addData.put("firstName", firstName);
                            addData.put("lastName", lastName);

                            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLayout, "Your name is updated successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                                            editText.clearComposingText();
                                            editText2.clearComposingText();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                                }
                            });

                            alertDialog.dismiss();
                        }

                    }
                });
            }
        });

        layoutUserEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.show();

                editText.setHint("Write your email");

                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                TextInputLayout2.setVisibility(View.GONE);

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String emailAddress = editText.getText().toString();

                        if (TextUtils.isEmpty(emailAddress)){
                            editText.setError("Please write your email");
                        }
                        else {

                            tvEmail.setText(emailAddress);
                            tvEmailAddress.setText(emailAddress);

                            Map<String, Object> addData = new HashMap<>();
                            addData.put("emailAddress", emailAddress);

                            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLayout, "Your email is updated successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                                            editText.clearComposingText();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(rootLayout, "Your email is updated successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                                }
                            });

                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        tvMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.show();

                editText.setHint("Write your phone with +92");

                editText.setInputType(InputType.TYPE_CLASS_PHONE);

                TextInputLayout2.setVisibility(View.GONE);

                editText.setText("");

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phoneNumber = editText.getText().toString();
                        if (TextUtils.isEmpty(phoneNumber)){
                            editText.setError("Please write your phone");
                        }
                        else {
                            tvMobile.setText(phoneNumber);

                            Map<String, Object> addData = new HashMap<>();
                            addData.put("phoneNumber", phoneNumber);

                            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLayout, "Your phone number is updated successfully!", Snackbar.LENGTH_LONG).show();
                                            editText.clearComposingText();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });

                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        layoutUserAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MapUtility.apiKey = getResources().getString(R.string.google_maps_key);
                Intent i = new Intent(FirstProfile.this, LocationPickerActivity.class);
                startActivityForResult(i, ADDRESS_PICKER_REQUEST);

            }
        });

        layoutUserDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                datePickerDialog = new DatePickerDialog(FirstProfile.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        tvDateOfBirth.setText(dayOfMonth + "/" + (month + 1) + "/" + year);

                        String DOB = tvDateOfBirth.getText().toString();

                        Map<String, Object> addData = new HashMap<>();
                        addData.put("DOB", DOB);

                        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(rootLayout, "Your DOB is updated successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout, "Your email is updated successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                            }
                        });
                    }
                },year, month, day);

                datePickerDialog.show();

            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dexter.withContext(FirstProfile.this)
                        .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {

                                    ImagePicker.Companion.with(FirstProfile.this)
                                            .cropSquare()
                                            .compress(1024)
                                            .start(REQUEST_IMAGE);
                                }
                                else {
                                    Snackbar.make(rootLayout, report.getDeniedPermissionResponses().toString(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                                }
                            }
                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }

        });

        SharedPreferences preferences = getSharedPreferences("profile", MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean("isFirstTime", true);

        if (!isFirstTime){
            finish();
            Intent intent = new Intent(FirstProfile.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        btnCompleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvName.getText().toString().equals("Your name")
                        || tvMobile.getText().toString().equals("+923000000000")
                        || tvEmail.getText().toString().equals("yourmail@gmail.com")
                        || tvAddress.getText().toString().equals("Your address")
                        || tvName.getText().toString().equals("")
                        || tvMobile.getText().toString().equals("")
                        || tvEmail.getText().toString().equals("")
                        || tvAddress.getText().toString().equals("")){

                    SharedPreferences preferences = getSharedPreferences("profile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isFirstTime", true);
                    editor.apply();

                    Snackbar.make(findViewById(android.R.id.content), "Please complete your profile.", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                }
                else {
                    SharedPreferences preferences = getSharedPreferences("profile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isFirstTime", false);
                    editor.apply();

                    Intent intent = new Intent(FirstProfile.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADDRESS_PICKER_REQUEST) {
            try {
                if (data != null && data.getStringExtra(MapUtility.ADDRESS) != null) {

                    Bundle completeAddress =data.getBundleExtra("fullAddress");

                    String state = new StringBuilder().append(completeAddress.getString("state")).toString();
                    String postalcode = new StringBuilder().append(completeAddress.getString("postalcode")).toString();;
                    String country = new StringBuilder().append(completeAddress.getString("country")).toString();;

                    String address = new StringBuilder().append(completeAddress.getString("addressline2")
                            .replace(state+",", "")
                            .replace(postalcode+",", "")
                            .replace(country+",", ""))
                            .toString();

                    tvAddress.setText(address);

                    Map<String, Object> addData = new HashMap<>();
                    addData.put("address", address);

                    firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Snackbar.make(rootLayout, "Your address is updated successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                                }
                            });

                }
            }
            catch (Exception e) {
                Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
            }
        }

        else if (requestCode == REQUEST_IMAGE ) {

            if (data!= null && data.getData() != null){

                //Image Uri will not be null for RESULT_OK
                Uri uri = data.getData();

                imageUri = String.valueOf(uri);

                Glide.with(this)
                        .load(uri)
                        .placeholder(ContextCompat.getDrawable(getApplicationContext(),R.drawable.user))
                        .transform(new CircleCrop())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressDialog.dismiss();
                                return false; // important to return false so the error placeholder can be placed
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressDialog.dismiss();
                                return false;
                            }
                        })
                        .into(ivProfile);

                uploadImage(uri);

            }
            else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ""+ImagePicker.RESULT_ERROR, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void uploadImage(Uri imageUri) {

        progressDialog.show();

        String someFilepath = String.valueOf(imageUri);
        String extension = someFilepath.substring(someFilepath.lastIndexOf("."));

        String userId = firebaseAuth.getCurrentUser().getUid();

        StorageReference riversRef = storageReference.child("images/Users/Profile" + extension+ " " + tvFirstName.getText().toString() +" "+ tvLastName.getText().toString());

        riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();

                final String sdownload_url = String.valueOf(downloadUrl);

                HashMap<String, Object> img = new HashMap<>();
                img.put("UsersImageProfile", sdownload_url);

                firebaseFirestore.collection("Users").document(userId).set(img, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Snackbar.make(rootLayout, "Image uploaded!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                            }
                        });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_image, menu);

        MenuItem menuItem = menu.findItem(R.id.image);
        menuItem.setActionView(R.layout.toobar_profile_image);
        View view = menuItem.getActionView();
        CircleImageView toolbar_profile_Image = view.findViewById(R.id.toolbar_profile_Image);
        toolbar_profile_Image.setVisibility(View.INVISIBLE);

        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    //visible image view
                    toolbar_profile_Image.setVisibility(View.VISIBLE);
                    isShow = true;
                }
                else if (isShow) {
                    //invisible image view
                    toolbar_profile_Image.setVisibility(View.INVISIBLE);

                    isShow = false;
                }
            }
        });

        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            if (documentSnapshot.getString("UsersImageProfile") != null){

                                if (isValidContextForGlide(FirstProfile.this)){

                                    imageUri = documentSnapshot.getString("UsersImageProfile");
                                    Glide.with(FirstProfile.this).load(imageUri).placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.account_circle_black)).into(toolbar_profile_Image);
                                }
                            }
                            else {
                                Log.d("TAG", "Not found!");
                            }
                        }
                        else {
                            Log.d("TAG", "No data found!");
                        }
                    }
                    else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences("profile", MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean("isFirstTime", true);

        if (!isFirstTime){
            finish();
            Intent intent = new Intent(FirstProfile.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }
}