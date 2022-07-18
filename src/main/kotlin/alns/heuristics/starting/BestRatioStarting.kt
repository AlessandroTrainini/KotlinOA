package alns.heuristics.starting

import alns.Data
import alns.Request
import alns.heuristics.StartingHeuristic

class BestRatioStarting : StartingHeuristic {

    /**
     *
     */
    override fun generateStartingPoint(data: Data){
        val toCollocate = data.instance.requests.sortedByDescending { it.gain }
        for (ir in data.instance.requests.sortedByDescending { it.gain }) {
            val d = ir.day
            val t = ir.timeslot
            val a = ir.activity
            val p = ir.proxy
            if (p > 0) { //trying to give it to a proxy in order to save activity capacity
                if (data.proxyDailyCapacity[d] > 0) //in that day, proxy can serve this request
                    if (data.proxyRequestsInActivity[a][d][t] > 0)  //a proxy is already in that activity, we don't need to subtract capacity, we can just add the request
                        data.takeTrustedRequest(Request(ir, true))
                    else //there is no proxy in the activity, check if there is enough space
                        if (data.freeSeatsInActivity[a][d][t] > 0) //in this case there is enough space, and proxy can take the request

                        else

            } else {
                if (data.freeSeatsInActivity[a][d][t] > 1)  //there is space for both the current request and a proxy
                    data.takeTrustedRequest(Request(ir, false))
            }
        }



        for (a in 0 until data.instance.num_activities)
            for (d in 0 until data.instance.num_days)
                for (t in 0 until data.instance.num_timeslots) {
                    if (data.freeSeatsInActivity[a][d][t] > 0) {
                        val nr = data.instance.requests.firstOrNull { it.activity == a && it.day == d && it.timeslot == t }
                        if (nr != null)

                    }

                }

    }

}