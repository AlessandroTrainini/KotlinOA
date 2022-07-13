package alns.heuristics

import alns.Data

interface StartingHeuristic {
    fun generateStartingPoint(data: Data)
}