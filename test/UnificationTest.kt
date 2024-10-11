import kotlin.test.Test
import kotlin.test.assertEquals

class UnificationTest {
    @Test
    fun testUnification() {
        val a = makeVariable("A")
        val b = makeVariable("B")
        val c = makeVariable("C")
        val d = makeVariable("D")
        checkUnify(mapOf(b to a), "A", "B")
        checkUnify(mapOf(c to "A -> B".toFormula()), "A -> B", "C")
        checkUnify(mapOf(c to a, d to b), "A -> B", "C -> D")
        checkUnify(mapOf(c to a, d to a), "A -> A", "C -> D")
        checkUnify(null, "(A | B) -> A", "F -> (G -> F) -> H")
        checkUnify(null, "((A -> A) -> !(A -> A)) -> !(A -> A)", "(B -> !(C -> B))")
        checkUnify(mapOf(c to a), "(A->B)->((B->C)->(A->C))", "(A->B)->((B->A)->(A->A))")
        checkUnify(mapOf(a to "B->C".toFormula()), "A->A", "(B->C)->(B->C)")
        checkUnify(null, "A->(A->A)", "(B->C)->(B->C)")
    }

    private fun checkUnify(expected: Map<Variable, Formula>?, a: String, b: String) {
        val af = a.toFormula()
        val bf = b.toFormula()
        assertEquals(expected, unify(af, bf))
        assertEquals(expected, unify(bf, af))
    }
}