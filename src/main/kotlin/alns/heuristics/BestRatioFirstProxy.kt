package alns.heuristics

import Instance.InstanceRequest
import alns.Data
import alns.Request

class BestRatioFirstProxy: InsertingHeuristic {
    private val insertionList = arrayListOf<Request>()
    override fun insertRequest(data: Data, q: Int): MutableList<Request> {
        insertionList.clear()
        val candidates = data.gOrder.filter { it.first in data.missing }.toMutableList()
        while (insertionList.size < q) {
            if (candidates.size < 1)
                return insertionList
            val candidate = data.instance.getRequestById(candidates.first().first)
            candidates.removeAt(0) //pop
            val d = candidate.day
            val t = candidate.timeslot
            val a = candidate.activity
            val proxy = candidate.proxy
            if (proxy >= 1) { //trying to give it to a proxy in order to save activity capacity
                if (data.proxyCapacity[d] > 0) { //in that day, proxy can serve this request
                    if (data.activityProxy[a][d][t] > 0)  //a proxy is already in that activity, we don't need to subtract capacity, we can just add the request
                        insertionList.add(Request(candidate, proxy = true))
                    else{ //there is no proxy in the activity, check if there is enough space
                        if (data.activityRoom[a][d][t] >= 1) //in this case there is enough space, and proxy can take the request
                            insertionList.add(Request(candidate, proxy = true))
                        else
                            trySomewhereElseWithProxy(candidate, data)
                    }
                }
                trySomewhereElseWithProxy(candidate, data)
            }
            else { //we can't use proxy
                if (data.activityRoom[a][d][t] >= 1)  //in this case there is enough space, and proxy can take the request
                    insertionList.add(Request(candidate, proxy = true))
                else
                    trySomewhereElseWithoutProxy(candidate, data)
            }
        }
        return insertionList
    }

    private fun trySomewhereElseWithProxy(candidate: InstanceRequest, data: Data){
        val activityList = data.categoryList[data.instance.getCategoryByActivity(candidate.activity)]
        activityList.remove(candidate.activity)
        activityList.add(0, candidate.activity)

        val timeList = (0 until data.instance.num_timeslots).toMutableList()
        timeList.remove(candidate.timeslot)
        timeList.add(0, candidate.timeslot)

        val dayList = (0 until data.instance.num_days).toMutableList()
        dayList.remove(candidate.day)
        dayList.add(0, candidate.day)

        for (a in activityList)
            for (t in timeList)
                for (d in dayList){
                    if (data.proxyCapacity[d] > 0) { //found a day proxy are free in
                        if (data.activityProxy[candidate.activity][d][candidate.timeslot] > 0) //a proxy is already in that activity, we don't need to subtract capacity, we can just add the request
                            insertionList.add(Request(candidate, proxy = true))
                        else{ //there is no proxy in the activity, check if there is enough space
                            if (data.activityRoom[a][d][t] >= 1)  //in this case there is enough space, and proxy can take the request
                                insertionList.add(Request(candidate, proxy = true))
                        }
                    }
                }
    }

    private fun trySomewhereElseWithoutProxy(candidate: InstanceRequest, data: Data){
        for (a in data.categoryList[data.instance.getCategoryByActivity(candidate.activity)])
            for (t in 0 until data.instance.num_timeslots)
                for (d in 0 until data.instance.num_days)
                    if (data.activityRoom[a][d][t] >= 1)  //in this case there is enough space, and proxy can take the request
                        insertionList.add(Request(candidate, proxy = false))
    }
}