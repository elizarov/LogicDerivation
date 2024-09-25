val logicAxioms: List<Axiom> = listOf(
    "A -> B -> A", // 1
    "(A -> B) -> (A -> B -> C) -> A -> C", // 2
    "A | B -> A", // 3
    "A | B -> B", // 4
    "A -> B -> A | B", // 5
    "A -> A & B", // 6
    "B -> A & B", // 7
    "(A -> C) -> (B -> C) -> (A & B -> C)", // 8
    "(A -> B) -> (A -> !B) -> !A", // 9
    "!!A -> A", // 10
).mapIndexed { i, s -> Axiom((i + 1).toString(), s.toFormula().normalize()) }

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
    override val depth: Int = premise.depth + implication.depth + 1
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
    fun Fact.explainDerivationImpl() {
        if (!explained.add(this)) return
        when (this) {
            is Axiom -> result += toString()
            is Theorem -> {
                premise.explainDerivationImpl()
                implication.explainDerivationImpl()
                val shiftedImpl = implication.formula.normalize(premise.formula.variables.size) as Implication
                val map = unify(premise.formula, shiftedImpl.a)!!
                val id = makeId(nextId++)
                val s = "${premise.withId()}, ${implication.withId()}"
                result += "MP: $s"
                result += "    ${"-".repeat(s.length)}"
                result += "        [$id] $formula"
                result += "         |  1. rename variables used in implication ${implication.withId()}"
                result += "         |       implication LHS: ${shiftedImpl.a}"
                result += "         |       implication RHS: ${shiftedImpl.b}"
                result += "         |  2. unify implication LHS with premise ${premise.withId()}"
                result += "         |      substitution map: ${map.toList().joinToString(", ") { "${it.first} := ${it.second}" }}"
                result += "         |  3. derive conclusion: ${shiftedImpl.b.substitute(map)}"
                ids[formula] = id
            }
        }
    }
    explainDerivationImpl()
    return result
}