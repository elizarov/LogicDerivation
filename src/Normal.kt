
private fun Formula.normalizeImpl(offset: Int, map: MutableMap<Variable, Variable>): Formula = when (this) {
    is Variable -> map.getOrPut(this) { makeVariable(offset + map.size, this) }
    else -> {
        val a1 = a!!.normalizeImpl(offset, map)
        val b1 = b?.normalizeImpl(offset, map)
        if (b1 == null) updateParts(a1) else updateParts(a1, b1)
    }
}

fun Formula.normalize(offset: Int = 0): Formula = normalizeImpl(offset, HashMap())