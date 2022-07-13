package alns

import alns.ins_rem_heuristics.InsertingHeuristic
import alns.ins_rem_heuristics.RandomInsertion
import alns.ins_rem_heuristics.RandomRemoval
import alns.ins_rem_heuristics.RemovalHeuristic

class Optimizer {

    private val data = Data()

    fun runInstance() {
        val insertingHeuristic: InsertingHeuristic = RandomInsertion()
        val removalHeuristic: RemovalHeuristic = RandomRemoval()

        for (i in 0..10) {
            removalHeuristic.removeRequest(data)
            insertingHeuristic.insertRequest(data)
        }
        println(getObjectiveValue())

    }

    private fun getObjectiveValue(): Int {
        var value = 0
        data.taken.forEach {
            value += data.instance.requests[it.id].gain
        }
        return value

    }


}