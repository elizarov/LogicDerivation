import junit.framework.TestCase.assertFalse
import kotlin.test.Test
import kotlin.test.assertTrue

class ValidityTest {
    @Test
    fun testAxiomValidness() {
        for (sys in AxiomSystem.entries) {
            println("Checking $sys")
            for (axiom in sys.axioms) {
                println("  $axiom")
                assertTrue(axiom.formula.isValid(), "$axiom is not valid")
            }
        }
    }

    @Test
    fun testValidness() {
        assertTrue("A | !A".toFormula().isValid())
        assertTrue("(A & B) -> (B & A)".toFormula().isValid())
        assertFalse("A -> B".toFormula().isValid())
    }
}