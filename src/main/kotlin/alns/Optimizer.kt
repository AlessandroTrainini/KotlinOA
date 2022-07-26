package alns

import alns.heuristics.*
import alns.heuristics.starting.BestStarting
import tools.ProgressBar
import java.io.File
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Optimizer {

    private val data = Data()
    private val cycleSize = 5
    private var segmentSize = -1
    private var qInsert = -1

    private val defaultQRemove = 100
    private var cycleQRemove = defaultQRemove
    private var segmentQRemove = cycleQRemove

    private var t: Double = 1.toDouble()
    private var tIncrement = 1.toDouble()
    private var distruptive = false

    private var currentObjValue: Double = 0.toDouble()
    private var maxObjValue: Double = 0.toDouble()
    private lateinit var maxTaken: List<Request>

    fun runInstance() {

        startingPhase()

        val heuristicsWheel = HeuristicsWheel()
        var insertingHeuristic: InsertingHeuristic
        var removalHeuristic: RemovalHeuristic

        val core: () -> Unit = {

            val startTime = System.currentTimeMillis()

            var cycleProgress = 0
            val progressBar = ProgressBar(cycleSize * segmentSize)

            while (cycleProgress < cycleSize) { // cycle
                heuristicsWheel.resetWeight()

                insertingHeuristic = heuristicsWheel.getInsHeuristic()
                removalHeuristic = heuristicsWheel.getRemHeuristic()

                var segmentProgress = 0
                var segmentIncrement = 1

                while (segmentProgress < segmentSize) { // Segment
                    progressBar.setCurrentValue(cycleProgress * segmentSize + segmentProgress)
                    progressBar.printProgressBar()

                    val toRemove = removalHeuristic.removeRequest(data, segmentQRemove)
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
                        segmentIncrement = max(segmentIncrement/2, 1)
                    } else { // the obj value was better before
                        if (distruptive && simulatedAnnealing(newObjValue, t)) {
                            currentObjValue = newObjValue
                            heuristicWeight = heuristicsWheel.W3
                        } else {
                            toInsert.forEach { if (!data.removeRequest(it)) error("Can't perform insertion backtraking") }
                            toRemove.forEach {
                                if (!data.takeNotTrustedRequest(it).first) error("Can't perform removal backtraking")
                            }
                            heuristicWeight = heuristicsWheel.W4
                            segmentIncrement++
                        }
                    }
                    heuristicsWheel.updateWeight(heuristicWeight)
                    segmentQRemove = max((cycleQRemove * (1 - segmentProgress.toDouble()/segmentSize)).toInt(), 1)

                    t += tIncrement

                    segmentProgress += segmentIncrement
                    progressBar.setCurrentValue(cycleProgress * segmentSize + segmentProgress)
                }
                cycleQRemove = max((defaultQRemove * (1 - cycleProgress.toDouble() / cycleSize)).toInt(), 1)

                cycleProgress++
            } // End search
            val endTime = System.currentTimeMillis()
            progressBar.printProgressBar()

            println("End phase (${(endTime - startTime) / 1000}s) objective value: ${getCurrentObjectiveValue()}")
        }

        intensifyingPhase(heuristicsWheel)
        core()

        for (i in 0 until cycleSize) {

            destroyingPhase(heuristicsWheel)
            core()

            intensifyingPhase(heuristicsWheel)
            core()
        }

        data.checkFeasibility()
        output()


    }

    private fun startingPhase() {
        val startingHeuristic: StartingHeuristic = BestStarting()

        println("Phase 0: Generation of a good starting point")
        startingHeuristic.generateStartingPoint(data)
        currentObjValue = getCurrentObjectiveValue()
        maxObjValue = currentObjValue
        maxTaken = data.taken.toList()
        println("Starting point generated with objective value: ${getCurrentObjectiveValue()}")
    }

    private fun destroyingPhase(heuristicsWheel: HeuristicsWheel) {
        println("Phase 1: destroying solution")
        distruptive = true
        segmentSize = 50
        t = 1.toDouble()
        tIncrement = 0.toDouble()
        qInsert = 1
        heuristicsWheel.destroyingPhase()
    }

    private fun intensifyingPhase(heuristicsWheel: HeuristicsWheel) {
        println("Phase 2: intensification")
        distruptive = false
        segmentSize = 100
        t = 1.toDouble()
        tIncrement = 10.toDouble()
        qInsert = 200
        heuristicsWheel.intensifyPhase()
    }

    private fun simulatedAnnealing(newCurrentObjValue: Double, t: Double): Boolean {
        val difference = newCurrentObjValue - currentObjValue
        val percentage = 1 - exp((difference) / (50 * t))
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

