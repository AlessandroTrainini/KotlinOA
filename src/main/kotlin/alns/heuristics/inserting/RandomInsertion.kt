package alns.heuristics.inserting

import alns.Data
import alns.Request
import alns.heuristics.InsertingHeuristic

class RandomInsertion : InsertingHeuristic {
    override fun insertRequest(data: Data, q: Int): List<Request> {
        val removalList = mutableListOf<Request>()

        while (removalList.size < q && data.taken.size != 0) {
            var r: Request
            do {
                r = data.taken.random()
            } while (r.instanceRequest.proxy == 2)

            removalList.add(r)
        }

        return removalList
    }
}