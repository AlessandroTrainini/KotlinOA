package alns

import alns.heuristics.*

class Optimizer {

    private val data = Data()
    private val q = 3

    fun runInstance() {
        val insertingHeuristic: InsertingHeuristic = RandomInsertion()
        val removalHeuristic: RemovalHeuristic = RandomRemoval()
        val startingHeuristic: StartingHeuristic = FirstWithProxyStartingHeuristic()

        startingHeuristic.generateStartingPoint(data)
        for (i in 0..10) {
            removalHeuristic.removeRequest(data)
            insertingHeuristic.insertRequest(data)
        }
        println(getObjectiveValue())

    }

    private fun getCurrentObjectiveValue(): Int {
        var value = 0
        data.taken.forEach {
            value += data.instance.requests[it.id].gain
        }
        return value

    }


}