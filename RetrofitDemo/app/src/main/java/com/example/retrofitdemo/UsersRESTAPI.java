package com.example.retrofitdemo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
public interface UsersRESTAPI {
    @GET("users/")
    Call<Users> getUsers();
}
