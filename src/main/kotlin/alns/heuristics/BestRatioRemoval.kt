package alns.heuristics

import alns.Data
import alns.Request

class BestRatioRemoval : RemovalHeuristic {

    override fun removeRequest(data: Data, q: Int): List<Request> {
        val orderedTaken = data.taken.sortedBy {
            it.instanceRequest.gain -
                    it.penalty_A * it.instanceRequest.penalty_A -
                    it.penalty_D * it.instanceRequest.penalty_D -
                    it.penalty_T * it.instanceRequest.penalty_T
        }
        return orderedTaken.subList(0, q)
    }

}

private operator fun Boolean.times(penaltyA: Float): Float {
    return if (this) penaltyA else 0.toFloat()
}
