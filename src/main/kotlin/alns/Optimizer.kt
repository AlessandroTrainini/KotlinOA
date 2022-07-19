package alns

import alns.heuristics.*
import alns.heuristics.inserting.BestInserting
import alns.heuristics.inserting.BestRatioBestProxy
import alns.heuristics.removal.BestRatioRemoval
import alns.heuristics.starting.BestStarting

class Optimizer {

    private val data = Data()
    private val q = 5

    fun runInstance() {
        val insertingHeuristic: InsertingHeuristic = BestInserting()
        val removalHeuristic: RemovalHeuristic = BestRatioRemoval()
        val startingHeuristic: StartingHeuristic = BestStarting()

        startingHeuristic.generateStartingPoint(data)
        var objValue = getCurrentObjectiveValue()

        println("starting objective value: ${getCurrentObjectiveValue()}")

        for (i in 0..10) {
            println("iteration n: $i")
            val toRemove = removalHeuristic.removeRequest(data, q)
            toRemove.forEach { data.removeRequest(it) }

            println("removing $toRemove")

            val toInsert = insertingHeuristic.insertRequest(data, q)
            toInsert.forEach { data.takeTrustedRequest(it) }

            println("inserting $toInsert")

            if (getCurrentObjectiveValue() > objValue)
                objValue = getCurrentObjectiveValue()
            else { // the obj value was better before, backtracking
                toInsert.forEach { data.removeRequest(it) }
                toRemove.forEach { data.takeTrustedRequest(it) }
                println("backtracking")

            }

            println(getCurrentObjectiveValue())
        }
        println(data.taken)
    }

    private fun getCurrentObjectiveValue(): Float {
        var value = 0f
        data.taken.forEach {
            value += (it.instanceRequest.gain -
                    it.penalty_A * it.instanceRequest.penalty_A -
                    it.penalty_D * it.instanceRequest.penalty_D -
                    it.penalty_T * it.instanceRequest.penalty_T)
        }
        return value

    }


}

operator fun Boolean.times(penaltyA: Float): Float {
    return if (this) penaltyA else 0.toFloat()
}