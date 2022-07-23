package alns

import alns.heuristics.*
import alns.heuristics.starting.BestStarting
import tools.ProgressBar
import java.io.File

class Optimizer {

    private val data = Data()
    private val qInsert = 1
    private val qRemove = 1
    private val segmentSize = 100
    private val maxIterNum = 10

    private var t = 0

    private var currentObjValue: Double = 0.toDouble()
    private var maxObjValue: Double = 0.toDouble()
    private lateinit var maxTaken: List<Request>



    fun runInstance() {
        val startingHeuristic: StartingHeuristic = BestStarting()

        println("Generation of a good starting point")
        startingHeuristic.generateStartingPoint(data)
        currentObjValue = getCurrentObjectiveValue()
        maxObjValue = currentObjValue
        maxTaken = data.taken.toList()
        println("Starting point generated with objective value: ${getCurrentObjectiveValue()}")

        val heuristicsWheel = HeuristicsWheel()
        var insertingHeuristic: InsertingHeuristic
        var removalHeuristic: RemovalHeuristic

        for (i in 1 until maxIterNum) {
            println("Segment n° $i")

            insertingHeuristic = heuristicsWheel.getInsHeuristic()
            removalHeuristic = heuristicsWheel.getRemHeuristic()

            val progressBar = ProgressBar(segmentSize)
            for (j in 0 until segmentSize) { // Segment
                progressBar.updateProgressBar()

                // printInterestingValues("Before removing")
                val toRemove = removalHeuristic.removeRequest(data, qRemove)
                toRemove.forEach { if (!data.removeRequest(it)) error("Can't remove request") }

                toRemove.forEach {
                    val result = data.takeNotTrustedRequest(it)
                    if (!result.first) {
                        data.checkFeasibility()
                        // printInterestingValues("While removal backtraking")
                        error("Can't perform removal backtraking")
                    }
                }
                toRemove.forEach { if (!data.removeRequest(it)) error("Can't remove request") }

                // printInterestingValues("Between insertion and removing")

                val toInsert = insertingHeuristic.insertRequest(data, qInsert)
                // printInterestingValues("After insertion")

                val heuristicWeight: Double
                val newObjValue = getCurrentObjectiveValue()
                if (newObjValue > currentObjValue) { // Found a better solution
                    currentObjValue = newObjValue
                    if (currentObjValue > maxObjValue) { // Found the best solution so far
                        maxObjValue = currentObjValue
                        maxTaken = data.taken.toList()
                        heuristicWeight = heuristicsWheel.W1
                    } else heuristicWeight = heuristicsWheel.W2 // Found better solution
                } else { // the obj value was better before
                    if (simulatedAnnealing(newObjValue)) {
                        currentObjValue = newObjValue
                        heuristicWeight = heuristicsWheel.W3
                    } else { // backtracking
                        // println("Backtraking?")
                        // printInterestingValues("Before insertion backtraking")
                        toInsert.forEach { if(!data.removeRequest(it)) error("Can't perform insertion backtraking") }
                        // printInterestingValues("Between insertion and removal backtraking")
                        toRemove.forEach {
                            val result = data.takeNotTrustedRequest(it)
                            if (!result.first) {
                                data.checkFeasibility()
                                // printInterestingValues("While removal backtraking")
                                error("Can't perform removal backtraking")
                            }
                        }
                        heuristicWeight = heuristicsWheel.W4
                    }
                }
                heuristicsWheel.updateWeight(heuristicWeight)
                t += 10
            }

            println("End segment n° $i objective value: ${getCurrentObjectiveValue()}")
        }

        output()
    }

    private fun printInterestingValues(description: String) {
        println("\n______________ $description ____________")
        println("${data.freeSeatsInActivity[0][0][0]} - ${data.freeSeatsInActivity[0][0][1]}")
        println("${data.proxyDailyCapacity[0]}")
        data.taken.forEach{println(it)}
        println("______________")
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

    private fun output() {
        File("out.txt").writeText("ID - Activity | def - Day | def - Time | def - Proxy | def - Gain - PenaltyA - PenaltyD - PenaltyT - Profit\n")
        maxTaken.forEach {
            File("out.txt").appendText(it.toString())
        }
    }
}

