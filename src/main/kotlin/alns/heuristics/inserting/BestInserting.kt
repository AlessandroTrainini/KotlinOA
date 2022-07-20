package alns.heuristics.inserting

import Instance.InstanceRequest
import alns.Data
import alns.Request
import alns.heuristics.InsertingHeuristic



class BestInserting: InsertingHeuristic {

    private val ADT = intArrayOf(0,1,2)
    private val ATD = intArrayOf(0,2,1)
    private val DAT = intArrayOf(1,0,2)
    private val DTA = intArrayOf(1,2,0)
    private val TAD = intArrayOf(2,0,1)
    private val TDA = intArrayOf(2,1,0)

    private lateinit var data: Data

    override fun insertRequest(data: Data, q: Int): List<Request> {
        this.data = data
        val insertionList = mutableListOf<Request>()
        val candidates = data.gOrder.filter { it.first in data.missing }.toMutableList()
        while (insertionList.size < q) {
            if (candidates.size == 0)
                return insertionList
            val candidate = data.instance.getRequestById(candidates.first().first)
            candidates.removeAt(0) //pop
            val fingerprint = getRequestTaste(candidate)
            val nr = when {
                fingerprint.contentEquals(ADT) -> adt(Request(candidate, candidate.proxy > 0), candidate.proxy > 0)
                fingerprint.contentEquals(ATD) -> atd(Request(candidate, candidate.proxy > 0), candidate.proxy > 0)
                fingerprint.contentEquals(DAT) -> dat(Request(candidate, candidate.proxy > 0), candidate.proxy > 0)
                fingerprint.contentEquals(DTA) -> dta(Request(candidate, candidate.proxy > 0), candidate.proxy > 0)
                fingerprint.contentEquals(TDA) -> tda(Request(candidate, candidate.proxy > 0), candidate.proxy > 0)
                fingerprint.contentEquals(TAD) -> tad(Request(candidate, candidate.proxy > 0), candidate.proxy > 0)
                else -> {
                    null
                }
            }
            if (nr != null) {
                data.takeTrustedRequest(nr)
                insertionList.add(nr)
            }
        }
        return insertionList
    }

    private fun getRequestTaste(r: InstanceRequest): IntArray {
        val ai = Pair(data.agRatioOrder.indexOfFirst { it.first == r.id },0)
        val di = Pair(data.dgRatioOrder.indexOfFirst { it.first == r.id },1)
        val ti = Pair(data.tgRatioOrder.indexOfFirst { it.first == r.id },2)


        return listOf(ai,ti,di).sortedBy { it.first }.map { it.second }.toIntArray()

    }

    private fun adt(r: Request, withProxy: Boolean): Request?{
        var nr: Request? = null
        val lists = getIndexesList(r.activity, r.day, r.time)
        for (t in lists[2])
            for (d in lists[1])
                for (a in lists[0])
                {
                    nr = tryToPlace(r, a, d, t, withProxy)
                    if (nr != null) break
                }
        if (nr == null && withProxy)
            adt(r,false)
        return nr
    }
    private fun atd(r: Request, withProxy: Boolean): Request?{
        var nr: Request? = null
        val lists = getIndexesList(r.activity, r.day, r.time)
        for (d in lists[1])
            for (t in lists[2])
                for (a in lists[0])
                {
                    nr = tryToPlace(r, a, d, t, withProxy)
                    if (nr != null) break
                }
        if (nr == null && withProxy)
            atd(r,false)
        return nr
    }

    private fun tda(r: Request, withProxy: Boolean): Request?{
        var nr: Request? = null
        val lists = getIndexesList(r.activity, r.day, r.time)
        for (a in lists[0])
            for (d in lists[1])
                for (t in lists[2])
                {
                    nr = tryToPlace(r, a, d, t, withProxy)
                    if (nr != null) break
                }
        if (nr == null && withProxy)
            tda(r,false)
        return nr
    }

    private fun tad(r: Request, withProxy: Boolean): Request?{
        var nr: Request? = null
        val lists = getIndexesList(r.activity, r.day, r.time)
        for (d in lists[1])
            for (a in lists[0])
                for (t in lists[2])
                {
                    nr = tryToPlace(r, a, d, t, withProxy)
                    if (nr != null) break
                }
        if (nr == null && withProxy)
            tad(r,false)
        return nr
    }

    private fun dta(r: Request, withProxy: Boolean): Request?{
        var nr: Request? = null
        val lists = getIndexesList(r.activity, r.day, r.time)
        for (a in lists[0])
            for (t in lists[2])
                for (d in lists[1]) {
                    nr = tryToPlace(r, a, d, t, withProxy)
                    if (nr != null) break
                }
        if (nr == null && withProxy)
            dta(r,false)
        return nr
    }

    private fun dat(r: Request, withProxy: Boolean): Request?{
        var nr: Request? = null
        val lists = getIndexesList(r.activity, r.day, r.time)
        for (t in lists[2])
            for (a in lists[0])
                for (d in lists[1])
                {
                    nr = tryToPlace(r, a, d, t, withProxy)
                    if (nr != null) break
                }
        if (nr == null && withProxy)
            dat(r,false)
        return nr
    }

    private fun tryToPlace(r: Request, a: Int, d: Int, t: Int, proxy: Boolean): Request?{
        var nr: Request?
        if (proxy) {
            nr = tryPlaceWithProxy(r, a, d, t)
            if (nr == null)
                nr = tryPlaceWithoutProxy(r,a,d,t)
        }
        else
            nr = tryPlaceWithoutProxy(r,a,d,t)
        return nr
    }



    private fun tryPlaceWithProxy(r: Request, a: Int, d: Int, t: Int): Request?{
        r.proxy = true
        if (data.proxyRequestsInActivity[a][d][t] > 0) { //is there already a proxy so the capacity won't grow
            if (data.proxyDailyCapacity[d] > 0) { //proxy can take this request
                r.setActivity(a)
                r.setDay(d)
                r.setTime(t)
                return r
            }
        } else { //there is no proxy there, trying to put it there if there's room
            if (data.freeSeatsInActivity[a][d][t] > 0)
                if (data.proxyDailyCapacity[d] > 0) {
                    r.setActivity(a)
                    r.setDay(d)
                    r.setTime(t)
                    return r
                }
        }
        return null
    }

    private fun tryPlaceWithoutProxy(r: Request, a: Int, d: Int, t: Int): Request?{
        r.proxy = false
        if (data.freeSeatsInActivity[a][d][t] > 0){
                r.setActivity(a)
                r.setDay(d)
                r.setTime(t)
                return r
            }
        return null
    }

    private fun getIndexesList(a: Int, d: Int, t:Int): Array<List<Int>> {
        val activityList = data.activitiesOfCategory[data.instance.getCategoryByActivity(a)]
        activityList.remove(a)
        activityList.add(0, a)

        val timeList = (0 until data.instance.num_timeslots).toMutableList()
        timeList.remove(t)
        timeList.add(0,t)

        val dayList = (0 until data.instance.num_days).toMutableList()
        dayList.remove(d)
        dayList.add(0,d)

        return arrayOf(activityList.toList(), dayList.toList(), timeList.toList())
    }
}