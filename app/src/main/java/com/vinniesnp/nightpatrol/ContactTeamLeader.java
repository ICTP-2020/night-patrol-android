package com.vinniesnp.nightpatrol;

import android.content.Intent;
import android.os.Bundle;

import com.vinniesnp.nightpatrol.api.model.ShiftDetails;
import com.vinniesnp.nightpatrol.api.model.ShiftUsers;
import com.vinniesnp.nightpatrol.api.service.ApiInterface;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ContactTeamLeader extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private String BASE_URL = "https://us-central1-vinnies-api-staging.cloudfunctions.net/api/";
    public String mTOKEN;
    private String shiftID;
    public String userType;
    public TextView textShiftTime;

    private String TAG = "ContactsTeamLeader - Error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_team_leader);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        mTOKEN = getIntent().getStringExtra("token");
        shiftID = getIntent().getStringExtra("id");
        userType =  getIntent().getStringExtra("type");

        textShiftTime = findViewById(R.id.textShiftTime);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        Intent intentContacts = new Intent(ContactTeamLeader.this, LandingScreen.class);
                        intentContacts.putExtra("token", mTOKEN);
                        startActivity(intentContacts);
                        break;

                    case R.id.nav_availability:
                        Intent intentAvailability = new Intent(ContactTeamLeader.this, AvailabilityScreen.class);
                        intentAvailability.putExtra("token", mTOKEN);
                        intentAvailability.putExtra("id", shiftID);
                        intentAvailability.putExtra("type", userType);
                        startActivity(intentAvailability);
                        break;

                    case R.id.nav_contacts:

                        break;

                }
                return false;
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestContacts();
    }


    private void requestContacts() {

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

        Call<ShiftDetails> callFirst = apiInterface.getShiftDetails(shiftID);

        callFirst.enqueue(new Callback<ShiftDetails>() {
            @Override
            public void onResponse(Call<ShiftDetails> call, Response<ShiftDetails> response) {
                int statusCode = response.code();

                if (response.isSuccessful() && (response.body().getShiftUsers() != null)) {
                    final List<ShiftUsers> user_list = response.body().getShiftUsers();


                    Date date = new java.util.Date((long) response.body().getStartTime());
                    // the format of your date
                    SimpleDateFormat day = new java.text.SimpleDateFormat("EEEE d MMM");
                    SimpleDateFormat time = new java.text.SimpleDateFormat("HH:mm");
                    // give a timezone reference for formatting (see comment at the bottom)
                    time.setTimeZone(java.util.TimeZone.getTimeZone("GMT+11"));
                    String formattedDate = day.format(date);


                    textShiftTime.setText("Shift: " + formattedDate);

                    user_list.add(addVinniesContact());
                    user_list.add(addHelplinet());

                    adapter = new ContactAdapter(user_list);

                    recyclerView.setAdapter(adapter);

                } else if (response.isSuccessful() && (response.body().getShiftUsers() == null)){
                    final List<ShiftUsers> user_list = new ArrayList<>();

                    user_list.add(addVinniesContact());
                    user_list.add(addHelplinet());

                    adapter = new ContactAdapter(user_list);

                    recyclerView.setAdapter(adapter);
                } else {
                    switch (statusCode) {
                        case 401:
                            //401 error, token missing or invalid.
                            Toast.makeText(ContactTeamLeader.this, "You do not have permission to do this.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "JWT missing or invalid");
                            break;
                        case 500:
                            //500 error, server error. Bug in API
                            Toast.makeText(ContactTeamLeader.this, "Server error, please try again", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "Something is wrong with the API");
                            break;
                        default:
                            Toast.makeText(ContactTeamLeader.this, "Unknown error, please try again.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "Unknown error");
                    }
                }
            }

            @Override
            public void onFailure(Call<ShiftDetails> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(ContactTeamLeader.this, "Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "No Internet", t);

                } else {
                    Toast.makeText(ContactTeamLeader.this, "Conversion issue, please contact the developer.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Conversion issue", t);
                }
            }
        });

    }

    public ShiftUsers addVinniesContact() {
        ShiftUsers vinniesContact = new ShiftUsers();

        vinniesContact.setFirstName("Vinnies");
        vinniesContact.setLastName("Support Contacts");
        vinniesContact.setPhone("610382357923");
        vinniesContact.setEmail("Vinnies@vinnies");

        return vinniesContact;
    }

    public ShiftUsers addHelplinet() {
        ShiftUsers helplineContact = new ShiftUsers();

        helplineContact.setFirstName("Police Assistance Line");
        helplineContact.setLastName("");
        helplineContact.setPhone("131 444");
        helplineContact.setEmail("");

        return helplineContact;
    }
}


