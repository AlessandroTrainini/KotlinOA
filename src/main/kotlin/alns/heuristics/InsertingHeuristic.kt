package alns.heuristics

import alns.Data

interface InsertingHeuristic {

    fun insertRequest(data: Data, q: Int): List<Request>

}