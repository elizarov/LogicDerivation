import javax.xml.validation.Schema
import kotlin.test.Test
import kotlin.test.assertEquals

class SchemaTest {
    @Test
    fun testSchema() {
        val a = makeVariable("A")
        val b = makeVariable("B")
        val c = makeVariable("C")
        checkSchema(mapOf(a to a), "A", "A")
        checkSchema(mapOf(a to b), "B", "A")
        checkSchema(null, "A", "A->B")
        checkSchema(mapOf(a to "A->B".toFormula()), "A->B", "A")
        checkSchema(mapOf(a to a, b to "A->A".toFormula()), "A->(A->A)", "A->B")
        checkSchema(mapOf(a to a), "A->A", "A->A")
        checkSchema(null, "A->B", "A->A")
        checkSchema(mapOf(a to "A->B".toFormula()), "(A->B)->(A->B)", "A->A")
        checkSchema(mapOf(a to "!A".toFormula()), "!A->!A", "A->A")
    }

    private fun checkSchema(expected: Map<Variable, Formula>?, instance: String, schema: String) {
        assertEquals(expected, instance.toFormula().isInstanceOfSchema(schema.toFormula()))
    }
}