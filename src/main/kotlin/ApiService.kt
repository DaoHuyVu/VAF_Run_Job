import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @GET("clubs/{id}/activities")
    suspend fun getData(
        @Path("id") id : Long,
        @Header("Authorization") token : String,
        @Query("page") page : Int ,
        @Query("after") epochTime : Long,
        @Query("per_page") perPage : Int = 200
    ) : Response<List<AthleteStat>>
    @GET("clubs/{id}/members")
    suspend fun getMembers(
        @Path("id") id : Long,
        @Header("Authorization") token : String,
        @Query("page") page : Int = 1,
        @Query("per_page") perPage : Int = 200
    ) : Response<List<Athlete>>
    @POST("oauth/token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("client_id") clientId : String,
        @Field("client_secret") clientSecret : String,
        @Field("refresh_token") refreshToken : String,
        @Field("grant_type") type : String = "refresh_token"
    ) : Response<RefreshTokenResponse>
}