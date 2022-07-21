package alns

import alns.heuristics.*
import alns.heuristics.starting.BestStarting
import tools.ProgressBar
import java.io.File

class Optimizer {

    private val data = Data()
    private val qInsert = 1
    private val qRemove = 10
    private val segmentSize = 100
    private val maxIterNum = 10

    private var t = 0

    private var currentObjValue: Double = 0.toDouble()
    private var maxObjValue: Double = 0.toDouble()

    fun runInstance() {
        val startingHeuristic: StartingHeuristic = BestStarting()

        println("Generation of a good starting point")
        startingHeuristic.generateStartingPoint(data)
        currentObjValue = getCurrentObjectiveValue()
        println("Starting point generated with objective value: ${getCurrentObjectiveValue()}\n")

        val heuristicsWheel = HeuristicsWheel()
        var insertingHeuristic: InsertingHeuristic = heuristicsWheel.getBestInsHeuristic()
        var removalHeuristic: RemovalHeuristic = heuristicsWheel.getBestRemHeuristic()

        for (i in 1 until maxIterNum) {
            println("Segment n° $i")

            val progressBar = ProgressBar(segmentSize)
            for (j in 0 until segmentSize) { // Segment
                progressBar.updateProgressBar()

                val toRemove = removalHeuristic.removeRequest(data, qRemove)
                toRemove.forEach { data.removeRequest(it) }

                val toInsert = insertingHeuristic.insertRequest(data, qInsert)

                val heuristicWeight: Float
                val newObjValue = getCurrentObjectiveValue()
                if (newObjValue > currentObjValue) { // Found a better solution
                    currentObjValue = newObjValue
                    if (newObjValue > maxObjValue) { // Found the best solution so far
                        maxObjValue = newObjValue
                        heuristicWeight = heuristicsWheel.W1
                    } else heuristicWeight = heuristicsWheel.W2 // Found better solution
                } else { // the obj value was better before
                    if (false) {
                        currentObjValue = newObjValue
                        heuristicWeight = heuristicsWheel.W3
                    } else { // backtracking
                        toInsert.forEach { data.removeRequest(it) }
                        toRemove.forEach { data.takeTrustedRequest(it) }
                        heuristicWeight = heuristicsWheel.W4
                    }
                }
                heuristicsWheel.updateWeight(heuristicWeight)
                t += 10
            }

            println("End segment n° $i objective value: ${getCurrentObjectiveValue()}\n")

            insertingHeuristic = heuristicsWheel.getInsHeuristic()
            removalHeuristic = heuristicsWheel.getRemHeuristic()
        }

        println(data.taken)
    }

    private fun getCurrentObjectiveValue(): Double {
        var value: Double = 0.toDouble()
        data.taken.shuffled().forEach {
            value += (it.instanceRequest.gain -
                    it.penalty_A * it.instanceRequest.penalty_A -
                    it.penalty_D * it.instanceRequest.penalty_D -
                    it.penalty_T * it.instanceRequest.penalty_T)
        }
        return value
    }

    private fun simulatedAnnealing(newObjValue: Float): Boolean {
//        val objValuesDifference = newObjValue - currentObjValue
//        if (objValuesDifference < 0) {
//            println("Absolute difference: $objValuesDifference")
//            val exponent = -1 * objValuesDifference / t
//            println("Exponent: $exponent")
//            val exponential = exp(exponent)
//            println("Exponential: $exponential")
//        }
        return false
    }

    operator fun Boolean.times(penaltyA: Double): Double {
        return if (this) penaltyA else 0.toDouble()
    }
}

