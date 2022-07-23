package alns.heuristics.removal

import alns.Data
import alns.Request
import alns.heuristics.RemovalHeuristic
import kotlin.math.min
import kotlin.random.Random

class RandomRemoval : RemovalHeuristic {
    override fun removeRequest(data: Data, q: Int): List<Request> {
        val tmp = data.taken.filter { it.instanceRequest.proxy < 2 }.shuffled()
        return tmp.subList(0, min(tmp.size, q))
    }
}