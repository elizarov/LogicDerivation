
private class EC(var root: Variable, var to: Formula? = null) {
    val vars = ArrayList<Variable>().also { it.add(root) }
    var checking: Boolean = false
    var checked: Boolean = false
    var nextChecked: EC? = null
}

private fun check(a: Variable, map: HashMap<Variable, EC>): Boolean {
    var lastChecked: EC? = null
    fun checkImpl(a: Variable): Boolean {
        val ec = map[a] ?: return true
        if (ec.checked) return true
        if (ec.checking) return false
        ec.checking = true
        val next = ec.to?.variables
        if (next != null && next.any { !check(it, map) }) return false
        ec.checking = false
        ec.checked = true
        ec.nextChecked = lastChecked
        lastChecked = ec
        return true
    }
    if (!checkImpl(a)) return false
    var cur = lastChecked
    while (cur != null) {
        val next = cur.nextChecked
        cur.checked = false
        cur.nextChecked = null
        cur = next
    }
    return true
}

private fun unifyVars(a: Variable, b: Variable, map: HashMap<Variable, EC>): Boolean {
    val aec = map[a]
    val bec = map[b]
    val ec = when {
        aec != null -> {
            if (bec != null) {
                aec.vars += bec.vars
                if (bec.to != null) {
                    if (aec.to != null) {
                        if (!unifyImpl(aec.to!!, bec.to!!, map)) return false
                    } else {
                        aec.to = bec.to
                    }
                }
            } else {
                aec.vars += b
            }
            aec
        }
        bec != null -> {
            bec.vars += a
            bec
        }
        else -> EC(a).also { it.vars += b }
    }
    ec.root = ec.vars.minBy { it.name }
    for (v in ec.vars) map[v] = ec
    return check(a, map)
}

private fun unifyVarExpr(a: Variable, b: Formula, map: HashMap<Variable, EC>): Boolean {
    val aec = map[a]
    if (aec == null) {
        map[a] = EC(a, b)
        return check(a, map)
    }
    val c = aec.to
    if (c == null) {
        aec.to = b
        return check(a, map)
    }
    return unifyImpl(b, c, map)
}

private fun unifyImpl(a: Formula, b: Formula, map: HashMap<Variable, EC>): Boolean = when {
    a == b -> true
    a is Variable && b is Variable -> unifyVars(a, b, map)
    a is Variable -> unifyVarExpr(a, b, map)
    b is Variable -> unifyVarExpr(b, a, map)
    else -> a.token == b.token && a.parts.zip(b.parts).all { unifyImpl(it.first, it.second, map) }
}

fun unify(a: Formula, b: Formula): Map<Variable, Formula>? {
    val map = HashMap<Variable, EC>()
    if (!unifyImpl(a, b, map)) return null
    return map.mapValues { it.value.to ?: it.value.root }.filter { it.key != it.value }
}