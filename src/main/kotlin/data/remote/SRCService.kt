package data.remote

import data.remote.utils.HttpRequestInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import data.remote.adapters.*
import data.remote.responses.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface SRCService {

    // https://github.com/speedruncomorg/api/blob/master/version1/games.md
    @GET("games/{id}")
    suspend fun fetchFullGame(
        @Path("id") gameId: String,
        // Fixed query params
        @Query("embed") embed: String = "categories.variables,moderators,levels",
    ): PaginatedFullGameResponse

    // https://github.com/speedruncomorg/api/blob/master/version1/games.md
    @GET("games")
    suspend fun fetchGames(
        @Query("offset") offset : Int?,
        // Fixed query params
        @Query("_bulk") _bulk: Boolean = true,
        @Query("max") max: Int = 1000
    ): PaginatedBulkGamesResponse

    // https://github.com/speedruncomorg/api/blob/master/version1/runs.md
    @GET("runs")
    suspend fun fetchRuns(
        @Query("game") gameId: String,
        @Query("status") status: String?,
        @Query("orderby") orderBy: String?,
        @Query("direction") direction: String?,
        @Query("offset") offset : Int?,
        // Fixed query params
        @Query("embed") embed: String = "players",
        @Query("max") max: Int = 200,
    ): PaginatedRunsResponse

    companion object {
        private const val BASE_URL = "https://www.speedrun.com/api/v1/"

        fun create(): SRCService =
            Retrofit.Builder()
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(HttpRequestInterceptor())
                        .build()
                )
                .baseUrl(BASE_URL)
                .addConverterFactory(
                    MoshiConverterFactory.create(
                        Moshi.Builder()
                            .add(Date::class.java, Rfc3339DateJsonAdapter())
                            .add(TimingMethodAdapter())
                            .add(PlayerCountTypeAdapter())
                            .add(VariableScopeAdapter())
                            .add(CategoryTypeAdapter())
                            .add(ValuesAdapter())
                            .add(StatusAdapter())
                            .add(RoleAdapter())
                            .add(VariablesAndValuesAdapter())
                            .add(
                                PolymorphicJsonAdapterFactory.of(PlayerResponse::class.java, "rel")
                                    .withSubtype(UserResponse::class.java, PlayerType.User.apiString)
                                    .withSubtype(GuestResponse::class.java, PlayerType.Guest.apiString)
                            )
                            .build()
                    )
                )
                .build()
                .create(SRCService::class.java)
    }
}
