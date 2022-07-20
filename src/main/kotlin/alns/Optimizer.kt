package alns

import alns.heuristics.*
import alns.heuristics.inserting.BestInserting
import alns.heuristics.removal.BestRatioRemoval
import alns.heuristics.starting.BestStarting
import kotlin.math.exp

class Optimizer {

    private val data = Data()
    private val q = 5
    private val segmentSize = 100
    private val maxIterNum = 10

    private var t = 1

    private var currentObjValue: Float = 0f
    private var maxObjValue: Float = 0f

    fun runInstance() {
        val startingHeuristic: StartingHeuristic = BestStarting()

        println("Generation of a good starting point")
        startingHeuristic.generateStartingPoint(data)
        currentObjValue = getCurrentObjectiveValue()
        println("Starting point generated with objective value: ${getCurrentObjectiveValue()}")

        val heuristicsWheel = HeuristicsWheel()
        var insertingHeuristic: InsertingHeuristic = heuristicsWheel.getBestInsHeuristic()
        var removalHeuristic: RemovalHeuristic = heuristicsWheel.getBestRemHeuristic()

        for (i in 1..maxIterNum) {
            println("Segment n° $i")

            for (j in 0..segmentSize) { // Segment
                val toRemove = removalHeuristic.removeRequest(data, q)
                toRemove.forEach { data.removeRequest(it) }

                val toInsert = insertingHeuristic.insertRequest(data, q)
                toInsert.forEach { data.takeNotTrustedRequest(it) }

                val newObjValue = getCurrentObjectiveValue()
                if (newObjValue > currentObjValue) {
                    currentObjValue = newObjValue
                    if (currentObjValue > maxObjValue) {
                        maxObjValue = currentObjValue
                        heuristicsWheel.updateWeight(heuristicsWheel.W1)
                    } else {
                        heuristicsWheel.updateWeight(heuristicsWheel.W2)
                    }
                } else { // the obj value was better before, backtracking
                    toInsert.forEach { data.removeRequest(it) }
                    toRemove.forEach { data.takeNotTrustedRequest(it) }
                    heuristicsWheel.updateWeight(heuristicsWheel.W4)
                }
                t += 10
            }

            println("End segment n° $i objective value: ${getCurrentObjectiveValue()}")

            insertingHeuristic = heuristicsWheel.getInsHeuristic()
            removalHeuristic = heuristicsWheel.getRemHeuristic()
        }
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

    private fun acceptNewSol(newObjValue: Float): Boolean {
        val objValuesDifference = newObjValue - currentObjValue
        if (objValuesDifference < 0) {
            println("Absolute difference: $objValuesDifference")
            val exponent = -1 * objValuesDifference / t
            println("Exponent: $exponent")
            val exponential = exp(exponent)
            println("Exponential: $exponential")
        }

        return false
    }


}

operator fun Boolean.times(penaltyA: Float): Float {
    return if (this) penaltyA else 0.toFloat()
}