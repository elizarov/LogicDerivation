import java.util.PriorityQueue

fun main(args: Array<String>) {
    val complexityPrintThreshold = 10
    val theoremsPrintStat = 1000

    val targetSet = args.map { it.toFormula().normalize() }
    val queue = PriorityQueue<Fact>(compareBy { it.formula.complexity })
    val enqueued = HashSet<Formula>()

    fun enqueue(fact: Fact): Boolean {
        if (!enqueued.add(fact.formula)) return false
        queue += fact
        return true
    }
    logicAxioms.forEach { enqueue(it) }

    fun tryDerive(premise: Fact, implication: Fact): Boolean {
        if (implication.formula !is Implication) return false
        val impl = implication.formula.normalize(premise.formula.variables.size) as Implication
        val map = unify(premise.formula, impl.a) ?: return false
        val conclusion = impl.b.substitute(map).normalize()
        val theorem = Theorem(conclusion, premise, implication)
        if (!enqueue(theorem)) return false
        if (conclusion.complexity <= complexityPrintThreshold) println(theorem)
        if (conclusion in targetSet) {
            println("=== FOUND TARGET THEOREM $conclusion === ")
            theorem.explainDerivation().forEach { println(it) }
            return true
        }
        return false
    }

    val derivedSet = LinkedHashSet<Fact>()

    while (true) {
        val a = queue.poll()!!
        if (tryDerive(a, a)) break
        for (b in derivedSet) {
            if (tryDerive(a, b)) break
            if (tryDerive(b, a)) break
        }
        derivedSet += a
        if (derivedSet.size % theoremsPrintStat == 0) println("Derived ${derivedSet.size} theorems")
    }
}