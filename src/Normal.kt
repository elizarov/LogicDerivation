
private fun Formula.normalizeImpl(offset: Int, map: VariablesMap<Variable>): Formula = when (this) {
    is Variable -> map.getOrPut(this) {
        makeVariable(offset + map.size, this)
    }
    else -> {
        val a1 = a!!.normalizeImpl(offset, map)
        val b1 = b?.normalizeImpl(offset, map)
        if (b1 == null) updateParts(a1) else updateParts(a1, b1)
    }
}

fun Formula.normalize(offset: Int = 0): Formula {
    if (isNormalized(offset)) return this
    val map = VariablesMap<Variable>()
    val result = normalizeImpl(offset, map)
    if (offset == 0) result.initNormalVariablesSize(map.size)
    return result
}

private fun Formula.checkNormalizedImpl(offset: Int, mapSize: Int): Int = when {
    this is Variable -> when(variableIndex) {
        in offset..<offset + mapSize -> mapSize
        offset + mapSize -> mapSize + 1
        else -> -1
    }
    offset == 0 && mapSize == 0 && isNormalized -> normalVariablesSize
    else -> {
        val mapSize1 = a!!.checkNormalizedImpl(offset, mapSize)
        if (mapSize1 < 0) -1 else b?.checkNormalizedImpl(offset, mapSize1) ?: mapSize1
    }
}

fun Formula.checkNormalized(offset: Int = 0): Boolean {
    if (offset == 0 && isNormalized) return true
    return checkNormalizedImpl(offset, 0) >= 0
}
