package alns.heuristics.inserting

import alns.Data
import alns.Request
import alns.heuristics.InsertingHeuristic
import kotlin.random.Random

class RandomInsertion : InsertingHeuristic {
    override fun insertRequest(data: Data, q: Int): List<Request> {
        val inserted = mutableListOf<Request>()

        while (inserted.size < q && data.missing.size != 0) {
            val nr = data.missing.random()
            val ir = data.instance.getRequestById(nr)

            val p = Random.nextBoolean() && ir.proxy != 0
            val a = Random.nextInt(data.instance.num_activities)
            val d = Random.nextInt(data.instance.num_days)
            val t = Random.nextInt(data.instance.num_timeslots)

            val r = Request(ir, p)
            inserted.add(r)
        }

        return inserted.toList()
    }
}