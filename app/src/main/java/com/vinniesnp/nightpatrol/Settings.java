package com.vinniesnp.nightpatrol;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.vinniesnp.nightpatrol.api.model.UserChange;
import com.vinniesnp.nightpatrol.api.service.ApiInterface;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Settings extends AppCompatActivity {

    public String mTOKEN;
    private String BASE_URL = "https://us-central1-vinnies-api-staging.cloudfunctions.net/api/";
    private String TAG = "Settings - Error";
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String teamID;
    private String userID;
    public TextView textFirstName, textLastName, textEmail, textPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mTOKEN = getIntent().getStringExtra("token");
        userID = getIntent().getStringExtra("userId");
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        email = getIntent().getStringExtra("email");
        phone = getIntent().getStringExtra("phone");
        teamID = getIntent().getStringExtra("teamId");

        textFirstName = findViewById(R.id.textFirstName);
        textLastName = findViewById(R.id.textLastName);
        textEmail = findViewById(R.id.textEmail);
        textPhone = findViewById(R.id.textPhone);

        textFirstName.setText(firstName);
        textLastName.setText(lastName);
        textEmail.setText(email);
        textPhone.setText(phone);

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentSettings = new Intent(Settings.this, LogIn.class);
                intentSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentSettings);
            }
        });

        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentSettings = new Intent(Settings.this, LandingScreen.class);
                intentSettings.putExtra("token", mTOKEN);
                startActivity(intentSettings);
            }
        });

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateDetails();
            }
        });


        ImageView creditImage = findViewById(R.id.imageCredit);
        creditImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final android.app.AlertDialog.Builder mBuilder = new AlertDialog.Builder(Settings.this);
                View mView = getLayoutInflater().inflate(R.layout.credit_screen, null);
                Button mClose = mView.findViewById(R.id.closeCreditButton);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                mClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();

                    }
                });
            }
        });
    }

    public void updateDetails() {
        String fName = textFirstName.getText().toString();
        String lName = textLastName.getText().toString();
        String email = textEmail.getText().toString();
        String phone = textPhone.getText().toString();

        Interceptor authInterception = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer " + mTOKEN).build();
                return chain.proceed(newRequest);
            }
        };

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(authInterception);
        OkHttpClient client = builder.build();

        Retrofit build = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        final ApiInterface apiInterface = build.create(ApiInterface.class);

        Call<UserChange> userChangeCall = apiInterface.putUser(new UserChange(fName, lName, email, phone));

        userChangeCall.enqueue(new Callback<UserChange>() {
            @Override
            public void onResponse(Call<UserChange> call, Response<UserChange> response) {
                int statusCode = response.code();

                if (response.isSuccessful()) {
                    Toast.makeText(Settings.this, "Details successfully changed!", Toast.LENGTH_SHORT).show();
                } else {
                    switch (statusCode) {
                        case 400:
                            //400 error, required parameters missing
                            Toast.makeText(Settings.this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + " " + response.errorBody().toString() + "Login error");
                            break;
                        case 401:
                            //401 error, token missing or invalid.
                            Toast.makeText(Settings.this, "You do not have permission to do this.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "JWT missing or invalid");
                            break;
                        case 500:
                            //500 error, server error. Bug in API
                            Toast.makeText(Settings.this, "Server error, please try again", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "Something is wrong with the API");
                            break;
                        default:
                            Toast.makeText(Settings.this, "Unknown error, please try again.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "Unknown error");
                    }
                }
            }

            @Override
            public void onFailure(Call<UserChange> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(Settings.this, "Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "No Internet", t);

                } else {
                    Toast.makeText(Settings.this, "Conversion issue, please contact the developer.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Conversion issue", t);
                }
            }
        });
    }
}
