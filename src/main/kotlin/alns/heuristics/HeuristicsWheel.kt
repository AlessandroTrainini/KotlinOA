package alns.heuristics

import alns.heuristics.inserting.BestInserting
import alns.heuristics.inserting.BestRatioBestProxy
import alns.heuristics.inserting.BestRatioFirstProxy
import alns.heuristics.inserting.RandomInsertion
import alns.heuristics.removal.BestRatioRemoval
import alns.heuristics.removal.RandomRemoval
import kotlin.math.floor

class HeuristicsWheel {

    val W1 = 10f.toDouble() // Global optimum
    val W2 = 7.5f.toDouble() // Better than before
    val W3 = 5f.toDouble() // Accepted
    val W4 = 2.5f.toDouble() // Rejected

    private val insertingStorage = mutableListOf<InsertingHeuristic>()
    private val removalStorage = mutableListOf<RemovalHeuristic>()
    private val insertingWeight = mutableListOf<Double>()
    private val removalWeight = mutableListOf<Double>()
    private lateinit var callBack: () -> Unit

    private val lambda = 0.999f

    private var currentIns = -1
    private var currentRem = -1

    init {
        intensifyPhase()
    }

    fun destroyingPhase() {
        clearWheel()
        addHeuristic(RandomRemoval(), W3)
        addHeuristic(RandomInsertion(), W3)
        callBack = {destroyingPhase()}
    }

    fun intensifyPhase() {
        clearWheel()
        addHeuristic(BestInserting(), W2)
        addHeuristic(RandomInsertion(), W4)
        addHeuristic(RandomRemoval(), W2)
        addHeuristic(BestRatioRemoval(), W2)
        addHeuristic(BestRatioFirstProxy(), W2)
        callBack = {intensifyPhase()}
    }

    fun resetWeight() {
        callBack
    }

    private fun clearWheel() {
        currentIns = -1
        currentRem = -1
        insertingStorage.clear()
        removalStorage.clear()
        insertingWeight.clear()
        removalWeight.clear()
    }

    private fun addHeuristic(h: Any, weight: Double) {
        if (h is InsertingHeuristic) {
            insertingStorage.add(h)
            insertingWeight.add(weight)
        } else if (h is RemovalHeuristic) {
            removalStorage.add(h)
            removalWeight.add(weight)
        }
    }

    fun updateWeight(w: Double) {
        insertingWeight[currentIns] = lambda * insertingWeight[currentIns] + (1 - lambda) * w
        removalWeight[currentRem] = lambda * removalWeight[currentRem] + (1 - lambda) * w
    }

    fun getInsHeuristic(): InsertingHeuristic {
        val index = getMostPromisingIndex(insertingWeight)
        if (index != currentIns) {
            currentIns = index
            // println("Ins heuristic: ${insertingStorage[currentIns].javaClass.name}")
        }
        return insertingStorage[currentIns]
    }

    fun getRemHeuristic(): RemovalHeuristic {
        val index = getMostPromisingIndex(removalWeight)
        if (index != currentRem) {
            currentRem = index
            // println("Rem heuristic: ${removalStorage[currentRem].javaClass.canonicalName}")
        }
        return removalStorage[currentRem]
    }

    private fun getMostPromisingIndex(weightList: MutableList<Double>): Int {
        val tot = weightList.sum()
        val distribution = mutableListOf<Int>()
        for (i in (0 until weightList.size)) {
            val p = floor(weightList[i] / tot * 100).toInt()
            for (j in 0 until p) distribution.add(i)
        }
        return distribution.random()
    }

}