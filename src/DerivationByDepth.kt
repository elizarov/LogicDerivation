fun main(args: Array<String>) {
    val d = Derivation(args)
    var targetDepth = 0
    var maxVars = d.derivedList.maxOf { it.formula.variables.size }
    while (!d.targetFound) {
        targetDepth++
        println("---- Theorems at depth $targetDepth ---- ")
        val curSize = d.derivedList.size
        for (i in 0..<curSize) {
            val implication = d.derivedList[i]
            if (implication.formula !is Implication) continue
            val impl = implication.formula.normalize(maxVars) as Implication
            for (j in 0..<curSize) {
                val premise = d.derivedList[j]
                if (premise.depth + implication.depth + 1 != targetDepth) continue
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