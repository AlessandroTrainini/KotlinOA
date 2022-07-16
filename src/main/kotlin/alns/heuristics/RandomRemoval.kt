package alns.heuristics

import alns.Data
import alns.Request

class RandomRemoval: RemovalHeuristic {
    override fun removeRequest(data: Data, q: Int): List<Request> {
        return listOf()
    }
}