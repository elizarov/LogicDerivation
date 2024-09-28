fun main(args: Array<String>) {
    val d = Derivation(args)
    var targetDepth = 0
    var maxVars = d.derivedList.maxOf { it.formula.variables.size }
    val depthOffset = ArrayList<Int>()
    depthOffset += 0
    while (!d.targetFound) {
        targetDepth++
        val curSize = d.derivedList.size
        depthOffset += curSize
        println("---- Theorems at depth $targetDepth ---- ")
        for (i in 0..<curSize) {
            val implication = d.derivedList[i]
            if (implication.formula !is Implication) continue
            val impl = implication.formula.normalize(maxVars) as Implication
            val premiseDepth = targetDepth - implication.depth - 1
            for (j in depthOffset[premiseDepth]..<depthOffset[premiseDepth + 1]) {
                val premise = d.derivedList[j]
                val map = unify(premise.formula, impl.a) ?: continue
                val conclusion = impl.b.substitute(map).normalize()
                if (d.add(conclusion) { Theorem(conclusion, premise, implication) }) return
            }
        }
        val topN = 20
        val newList = d.derivedList.subList(curSize, d.derivedList.size)
        println("Found ${newList.size} theorems, top $topN simplest ones")
        newList.sortedBy { it.formula.complexity }.take(topN).forEach { println(it) }
        maxVars = maxOf(maxVars, newList.maxOf { it.formula.variables.size })
    }
}