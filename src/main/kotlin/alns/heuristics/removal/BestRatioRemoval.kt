package alns.heuristics.removal

import alns.Data
import alns.Request
import alns.heuristics.RemovalHeuristic
import kotlin.math.min

class BestRatioRemoval : RemovalHeuristic {

    override fun removeRequest(data: Data, q: Int): List<Request> {
        val orderedTaken = data.taken.filter{it.instanceRequest.proxy != 2}.sortedBy {
            it.instanceRequest.gain -
                    it.penalty_A * it.instanceRequest.penalty_A -
                    it.penalty_D * it.instanceRequest.penalty_D -
                    it.penalty_T * it.instanceRequest.penalty_T
        }
        return orderedTaken.subList(0, min(orderedTaken.size, q))
    }

    operator fun Boolean.times(penaltyA: Float): Float {
        return if (this) penaltyA else 0.toFloat()
    }

}



