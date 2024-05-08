package com.example.retrofitdemo;

import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserControlerRESTAPI implements retrofit2.Callback<Users>{
    final  String BASE_URL = "https://reqres.in/api/";
    private Users users;
    public void start(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UsersRESTAPI usersRESTAPI = retrofit.create(UsersRESTAPI.class);
        Call<Users> call = usersRESTAPI.getUsers();
        call.enqueue(this);
    }
    @Override
    public void onResponse(Call<Users> call, Response<Users> response) {
        if(response.isSuccessful()){
            //Log.d("Response",response.toString());
            users = response.body();
            Log.d("USER_Count"," User Count "+ users.data.size());
            List<User> usersList = users.getData();
            if(usersList!=null)
                for (User u: usersList){
                    Log.d("USER_INFO"," User :"+ u.toString());
                }
            else
                Log.d("USER_INFO"," User's List empty");
            Log.d("USER_Count"," User Count- "+ users.data.size());
        }
    }

    @Override
    public void onFailure(Call<Users> call, Throwable t) {
        t.printStackTrace();
        Log.d("USER_INFO","Error getting users");
    }
    public Users getUsers() {
        if( users !=null)
            Log.d("USER_Count"," User Count--"+ users.data.size());
        else
            Log.d("USER_Count"," Users object is null");

        return users;

    }
}