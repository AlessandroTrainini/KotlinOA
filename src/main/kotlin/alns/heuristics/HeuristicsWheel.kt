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

    private val lambda = 0.3f

    private var currentIns = -1
    private var currentRem = -1

    init {
        addHeuristic(BestInserting(), W1)
        addHeuristic(BestRatioBestProxy(), W3)
        addHeuristic(RandomInsertion(), W4)
        addHeuristic(BestRatioFirstProxy(), W1)

        addHeuristic(BestRatioRemoval(), W1)
        addHeuristic(RandomRemoval(), W2)
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

    fun getBestInsHeuristic(): InsertingHeuristic {
        currentIns = insertingWeight.indexOf(insertingWeight.max())
        println("Ins heuristic: ${insertingStorage[currentIns].javaClass.name}")
        return insertingStorage[currentIns]
    }

    fun getBestRemHeuristic(): RemovalHeuristic {
        currentRem = removalWeight.indexOf(removalWeight.max())
        println("Rem heuristic: ${removalStorage[currentRem].javaClass.name}")
        return removalStorage[currentRem]
    }

    fun getInsHeuristic(): InsertingHeuristic {
        val index = getMostPromisingIndex(insertingWeight)
        if (index != currentIns) {
            currentIns = index
            println("Ins heuristic: ${insertingStorage[currentIns].javaClass.name}")
        }
        return insertingStorage[currentIns]
    }

    fun getRemHeuristic(): RemovalHeuristic {
        val index = getMostPromisingIndex(removalWeight)
        if (index != currentRem) {
            currentRem = index
            println("Rem heuristic: ${removalStorage[currentRem].javaClass.name}")
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