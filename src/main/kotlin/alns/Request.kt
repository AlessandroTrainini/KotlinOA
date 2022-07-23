package alns

import Instance.InstanceRequest

data class Request(
    val instanceRequest: InstanceRequest,
    var proxy: Boolean,
    private var day: Int = instanceRequest.day,
    private var time: Int = instanceRequest.timeslot,
    private var activity: Int = instanceRequest.activity,
    var penalty_A: Boolean = instanceRequest.activity != activity,
    var penalty_D: Boolean = instanceRequest.day != day,
    var penalty_T: Boolean = instanceRequest.timeslot != time
) {

    fun getA(): Int {
        return activity
    }

    fun getD(): Int {
        return day
    }

    fun getT(): Int {
        return time
    }

    fun setActivity(activity: Int) {
        this.activity = activity
        penalty_A = instanceRequest.activity != activity
    }

    fun setDay(day: Int) {
        this.day = day
        penalty_D = instanceRequest.day != day
    }

    fun setTime(time: Int) {
        this.time = time
        penalty_T = instanceRequest.timeslot != time
    }

    private operator fun Boolean.times(x: Double): Double {
        return if (this) x else 0.toDouble()
    }

    override fun toString(): String {
        val gain = instanceRequest.gain -
                penalty_A * instanceRequest.penalty_A -
                penalty_D * instanceRequest.penalty_D -
                penalty_T * instanceRequest.penalty_T
        return "${instanceRequest.id} - " +
                "$activity | ${instanceRequest.activity} - " +
                "$day | ${instanceRequest.day} - " +
                "$time | ${instanceRequest.timeslot} - " +
                "$proxy | ${instanceRequest.proxy} - " +
                "${instanceRequest.gain} - " +
                "${if (penalty_A) instanceRequest.penalty_A else 0} - " +
                "${if (penalty_D) instanceRequest.penalty_D else 0} - " +
                "${if (penalty_T) instanceRequest.penalty_T else 0} - " +
                "$gain\n"
    }
}


