import kotlin.test.Test
import kotlin.test.assertEquals

class FormulaTest {
    @Test
    fun testToString() {
        val a = Variable("A")
        val b = Variable("B")
        val c = Variable("C")
        assertEquals("A", a.toString())
        assertEquals("!A", Negation(a).toString())
        assertEquals("A & B", (Conjunction(a, b)).toString())
        assertEquals("!(A & B)", (Negation(Conjunction(a, b))).toString())
        assertEquals("A | B", (Disjunction(a, b)).toString())
        assertEquals("A -> B", (Implication(a, b)).toString())
        assertEquals("A -> B -> C", (Implication(a, Implication(b, c))).toString())
        assertEquals("(A -> B) -> C", (Implication(Implication(a, b), c)).toString())
        assertEquals("(A & B) -> C", (Implication(Conjunction(a, b), c)).toString())
        assertEquals("(A | B) -> C", (Implication(Disjunction(a, b), c)).toString())
        assertEquals("A & B & C", (Conjunction(Conjunction(a, b), c)).toString())
        assertEquals("A & (B & C)", (Conjunction(a, Conjunction(b, c))).toString())
        assertEquals("A | B | C", (Disjunction(Disjunction(a, b), c)).toString())
    }

    @Test
    fun testSubstitution() {
        val a = Variable("A")
        val b = Variable("B")
        val c = Variable("C")
        assertEquals("A -> C".toFormula(), "A -> B".toFormula().substitute(mapOf(b to c)))
        assertEquals("C & B".toFormula(), "A & B".toFormula().substitute(mapOf(a to c)))
        assertEquals("!(C | B) -> C".toFormula(), "!(A | B) -> C".toFormula().substitute(mapOf(a to c)))
    }
}