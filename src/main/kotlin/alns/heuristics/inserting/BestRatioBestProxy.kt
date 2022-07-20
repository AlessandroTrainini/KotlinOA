package alns.heuristics.inserting

import Instance.InstanceRequest
import alns.Data
import alns.Request

class BestRatioBestProxy : BestRatioFirstProxy() {
    private lateinit var data: Data

    override fun trySomewhereElseWithProxy(candidate: InstanceRequest, data: Data) {
        this.data = data
        val activityIndex = data.agRatioOrder.indexOfFirst { it.first == candidate.id }
        val timeIndex = data.tgRatioOrder.indexOfFirst { it.first == candidate.id }
        val dayIndex = data.dgRatioOrder.indexOfFirst { it.first == candidate.id }
        val r: Request?

        if (activityIndex < timeIndex && activityIndex < dayIndex) { //it's better to change the activity
            r = tryAnotherActivityWithProxy(Request(candidate, proxy = true))
                ?: tryAnotherDayWithProxy(Request(candidate, proxy = true))
                        ?: tryAnotherTimeWithProxy(Request(candidate, proxy = true))

        } else if (timeIndex < activityIndex && timeIndex < dayIndex) { //it's better to change the time
            r = tryAnotherTimeWithProxy(Request(candidate, proxy = true))
                ?: tryAnotherDayWithProxy(Request(candidate, proxy = true))
                        ?: tryAnotherActivityWithProxy(Request(candidate, proxy = true))
        } else { //it's better to change the day
            r = tryAnotherDayWithProxy(Request(candidate, proxy = true))
                ?: tryAnotherActivityWithProxy(Request(candidate, proxy = true))
                        ?: tryAnotherTimeWithProxy(Request(candidate, proxy = true))
        }
        if (r != null) {
            insertionList.add(r)
            data.takeTrustedRequest(r)
        } else if (candidate.proxy != 2)
            trySomewhereElseWithoutProxy(candidate, data)
    }

    private fun tryAnotherActivityWithProxy(r: Request): Request? {
        for (a in data.activitiesOfCategory[data.instance.getCategoryByActivity(r.activity)]) {
            if (data.proxyRequestsInActivity[a][r.day][r.time] > 0) { //is there already a proxy so the capacity won't grow
                if (data.proxyDailyCapacity[r.day] > 0) { //proxy can take this request
                    r.setActivity(a)
                    return r
                }
            } else { //there is no proxy there, trying to put it there if there's room
                if (data.freeSeatsInActivity[a][r.day][r.time] > 0)
                    if (data.proxyDailyCapacity[r.day] > 0) {
                        r.setActivity(a)
                        return r
                    }
            }
        }
        return null
    }

    private fun tryAnotherDayWithProxy(r: Request): Request? {
        for (d in 0 until data.instance.num_days) {
            if (data.proxyDailyCapacity[d] > 0) {
                if (data.proxyRequestsInActivity[r.activity][d][r.time] > 0) { //there is already a proxy in the activity
                    r.setDay(d)
                    return r
                } else if (data.freeSeatsInActivity[r.activity][d][r.time] > 0) {
                    r.setDay(d)
                    return r
                }
            }
        }
        return null
    }

    private fun tryAnotherTimeWithProxy(r: Request): Request? {
        for (t in 0 until data.instance.num_timeslots) {
            if (data.proxyRequestsInActivity[r.activity][r.day][t] > 0) { //is there already a proxy so the capacity won't grow
                if (data.proxyDailyCapacity[r.day] > 0) { //proxy can take this request
                    r.setTime(r.time)
                    return r
                }
            } else { //there is no proxy there, trying to put it there if there's room
                if (data.freeSeatsInActivity[r.activity][r.day][t] > 0)
                    if (data.proxyDailyCapacity[r.day] > 0) {
                        r.setTime(r.time)
                        return r
                    }
            }
        }
        return null
    }
}