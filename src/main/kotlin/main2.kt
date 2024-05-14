import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
fun main(){
    val token = "Bearer 1bc590c876e84b751d3afe6c35dd85878c949f8d"
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
            try{
                val response = withContext(Dispatchers.IO){
                    apiService.getMembers(clubId,token)
                }
                val athletes = response.body()!!
                athletes.forEach{athlete ->
                    try{
                        val name = athlete.firstname + " " + athlete.lastname
                        val result = localService.addAthlete(name)
                        if(!result.isSuccessful){
                            println("Add failed")
                            return@runBlocking
                        }
                    }catch(ex : Exception){
                        ex.printStackTrace()
                    }
                }
            }
            catch(ex : Exception){
                ex.printStackTrace()
            }
    }
}