sealed class Fact {
    abstract val formula: Formula
    abstract val depth: Int
}

data class Axiom(val name: String, override val formula: Formula) : Fact() {
    override val depth: Int = 0
    override fun toString(): String = "[$name] $formula"
}

data class Theorem(
    override val formula: Formula,
    val premise: Fact,
    val implication: Fact
) : Fact() {
    private var _depth = -1
    override val depth: Int = if (_depth >= 0) _depth else (premise.depth + implication.depth + 1).also { _depth = it }
    override fun toString(): String = formula.toString()
}

fun Fact.explainDerivation(): List<String> {
    val explained = HashSet<Fact>()
    val result = ArrayList<String>()
    val ids = HashMap<Formula, String>()
    var nextId = 0
    fun makeId(i: Int): String {
        val ch = 'a' + (i % 26)
        if (i < 26) return ch.toString()
        return makeId((i - 26) / 26) + ch
    }
    fun Fact.withId(): String = when(this) {
        is Axiom -> toString()
        is Theorem -> "[${ids[formula]}] $formula"
    }
    var mostComplex: Formula = formula
    var depth = 0
    val usage = HashMap<Fact, Int>()
    fun Fact.explainDerivationImpl() {
        usage[this] = (usage[this] ?: 0) + 1
        if (!explained.add(this)) return
        if (formula.complexity > mostComplex.complexity) mostComplex = formula
        when (this) {
            is Axiom -> result += toString()
            is Theorem -> {
                premise.explainDerivationImpl()
                implication.explainDerivationImpl()
                val shiftedImpl = implication.formula.normalize(premise.formula.normalVariablesSize) as Implication
                val map = unify(premise.formula, shiftedImpl.a)!!
                val id = makeId(nextId++)
                val s0 = "[$id] $formula"
                val s1 = premise.withId().toString()
                val s2 = implication.withId().toString()
                depth++
                result += "-".repeat(maxOf(s0.length, maxOf(s1.length, s2.length) + 4))
                result += s0
                result += "MP: $s1"
                result += "    $s2"
                result += "    |  1. rename variables used in implication ${implication.withId()}"
                result += "    |       implication LHS: ${shiftedImpl.a}"
                result += "    |       implication RHS: ${shiftedImpl.b}"
                result += "    |  2. unify implication LHS with premise ${premise.withId()}"
                result += "    |      substitution map: ${map.toList().joinToString(", ") { "${it.first} := ${it.second}" }}"
                result += "    |  3. derive conclusion: ${shiftedImpl.b.substitute(map)}"
                ids[formula] = id
            }
        }
    }
    explainDerivationImpl()
    result += "# Actual derivation depth is $depth (computed depth is ${this.depth})"
    result += "# Derivation complexity is ${mostComplex.complexity} with $mostComplex"
    val topN = 3
    val popularity = usage.toList().sortedByDescending { it.second }.take(topN)
    result += "# Top $topN most popular facts are:"
    for ((fact, pop) in popularity) {
        result += "#   $pop: ${fact.withId()}"
    }
    return result
}