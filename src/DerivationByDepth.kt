import kotlin.time.TimeSource

fun main(args: Array<String>) {
    val d = Derivation(args)
    var targetDepth = 0
    var maxVars = d.derivedList.maxOf { it.formula.normalVariablesSize }
    val depthOffset = ArrayList<Int>()
    depthOffset += 0
    while (!d.stop) {
        targetDepth++
        val curSize = d.derivedList.size
        depthOffset += curSize
        println("---- Theorems at depth $targetDepth ---- ")
        val start = TimeSource.Monotonic.markNow()
        for (i in 0..<curSize) {
            val implication = d.derivedList[i]
            if (implication.formula !is Implication) continue
            val impl = implication.formula.normalize(maxVars) as Implication
            val premiseDepth = targetDepth - implication.depth - 1
            for (j in depthOffset[premiseDepth]..<depthOffset[premiseDepth + 1]) {
                val premise = d.derivedList[j]
                val map = unify(premise.formula, impl.a) ?: continue
                val conclusion = impl.b.substitute(map).normalize()
                d.addDerived(conclusion) { Theorem(conclusion, premise, implication) }
                if (d.stop) return
            }
        }
        val topN = 10
        val newList = d.derivedList.subList(curSize, d.derivedList.size)
        val elapsed = start.elapsedNow()
        println("Found ${newList.size} new theorems at depth $targetDepth in ${elapsed.inWholeSeconds} seconds (${(elapsed / newList.size).inWholeNanoseconds} ns/theorem)")
        println("Top $topN simplest theorems")
        newList.sortedBy { it.formula.complexity }.take(topN).forEach { println("  $it") }
        maxVars = maxOf(maxVars, newList.maxOf { it.formula.normalVariablesSize })
    }
}