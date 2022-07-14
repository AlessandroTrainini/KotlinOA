package alns.heuristics

import alns.Data
import alns.Request

class FirstWithProxyStartingHeuristic: StartingHeuristic {
    override fun generateStartingPoint(data: Data) {
        for (r in data.instance.requests){
            if (r.proxy == 2){
                val nr = Request(instanceRequest = r, true)
                data.takeRequestTrusted(nr)
            }
            else
                data.missing.add(r.id)
        }
    }
}