package alns.heuristics

import alns.Data

interface RemovalHeuristic {
    fun removeRequest(data: Data, q: Int): List<Request>
}