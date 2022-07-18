package alns.heuristics

import alns.Data
import alns.Request

interface RemovalHeuristic {

    fun removeRequest(data: Data, q: Int): List<Request>

}