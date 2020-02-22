package com.vinniesnp.nightpatrol.api.service;

import com.vinniesnp.nightpatrol.api.model.CurrentUser;
import com.vinniesnp.nightpatrol.api.model.PasswordChange;
import com.vinniesnp.nightpatrol.api.model.Schedule;
import com.vinniesnp.nightpatrol.api.model.Shift;
import com.vinniesnp.nightpatrol.api.model.ShiftDetails;
import com.vinniesnp.nightpatrol.api.model.User;
import com.vinniesnp.nightpatrol.api.model.UserChange;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    //Used when logging in, this gets the users token
    @GET("auth")
    Call<User> getUser(
            @Query("email") String email,
            @Query("password") String password);

    //Used on Landing Screen, this lifts the shifts
    @GET("shifts")
    Call<List<Shift>> getShifts();

    //Gets the information for the current user, including their availability.
    @GET("users/me")
    Call<CurrentUser> getCurrentUser();

    //Call to change the users password. Used when isTemporary is set to true.
    @POST("users/me/change-password")
    Call<PasswordChange> postPassword(
            @Body PasswordChange body);

    //Call to change the users availability.
    @PUT("users/me/schedule")
    Call<Schedule> setSchedule(
            //@Path("id") String id,
            @Body Schedule body);

    //Call to cancel an assigned shift.
    @POST("shifts/{id}/cancel")
    Call<String> cancelShift(
            @Path("id") String id);

    //Get a team leader
    @GET("shifts/{id}")
    Call<ShiftDetails> getShiftDetails(
            @Path("id") String id);

    //Call to edit a user's details.
    @PUT("users/me")
    Call<UserChange> putUser(
            @Body UserChange body);
}
