fun main(args: Array<String>) {
    val axioms = axiomsByName(args[0])
    val targetSet = args.drop(1).map { it.toFormula().normalize() }

    val derivedList = ArrayList<Fact>(axioms)
    val derivedSet = derivedList.mapTo(HashSet()) { it.formula }
    println("---- Axioms ---- ")
    derivedList.forEach { println(it) }
    var targetDepth = 0
    var maxVars = derivedList.maxOf { it.formula.variables.size }
    while (true) {
        targetDepth++
        println("---- Theorems at depth $targetDepth ---- ")
        val newList = ArrayList<Theorem>()
        for (implication in derivedList) if (implication.formula is Implication) {
            val impl = implication.formula.normalize(maxVars) as Implication
            for (premise in derivedList) if (premise.depth + implication.depth + 1 == targetDepth) {
                val map = unify(premise.formula, impl.a) ?: continue
                val conclusion = impl.b.substitute(map).normalize()
                if (derivedSet.add(conclusion)) {
                    val theorem = Theorem(conclusion, premise, implication)
                    if (conclusion in targetSet) {
                        println("=== FOUND TARGET THEOREM $conclusion === ")
                        theorem.explainDerivation().forEach { println(it) }
                        return
                    }
                    newList += theorem
                }
            }
        }
        val topN = 20
        println("Found ${newList.size} theorems, top $topN simplest ones")
        newList.sortedBy { it.formula.complexity }.take(topN).forEach { println(it) }
        derivedList += newList
        maxVars = maxOf(maxVars, newList.maxOf { it.formula.variables.size })
    }
}