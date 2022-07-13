package alns.heuristics

import alns.Data

class RandomRemoval: RemovalHeuristic {
    override fun removeRequest(data: Data) {
//        data.dismissRequest(data.taken[Random.nextInt(0,data.taken.size)].id)
    }
}