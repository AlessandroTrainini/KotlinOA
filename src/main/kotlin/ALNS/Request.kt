package ALNS

import java.util.stream.IntStream.IntMapMultiConsumer

data class Request(
    val id: Int,
    var day: Int,
    var time: Int,
    var activity: Int,
)
