package alns.heuristics

import alns.Data
import alns.Request

interface StartingHeuristic {
    fun generateStartingPoint(data: Data)

}