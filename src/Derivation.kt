class Derivation(args: Array<String>) {
    init {
        require(args.isNotEmpty()) {
            "Usage: Derivation <axiom-system> <target-theorems> ..."
        }
    }

    val axioms = axiomsByName(args[0])
    val targetSet: MutableSet<Formula> = args.drop(1).flatMap { s ->
        if (s.startsWith("+")) axiomsByName(s.drop(1)).map { it.formula } else listOf(s.toFormula().normalize())
    }.toMutableSet()
    val derivedList = ArrayList<Fact>()
    val derivedSet = HashSet<Formula>()
    var targetFound = false

    init {
        val targets = targetSet.toList()
        println("---- Axioms ---- ")
        axioms.forEach {
            println(it)
            add(it.formula, silent = true) { it }
        }
        if (targets.isNotEmpty()) {
            println("----- Target theorems -----")
            targets.forEach { formula ->
                print(formula)
                if (formula !in targetSet) print("  //  === FOUND IN AXIOMS ===")
                println()
            }
        }
    }

    inline fun add(formula: Formula, silent:  Boolean = false, factBuilder: () -> Fact): Boolean {
        if (!derivedSet.add(formula)) return false
        return addImpl(factBuilder(), formula, silent)
    }

    @PublishedApi
    internal fun addImpl(fact: Fact, formula: Formula, silent: Boolean): Boolean {
        derivedList += fact
        if (targetSet.remove(formula)) {
            if (!silent) {
                println("=== FOUND TARGET THEOREM $formula === ")
                fact.explainDerivation().forEach { println(it) }
            }
            if (targetSet.isEmpty()) {
                if (!silent) {
                    println("=== FOUND ALL TARGET THEOREMS ===")
                }
                targetFound = true
                return true
            }
        }
        return false
    }
}