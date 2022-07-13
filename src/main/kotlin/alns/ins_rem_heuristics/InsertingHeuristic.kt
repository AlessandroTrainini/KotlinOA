package alns.ins_rem_heuristics

import alns.Data

interface InsertingHeuristic {
    fun insertRequest(data: Data)
}