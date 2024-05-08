

package com.example.retrofitdemo;



import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserControlerRESTAPI {
    private final String BASE_URL = "https://reqres.in/api/";
    private Users users;
    private Callback callback;

    public interface Callback {
        void onUsersReceived(List<User> users);
        void onFailure(Throwable t);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void start() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UsersRESTAPI usersRESTAPI = retrofit.create(UsersRESTAPI.class);
        Call<Users> call = usersRESTAPI.getUsers();
        call.enqueue(new retrofit2.Callback<Users>() {
            @Override
            public void onResponse(Call<Users> call, Response<Users> response) {
                if (response.isSuccessful()) {
                    users = response.body();
                    List<User> usersList = users.getData();
                    if (callback != null) {
                        callback.onUsersReceived(usersList);
                    }
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(t);
                }
            }
        });
    }

    public Users getUsers() {
        if (users != null) {
            Log.d("USER_Count", " User Count--" + users.data.size());
        } else {
            Log.d("USER_Count", " Users object is null");
        }
        return users;
    }
}