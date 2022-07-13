package alns

import Instance.FileParser
import Instance.Instance
import Instance.InstanceRequest

class Data {
    val instance: Instance = FileParser("inst/istanza_prova.txt").istance
    val taken = arrayListOf<Request>() //requests that are in the current solution
    val missing = arrayListOf<Int>() //ids of requests that could be added at the current solution

    private val activityRoom = arrayListOf<Array<IntArray>>()
    private val categoryList: Array<ArrayList<Int>> = Array(instance.num_categories) { arrayListOf() }
    private val proxyCapacity = Array(instance.num_days) { instance.num_proxyRequests }

    private val gOrder = ArrayList<Pair<Int, Int>>()
    private val agRatioOrder = ArrayList<Pair<Int, Float>>()
    private val tgRatioOrder = ArrayList<Pair<Int, Float>>()
    private val dgRatioOrder = ArrayList<Pair<Int, Float>>()

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
            gOrder.add(Pair(r.id, r.gain))
            agRatioOrder.add(Pair(r.id, r.gain / r.penalty_A))
            dgRatioOrder.add(Pair(r.id, r.gain / r.penalty_D))
            tgRatioOrder.add(Pair(r.id, r.gain / r.penalty_T))

            if (r.proxy == 2)
                takeMandatoryProxyRequest(r)
            else
                missing.add(r.id)
        }

        gOrder.sortByDescending { it.second }
        agRatioOrder.sortByDescending { it.second }
        dgRatioOrder.sortByDescending { it.second }
        tgRatioOrder.sortByDescending { it.second }

        print(dgRatioOrder)
    }

    private fun takeMandatoryProxyRequest(r: InstanceRequest) {
        val nr = Request(r.id, r.day, r.timeslot, r.activity, r.proxy, true)
        if (proxyCapacity[nr.day] > 0) {
            if (activityRoom[r.activity][r.day][r.timeslot] > 0) {
                takeRequest(nr)
            } else {

            }
        } else {

        }
    }

    private fun takeRequest(nr: Request, a: Int = nr.activity, d: Int = nr.day, t: Int = nr.time) {
        if (nr.activity != a) nr.penalty_A = instance.getPenaltyAByRequest(nr.id).toInt()
        if (nr.day != d) nr.penalty_D = instance.getPenaltyDByRequest(nr.id).toInt()
        if (nr.time != t) nr.penalty_T = instance.getPenaltyAByRequest(nr.id).toInt()
        taken.add(nr)
        missing.remove(nr.id)
    }

//    private fun takeRequest(r: InstanceRequest, p: Boolean) {
//        val nr = Request(r.id, r.day, r.timeslot, r.activity, p)
//        if (activityRoom[r.activity][r.day][r.timeslot] >= 1) {
//            taken.add(nr)
//            missing.remove(nr.id)
//            activityRoom[r.activity][r.day][r.timeslot] -= 1
//        } else
//            tryToCollocateInTheFirst(r, p)
//    }

//    fun takeRequestAt(r: InstanceRequest, a: Int, d: Int, t: Int, p: Boolean) {
//        val nr = Request(r.id, d, t, a, p)
//        taken.add(nr)
//        missing.remove(r.id)
//    }

//    fun tryToCollocateInTheFirst(r: InstanceRequest, p: Boolean) {
//
//        val c = instance.getCategoryByActivity(r.activity)
//        //trying changing timeslot and days
//        for (a in categoryList[c])
//            for (d in 0 until instance.num_days)
//                for (t in 0 until instance.num_timeslots)
//                    if (activityRoom[a][d][t] >= 1) {
//                        val nr = Request(r.id, d, t, a, p)
//                        taken.add(nr)
//                        missing.remove(nr.id)
//                        activityRoom[a][d][t] -= 1
//                        return
//                    }
//    }

//    fun dismissRequest(id: Int){
//        val r = taken.first {it.id == id}
//        taken.remove(r)
//        missing.add(id)
//        activityRoom[r.activity][r.day][r.time] += 1
//    }


}