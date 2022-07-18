package alns.heuristics.removal

import alns.Data
import alns.Request
import alns.heuristics.RemovalHeuristic

class RandomRemoval: RemovalHeuristic {
    override fun removeRequest(data: Data, q: Int): List<Request> {
        return listOf()
    }
}