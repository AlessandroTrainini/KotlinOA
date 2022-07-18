package alns.heuristics.starting

import alns.Data
import alns.Request
import alns.heuristics.StartingHeuristic

class FillEmptySpacesStarting: StartingHeuristic {
    override fun generateStartingPoint(data: Data){
        val proxyCandidates = data.instance.requests.filter { it.proxy >= 1 }.sortedByDescending { it.gain }
        for (d in 0 until data.instance.num_days){
            if (data.proxyDailyCapacity[d] > 0 ){
                proxyCandidates.filter { it.day == d }.forEach {
                    data.takeNotTrustedRequest(Request(it, true))

                }
            }

        }

    }
}