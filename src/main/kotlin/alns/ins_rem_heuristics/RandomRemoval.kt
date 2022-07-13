package alns.ins_rem_heuristics

import alns.Data
import kotlin.random.Random

class RandomRemoval: RemovalHeuristic {
    override fun removeRequest(data: Data) {
//        data.dismissRequest(data.taken[Random.nextInt(0,data.taken.size)].id)
    }
}