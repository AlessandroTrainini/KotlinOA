package alns.heuristics.removal

import alns.Data
import alns.Request
import alns.heuristics.RemovalHeuristic
import kotlin.random.Random

class RandomRemoval : RemovalHeuristic {
    override fun removeRequest(data: Data, q: Int): List<Request> {
        val removalList = mutableSetOf<Request>()
        val requestTakenNumber = data.taken.filter { it.instanceRequest.proxy < 2 }.size
        while (removalList.size < q && removalList.size < requestTakenNumber) {
            var r: Request
            do {
                r = data.taken.random()
            } while (r.instanceRequest.proxy == 2)

            removalList.add(r)
        }

        return removalList.toList()
    }
}