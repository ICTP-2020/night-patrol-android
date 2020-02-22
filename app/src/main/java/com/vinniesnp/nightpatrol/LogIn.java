package com.vinniesnp.nightpatrol;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vinniesnp.nightpatrol.api.model.Login;
import com.vinniesnp.nightpatrol.api.model.PasswordChange;
import com.vinniesnp.nightpatrol.api.model.User;
import com.vinniesnp.nightpatrol.api.service.ApiInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LogIn extends AppCompatActivity {

    public String BASE_URL = "https://us-central1-vinnies-api-staging.cloudfunctions.net/api/";

    private static final String TAG = "LogIn - Error";

    private EditText inputEmail;
    private EditText inputPassword;

    public int statusCode;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 4 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View loginView = findViewById(R.id.loginView);

        loginView.setOnClickListener(loginViewListener);
        inputEmail = findViewById(R.id.emailText);
        inputPassword = findViewById(R.id.passwordText);

    }

    private View.OnClickListener loginViewListener = new View.OnClickListener() {
        public void onClick(final View v) {

            Login login = new Login();
            login.setEmail(inputEmail.getText().toString());
            login.setPassword(inputPassword.getText().toString());

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit.Builder builderOne = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson));

            Retrofit retrofit = builderOne.build();

            ApiInterface apiService = retrofit.create(ApiInterface.class);

            Call<User> callUser = apiService.getUser(login.getEmail(), login.getPassword());

            callUser.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    final User token = response.body();
                    //Get response code
                    statusCode = response.code();

                    //If response == 200
                    if (response.isSuccessful()) {

                        if (token.getTemporaryStatus().toLowerCase().equals("true")) {
                            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(LogIn.this);
                            View mView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
                            final EditText mPassword1 = mView.findViewById(R.id.password1);
                            final EditText mPassword2 = mView.findViewById(R.id.password2);
                            Button mSave = mView.findViewById(R.id.savePasswordButton);
                            Button mCancel = mView.findViewById(R.id.cancelButton);

                            mBuilder.setView(mView);
                            final AlertDialog dialog = mBuilder.create();
                            dialog.show();

                            mSave.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    final String mPasswordOne = mPassword1.getText().toString();
                                    final String mPasswordTwo = mPassword2.getText().toString();

                                    if ((!mPasswordOne.isEmpty() && !mPasswordTwo.isEmpty()) || (!mPasswordOne.isEmpty() || !mPasswordTwo.isEmpty())) {
                                        if (mPasswordOne.equals(mPasswordTwo)) {
                                            if (!PASSWORD_PATTERN.matcher(mPasswordOne).matches() || !PASSWORD_PATTERN.matcher(mPasswordTwo).matches()) {
                                                mPassword1.setError("Password does not meet standards.");
                                            } else {

                                                Interceptor interceptor = new Interceptor() {
                                                    @Override
                                                    public okhttp3.Response intercept(Chain chain) throws IOException {
                                                        Request newRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer " + token.getToken()).build();
                                                        return chain.proceed(newRequest);
                                                    }
                                                };

                                                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                                                builder.interceptors().add(interceptor);
                                                OkHttpClient client = builder.build();

                                                Retrofit builderTwo = new Retrofit.Builder()
                                                        .baseUrl(BASE_URL)
                                                        .addConverterFactory(GsonConverterFactory.create())
                                                        .client(client)
                                                        .build();

                                                ApiInterface apiService = builderTwo.create(ApiInterface.class);

                                                Call<PasswordChange> callPassword = apiService.postPassword(new PasswordChange(mPassword1.getText().toString()));

                                                callPassword.enqueue(new Callback<PasswordChange>() {
                                                    @Override
                                                    public void onResponse(Call<PasswordChange> call, Response<PasswordChange> response) {
                                                        //Get response code
                                                        statusCode = response.code();

                                                        //If response == 200
                                                        if (response.isSuccessful()) {
                                                            Toast.makeText(LogIn.this, "Password successfully changed!", Toast.LENGTH_SHORT).show();

                                                            //Goto Landing screen on success.
                                                            Intent intent = new Intent(LogIn.this, LandingScreen.class);
                                                            intent.putExtra("token", token.getToken());
                                                            startActivity(intent);
                                                        } else {
                                                            switch (statusCode) {
                                                                case 400:
                                                                    //400 error, required parameters missing
                                                                    Toast.makeText(LogIn.this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show();
                                                                    Log.e(TAG, statusCode + " " + response.errorBody().toString() + "Login error");
                                                                    break;
                                                                case 401:
                                                                    //401 error, token missing or invalid.
                                                                    Toast.makeText(LogIn.this, "You do not have permission to do this.", Toast.LENGTH_SHORT).show();
                                                                    Log.e(TAG, statusCode + "JWT missing or invalid");
                                                                    break;
                                                                case 500:
                                                                    //500 error, server error. Bug in API
                                                                    Toast.makeText(LogIn.this, "Server error, please try again", Toast.LENGTH_SHORT).show();
                                                                    Log.e(TAG, statusCode + "Something is wrong with the API");
                                                                    break;
                                                                default:
                                                                    Toast.makeText(LogIn.this, "Unknown error, please try again.", Toast.LENGTH_SHORT).show();
                                                                    Log.e(TAG, statusCode + "Unknown error");
                                                            }
                                                        }

                                                    }

                                                    @Override
                                                    public void onFailure(Call<PasswordChange> call, Throwable t) {
                                                        if (t instanceof IOException) {
                                                            Toast.makeText(LogIn.this, "Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                                                            Log.e(TAG, "No Internet", t);

                                                        } else {
                                                            Toast.makeText(LogIn.this, "Conversion issue, please contact the developer.", Toast.LENGTH_SHORT).show();
                                                            Log.e(TAG, "Conversion issue", t);
                                                        }
                                                    }
                                                });
                                            }
                                        } else {
                                            mPassword1.setError(null);
                                            mPassword2.setError("Password does not match");
                                        }

                                    } else {
                                        mPassword1.setError("Field can't be empty");
                                        mPassword2.setError("Field can't be empty");
                                    }
                                }
                            });

                            mCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });

                        } else {
                            Toast.makeText(LogIn.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LogIn.this, LandingScreen.class);
                            intent.putExtra("token", token.getToken());
                            startActivity(intent);
                        }

                    } else {
                        switch (statusCode) {
                            case 400:
                                //400 error, required parameters missing
                                Toast.makeText(LogIn.this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, statusCode + " " + response.errorBody().toString() + "Login error");
                                break;
                            case 401:
                                //401 error, token missing or invalid.
                                Toast.makeText(LogIn.this, "You do not have permission to do this.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, statusCode + "JWT missing or invalid");
                                break;
                            case 404:
                                //401 error, token missing or invalid.
                                Toast.makeText(LogIn.this, "Wrong username or password.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, statusCode + "Wrong username or password");
                                break;
                            case 500:
                                //500 error, server error. Bug in API
                                Toast.makeText(LogIn.this, "Server error, please try again", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, statusCode + "Something is wrong with the API");
                                break;
                            default:
                                Toast.makeText(LogIn.this, "Unknown error, please try again.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, statusCode + "Unknown error");
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    if (t instanceof IOException) {
                        Toast.makeText(LogIn.this, "Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "No Internet", t);

                    } else {
                        Toast.makeText(LogIn.this, "Conversion issue, please contact the developer.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Conversion issue", t);
                    }
                }
            });
        }
    };

}
