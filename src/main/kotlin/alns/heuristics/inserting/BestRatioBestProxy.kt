package alns.heuristics.inserting

import Instance.InstanceRequest
import alns.Data
import alns.Request

class BestRatioBestProxy : BestRatioFirstProxy() {

    override fun trySomewhereElseWithProxy(candidate: InstanceRequest, data: Data) {
        val activityIndex = data.agRatioOrder.indexOfFirst { it.first == candidate.id }
        val timeIndex = data.tgRatioOrder.indexOfFirst { it.first == candidate.id }
        val dayIndex = data.dgRatioOrder.indexOfFirst { it.first == candidate.id }
        val r: Request?

        if (activityIndex > timeIndex && activityIndex > dayIndex){ //it's better to change the activity
            r = tryAnotherActivityWithProxy(Request(candidate, proxy = true), data)
                ?: tryAnotherDayWithProxy(Request(candidate, proxy = true), data)
                ?: tryAnotherTimeWithProxy(Request(candidate, proxy = true), data)

        }
        else if (timeIndex > activityIndex && timeIndex > dayIndex){ //it's better to change the time
            r = tryAnotherTimeWithProxy(Request(candidate, proxy = true), data)
                ?: tryAnotherDayWithProxy(Request(candidate, proxy = true), data)
                ?: tryAnotherActivityWithProxy(Request(candidate, proxy = true), data)
        }
        else { //it's better to change the day
            r = tryAnotherDayWithProxy(Request(candidate, proxy = true), data)
                ?: tryAnotherActivityWithProxy(Request(candidate, proxy = true), data)
                ?: tryAnotherTimeWithProxy(Request(candidate, proxy = true), data)
        }
        if (r != null)
            insertionList.add(r)
    }

    override fun trySomewhereElseWithoutProxy(candidate: InstanceRequest, data: Data) {

    }

    private fun tryAnotherActivityWithProxy(r: Request, data: Data): Request? {
        for (a in data.instance.activities) {
            if (data.proxyRequestsInActivity[a.id][r.day][r.time] > 0) { //is there already a proxy so the capacity won't grow
                if (data.proxyDailyCapacity[r.day] > 0) { //proxy can take this request
                    r.setActivity(a.id)
                    return r
                }
            } else { //there is no proxy there, trying to put it there if there's room
                if (data.freeSeatsInActivity[a.id][r.day][r.time] > 0)
                    if (data.proxyDailyCapacity[r.day] > 0) {
                        r.setActivity(a.id)
                        return r
                    }
            }
        }
        return null
    }

    private fun tryAnotherDayWithProxy(r: Request, data: Data): Request? {
        for (d in 0 until data.instance.num_days){
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

    private fun tryAnotherTimeWithProxy(r: Request, data: Data): Request? {
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