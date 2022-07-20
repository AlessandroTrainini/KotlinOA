package alns.heuristics

import alns.heuristics.inserting.BestInserting
import alns.heuristics.inserting.BestRatioBestProxy
import alns.heuristics.inserting.BestRatioFirstProxy
import alns.heuristics.inserting.RandomInsertion
import alns.heuristics.removal.BestRatioRemoval
import alns.heuristics.removal.RandomRemoval
import kotlin.math.floor

class HeuristicsWheel {

    val W1 = 10f // Global optimum
    val W2 = 7.5f // Better than before
    val W3 = 5f // Accepted
    val W4 = 2.5f // Rejected

    private val insertingStorage = mutableListOf<InsertingHeuristic>()
    private val removalStorage = mutableListOf<RemovalHeuristic>()
    private val insertingWeight = mutableListOf<Float>()
    private val removalWeight = mutableListOf<Float>()

    private val lambda = 0.3f

    private var currentIns = 0
    private var currentRem = 0

    init {
        addHeuristic(BestInserting(), W1)
        addHeuristic(BestRatioBestProxy(), W3)
        addHeuristic(BestRatioFirstProxy(), 3f)
        addHeuristic(RandomInsertion(), 2f)

        addHeuristic(BestRatioRemoval(), W1)
        addHeuristic(RandomRemoval(), 2f)
    }

    private fun addHeuristic(h: Any, weight: Float) {
        if (h is InsertingHeuristic) {
            insertingStorage.add(h)
            insertingWeight.add(weight)
        } else if (h is RemovalHeuristic) {
            removalStorage.add(h)
            removalWeight.add(weight)
        }
    }

    fun updateWeight(w: Float) {
        insertingWeight[currentIns] = lambda * insertingWeight[currentIns] + (1 - lambda) * w
        removalWeight[currentRem] = lambda * removalWeight[currentRem] + (1 - lambda) * w
    }

    fun getBestInsHeuristic(): InsertingHeuristic {
        currentIns = insertingWeight.indexOf(insertingWeight.max())
        return insertingStorage[currentIns]
    }

    fun getBestRemHeuristic(): RemovalHeuristic {
        currentRem = removalWeight.indexOf(removalWeight.max())
        return removalStorage[currentRem]
    }

    fun getInsHeuristic(): InsertingHeuristic {
        val index = getMostPromisingIndex(insertingWeight)
        if (index != currentIns) {
            println("Ins heuristic changed")
            currentIns = index
        }
        return insertingStorage[currentIns]
    }

    fun getRemHeuristic(): RemovalHeuristic {
        val index = getMostPromisingIndex(removalWeight)
        if (index != currentRem) {
            println("Rem heuristic changed")
            currentRem = index
        }
        return removalStorage[currentRem]
    }

    private fun getMostPromisingIndex(weightList: MutableList<Float>): Int {
        val tot = weightList.sum()
        val distribution = mutableListOf<Int>()
        for (i in (0 until  weightList.size)) {
            val p = floor(weightList[i] / tot * 100).toInt()
            for (j in 0 until p) distribution.add(i)
        }
        return distribution.random()
    }

}