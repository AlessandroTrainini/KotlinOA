package alns

data class Request(
    val id: Int,
    var day: Int,
    var time: Int,
    var activity: Int,
    var proxyMandatory: Int,
    var proxy: Boolean,
    var penalty_A: Int = 0,
    var penalty_D: Int = 0,
    var penalty_T: Int = 0
)
{
    override fun toString(): String {
        return instanceRequest.id.toString()
    }
}