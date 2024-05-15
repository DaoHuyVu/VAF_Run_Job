import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.time.*



fun main(){
    var insertedRemainActivities = false
    val clientId = "126247"
    val clientSecret = "1624d97e749d5eb324fbb47626e40e5e979f461c"
    val file = File("D://token.txt")
    if(!file.exists()){
        println("File token.txt isn't exist")
        return
    }
    val lines = file.readLines()
    var refreshToken = lines[1]
    var accessToken = "1410a99ab45033920c1d7928fe0c26fcf3380c95"

    fun getToken() = "Bearer $accessToken"

    val currentDate = LocalDate.now(ZoneId.of("GMT+7"))
    val midNightTime = LocalTime.of(0,0,0)
    val currentEpoch = LocalDateTime.of(currentDate,midNightTime).atZone(ZoneId.of("GMT+7")).toEpochSecond()
    val previousDateEpoch = currentEpoch - 86400
    val previousDate = Instant.ofEpochSecond(previousDateEpoch).atZone(ZoneId.of("GMT+7")).toLocalDate()
    val clubId = 1241688L

    val retrofit = Retrofit.Builder()
        .baseUrl("https://www.strava.com/api/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val localRetrofit = Retrofit.Builder()
        .baseUrl("http://localhost:9192/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    val localService = localRetrofit.create(LocalService::class.java)
    runBlocking {
       while(true){
           try{
               for(i in 1..2){
                   val response = withContext(Dispatchers.IO){
                       apiService.getData(clubId,getToken(),i, currentEpoch)
                   }
                   if(response.isSuccessful){
                       val currentDateActivities = response.body()!!
                       if(!insertedRemainActivities){
                           val totalActivitiesResponse = withContext(Dispatchers.IO){
                               apiService.getData(clubId,getToken(),i,previousDateEpoch)
                           }
                           if(!totalActivitiesResponse.isSuccessful){
                               println("totalActivityResponse : FAILED")
                               return@runBlocking
                           }

                           val insertedPreviousActivitiesResponse = withContext(Dispatchers.IO){
                               localService.getActivitiesAtDate(previousDate)
                           }
                           if(!insertedPreviousActivitiesResponse.isSuccessful){
                               println("insertedPreviousActivitiesResponse : FAILED")
                               return@runBlocking
                           }
                           val totalActivities = totalActivitiesResponse.body()!!.toMutableList()
                           val insertedPreviousActivities = insertedPreviousActivitiesResponse.body()!!
                           currentDateActivities.forEach { activity ->
                               val a = totalActivities.firstOrNull{ athleteStat ->
                                   activity.compare(athleteStat)
                               }
                               a?.let{totalActivities.remove(it)}
                           }
                           insertedPreviousActivities.forEach { activity ->
                               val a = totalActivities.firstOrNull{ athleteStat ->
                                   activity.compare(athleteStat)
                               }
                               a?.let{totalActivities.remove(it)}
                           }
                           totalActivities.forEach { stat ->
                                val name = "${stat.name.firstname} ${stat.name.lastname}"
                                val pace: String
                                if(stat.movingTime == 0 || stat.distance == 0f){
                                    pace = "00:00"
                                }
                                else{
                                    val paceKm = stat.movingTime/(stat.distance/1000)/60
                                    val paceMinutes = paceKm.toInt()
                                    val paceSeconds = ((paceKm - paceMinutes)*60).toInt()
                                    pace = String.format("%02d:%02d",paceMinutes,paceSeconds)
                                }
                               val res = localService.addActivity(name,stat.distance,stat.movingTime,pace, previousDate)
                               if(!res.isSuccessful){
                                   println(res.code())
                                   return@runBlocking
                               }
                           }
                       }
                       currentDateActivities.forEach { stat ->
                           val name = "${stat.name.firstname} ${stat.name.lastname}"
                           val paceKm = stat.movingTime/(stat.distance/1000)/60
                           val paceMinutes = paceKm.toInt()
                           val paceSeconds = ((paceKm - paceMinutes)*60).toInt()
                           val pace = String.format("%02d:%02d",paceMinutes,paceSeconds)
                           val res = localService.addActivity(name,stat.distance,stat.movingTime,pace,currentDate)
                           if(!res.isSuccessful){
                               println(res.code())
                               return@runBlocking
                           }
                       }
                       println("Updated")
                   }
                   else if(response.code() == 401){
                        val res = apiService.getAccessToken(
                            clientId,clientSecret,refreshToken
                        )
                        if(res.isSuccessful){
                            accessToken = res.body()!!.accessToken
                            refreshToken = res.body()!!.refreshToken
                            println(res.body())
                            file.writeText("$accessToken\n$refreshToken")
                            println("Token refreshed")
                        }
                        else{
                            println("Refresh token failed . Error code ${res.code()}")
                            println(res.errorBody()?.string())
                            return@runBlocking
                        }
                   }
                   else{
                       println("Error code : " + response.code())
                       return@runBlocking
                   }
               }
               insertedRemainActivities = true
               delay(1000*60*5)
           }
           catch(ex : Exception){
               ex.printStackTrace()
               break
           }
       }
    }
}
