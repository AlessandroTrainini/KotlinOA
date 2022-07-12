package ALNS

import Instance.FileParser
import Instance.Instance

class Optimizer {

    private lateinit var instance: Instance
    private val data = Data()

    fun runInstance() {
        val parser = FileParser("inst/istanza_prova.txt")
        instance = parser.istance

        val insertingHeuristic: InsertingHeuristic = RandomInsertion()
        val removalHeuristic: RemovalHeuristic = RandomRemoval()

        for (i in 0..10) {
            removalHeuristic.removeRequest(data)
            insertingHeuristic.insertRequest(data)
        }
        println(getObjectiveValue())



    }

    private fun getObjectiveValue(): Int{
        var value = 0
        data.taken.forEach {
            value += instance.requests[it.id].gain
        }
        return value
    }


}