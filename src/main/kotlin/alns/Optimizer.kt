package alns

import alns.heuristics.*

class Optimizer {

    private val data = Data()
    private val q = 3

    fun runInstance() {
        val insertingHeuristic: InsertingHeuristic = BestRatioFirstProxy()
        val removalHeuristic: RemovalHeuristic = BestRatioRemoval()
        val startingHeuristic: StartingHeuristic = FirstWithProxyStartingHeuristic()

        startingHeuristic.generateStartingPoint(data)

        var objValue = 0

        for (i in 0..20) {
            val toRemove = removalHeuristic.removeRequest(data, q)
            toRemove.forEach { data.removeRequest(it) }

            println("removing $toRemove")

            val toInsert = insertingHeuristic.insertRequest(data, q)
            toInsert.forEach { data.takeRequestTrusted(it) }

            println("inserting $toInsert")

//            if (getCurrentObjectiveValue() > objValue)
//                objValue = getCurrentObjectiveValue()
//            else{ // the obj value was better before, backtracking
//                toInsert.forEach { data.removeRequest(it) }
//                toRemove.forEach { data.takeRequestTrusted(it)
//                println("backtracking")}
//            }
        }

        println(getCurrentObjectiveValue())
        println(data.taken)
    }

    private fun getCurrentObjectiveValue(): Int {
        var value = 0
        data.taken.forEach {
            value += data.instance.requests[it.instanceRequest.id].gain
        }
        return value

    }


}