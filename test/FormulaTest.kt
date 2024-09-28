import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormulaTest {
    @Test
    fun testToString() {
        val a = makeVariable("A")
        val b = makeVariable("B")
        val c = makeVariable("C")
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
        val a = makeVariable("A")
        val b = makeVariable("B")
        val c = makeVariable("C")
        assertEquals("A -> C".toFormula(), "A -> B".toFormula().substitute(mapOf(b to c)))
        assertEquals("C & B".toFormula(), "A & B".toFormula().substitute(mapOf(a to c)))
        assertEquals("!(C | B) -> C".toFormula(), "!(A | B) -> C".toFormula().substitute(mapOf(a to c)))
    }

    @Test
    fun testCache() {
        val a = makeVariable("A")
        val b = makeVariable("B")
        val c = makeVariable("C")
        val notA = makeFormula(Operation.Negation, a)
        assertTrue(notA.cacheIndex >= 0)
        assertEquals("!A", notA.toString())
        val aAndB = makeFormula(Operation.Conjunction, a, b)
        assertTrue(aAndB.cacheIndex >= 0)
        assertEquals("A & B", aAndB.toString())
        val aOrBimpliesC = makeFormula(Operation.Implication, makeFormula(Operation.Disjunction, a, b), c)
        assertTrue(aOrBimpliesC.cacheIndex >= 0)
        assertEquals("(A | B) -> C", aOrBimpliesC.toString())
    }

    @Test
    fun testVariables() {
        val a = makeVariable("A")
        val b = makeVariable("B")
        val c = makeVariable("C")
        assertEquals(setOf(a), "A".toFormula().variables)
        assertEquals(setOf(a, b), "A & B".toFormula().variables)
        assertEquals(setOf(a, c), "!A -> C".toFormula().variables)
        assertEquals(setOf(b), "B | !B".toFormula().variables)
    }
}