import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.LocalDate

interface LocalService {
    @POST("athlete")
    @FormUrlEncoded
    suspend fun addAthlete(
        @Field("name") name : String
    ) : Response<Unit>
    @POST("athletes/activity")
    @FormUrlEncoded
    suspend fun addActivity(
        @Field("name") name : String,
        @Field("distance") distance : Float,
        @Field("totalTime") totalTime : Int,
        @Field("pace") pace : String,
        @Field("date") date : LocalDate
    ) : Response<Unit>
    @GET("athletes/activity")
    suspend fun getActivitiesAtDate(
        @Query("date") date : LocalDate
    ) : Response<List<AthleteStat>>
}