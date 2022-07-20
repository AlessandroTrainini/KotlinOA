package alns.heuristics.removal

import alns.Data
import alns.Request
import alns.heuristics.RemovalHeuristic
import kotlin.random.Random

class RandomRemoval: RemovalHeuristic {
    override fun removeRequest(data: Data, q: Int): List<Request> {
        val insertionList = mutableListOf<Request>()

        while (insertionList.size < q && data.missing.size != 0) {
            val nr = data.missing.random()
            val ir = data.instance.getRequestById(nr)

            val p = Random.nextBoolean() && ir.proxy != 0
            val a = Random.nextInt(data.instance.num_activities)
            val d = Random.nextInt(data.instance.num_days)
            val t = Random.nextInt(data.instance.num_timeslots)

            val r = Request(ir, p)
            insertionList.add(r)
        }

        return insertionList.toList()

    }
}