package alns.heuristics.starting

import alns.Data
import alns.Request
import alns.heuristics.StartingHeuristic

class BestStarting : StartingHeuristic {

    private lateinit var data: Data


    override fun generateStartingPoint(data: Data) {

        for (ir in data.instance.requests.sortedByDescending { it.gain }) {
            val r = Request(ir, false)
            this.data = data
            locate(r)
        }
    }

    private fun locate(r: Request) {

        val d = r.day
        val t = r.time
        val a = r.activity
        val p = r.instanceRequest.proxy

        if (p > 0) { //trying to give it to a proxy in order to save activity capacity
            if (data.proxyDailyCapacity[d] > 0) { //in that day, proxy can serve this request
                if (data.proxyRequestsInActivity[a][d][t] > 0) { //a proxy is already in that activity, we don't need to subtract capacity, we can just add the request
                    r.proxy = true
                    data.takeNotTrustedRequest(r)
                } else { // there is no proxy in the activity, check if there is enough space
                    if (data.freeSeatsInActivity[a][d][t] > 0) { //in this case there is enough space, and proxy can take the request
                        r.proxy = true
                        data.takeNotTrustedRequest(r)
                    } else { // There is no space in the activity
                        findOrProxyOrGoodLocation(r)
                    }
                }
            } else { // in that day, proxy can't serve this request
                findOrProxyOrGoodLocation(r)
            }
        } else { // Request can't be taken by a proxy
            if (data.freeSeatsInActivity[a][d][t] > 1)  // there is space for both the current request and a proxy
                data.takeNotTrustedRequest(r)
            else { // There is no space
                findGoodLocation(r)
            }
        }
    }

    private fun findOrProxyOrGoodLocation(r: Request): Boolean {
        return if (r.instanceRequest.proxy != 2) {
            findGoodLocation(r)
        } else {
            findProxyLocation(r)
        }
    }

    private fun findProxyLocation(r: Request): Boolean {
        var ok = false
        for (d in (0 until data.instance.num_days).shuffled())
            if (data.proxyDailyCapacity[d] > 0)
                if (data.proxyRequestsInActivity[r.activity][d][r.time] > 0 || data.freeSeatsInActivity[r.activity][d][r.time] > 0) { //in this day there is already a proxy that can serve the activity, top
                    r.setDay(d)
                    r.proxy = true
                    ok = data.takeNotTrustedRequest(r).first
                    break
                }

        return ok
    }

    private fun findGoodLocation(r: Request): Boolean {
        var ok = false

        val activityIndex = data.agRatioOrder.indexOfFirst { it.first == r.instanceRequest.id }
        val timeIndex = data.tgRatioOrder.indexOfFirst { it.first == r.instanceRequest.id }
        val dayIndex = data.dgRatioOrder.indexOfFirst { it.first == r.instanceRequest.id }

        if (activityIndex > timeIndex && activityIndex > dayIndex) { //it's better to change the activity
            for (a in data.activitiesOfCategory[data.instance.getCategoryByActivity(r.instanceRequest.activity)].shuffled())
                if (data.freeSeatsInActivity[a][r.day][r.time] > 0) {
                    r.setActivity(a)
                    data.takeNotTrustedRequest(r)
                    ok = true
                    break
                }
        } else if (timeIndex > activityIndex && timeIndex > dayIndex) { //it's better to change the time
            for (d in (0 until data.instance.num_days).shuffled())
                if (data.freeSeatsInActivity[r.activity][d][r.time] > 0) {
                    r.setDay(d)
                    data.takeNotTrustedRequest(r)
                    ok = true
                    break
                }
        } else { //it's better to change the day
            for (t in (0 until data.instance.num_timeslots).shuffled())
                if (data.freeSeatsInActivity[r.activity][r.day][t] > 0) {
                    r.setTime(t)
                    data.takeNotTrustedRequest(r)
                    ok = true
                    break
                }
        }

        if (!ok) data.missing.add(r.instanceRequest.id)
        return ok
    }

}