package alns

import Instance.InstanceRequest

data class Request(
    val instanceRequest: InstanceRequest,
    var proxy: Boolean,
    var day: Int = instanceRequest.day,
    var time: Int = instanceRequest.timeslot,
    var activity: Int = instanceRequest.activity,
    var penalty_A: Boolean = false,
    var penalty_D: Boolean = false,
    var penalty_T: Boolean = false
)
{
    override fun toString(): String {
        return instanceRequest.id.toString()
    }
}