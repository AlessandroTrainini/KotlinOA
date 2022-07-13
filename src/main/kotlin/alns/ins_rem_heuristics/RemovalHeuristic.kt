package alns.ins_rem_heuristics

import alns.Data

interface RemovalHeuristic {
    fun removeRequest(data: Data)
}