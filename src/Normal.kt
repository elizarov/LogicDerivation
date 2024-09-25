private fun makeName(i: Int): String {
    val ch = 'A' + (i % 26)
    if (i < 26) return ch.toString()
    return makeName((i - 26) / 26) + ch
}

private fun Formula.normalizeImpl(offset: Int, map: MutableMap<Variable, Variable>): Formula = when (this) {
    is Variable -> map.getOrPut(this) {
        val name = makeName(offset + map.size)
        if (name == this.name) this else Variable(name)
    }
    else -> updateParts(this.parts.map<Formula, Formula> { it.normalizeImpl(offset, map) })
}

fun Formula.normalize(offset: Int = 0): Formula = normalizeImpl(offset, HashMap())