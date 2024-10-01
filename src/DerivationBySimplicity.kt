import java.util.PriorityQueue
import kotlin.time.TimeSource

fun main(args: Array<String>) {
    val d = Derivation(args)
    val start = TimeSource.Monotonic.markNow()
    val complexityPrintThreshold = 7
    val theoremsPrintStat = 1000
    val queue = PriorityQueue(compareBy<Fact> { it.formula.complexity }.thenBy { it.depth })
    for (fact in d.derivedList) queue += fact

    fun tryDerive(premise: Fact, implication: Fact): Boolean {
        if (implication.formula !is Implication) return false
        val impl = implication.formula.normalize(premise.formula.normalVariablesSize) as Implication
        val map = unify(premise.formula, impl.a) ?: return false
        val conclusion = impl.b.substitute(map).normalize()
        val fact = d.addDerived(conclusion) { Theorem(conclusion, premise, implication) } ?: return false
        if (d.targetFound) return true
        queue += fact
        if (conclusion.complexity <= complexityPrintThreshold) println(fact)
        return false
    }

    val checkedFacts = ArrayList<Fact>()
    loop@while (true) {
        val a = queue.poll()!!
        if (tryDerive(a, a)) break@loop
        for (b in checkedFacts) {
            if (tryDerive(a, b)) break@loop
            if (tryDerive(b, a)) break@loop
        }
        checkedFacts += a
        if (checkedFacts.size % theoremsPrintStat == 0) {
            val speed = (start.elapsedNow() / (checkedFacts.size + queue.size)).inWholeNanoseconds
            println("# Current complexity is ${a.formula.complexity}; checked ${checkedFacts.size} facts, queued ${queue.size} at $speed ns per theorem")
        }
    }
}