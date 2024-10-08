import junit.framework.TestCase.assertFalse
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
        val aThenB = makeFormula(Operation.Implication, a, b)
        assertTrue(aThenB.cacheIndex >= 0)
        assertEquals("A -> B", aThenB.toString())
        val aThenBThenNotC = makeFormula(Operation.Implication, makeFormula(Operation.Implication, a, b), makeFormula(Operation.Negation, c))
        assertTrue(aThenBThenNotC.cacheIndex >= 0)
        assertEquals("(A -> B) -> !C", aThenBThenNotC.toString())
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

    @Test
    fun testVariableNames() {
        for (name in listOf("A", "Z", "AA", "ABC", "CBA", "ZX")) {
            val i = recoverVariableIndex(name)
            assertEquals(name, makeVariableName(i))
        }
    }

    @Test
    fun testIsNormalized() {
        assertTrue("A".toFormula().isNormalized)
        assertFalse("B".toFormula().isNormalized)
        assertFalse("C".toFormula().isNormalized)
        assertTrue("!A".toFormula().isNormalized)
        assertFalse("!B".toFormula().isNormalized)
        assertFalse("!C".toFormula().isNormalized)
        assertTrue("A -> A".toFormula().isNormalized)
        assertTrue("A -> B".toFormula().isNormalized)
        assertFalse("A -> C".toFormula().isNormalized)
        assertFalse("B -> A".toFormula().isNormalized)
        assertTrue("A -> (A -> A)".toFormula().isNormalized)
        assertTrue("A -> (A -> B)".toFormula().isNormalized)
        assertTrue("A -> (B -> C)".toFormula().isNormalized)
        assertTrue("A -> (B -> A)".toFormula().isNormalized)
        assertTrue("A -> (B -> B)".toFormula().isNormalized)
        assertFalse("B -> (A -> A)".toFormula().isNormalized)
        assertFalse("C -> (B -> A)".toFormula().isNormalized)
        assertFalse("A -> (C -> B)".toFormula().isNormalized)
        assertFalse("A -> (C -> C)".toFormula().isNormalized)
    }
}