import com.google.gson.annotations.SerializedName

data class AthleteStat(
    @SerializedName("athlete")
    val name : Name,
    var distance: Float,
    @SerializedName("moving_time")
    var movingTime : Int
) {
    fun compare(other: AthleteStat): Boolean {
        return name == other.name && distance == other.distance && movingTime == other.movingTime
    }
}

data class Name(
    val firstname : String,
    val lastname : String
)
