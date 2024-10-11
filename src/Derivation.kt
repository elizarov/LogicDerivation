
private const val FILTER_INSTANCES = false

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
    private val derivedSet = HashSet<Formula>()

    var stop = false
        private set
    val derivedList: List<Fact>
        get() = _derivedList
    var totalRemoved = 0
    var totalRemovedAtDepth = IntArray(1024)

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
                if (formula !in remainingTargets) {
                    print("  //  === FOUND IN AXIOMS ===")
                }
                if (!formula.isValid()) {
                    println("  // === !!! IT IS NOT VALID !!! ===")
                    stop = true
                }
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
        if (isAlreadyDerived(formula)) return null
        val fact = factBuilder()
        addImpl(fact, formula, silent)
        return fact
    }

    @PublishedApi
    internal fun isAlreadyDerived(formula: Formula): Boolean {
        if (formula in derivedSet) return true
        if (FILTER_INSTANCES) {
            if (derivedSet.any { scheme -> formula.isInstanceOfSchema(scheme) != null }) return true
        }
        return false
    }

    @PublishedApi
    internal fun addImpl(fact: Fact, formula: Formula, silent: Boolean) {
        if (FILTER_INSTANCES) {
            removeAllInstancesOfSchema(formula)
        }
        _derivedList += fact
        derivedSet += formula
        if (!remainingTargets.remove(formula)) return
        if (remainingTargets.isEmpty()) stop = true
        if (silent) return
        println("=== FOUND TARGET THEOREM $formula === ")
        fact.explainDerivation().forEach { println(it) }
        printTargetStats()
    }

    private fun removeAllInstancesOfSchema(schema: Formula) {
        var j = 0
        for (i in _derivedList.indices) {
            val fact = _derivedList[i]
            if (fact.formula.isInstanceOfSchema(schema) != null) {
                totalRemoved++
                totalRemovedAtDepth[fact.depth]++
                derivedSet.remove(fact.formula)
                continue
            }
            _derivedList[j++] = _derivedList[i]
        }
        if (_derivedList.size - j > 0) {
            _derivedList.subList(j, _derivedList.size).clear()
        }
    }
}