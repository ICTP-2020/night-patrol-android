package com.vinniesnp.nightpatrol;

import android.content.Intent;
import android.os.Bundle;

import com.vinniesnp.nightpatrol.api.model.CurrentUser;
import com.vinniesnp.nightpatrol.api.model.Schedule;
import com.vinniesnp.nightpatrol.api.service.ApiInterface;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
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

public class AvailabilityScreen extends AppCompatActivity {
    public Switch switchMonday, switchTuesday, switchWednesday, switchThursday, switchFriday, switchSaturday, switchSunday;
    public boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;

    private String BASE_URL = "https://us-central1-vinnies-api-staging.cloudfunctions.net/api/";
    public String mTOKEN;
    private String TAG = "Availability - Error";
    public Button saveScheduleButton;
    public String id;
    public String contactId;
    public String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availability_screen);

        mTOKEN = getIntent().getStringExtra("token");
        contactId = getIntent().getStringExtra("id");
        userType = getIntent().getStringExtra("type");

        switchMonday = findViewById(R.id.switchMonday);
        switchTuesday = findViewById(R.id.switchTuesday);
        switchWednesday = findViewById(R.id.switchWednesday);
        switchThursday = findViewById(R.id.switchThursday);
        switchFriday = findViewById(R.id.switchFriday);
        switchSaturday = findViewById(R.id.switchSaturday);
        switchSunday = findViewById(R.id.switchSunday);

        saveScheduleButton = findViewById(R.id.saveScheduleButton);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        Intent intentAvailability = new Intent(AvailabilityScreen.this, LandingScreen.class);
                        intentAvailability.putExtra("token", mTOKEN);
                        intentAvailability.putExtra("id", contactId);
                        startActivity(intentAvailability);
                        break;

                    case R.id.nav_availability:

                        break;

                    case R.id.nav_contacts:
                        if (userType.equals("1")) {
                            Intent intentContacts = new Intent(AvailabilityScreen.this, ContactTeamLeader.class);
                            intentContacts.putExtra("token", mTOKEN);
                            intentContacts.putExtra("id", contactId);
                            intentContacts.putExtra("type", userType);
                            startActivity(intentContacts);
                            break;
                        } else {
                            Intent intentContacts = new Intent(AvailabilityScreen.this, ContactUs.class);
                            intentContacts.putExtra("token", mTOKEN);
                            intentContacts.putExtra("id", contactId);
                            intentContacts.putExtra("type", userType);
                            startActivity(intentContacts);
                            break;
                        }
                }
                return false;
            }
        });
        getAvailability();
        saveSchedule();
    }

    private void getAvailability() {
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

        ApiInterface apiInterface = build.create(ApiInterface.class);

        Call<CurrentUser> call = apiInterface.getCurrentUser();

        call.enqueue(new Callback<CurrentUser>() {
            @Override
            public void onResponse(Call<CurrentUser> call, Response<CurrentUser> response) {

                int statusCode = response.code();

                monday = response.body().getSchedule().getMonday();
                tuesday = response.body().getSchedule().getTuesday();
                wednesday = response.body().getSchedule().getWednesday();
                thursday = response.body().getSchedule().getThursday();
                friday = response.body().getSchedule().getFriday();
                saturday = response.body().getSchedule().getSaturday();
                sunday = response.body().getSchedule().getSunday();

                id = response.body().getId();

                if (response.isSuccessful()) {
                    switchMonday.setChecked(monday);
                    switchTuesday.setChecked(tuesday);
                    switchWednesday.setChecked(wednesday);
                    switchThursday.setChecked(thursday);
                    switchFriday.setChecked(friday);
                    switchSaturday.setChecked(saturday);
                    switchSunday.setChecked(sunday);
                } else {
                    switch (statusCode) {
                        case 401:
                            //401 error, token missing or invalid.
                            Toast.makeText(AvailabilityScreen.this, "You do not have permission to do this.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "JWT missing or invalid");
                            break;
                        case 500:
                            //500 error, server error. Bug in API
                            Toast.makeText(AvailabilityScreen.this, "Server error, please try again", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "Something is wrong with the API");
                            break;
                        default:
                            Toast.makeText(AvailabilityScreen.this, "Unknown error, please try again.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "Unknown error");
                    }
                }
            }

            @Override
            public void onFailure(Call<CurrentUser> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(AvailabilityScreen.this, "Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "No Internet", t);

                } else {
                    Toast.makeText(AvailabilityScreen.this, "Conversion issue, please contact the developer.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Conversion issue", t);
                }
            }
        });
    }

    private void saveSchedule() {
        saveScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Interceptor authInterception = new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer " + mTOKEN).build();
                        return chain.proceed(newRequest);
                    }
                };

                String test = switchThursday.isChecked() + " " + switchSunday.isChecked() + " " + switchMonday.isChecked() + " ";

                Log.d(TAG, test);

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.interceptors().add(authInterception);
                OkHttpClient client = builder.build();

                Retrofit build = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .build();

                ApiInterface apiInterface = build.create(ApiInterface.class);

                Call<Schedule> call = apiInterface.setSchedule(new Schedule(switchMonday.isChecked(), switchTuesday.isChecked(), switchWednesday.isChecked(),
                        switchThursday.isChecked(), switchFriday.isChecked(), switchSaturday.isChecked(), switchSunday.isChecked()));

                call.enqueue(new Callback<Schedule>() {
                    @Override
                    public void onResponse(Call<Schedule> call, Response<Schedule> response) {

                        int statusCode = response.code();

                        if (response.isSuccessful()) {
                            Toast.makeText(AvailabilityScreen.this, "Availability saved!", Toast.LENGTH_SHORT).show();
                        } else {
                            switch (statusCode) {
                                case 400:
                                    //400 error, required parameters missing
                                    Toast.makeText(AvailabilityScreen.this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, statusCode + " " + response.errorBody().toString() + "Login error");
                                    break;
                                case 401:
                                    //401 error, token missing or invalid.
                                    Toast.makeText(AvailabilityScreen.this, "You do not have permission to do this.", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, statusCode + "JWT missing or invalid");
                                    break;
                                case 403:
                                    //401 error, token missing or invalid.
                                    Toast.makeText(AvailabilityScreen.this, "You do not have permission to do this.", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, statusCode + "JWT missing or invalid");
                                    break;
                                case 500:
                                    //500 error, server error. Bug in API
                                    Toast.makeText(AvailabilityScreen.this, "Server error, please try again", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, statusCode + "Something is wrong with the API");
                                    break;
                                default:
                                    Toast.makeText(AvailabilityScreen.this, "Unknown error, please try again.", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, statusCode + "Unknown error");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Schedule> call, Throwable t) {
                        if (t instanceof IOException) {
                            Toast.makeText(AvailabilityScreen.this, "Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "No Internet", t);

                        } else {
                            Toast.makeText(AvailabilityScreen.this, "Conversion issue, please contact the developer.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Conversion issue", t);
                        }
                    }
                });
            }
        });

    }
}

