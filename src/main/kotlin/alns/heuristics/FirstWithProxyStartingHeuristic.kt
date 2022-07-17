package alns.heuristics

import alns.Data
import alns.Request

class FirstWithProxyStartingHeuristic : StartingHeuristic {

    override fun generateStartingPoint(data: Data): List<Request> {
        val list = mutableListOf<Request>()
        for (ir in data.instance.requests.sortedByDescending { it.gain }) {
            val d = ir.day
            val t = ir.timeslot
            val a = ir.activity
            val proxy = ir.proxy
            if (proxy >= 1) { //trying to give it to a proxy in order to save activity capacity
                if (data.proxyDailyCapacity[d] > 0) //in that day, proxy can serve this request
                    if (data.proxyRequestsInActivity[a][d][t] > 0)  //a proxy is already in that activity, we don't need to subtract capacity, we can just add the request
                        list.add(Request(ir, proxy = true))
                    else //there is no proxy in the activity, check if there is enough space
                        if (data.freeSeatsInActivity[a][d][t] >= 1) //in this case there is enough space, and proxy can take the request
                            list.add(Request(ir, proxy = true))

            }
        }
        return list
    }

}