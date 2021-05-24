package data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import data.remote.adapters.*
import data.remote.responses.*
import data.remote.utils.UserAgentInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface SRCService {

    // https://github.com/speedruncomorg/api/blob/master/version1/games.md#get-gamesid
    @GET("games/{id}")
    suspend fun fetchFullGame(
        @Path("id") gameId: String,
        // Fixed query params
        @Query("embed") embed: String = "categories.variables,moderators,levels.variables",
    ): PaginatedFullGameResponse

    // https://github.com/speedruncomorg/api/blob/master/version1/games.md#get-games
    @GET("games")
    suspend fun fetchGames(
        @Query("offset") offset: Int?,
        // Fixed query params
        @Query("_bulk") _bulk: Boolean = true,
        @Query("max") max: Int = PAGINATION_MAX_BULK_MODE
    ): PaginatedBulkGamesResponse

    // https://github.com/speedruncomorg/api/blob/master/version1/runs.md#get-runs
    @GET("runs")
    suspend fun fetchRuns(
        @Query("game") gameId: String,
        @Query("category") categoryId: String? = null,
        @Query("status") status: String? = null,
        @Query("orderby") orderBy: String? = null,
        @Query("direction") direction: String? = null,
        @Query("offset") offset: Int? = null,
        // Fixed query params
        @Query("embed") embed: String = "players",
        @Query("max") max: Int = PAGINATION_MAX,
    ): PaginatedRunsResponse

    // https://github.com/speedruncomorg/api/blob/master/version1/runs.md#get-runsid
    @GET("runs/{runId}")
    suspend fun fetchFullRun(
        @Path("runId") runId: String,
        // Fixed query params
        @Query("embed") embed: String = "players,category.variables"
    ): PaginatedFullRunResponse

    // https://github.com/speedruncomorg/api/blob/master/version1/users.md#get-usersid
    @GET("users/{userId}")
    suspend fun fetchUser(
        @Path("userId") userId: String,
    ): PaginatedUserResponse

    companion object {
        private const val BASE_URL = "https://www.speedrun.com/api/v1/"
        const val PAGINATION_MAX = 200
        const val PAGINATION_MAX_BULK_MODE = 1000
        const val PARALLEL_REQUESTS = 20

        fun create(): SRCService =
            Retrofit.Builder()
                .client(
                    OkHttpClient.Builder()
//                        .addInterceptor(LoggerInterceptor())
                        .addInterceptor(UserAgentInterceptor())
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
