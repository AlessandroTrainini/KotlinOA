package alns.heuristics

import alns.Data
import alns.Request

class FirstWithProxyStartingHeuristic : StartingHeuristic {

    override fun generateStartingPoint(data: Data) {
        for (ir in data.instance.requests.sortedByDescending { it.gain }) {
            if (ir.proxy > 1) {
                val r = Request(ir, true)
                // data.takeRequestTrusted(nr) // in realtà non è trusted
                val result = data.takeNotTrustedRequest(r)
                if (!result.first) {
                    when (result.second) {
                        1 -> throw Exception("Impossible")
                        2 -> { println("We have to look for another day") }
                        3 -> { println("We have to change activity, day or timeslot") }
                        4 -> { println("Impossible") }
                    }
                }
            } else
                data.missing.add(ir.id)
        }
    }

}