package alns

import Instance.FileParser
import Instance.Instance


class Data {
    val instance: Instance = FileParser("inst/OTSP4.txt").istance
    val taken = arrayListOf<Request>() //requests that are in the current solution
    private val missing = arrayListOf<Int>() //ids of requests that could be added at the current solution

    val freeSeatsInActivity =
        arrayListOf<Array<IntArray>>() // The capacity of each activity, for each day, in each timeslot [a][d][t] -> capacity: Int
    val proxyRequestsInActivity =
        arrayListOf<Array<IntArray>>() // How many requests are handled by a proxy inside each activity in each day in each timeslot [a][d][t] -> presence: Int
    val activitiesOfCategory: Array<ArrayList<Int>> = Array(instance.num_categories) { arrayListOf() }
    val proxyDailyCapacity = Array(instance.num_days) { instance.num_proxyRequests }


    val gOrder = ArrayList<Pair<Int, Int>>()
    val agRatioOrder = ArrayList<Pair<Int, Double>>()
    val tgRatioOrder = ArrayList<Pair<Int, Double>>()
    val dgRatioOrder = ArrayList<Pair<Int, Double>>()

    /**
     * In this method we create the map of the capacity for each activity, when we'll insert a request in "taken" we first
     * check whether the capacity of the activity in that day and in that timeslot is full or not
     */
    init {
        instance.activities.forEach { a ->
            freeSeatsInActivity.add(Array(instance.num_days) { IntArray(instance.num_timeslots) { a.capacity } })
            proxyRequestsInActivity.add(Array(instance.num_days) { IntArray(instance.num_timeslots) { 0 } })
            activitiesOfCategory[a.category].add(a.id)
        }

        instance.requests.forEach { r ->
            gOrder.add(Pair(r.id, r.gain))
            agRatioOrder.add(Pair(r.id, r.gain / r.penalty_A))
            dgRatioOrder.add(Pair(r.id, r.gain / r.penalty_D))
            tgRatioOrder.add(Pair(r.id, r.gain / r.penalty_T))
            addIdToMissing(r.id)
        }

        gOrder.sortByDescending { it.second }
        agRatioOrder.sortByDescending { it.second }
        dgRatioOrder.sortByDescending { it.second }
        tgRatioOrder.sortByDescending { it.second }

    }

    fun takeNotTrustedRequest(r: Request): Pair<Boolean, Int> {

        val a = r.getA()
        val d = r.getD()
        val t = r.getT()

        // if (taken.map { it.instanceRequest.id }.contains(r.instanceRequest.id)) error("Request already taken")

        if (instance.getCategoryByActivity(a) != instance.getCategoryByActivity(r.instanceRequest.activity))
            return Pair(false, 5) // Chosen activity of wrong category

        val activityCapacityOk = freeSeatsInActivity[a][d][t] > 0

        if (r.proxy) {
            if (r.instanceRequest.proxy < 1) return Pair(false, 1) // Request can't be handled by a proxy

            val proxyCanHandleOtherR = proxyDailyCapacity[d] > 0
            if (!proxyCanHandleOtherR) return Pair(false, 2) // Proxy is full of requests

            val proxyAlreadyIn = proxyRequestsInActivity[a][d][t] > 0
            // Proxy can't enter because activity is full of people
            if (!proxyAlreadyIn && !activityCapacityOk) return Pair(false, 3)

            // Proxy can handle another request. a)"proxy is already inside" OR b)"proxy can go inside"
            if (!proxyAlreadyIn) freeSeatsInActivity[a][d][t] -= 1 // b) -> proxy takes up a seat
            proxyRequestsInActivity[a][d][t] += 1
            proxyDailyCapacity[d] -= 1

        } else {
            if (!activityCapacityOk) return Pair(false, 4) // Activity is full of people
            freeSeatsInActivity[a][d][t] -= 1
        }

//        val result = setPenalty(r)
//        if (!result.first) return result

        removeIdFromMissing(r.instanceRequest.id)
        taken.add(r)

        return Pair(true, 0)
    }

    fun removeRequest(toDelete: Request): Boolean {
        val r = taken.firstOrNull { it.instanceRequest.id == toDelete.instanceRequest.id } ?: return false
        val result = taken.remove(r)
        if (!result) error("Can't remove request ${r.instanceRequest.id}")
        addIdToMissing(r.instanceRequest.id)
        if (r.proxy) {
            proxyRequestsInActivity[r.getA()][r.getD()][r.getT()] -= 1
            if (proxyRequestsInActivity[r.getA()][r.getD()][r.getT()] == 0)
                freeSeatsInActivity[r.getA()][r.getD()][r.getT()] += 1
            proxyDailyCapacity[r.getD()] += 1
        } else {
            freeSeatsInActivity[r.getA()][r.getD()][r.getT()] += 1
        }
        return true
    }

    private fun addIdToMissing(id: Int) {
//        if (missing.contains(id))
//            error("Request id already in missing!")
        missing.add(id)
    }

    private fun removeIdFromMissing(id: Int) {
        if (!missing.remove(id))
            error("Can't remove id from missing")
//        else if (missing.contains(id))
//            error("Request still in missing")
    }

    fun getMissing(): ArrayList<Int> { return missing }

    fun checkFeasibility() {
        val takenIDs = taken.map{it.instanceRequest.id}
        if (takenIDs.size != takenIDs.distinct().size) error("Duplicates in taken")
        val missingIDs = missing
        if (missingIDs.size != missingIDs.distinct().size) error("Duplicates in missing")
        val IDs = instance.requests.map { it.id }
        val instanceIDs = IDs.toSet()
        val runIDs = takenIDs.toSet().union(missingIDs.toSet())
        if (!instanceIDs.containsAll(runIDs)) error("We have more ids :0")
        if (!runIDs.containsAll(instanceIDs)) error("We have lost some id")
    }

//    private fun setPenalty(r: Request): Pair<Boolean, Int> {
//
//        val a = r.activity
//        val t = r.time
//        val d = r.day
//
//        if (r.instanceRequest.activity != a) {
//            if (instance.getCategoryByActivity(a) != instance.getCategoryByActivity(r.instanceRequest.activity))
//                return Pair(false, 5) // Chosen activity of wrong category
//            r.penalty_A = true
//        }
//        if (r.instanceRequest.day != d) r.penalty_D = true
//        if (r.instanceRequest.timeslot != t) r.penalty_T = true
//
//        return Pair(true, 0)
//    }

//    fun takeTrustedRequest(r: Request) {
//        if (r.proxy) {
//            proxyDailyCapacity[r.day] -= 1
//            if (proxyRequestsInActivity[r.activity][r.day][r.time] == 0)
//                freeSeatsInActivity[r.activity][r.day][r.time] -= 1
//            proxyRequestsInActivity[r.activity][r.day][r.time] += 1
//        } else
//            freeSeatsInActivity[r.activity][r.day][r.time] -= 1
//
//        setPenalty(r)
//
//        missing.remove(r.instanceRequest.id)
//        taken.add(r)
//    }

//    fun tryToCollocateInTheFirst(r: InstanceRequest) {
//        val c = instance.getCategoryByActivity(r.activity)
//        //trying changing timeslot and days
//        for (a in activitiesOfCategory[c])
//            for (d in 0 until instance.num_days)
//                for (t in 0 until instance.num_timeslots)
//                    if (freeSeatsInActivity[a][d][t] >= 1) {
//                        val nr = Request(instanceRequest = r, false)
//                        taken.add(nr)
//                        missing.remove(nr.instanceRequest.id)
//                        freeSeatsInActivity[a][d][t] -= 1
//                        return
//                    }
//    }

}