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
    else -> {
        val a1 = a!!.normalizeImpl(offset, map)
        val b1 = b?.normalizeImpl(offset, map)
        if (b1 == null) updateParts1(a1) else updateParts2(a1, b1)
    }
}

fun Formula.normalize(offset: Int = 0): Formula = normalizeImpl(offset, HashMap())