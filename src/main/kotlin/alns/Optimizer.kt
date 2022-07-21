package alns

import alns.heuristics.*
import alns.heuristics.starting.BestStarting
import tools.ProgressBar

class Optimizer {

    private val data = Data()
    private val qInsert = 50
    private val qRemove = 30
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
        println("Starting point generated with objective value: ${getCurrentObjectiveValue()}")

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

                val heuristicWeight: Double
                val newObjValue = getCurrentObjectiveValue()
                if (newObjValue > currentObjValue) { // Found a better solution
                    currentObjValue = newObjValue
                    if (currentObjValue > maxObjValue) { // Found the best solution so far
                        maxObjValue = currentObjValue
                        heuristicWeight = heuristicsWheel.W1
                    } else heuristicWeight = heuristicsWheel.W2 // Found better solution
                } else { // the obj value was better before
                    if (simulatedAnnealing(newObjValue)) {
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

            println("End segment n° $i objective value: ${getCurrentObjectiveValue()}")

            insertingHeuristic = heuristicsWheel.getInsHeuristic()
            removalHeuristic = heuristicsWheel.getRemHeuristic()
        }

        println(data.taken)
    }

    private fun getCurrentObjectiveValue(): Double {
        var value = 0.toDouble()
        data.taken.forEach {
            value += (it.instanceRequest.gain -
                    it.penalty_A * it.instanceRequest.penalty_A -
                    it.penalty_D * it.instanceRequest.penalty_D -
                    it.penalty_T * it.instanceRequest.penalty_T)
        }
        return value
    }

    private fun simulatedAnnealing(newObjValue: Double): Boolean {
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

