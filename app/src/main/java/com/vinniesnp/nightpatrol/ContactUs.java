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

public class ContactUs extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private String BASE_URL = "https://us-central1-vinnies-api-staging.cloudfunctions.net/api/";
    public String mTOKEN;
    private String TAG = "ContactsVolunteer - Error";
    private String shiftID;
    public String userType;
    public TextView textShiftTimeStandard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        mTOKEN = getIntent().getStringExtra("token");
        shiftID = getIntent().getStringExtra("id");
        userType = getIntent().getStringExtra("type");
        Log.d(TAG, "" + mTOKEN);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        Intent intentHome = new Intent(ContactUs.this, LandingScreen.class);
                        intentHome.putExtra("token", mTOKEN);
                        startActivity(intentHome);
                        break;

                    case R.id.nav_availability:

                        Intent intentAvailability = new Intent(ContactUs.this, AvailabilityScreen.class);
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

        textShiftTimeStandard = findViewById(R.id.textShiftTimeStandard);

        recyclerView = findViewById(R.id.recyclerView);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestContact();

    }

    private void requestContact() {

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

        Call<ShiftDetails> call = apiInterface.getShiftDetails(shiftID);

        Log.d(TAG, shiftID + "");

        call.enqueue(new Callback<ShiftDetails>() {
            @Override
            public void onResponse(Call<ShiftDetails> call, Response<ShiftDetails> response) {

                int statusCode = response.code();

                //First if statement checks if there is a shift leader assigned to the shift, if there is we get the shift leader.
                if (response.isSuccessful() && (response.body().getShiftLeader()!= null)) {
                    final List<ShiftUsers> user_list = new ArrayList<ShiftUsers>();

                    ShiftUsers leaderContact = new ShiftUsers();

                    Date date = new java.util.Date((long) response.body().getStartTime());
                    // the format of your date
                    SimpleDateFormat day = new java.text.SimpleDateFormat("EEEE d MMM");
                    SimpleDateFormat time = new java.text.SimpleDateFormat("HH:mm");
                    // give a timezone reference for formatting (see comment at the bottom)
                    time.setTimeZone(java.util.TimeZone.getTimeZone("GMT+11"));
                    String formattedDate = day.format(date);


                    textShiftTimeStandard.setText("Shift: " + formattedDate);


                    leaderContact.setFirstName(response.body().getShiftLeader().getFirstName());
                    leaderContact.setLastName(response.body().getShiftLeader().getLastName());
                    leaderContact.setPhone(response.body().getShiftLeader().getPhone());
                    leaderContact.setEmail(response.body().getShiftLeader().getEmail());
                    user_list.add(leaderContact);

                    user_list.add(addVinniesContact());
                    user_list.add(addHelplinet());

                    adapter = new ContactAdapter(user_list);
                    recyclerView.setAdapter(adapter);
                    //Second if checks if there is a shift leader assigned to the shift, if there isn't, we don't set it.
                } else if (response.isSuccessful() && (response.body().getShiftLeader() == null)) {
                    final List<ShiftUsers> user_list = new ArrayList<ShiftUsers>();

                    user_list.add(addVinniesContact());
                    user_list.add(addHelplinet());

                    adapter = new ContactAdapter(user_list);
                    recyclerView.setAdapter(adapter);

                } else {
                    switch (statusCode) {
                        case 401:
                            //401 error, token missing or invalid.
                            Toast.makeText(ContactUs.this, "You do not have permission to do this.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "JWT missing or invalid");
                            break;
                        case 500:
                            //500 error, server error. Bug in API
                            Toast.makeText(ContactUs.this, "Server error, please try again", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "Something is wrong with the API");
                            break;
                        case 403:
                            //403 error, does not have permission to access
                            Toast.makeText(ContactUs.this, "You do not have permission to do this.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "Client not allowed to access this content");
                            break;
                        default:
                            Toast.makeText(ContactUs.this, "Unknown error, please try again.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, statusCode + "Unknown error");
                    }
                }
            }

            @Override
            public void onFailure(Call<ShiftDetails> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(ContactUs.this, "Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "No Internet", t);

                } else {
                    Toast.makeText(ContactUs.this, "Conversion issue, please contact the developer.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Conversion issue", t);
                }
            }
        });

    }

    public ShiftUsers addVinniesContact() {
        ShiftUsers vinniesContact = new ShiftUsers();

        vinniesContact.setFirstName("Vinnies");
        vinniesContact.setLastName("Support Contacts");
        vinniesContact.setPhone("02 6282 2722");
        vinniesContact.setEmail("info@svdp-cg.org.au");

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
