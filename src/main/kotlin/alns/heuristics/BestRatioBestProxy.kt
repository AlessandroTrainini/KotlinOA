package alns.heuristics

import Instance.InstanceRequest
import alns.Data
import alns.Request

class BestRatioBestProxy : BestRatioFirstProxy() {

    override fun trySomewhereElseWithProxy(candidate: InstanceRequest, data: Data) {
        val activityIndex = data.agRatioOrder.indexOfFirst { it.first == candidate.id }
        val timeIndex = data.tgRatioOrder.indexOfFirst { it.first == candidate.id }
        val dayIndex = data.dgRatioOrder.indexOfFirst { it.first == candidate.id }

        if (activityIndex > timeIndex && activityIndex > dayIndex){ //it's better to change the activity
            val r = tryAnotherActivityWithProxy(Request(candidate, proxy = true), data)
        }
        else if (timeIndex > activityIndex && timeIndex > dayIndex){ //it's better to change the time
            val r = tryAnotherTimeWithProxy(Request(candidate, proxy = true), data)
        }
        else { //it's better to change the day
            val r = tryAnotherDayWithProxy(Request(candidate, proxy = true), data)
        }
    }

    override fun trySomewhereElseWithoutProxy(candidate: InstanceRequest, data: Data) {

    }

    private fun tryAnotherActivityWithProxy(r: Request, data: Data): Request{
        for (a in data.instance.activities) {
            if (data.proxyRequestsInActivity[a.id][r.day][r.time] > 0) { //is there already a proxy so the capacity won't grow
                if (data.proxyDailyCapacity[r.day] > 0) { //proxy can take this request
                    r.setActivity(a.id)
                    return r
                }
            }
            else { //there is no proxy there, trying to put it there
                if (data.freeSeatsInActivity)
            }
        }
    }

    private fun tryAnotherDayWithProxy(r: Request, data: Data): Request{

    }

    private fun tryAnotherTimeWithProxy(r: Request, data: Data): Request{

    }
}