class Derivation(args: Array<String>) {
    init {
        require(args.isNotEmpty()) {
            "Usage: Derivation <axiom-system> <target-theorems> ..."
        }
    }

    private val axioms = axiomsByName(args[0])
    private val targetTheorems = args.drop(1).flatMap { s -> s.toFormulaList() }
    private val remainingTargets = targetTheorems.toMutableSet()
    private val _derivedList = ArrayList<Fact>()

    @PublishedApi
    internal val derivedSet = HashSet<Formula>()

    var targetFound = false
        private set
    val derivedList: List<Fact>
        get() = _derivedList

    init {
        println("---- Axioms ---- ")
        axioms.forEach {
            println(it)
            addDerived(it.formula, silent = true) { it }
        }
        if (targetTheorems.isNotEmpty()) {
            println("----- Target theorems -----")
            targetTheorems.forEach { formula ->
                print(formula)
                if (formula !in remainingTargets) print("  //  === FOUND IN AXIOMS ===")
                println()
            }
        }
        printTargetStats()
    }

    fun printTargetStats() {
        if (targetTheorems.isEmpty()) return
        if (remainingTargets.isEmpty()) {
            println("=== FOUND ALL TARGET THEOREMS ===")
            return
        }
        println("# ${remainingTargets.size} target theorems out of ${targetTheorems.size} remaining")
    }

    inline fun addDerived(formula: Formula, silent:  Boolean = false, factBuilder: () -> Fact): Fact? {
        if (!derivedSet.add(formula)) return null
        val fact = factBuilder()
        addImpl(fact, formula, silent)
        return fact
    }

    @PublishedApi
    internal fun addImpl(fact: Fact, formula: Formula, silent: Boolean) {
        _derivedList += fact
        if (!remainingTargets.remove(formula)) return
        if (remainingTargets.isEmpty()) targetFound = true
        if (silent) return
        println("=== FOUND TARGET THEOREM $formula === ")
        fact.explainDerivation().forEach { println(it) }
        printTargetStats()
    }
}