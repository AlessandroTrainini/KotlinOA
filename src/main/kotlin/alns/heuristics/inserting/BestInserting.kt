package alns.heuristics.inserting

import Instance.InstanceRequest
import alns.Data
import alns.Request
import alns.heuristics.InsertingHeuristic


class BestInserting : InsertingHeuristic {

    private val ADT = intArrayOf(0, 1, 2)
    private val ATD = intArrayOf(0, 2, 1)
    private val DAT = intArrayOf(1, 0, 2)
    private val DTA = intArrayOf(1, 2, 0)
    private val TAD = intArrayOf(2, 0, 1)
    private val TDA = intArrayOf(2, 1, 0)

    private lateinit var data: Data
    lateinit var insertionList: MutableList<Request>

    override fun insertRequest(data: Data, q: Int): List<Request> {
        this.data = data
        insertionList = mutableListOf()
        // println("insertRequest")
        val candidates = data.gOrder.filter { data.getMissing().contains(it.first) }.toMutableList()
        while (insertionList.size < q) {
            // println("Cycle $candidates")
            if (candidates.size == 0)
                return insertionList
            val candidate = data.instance.getRequestById(candidates.first().first)
            candidates.removeAt(0) //pop
            val fingerprint = getRequestTaste(candidate)
            val r = Request(candidate, candidate.proxy > 0)
            when {
                fingerprint.contentEquals(ADT) -> adt(r, r.proxy)
                fingerprint.contentEquals(ATD) -> atd(r, r.proxy)
                fingerprint.contentEquals(DAT) -> dat(r, r.proxy)
                fingerprint.contentEquals(DTA) -> dta(r, r.proxy)
                fingerprint.contentEquals(TDA) -> tda(r, r.proxy)
                fingerprint.contentEquals(TAD) -> tad(r, r.proxy)
            }
        }
        return insertionList
    }

    private fun getRequestTaste(r: InstanceRequest): IntArray {
        val ai = Pair(data.agRatioOrder.indexOfFirst { it.first == r.id }, 0)
        val di = Pair(data.dgRatioOrder.indexOfFirst { it.first == r.id }, 1)
        val ti = Pair(data.tgRatioOrder.indexOfFirst { it.first == r.id }, 2)


        return listOf(ai, ti, di).sortedBy { it.first }.map { it.second }.toIntArray()

    }

    private fun adt(r: Request, withProxy: Boolean): Boolean {
        var result = false
        val lists = getIndexesList(r.getA(), r.getD(), r.getT())
        run bigBrother@{
            lists[2].forEach { t ->
                lists[1].forEach { d ->
                    lists[0].forEach { a ->
                        result = tryToPlace(r, a, d, t, withProxy)
                        if (result) return@bigBrother
                    }
                }
            }
        }
        if (!result && withProxy)
            result = adt(r, false)
        return result
    }

    private fun atd(r: Request, withProxy: Boolean): Boolean {
        var result = false
        val lists = getIndexesList(r.getA(), r.getD(), r.getT())
        run bigBrother@{
            lists[1].forEach { d ->
                lists[2].forEach { t ->
                    lists[0].forEach { a ->
                        result = tryToPlace(r, a, d, t, withProxy)
                        if (result) return@bigBrother
                    }
                }
            }
        }
        if (!result && withProxy)
            result = atd(r, false)
        return result
    }

    private fun tda(r: Request, withProxy: Boolean): Boolean {
        var result = false
        val lists = getIndexesList(r.getA(), r.getD(), r.getT())
        run bigBrother@{
            lists[0].forEach { a ->
                lists[1].forEach { d ->
                    lists[2].forEach { t ->
                        result = tryToPlace(r, a, d, t, withProxy)
                        if (result) return@bigBrother
                    }
                }
            }
        }
        if (!result && withProxy)
            result = tda(r, false)
        return result
    }

    private fun tad(r: Request, withProxy: Boolean): Boolean {
        var result = false
        val lists = getIndexesList(r.getA(), r.getD(), r.getT())
        run bigBrother@{
            lists[1].forEach { d ->
                lists[0].forEach { a ->
                    lists[2].forEach { t ->
                        result = tryToPlace(r, a, d, t, withProxy)
                        if (result) return@bigBrother
                    }
                }
            }
        }
        if (!result && withProxy)
            result = tad(r, false)
        return result
    }

