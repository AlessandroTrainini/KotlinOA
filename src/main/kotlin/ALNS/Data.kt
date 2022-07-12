package ALNS

import Instance.FileParser
import Instance.InstanceRequest

class Data {
    val instance = FileParser("inst/istanza_prova.txt").istance
    val taken = arrayListOf<Request>() //requests that are in the current solution
    val missing = arrayListOf<Int>() //ids of requests that could be added at the current solution

    var activityRoom = arrayListOf<Array<IntArray>>()
    var categoryList: Array<ArrayList<Int>> = Array(instance.num_categories) { arrayListOf() }

    /**
     * In this method we create the map of the capacity for each activity, when we'll insert a request in "taken" we first
     * check whether the capacity of the activity in that day and in that timeslot is full or not
     */
    init {
        instance.activities.forEach { a ->
            activityRoom.add(Array(instance.num_days) { IntArray(instance.num_timeslots) { a.capacity } })
            categoryList[a.category].add(a.id)
        }

        instance.requests.forEach { r ->
            if (r.proxy == 2)
                takeRequest(r)
            else
                missing.add(r.id)
        }
        println(taken)
    }

    fun takeRequest(r: InstanceRequest) {
        val nr = Request(r.id, r.day, r.timeslot, r.activity)
        if (activityRoom[r.activity][r.day][r.timeslot] >= 1) {
            taken.add(nr)
            missing.remove(nr.id)
            activityRoom[r.activity][r.day][r.timeslot] -= 1
        } else
            tryToCollocateInTheFirst(r)
    }

    fun takeRequestAt(r: InstanceRequest, a: Int, d: Int, t: Int) {
        val nr = Request(r.id, d, t, a)
        taken.add(nr)
        missing.remove(r.id)
    }

    fun tryToCollocateInTheFirst(r: InstanceRequest) {

        val c = instance.getCategoryByActivity(r.activity)
        //trying changing timeslot and days
        for (a in categoryList[c])
            for (d in 0 until instance.num_days)
                for (t in 0 until instance.num_timeslots)
                    if (activityRoom[a][d][t] >= 1) {
                        val nr = Request(r.id, d, t, a)
                        taken.add(nr)
                        missing.remove(nr.id)
                        activityRoom[a][d][t] -= 1
                        return
                    }
    }

    fun dismissRequest(id: Int){
        val r = taken.first {it.id == id}
        taken.remove(r)
        missing.add(id)
        activityRoom[r.activity][r.day][r.time] += 1
    }


}