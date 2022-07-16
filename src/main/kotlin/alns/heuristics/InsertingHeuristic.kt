package alns.heuristics

import alns.Data
import alns.Request

interface InsertingHeuristic {

    fun insertRequest(data: Data, q: Int): List<Request>

}