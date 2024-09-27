import java.util.PriorityQueue
import kotlin.time.TimeSource

fun main(args: Array<String>) {
    val axioms = axiomsByName(args[0])
    val targetSet = args.drop(1).map { it.toFormula().normalize() }

    val start = TimeSource.Monotonic.markNow()
    val complexityPrintThreshold = 10
    val theoremsPrintStat = 1000

    val queue = PriorityQueue(compareBy<Fact> { it.formula.complexity }.thenBy { it.depth })
    val enqueued = HashSet<Formula>()

    fun enqueue(fact: Fact): Boolean {
        if (!enqueued.add(fact.formula)) return false
        queue += fact
        return true
    }
    axioms.forEach { enqueue(it) }

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

    val checkedSet = LinkedHashSet<Fact>()
    loop@while (true) {
        val a = queue.poll()!!
        if (tryDerive(a, a)) break@loop
        for (b in checkedSet) {
            if (tryDerive(a, b)) break@loop
            if (tryDerive(b, a)) break@loop
        }
        checkedSet += a
        if (checkedSet.size % theoremsPrintStat == 0) {
            val speed = (start.elapsedNow() / enqueued.size).inWholeNanoseconds
            println("# Current complexity is ${a.formula.complexity}; checked ${checkedSet.size} theorems, queued ${enqueued.size} at $speed ns per theorem")
        }
    }
}