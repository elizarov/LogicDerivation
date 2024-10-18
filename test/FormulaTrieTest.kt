import junit.framework.TestCase.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class FormulaTrieTest {
    @Test
    fun testTrie() {
        val ft = FormulaTrie()
        val ff = AxiomSystem.Frege.axioms.map { it.formula }
        for (f in ff) {
            assertTrue(ft.add(f))
        }
        for (f in ff) {
            assertEquals(f, ft.findSchemaFor(f))
        }
        assertEquals(null, ft.findSchemaFor("A".toFormula()))
        assertEquals(null, ft.findSchemaFor("A->!B".toFormula()))
        assertEquals(null, ft.findSchemaFor("A->(B->C)".toFormula()))
        assertEquals(ff[0], ft.findSchemaFor("B->(A->B)".toFormula()))
        assertEquals(ff[0], ft.findSchemaFor("!A->(B->!A)".toFormula()))
        assertEquals(null, ft.findSchemaFor("!A->A".toFormula()))
        assertEquals(ff[4], ft.findSchemaFor("!!B->B".toFormula()))
        assertEquals(ff[4], ft.findSchemaFor("!!(A->B)->(A->B)".toFormula()))
    }
}