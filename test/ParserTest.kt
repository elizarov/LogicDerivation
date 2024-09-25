import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    @Test
    fun testParser() {
        assertEquals("A", "A".toFormula().toString())
        assertEquals("A & B", "A&B".toFormula().toString())
        assertEquals("A | B", "((A|B))".toFormula().toString())
        assertEquals("A -> B -> C", "A->B->C".toFormula().toString())
    }

}