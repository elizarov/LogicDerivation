import kotlin.collections.ArrayList

class FormulaTrie {
    public var size = 0
        private set
    private val root = Node()

    fun add(formula: Formula): Boolean {
        val res: Formula = recAdd(root, formula) { (it ?: formula.also { size++ }) as Formula }
        return res === formula
    }

    private tailrec fun <T> recAdd(cur: Node, f: Formula, cont: (Any?) -> T): T = when {
        f is Variable -> {
            val i = f.variableIndex
            val n = cont(cur.varAtOrNull(i))
            cur.setVarAt(i, n)
            n
        }
        f.b == null -> {
            val n = cur.opAt(f.operation)
            recAdd(n, f.a!!, cont)
        }
        else -> {
            val n1 = cur.opAt(f.operation)
            @Suppress("NON_TAIL_RECURSIVE_CALL")
            val n2 = recAdd(n1, f.a!!, nextNodeBuilder)
            recAdd(n2, f.b!!, cont)
        }
    }

    fun containsSchemaFor(f: Formula): Boolean = findSchemaFor(f) != null

    fun findSchemaFor(f: Formula): Formula? = recFindSchemaFor(root, f, ArrayList<Formula?>(), nextAsFormula)

    private fun recFindSchemaFor(cur: Node, f: Formula, map: ArrayList<Formula?>, cont: (Any) -> Formula?): Formula? {
        when {
            f is Variable -> { /* skip to var assignment */ }
            f.b == null -> {
                val n = cur.opAtOrNull(f.operation)
                if (n != null) {
                    recFindSchemaFor(n, f.a!!, map, cont)?.let { return it }
                }
            }
            else -> {
                val n = cur.opAtOrNull(f.operation)
                if (n != null) {
                    recFindSchemaFor(n, f.a!!, map) { k ->
                        recFindSchemaFor(k as Node, f.b!!, map, cont)
                    }?.let { return it }
                }
            }
        }
        // variable assignment
        cur.forAllVars { i, n ->
            val prev = map.getOrNull(i)
            if (prev == null) {
                while(i >= map.size) map += null
                map[i] = f
                cont(n)?.let { return it }
                map[i] = null
            } else if (prev == f) {
                cont(n)?.let { return it }
            }
        }
        return null
    }

    private class Node {
        var negation: Node? = null
        var implication: Node? = null
        var vars: Array<Any?>? = null // array of Node | Formula
        fun varAtOrNull(i: Int): Any? = vars?.getOrNull(i)
        fun setVarAt(i: Int, v: Any?) {
            var vars = this.vars
            if (vars == null) {
                vars = arrayOfNulls((i + 1).coerceAtLeast(4))
                this.vars = vars
            } else if (i >= vars.size) {
                vars = vars.copyOf((i + 1).coerceAtLeast(vars.size * 2))
                this.vars = vars
            }
            vars[i] = v
        }
        inline fun forAllVars(block: (i: Int, next: Any) -> Unit) {
            val vars = this.vars ?: return
            for (i in vars.indices) vars[i]?.let { block(i, it) }
        }
        fun opAtOrNull(operation: Operation): Node? = when (operation) {
            Operation.Negation -> negation
            Operation.Implication -> implication
            else -> error("Unsupported operation $operation")
        }
        fun opAt(operation: Operation): Node = when (operation) {
            Operation.Negation -> negation ?: Node().also { negation = it }
            Operation.Implication -> implication ?: Node().also { implication = it }
            else -> error("Unsupported operation $operation")
        }
    }

    private val nextNodeBuilder: (Any?) -> Node = { (it ?: Node()) as Node }
    private val nextAsFormula: (Any) -> Formula? = { it as? Formula }
}