package alns.heuristics.starting

import alns.Data
import alns.Request
import alns.heuristics.StartingHeuristic
import tools.ProgressBar

class BestStarting : StartingHeuristic {

    private lateinit var data: Data


    override fun generateStartingPoint(data: Data) {
        val progressBar = ProgressBar(data.instance.requests.size)
        for (ir in data.instance.requests.sortedByDescending { it.gain }) {
            val r = Request(ir, false)
            this.data = data
            locateRequest(r)
            progressBar.updateProgressBar()
        }
    }

    private fun locateRequest(r: Request) {

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
            if (data.freeSeatsInActivity[a][d][t] > 0)  // there is space for both the current request and a proxy
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
            r.proxy = true
            var ok = findProxyLocation(r)
            if (!ok) { // The exhaustive search failed -> replace this request with the first not mandatory
                ok = replaceRequest(r)
                if (!ok) error("Implossible problem!")
            }
            ok
        }
    }

    // This function inserts proxy_r in place of a replaceable request
    private fun replaceRequest(proxy_r: Request): Boolean {
        for (i in (0 until data.taken.size).shuffled()) { // loop on taken requests
            val r = data.taken[i]
            if (r.instanceRequest.proxy < 2 &&
                data.activitiesOfCategory[data.instance.getCategoryByActivity(r.activity)].contains(proxy_r.activity)
            ) { // not mandatory request & compatible r activity
                if (r.proxy || data.proxyDailyCapacity[r.day] > 0) { // proxy_r has to be handled by a proxy. Either a request managed by a proxy is removed (space is freed), or a request is removed on a day when the proxy is not full
                    data.removeRequest(r)
                    proxy_r.setActivity(r.activity)
                    proxy_r.setDay(r.day)
                    proxy_r.setTime(r.time)
                    data.takeNotTrustedRequest(proxy_r)
                    return true
                }
            }
        }
        return false
    }

    // This function look for a good proxy request position:
    // first tries to change the day
    // If this first attempt is unsuccessful, it performs an exhaustive search in all possible a, d, t
    private fun findProxyLocation(r: Request): Boolean {
        var ok = false
        val suitableDays = mutableListOf<Int>()

        for (d in (0 until data.instance.num_days).shuffled()) { // First try: only try to change the day
            if (data.proxyDailyCapacity[d] > 0) {
                if (data.proxyRequestsInActivity[r.activity][d][r.time] > 0 || data.freeSeatsInActivity[r.activity][d][r.time] > 0) { //in this day there is already a proxy that can serve the activity, top
                    r.setDay(d)
                    ok = data.takeNotTrustedRequest(r).first
                    if (ok) break
                    suitableDays.add(d)
                }
            }
        }

        if (!ok) { // Second try: look for a seat in all the available space
            for (d in suitableDays) {
                ok = setSuitableProxyAT(r, d)
                if (ok) break
            }
        }

        return ok
    }

    private fun setSuitableProxyAT(r: Request, d: Int): Boolean {
        var ok = false
        val category = data.instance.getCategoryByActivity(r.instanceRequest.activity)
        for (a in data.activitiesOfCategory[category].shuffled()) {
            r.setActivity(a)
            for (t in (0 until data.instance.num_timeslots).shuffled()) {
                if (data.proxyRequestsInActivity[a][d][t] > 0 || data.freeSeatsInActivity[a][d][t] > 0) {
                    r.setTime(t)
                    ok = data.takeNotTrustedRequest(r).first
                    break
                }
            }
        }
        return ok
    }

    private fun findGoodLocation(r: Request): Boolean {
        var ok = false

        val activityIndex = data.agRatioOrder.indexOfFirst { it.first == r.instanceRequest.id }
        val dayIndex = data.dgRatioOrder.indexOfFirst { it.first == r.instanceRequest.id }
        val timeIndex = data.tgRatioOrder.indexOfFirst { it.first == r.instanceRequest.id }

        val funOrder = mutableListOf(
            Pair(activityIndex) { request: Request -> findGoodLocationActivity(request) },
            Pair(dayIndex) { request: Request -> findGoodLocationDay(request) },
            Pair(timeIndex) { request: Request -> findGoodLocationTime(request) },
        )

        funOrder.sortByDescending { it.first }

        for (fo in funOrder) {
            ok = fo.second(r)
            if (ok) break
        }

        if (!ok) data.missing.add(r.instanceRequest.id)
        return ok
    }

    private fun findGoodLocationActivity(r: Request): Boolean {
        var ok = false
        for (a in data.activitiesOfCategory[data.instance.getCategoryByActivity(r.instanceRequest.activity)].shuffled())
            if (data.freeSeatsInActivity[a][r.day][r.time] > 0) {
                r.setActivity(a)
                data.takeNotTrustedRequest(r)
                ok = true
                break
            }
        return ok
    }

    private fun findGoodLocationDay(r: Request): Boolean {
        var ok = false
        for (d in (0 until data.instance.num_days).shuffled())
            if (data.freeSeatsInActivity[r.activity][d][r.time] > 0) {
                r.setDay(d)
                data.takeNotTrustedRequest(r)
                ok = true
                break
            }
        return ok
    }

    private fun findGoodLocationTime(r: Request): Boolean {
        var ok = false
        for (t in (0 until data.instance.num_timeslots).shuffled())
            if (data.freeSeatsInActivity[r.activity][r.day][t] > 0) {
                r.setTime(t)
                data.takeNotTrustedRequest(r)
                ok = true
                break
            }
        return ok
    }

}