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
        val r = Request(candidate, true)

        val result = if (activityIndex < timeIndex && activityIndex < dayIndex) { //it's better to change the activity
            tryAnotherActivityWithProxy(r) || tryAnotherDayWithProxy(r) || tryAnotherTimeWithProxy(r)
        } else if (timeIndex < activityIndex && timeIndex < dayIndex) { //it's better to change the time
            tryAnotherTimeWithProxy(r) || tryAnotherDayWithProxy(r) || tryAnotherActivityWithProxy(r)
        } else { //it's better to change the day
            tryAnotherDayWithProxy(r) || tryAnotherActivityWithProxy(r) || tryAnotherTimeWithProxy(r)
        }
        if (!result) trySomewhereElseWithoutProxy(candidate, data)
        else insertionList.add(r)
    }

    private fun tryAnotherActivityWithProxy(r: Request): Boolean {
        for (a in data.activitiesOfCategory[data.instance.getCategoryByActivity(r.getA())]) {
            if (data.proxyRequestsInActivity[a][r.getD()][r.getT()] > 0) { //is there already a proxy so the capacity won't grow
                if (data.proxyDailyCapacity[r.getD()] > 0) { //proxy can take this request
                    r.setActivity(a)
                    if (data.takeNotTrustedRequest(r).first) {
                        return true
                    }
                }
            } else { //there is no proxy there, trying to put it there if there's room
                if (data.freeSeatsInActivity[a][r.getD()][r.getT()] > 0)
                    if (data.proxyDailyCapacity[r.getD()] > 0) {
                        r.setActivity(a)
                        if (data.takeNotTrustedRequest(r).first) {
                            return true
                        }
                    }
            }
        }
        return false
    }

    private fun tryAnotherDayWithProxy(r: Request): Boolean {
        for (d in 0 until data.instance.num_days) {
            if (data.proxyDailyCapacity[d] > 0) {
                if (data.proxyRequestsInActivity[r.getA()][d][r.getT()] > 0) { //there is already a proxy in the activity
                    r.setDay(d)
                    if (data.takeNotTrustedRequest(r).first) {
                        return true
                    }
                } else if (data.freeSeatsInActivity[r.getA()][d][r.getT()] > 0) {
                    r.setDay(d)
                    if (data.takeNotTrustedRequest(r).first) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun tryAnotherTimeWithProxy(r: Request): Boolean {
        for (t in 0 until data.instance.num_timeslots) {
            if (data.proxyRequestsInActivity[r.getA()][r.getD()][t] > 0) { //is there already a proxy so the capacity won't grow
                if (data.proxyDailyCapacity[r.getD()] > 0) { //proxy can take this request
                    r.setTime(t)
                    if (data.takeNotTrustedRequest(r).first) {
                        return true
                    }
                }
            } else { //there is no proxy there, trying to put it there if there's room
                if (data.freeSeatsInActivity[r.getA()][r.getD()][t] > 0)
                    if (data.proxyDailyCapacity[r.getD()] > 0) {
                        r.setTime(t)
                        if (data.takeNotTrustedRequest(r).first) {
                            return true
                        }
                    }
            }
        }
        return false
    }
}