    private fun dta(r: Request, withProxy: Boolean): Boolean {
        var result = false
        val lists = getIndexesList(r.getA(), r.getD(), r.getT())
        run bigBrother@{
            lists[0].forEach { a ->
                lists[2].forEach { t ->
                    lists[1].forEach { d ->
                        result = tryToPlace(r, a, d, t, withProxy)
                        if (result)
                            return@bigBrother
                    }
                }
            }
        }
        if (!result && withProxy)
            result = dta(r, false)
        return result
    }

    private fun dat(r: Request, withProxy: Boolean): Boolean {
        var result = false
        val lists = getIndexesList(r.getA(), r.getD(), r.getT())
        run bigBrother@{
            lists[2].forEach { t ->
                lists[0].forEach { a ->
                    lists[1].forEach { d ->
                        result = tryToPlace(r, a, d, t, withProxy)
                        if (result) return@bigBrother
                    }
                }
            }
        }
        if (!result && withProxy)
            result = dat(r, false)
        return result
    }

    private fun tryToPlace(r: Request, a: Int, d: Int, t: Int, proxy: Boolean): Boolean {
        r.setActivity(a)
        r.setDay(d)
        r.setTime(t)
        r.proxy = proxy
        // printInterestingValues("In Best inserting try to place")
        // println("Try $r")
        val result = data.takeNotTrustedRequest(r)
        if (result.first) {
            // println("Placed $r")
            // printInterestingValues("In Best inserting succeed to place")
            insertionList.add(r)
        }
        return result.first
    }


    private fun printInterestingValues(description: String) {
        println("\n______________ $description ____________")
        data.taken.forEach { println(it) }
        println("______________")
    }

//    private fun tryToPlace(r: Request, a: Int, d: Int, t: Int, proxy: Boolean): Request?{
//        var nr: Request?
//        if (proxy) {
//            nr = tryPlaceWithProxy(r, a, d, t)
//            if (nr == null)
//                nr = tryPlaceWithoutProxy(r,a,d,t)
//        }
//        else
//            nr = tryPlaceWithoutProxy(r,a,d,t)
//        return nr
//    }
//
//
//
//    private fun tryPlaceWithProxy(r: Request, a: Int, d: Int, t: Int): Request?{
//        r.proxy = true
//        if (data.proxyRequestsInActivity[a][d][t] > 0) { //is there already a proxy so the capacity won't grow
//            if (data.proxyDailyCapacity[d] > 0) { //proxy can take this request
//                r.setActivity(a)
//                r.setDay(d)
//                r.setTime(t)
//                return r
//            }
//        } else { //there is no proxy there, trying to put it there if there's room
//            if (data.freeSeatsInActivity[a][d][t] > 0)
//                if (data.proxyDailyCapacity[d] > 0) {
//                    r.setActivity(a)
//                    r.setDay(d)
//                    r.setTime(t)
//                    return r
//                }
//        }
//        return null
//    }
//
//    private fun tryPlaceWithoutProxy(r: Request, a: Int, d: Int, t: Int): Request?{
//        r.proxy = false
//        if (data.freeSeatsInActivity[a][d][t] > 0){
//                r.setActivity(a)
//                r.setDay(d)
//                r.setTime(t)
//                return r
//            }
//        return null
//    }

    private fun getIndexesList(a: Int, d: Int, t: Int): Array<List<Int>> {
        val activityList = data.activitiesOfCategory[data.instance.getCategoryByActivity(a)]
        activityList.remove(a)
        activityList.shuffle()
        activityList.add(0, a)

        val timeList = (0 until data.instance.num_timeslots).toMutableList()
        timeList.remove(t)
        timeList.shuffle()
        timeList.add(0, t)

        val dayList = (0 until data.instance.num_days).toMutableList()
        dayList.remove(d)
        dayList.shuffle()
        dayList.add(0, d)

        return arrayOf(activityList.toList(), dayList.toList(), timeList.toList())
    }
}