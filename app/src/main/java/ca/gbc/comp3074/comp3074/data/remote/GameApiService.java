package ca.gbc.comp3074.comp3074.data.remote;

import java.util.List;

import ca.gbc.comp3074.comp3074.data.remote.models.GameApiModel;
import retrofit2.Call;
import retrofit2.http.GET;

public interface GameApiService {

    @GET("games/trending")
    Call<List<GameApiModel>> getTrendingGames();
}
