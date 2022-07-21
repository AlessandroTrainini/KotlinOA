package alns

import Instance.InstanceRequest

data class Request(
    val instanceRequest: InstanceRequest,
    var proxy: Boolean,
    var day: Int = instanceRequest.day,
    var time: Int = instanceRequest.timeslot,
    var activity: Int = instanceRequest.activity,
    var penalty_A: Boolean = instanceRequest.activity != activity,
    var penalty_D: Boolean = instanceRequest.day != day,
    var penalty_T: Boolean = instanceRequest.timeslot != time
)
{
    override fun toString(): String {
        return "${instanceRequest.id} - $day - $time - $activity - $penalty_A - $penalty_D - $penalty_T - $proxy"
    }

    @JvmName("setActivity1")
    fun setActivity(activity: Int) {
        this.activity = activity
        penalty_A = instanceRequest.activity != activity
    }

    @JvmName("setDay1")
    fun setDay(day: Int) {
        this.day = day
        penalty_D = instanceRequest.day != day
    }

    @JvmName("setTime1")
    fun setTime(time: Int) {
        this.time = time
        penalty_T = instanceRequest.timeslot != time
    }
}

