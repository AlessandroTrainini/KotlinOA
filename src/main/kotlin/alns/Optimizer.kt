package alns

import alns.heuristics.*
import alns.heuristics.starting.BestStarting
import tools.ProgressBar
import java.io.File
import kotlin.math.exp
import kotlin.random.Random

class Optimizer {

    private val data = Data()
    private val segmentSize = 100
    private val maxIterNum = 5
    private var qInsert = 100

    private val qRemove = 100
    private var currentQRemove = qRemove
    private var currentSegmentQRemove = currentQRemove

    private var t: Double = 1.toDouble()
    var tIncrement = 1.toDouble()

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

        val core: () -> Unit = {
            for (i in 1..maxIterNum) {
                println("Segment n° $i")

                insertingHeuristic = heuristicsWheel.getInsHeuristic()
                removalHeuristic = heuristicsWheel.getRemHeuristic()
                val progressBar = ProgressBar(segmentSize)
                val startTime = System.currentTimeMillis()
                do { // Segment
                    progressBar.printProgressBar()

                    val toRemove = removalHeuristic.removeRequest(data, currentSegmentQRemove)
                    toRemove.forEach { if (!data.removeRequest(it)) error("Can't remove request") }

                    toRemove.forEach {
                        if (!data.takeNotTrustedRequest(it).first) error("Can't perform removal backtraking")
                    }
                    toRemove.forEach { if (!data.removeRequest(it)) error("Can't remove request") }

                    val toInsert = insertingHeuristic.insertRequest(data, qInsert)

                    val heuristicWeight: Double
                    val newObjValue = getCurrentObjectiveValue()
                    if (newObjValue > currentObjValue) { // Found a better solution
                        currentObjValue = newObjValue
                        if (currentObjValue > maxObjValue) { // Found the best solution so far
                            maxObjValue = currentObjValue
                            maxTaken = data.taken.toList()
                            heuristicWeight = heuristicsWheel.W1
                        } else heuristicWeight = heuristicsWheel.W2 // Found better solution
                        progressBar.decreaseIncrement()
                    } else { // the obj value was better before
                        if (simulatedAnnealing(newObjValue, t)) {
                            currentObjValue = newObjValue
                            heuristicWeight = heuristicsWheel.W3
                        } else {
                            toInsert.forEach { if (!data.removeRequest(it)) error("Can't perform insertion backtraking") }
                            toRemove.forEach {
                                if (!data.takeNotTrustedRequest(it).first) error("Can't perform removal backtraking")
                            }
                            heuristicWeight = heuristicsWheel.W4
                            progressBar.increaseIncrement()
                        }
                    }
                    heuristicsWheel.updateWeight(heuristicWeight)
                    currentSegmentQRemove =
                        (currentQRemove * (1 - progressBar.getPercentage().toDouble() / 100)).toInt()
                    t += tIncrement
                } while (progressBar.updateAndCheck())
                val endTime = System.currentTimeMillis()
                progressBar.printProgressBar()
                println("End segment n° $i (${(endTime - startTime) / 1000}s) objective value: ${getCurrentObjectiveValue()}")
                currentQRemove = (qRemove * (1 - i.toDouble() / maxIterNum)).toInt()
            } // End search
        }

        firstPhase(heuristicsWheel)
        core()

        destroyingPhase(heuristicsWheel)
        core()

        intensifyingPhase(heuristicsWheel)
        core()

        data.checkFeasibility()
        output()


    }

    private fun firstPhase(heuristicsWheel: HeuristicsWheel) {
        println("Phase 1: getting better init solution")
        t = 1.toDouble()
        tIncrement = 1.toDouble()
        qInsert = 100
        heuristicsWheel.firstPhase()
    }

    private fun destroyingPhase(heuristicsWheel: HeuristicsWheel) {
        println("Phase 2: destroying init solution")
        t = 1.toDouble()
        tIncrement = 0.toDouble()
        qInsert = 10
        heuristicsWheel.destroyingPhase()
    }

    private fun intensifyingPhase(heuristicsWheel: HeuristicsWheel) {
        println("Phase 3: Change solution")
        t = 1.toDouble()
        tIncrement = 10.toDouble()
        qInsert = 100
        heuristicsWheel.intensifyPhase()
    }

    private fun simulatedAnnealing(newCurrentObjValue: Double, t: Double): Boolean {

        val difference = currentObjValue - newCurrentObjValue
        val percentage = 1 - exp(-1 * ((difference) / (50 * t)))
        // println("$difference - $t - $percentage")
        val randomNumber = Random.nextDouble()
        return randomNumber < percentage
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

    private fun output() {
        println("Best objective value: $maxObjValue")
        File("out.txt").writeText("ID - Activity | def - Day | def - Time | def - Proxy | def - Gain - PenaltyA - PenaltyD - PenaltyT - Profit\n")
        maxTaken.forEach {
            File("out.txt").appendText(it.toString())
        }
    }

    operator fun Boolean.times(penaltyA: Double): Double {
        return if (this) penaltyA else 0.toDouble()
    }
}

