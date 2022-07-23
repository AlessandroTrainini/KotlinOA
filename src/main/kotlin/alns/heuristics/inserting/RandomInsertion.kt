package alns.heuristics.inserting

import alns.Data
import alns.Request
import alns.heuristics.InsertingHeuristic
import kotlin.random.Random

class RandomInsertion : InsertingHeuristic {
    override fun insertRequest(data: Data, q: Int): List<Request> {
        val insertionList = mutableListOf<Request>()

        var trial = 100
        while (insertionList.size < q && data.getMissing().size != 0 && trial > 0) {
            val nr = data.getMissing().random()
            val ir = data.instance.getRequestById(nr)

            val p = Random.nextBoolean() && ir.proxy != 0
            val a = Random.nextInt(data.instance.num_activities)
            val d = Random.nextInt(data.instance.num_days)
            val t = Random.nextInt(data.instance.num_timeslots)

            val r = Request(ir, p)
            if (data.takeNotTrustedRequest(r).first)
                insertionList.add(r)
            trial--
        }

        return insertionList.toList()
    }
}