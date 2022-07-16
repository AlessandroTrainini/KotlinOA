package alns

import Instance.FileParser
import Instance.Instance
import Instance.InstanceRequest


class Data {
    val instance: Instance = FileParser("inst/istanza_prova.txt").istance
    val taken = arrayListOf<Request>() //requests that are in the current solution
    val missing = arrayListOf<Int>() //ids of requests that could be added at the current solution

    val activityRoom =
        arrayListOf<Array<IntArray>>() //the capacity of each activity, for each day, in each timeslot [a][d][t] -> capacity: Int
    val activityProxy =
        arrayListOf<Array<IntArray>>() //How many proxies are inside each activity in each day in each timeslot [a][d][t] -> presence: Int
    val categoryList: Array<ArrayList<Int>> = Array(instance.num_categories) { arrayListOf() }
    val proxyCapacity = Array(instance.num_days) { instance.num_proxyRequests }


    val gOrder = ArrayList<Pair<Int, Int>>()
    val agRatioOrder = ArrayList<Pair<Int, Float>>()
    val tgRatioOrder = ArrayList<Pair<Int, Float>>()
    val dgRatioOrder = ArrayList<Pair<Int, Float>>()

    /**
     * In this method we create the map of the capacity for each activity, when we'll insert a request in "taken" we first
     * check whether the capacity of the activity in that day and in that timeslot is full or not
     */
    init {
        instance.activities.forEach { a ->
            activityRoom.add(Array(instance.num_days) { IntArray(instance.num_timeslots) { a.capacity } })
            activityProxy.add(Array(instance.num_days) { IntArray(instance.num_timeslots) { 0 } })
            categoryList[a.category].add(a.id)
        }

        instance.requests.forEach { r ->
            gOrder.add(Pair(r.id, r.gain))
            agRatioOrder.add(Pair(r.id, r.gain / r.penalty_A))
            dgRatioOrder.add(Pair(r.id, r.gain / r.penalty_D))
            tgRatioOrder.add(Pair(r.id, r.gain / r.penalty_T))
        }

        gOrder.sortByDescending { it.second }
        agRatioOrder.sortByDescending { it.second }
        dgRatioOrder.sortByDescending { it.second }
        tgRatioOrder.sortByDescending { it.second }

    }

//    private fun takeMandatoryProxyRequest(r: InstanceRequest) {
//        val nr = Request(r, true)
//        if (proxyCapacity[nr.day] > 0) {
//            if (activityRoom[r.activity][r.day][r.timeslot] > 0) {
//                takeRequestTrusted(nr)
//            } else {
//
//            }
//        } else {
//
//        }
//    }

/*    fun takeRequest(r: Request, a: Int = r.activity, d: Int = r.day, t: Int = r.time): Boolean {

        val activityCapacityOk = activityRoom[a][d][t] > 0

        if (r.proxy) {
            if (r.instanceRequest.proxy < 1) return false // Request can't be handled by a proxy

            val proxyCanHandleOtherR = proxyCapacity[d] != 0
            if (!proxyCanHandleOtherR) return false // Proxy is full of requests

            val proxyAlreadyIn = activityProxy[a][d][t] != 0
            if (!proxyAlreadyIn && !activityCapacityOk) return false // Proxy can't enter because activity is full of people

            // Proxy can handle another request. a)"proxy is already inside" OR b)"proxy can go inside"
            if (!proxyAlreadyIn) activityRoom[a][d][t] -= 1 // b) -> proxy takes up a seat
            activityProxy[a][d][t] += 1
            proxyCapacity[d] -= 1

        } else {
            if (!activityCapacityOk) return false // Activity is full of people
            activityRoom[a][d][t] -= 1
        }

        r.activity = a
        r.day = d
        r.time = t

        if (r.instanceRequest.activity != a) r.penalty_A = true
        if (r.instanceRequest.day != d) r.penalty_D = true
        if (r.instanceRequest.timeslot != t) r.penalty_T = true

        missing.remove(r.instanceRequest.id)
        taken.add(r)

        return true
    }*/

    fun takeRequestTrusted(r: Request) {
        taken.add(r)
        missing.remove(r.instanceRequest.id)
        if (r.proxy){
            proxyCapacity[r.day] -= 1
            if (activityProxy[r.activity][r.day][r.time] == 0)
                activityRoom[r.activity][r.day][r.time] += 1
            activityProxy[r.activity][r.day][r.time] += 1
        }
        else
            activityRoom[r.activity][r.day][r.time] += 1
    }

    fun removeRequest(r: Request) : Boolean {
        val r = taken.firstOrNull { it.instanceRequest.id == r.instanceRequest.id } ?: return false
        if (r.proxy) {
            activityProxy[r.activity][r.day][r.time] -= 1
            if (activityProxy[r.activity][r.day][r.time] == 0) activityRoom[r.activity][r.day][r.time] += 1
            proxyCapacity[r.time] += 1
        } else {
            activityRoom[r.activity][r.day][r.time] += 1
        }
        taken.remove(r)
        missing.add(r.instanceRequest.id)
        return true
    }

//    private fun takeRequest(r: InstanceRequest) {
//        val nr = Request(instanceRequest = r, false)
//        if (activityRoom[r.activity][r.day][r.timeslot] >= 1) {
//            taken.add(nr)
//            missing.remove(nr.instanceRequest.id)
//            activityRoom[r.activity][r.day][r.timeslot] -= 1
//        } else
//            tryToCollocateInTheFirst(r)
//    }

    fun takeRequestAt(nr: Request) {
        taken.add(nr)
        missing.remove(nr.instanceRequest.id)
    }

    fun tryToCollocateInTheFirst(r: InstanceRequest) {

        val c = instance.getCategoryByActivity(r.activity)
        //trying changing timeslot and days
        for (a in categoryList[c])
            for (d in 0 until instance.num_days)
                for (t in 0 until instance.num_timeslots)
                    if (activityRoom[a][d][t] >= 1) {
                        val nr = Request(instanceRequest = r, false)
                        taken.add(nr)
                        missing.remove(nr.instanceRequest.id)
                        activityRoom[a][d][t] -= 1
                        return
                    }
    }

    fun dismissRequest(id: Int){
        val r = taken.first {it.instanceRequest.id == id}
        taken.remove(r)
        missing.add(id)
        activityRoom[r.activity][r.day][r.time] += 1
    }
}