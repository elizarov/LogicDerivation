import kotlin.test.Test
import kotlin.test.assertEquals

class UnificationTest {
    @Test
    fun testUnification() {
        val a = makeVariable("A")
        val b = makeVariable("B")
        val c = makeVariable("C")
        val d = makeVariable("D")
        assertEquals(mapOf(b to a), unify("A".toFormula(), "B".toFormula()))
        assertEquals(mapOf(c to "A -> B".toFormula()), unify("A -> B".toFormula(), "C".toFormula()))
        assertEquals(mapOf(c to a, d to b), unify("A -> B".toFormula(), "C -> D".toFormula()))
        assertEquals(mapOf(c to a, d to a), unify("A -> A".toFormula(), "C -> D".toFormula()))
        assertEquals(null, unify("(A | B) -> A".toFormula(), "F -> (G -> F) -> H".toFormula()))
        assertEquals(null, unify("((A -> A) -> !(A -> A)) -> !(A -> A)".toFormula(), "(B -> !(C -> B))".toFormula()))
        assertEquals(mapOf(c to a), unify("(A->B)->((B->C)->(A->C))".toFormula(), "(A->B)->((B->A)->(A->A))".toFormula()))
        assertEquals(mapOf(a to "B->C".toFormula()), unify("A->A".toFormula(), "(B->C)->(B->C)".toFormula()))
    }
}