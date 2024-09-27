
fun Formula.compute(assignment: (Variable) -> Boolean): Boolean = when(this) {
    is Variable -> assignment(this)
    is Negation -> !a.compute(assignment)
    is Conjunction -> a.compute(assignment) && b.compute(assignment)
    is Disjunction -> a.compute(assignment) || b.compute(assignment)
    is Implication -> !a.compute(assignment) || b.compute(assignment)
}

fun Formula.isValid(): Boolean {
    val vars = variables.toList()
    val n = vars.size
    val map = vars.withIndex().associate { it.value to it.index }
    val a = BooleanArray(n)
    val assignment = fun (v: Variable): Boolean = a[map[v]!!]
    fun check(i: Int): Boolean {
        if (i >= n) return compute(assignment)
        if (!check(i + 1)) return false
        a[i] = true
        if (!check(i + 1)) return false
        a[i] = false
        return true
    }
    return check(0)
}