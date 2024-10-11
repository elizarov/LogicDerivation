
private fun checkInstanceOfSchemaImpl(formula: Formula, schema: Formula, map: VariablesMap<Formula>): Boolean = when {
    schema is Variable -> {
        val prev = map[schema]
        if (prev == null) {
            map[schema] = formula
            true
        } else {
            prev == formula
        }
    }
    formula.operation != schema.operation -> false
    else -> checkInstanceOfSchemaImpl(formula.a!!, schema.a!!, map) &&
        formula.b?.let { checkInstanceOfSchemaImpl(it, schema.b!!, map) } != false
}

fun Formula.isInstanceOfSchema(schema: Formula): VariablesMap<Formula>? {
    val map = VariablesMap<Formula>()
    if (!checkInstanceOfSchemaImpl(this, schema, map)) return null
    return map
}