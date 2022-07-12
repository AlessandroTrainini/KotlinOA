package ALNS

import kotlin.random.Random

class RandomInsertion: InsertingHeuristic {
    override fun insertRequest(data: Data) {
        val randomRequest = data.instance.getRequestById(data.missing[Random.nextInt(0, data.missing.size)])
        data.tryToCollocateInTheFirst(randomRequest)
    }
}