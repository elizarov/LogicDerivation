sealed class Token {
    data class Variable(val name: String) : Token() { override fun toString(): String = name }
    data object Negation : Token() { override fun toString() = "!" }
    data object Conjunction : Token() { override fun toString() = "&" }
    data object Disjunction : Token() { override fun toString() = "|" }
    data object Implication : Token() { override fun toString() = "->" }
    data object OpenBrace : Token() { override fun toString() = "(" }
    data object ClosingBrace : Token() { override fun toString() = ")" }
}

enum class Operation(val arity: Int) {
    Variable(0), Negation(1), Conjunction(2), Disjunction(2), Implication(2);
    fun braceAround(inner: Operation) = this < inner || this == Implication && (inner == Conjunction || inner == Disjunction)
}

private val hashPrime = 1000003

sealed class Formula() {
    abstract val token: Token
    abstract val operation: Operation
    open val a: Formula? get() = null
    open val b: Formula? get() = null
    var cacheIndex = -1; private set
    fun initCacheIndex(i: Int) { check(cacheIndex == -1); cacheIndex = i }
    private var _variables: Set<Variable>? = null
    val variables: Set<Variable> get() = _variables ?: extractVariables().also { _variables = it }
    private var _complexity: Int = 0
    val complexity: Int get() = if (_complexity > 0) _complexity else
        (1 + (a?.complexity ?: 0) + (b?.complexity ?: 0)).also { _complexity = it }
    fun toString(outer: Operation, braceSame: Boolean = false): String =
        if (outer.braceAround(operation) || outer == operation && braceSame) "(${toString()})" else toString()
    protected open fun extractVariables(): Set<Variable> {
        val av: Set<Variable>? = a?.variables
        val bv: Set<Variable>? = b?.variables
        return if ((av == null || av is VariablesBitSet) && (bv == null || bv is VariablesBitSet)) {
            variablesSetCache[(av?.bits ?: 0) or (bv?.bits ?: 0)]
        } else buildSet {
            av?.let { addAll(it) }
            bv?.let { addAll(it) }
        }
    }
    fun updateParts(a: Formula): Formula {
        require(operation.arity == 1)
        return if (a == this.a) this else makeFormula(operation, a)
    }
    fun updateParts(a: Formula, b: Formula): Formula {
        require(operation.arity == 2)
        return if (a == this.a && b == this.b) this else makeFormula(operation, a, b)
    }
    open fun substitute(map: Map<Variable, Formula>): Formula = updateParts(a!!.substitute(map), b!!.substitute(map))
}

data class Variable(val name: String) : Formula() {
    override val token: Token get() = Token.Variable(name)
    override val operation: Operation = Operation.Variable
    override fun toString(): String = name
    override fun extractVariables(): Set<Variable> = if (cacheIndex >= 0) variablesSetCache[1 shl cacheIndex] else setOf(this)
    override fun substitute(map: Map<Variable, Formula>) = map[this]?.takeIf { this != it }?.substitute(map) ?: this
    override fun hashCode(): Int = name.hashCode()
}

data class Negation(override val a: Formula) : Formula() {
    override val token: Token get() = Token.Negation
    override val operation: Operation = Operation.Negation
    override fun toString(): String = "!${a.toString(Operation.Negation)}"
    override fun substitute(map: Map<Variable, Formula>): Formula = updateParts(a.substitute(map))
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        (a.hashCode() * hashPrime + 1).also { _hash = it }
}

data class Conjunction(override val a: Formula, override val b: Formula) : Formula() {
    override val token: Token get() = Token.Conjunction
    override val operation: Operation = Operation.Conjunction
    override fun toString(): String = "${a.toString(Operation.Conjunction)} & ${b.toString(Operation.Conjunction, true)}"
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        ((a.hashCode() * hashPrime + b.hashCode()) * hashPrime + 2).also { _hash = it }
}

data class Disjunction(override val a: Formula, override val b: Formula) : Formula() {
    override val token: Token get() = Token.Disjunction
    override val operation: Operation = Operation.Disjunction
    override fun toString(): String = "${a.toString(Operation.Disjunction)} | ${b.toString(Operation.Disjunction, true)}"
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        ((a.hashCode() * hashPrime + b.hashCode()) * hashPrime + 3).also { _hash = it }
}

data class Implication(override val a: Formula, override val b: Formula) : Formula() {
    override val token: Token get() = Token.Implication
    override val operation: Operation = Operation.Implication
    override fun toString(): String = "${a.toString(Operation.Implication, true)} -> ${b.toString(Operation.Implication)}"
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        ((a.hashCode() * hashPrime + b.hashCode()) * hashPrime + 4).also { _hash = it }
}

private val cacheVars = 12
private val cacheComplexity = 6

private val cacheSize = IntArray(cacheComplexity + 1).also { cachedSize ->
    cachedSize[1] = cacheVars
    for (k in 2..cacheComplexity) {
        for (op in Operation.entries) {
            when (op.arity) {
                1 -> cachedSize[k] += cachedSize[k - 1]
                2 -> for (l in 1..k - 2) {
                    cachedSize[k] += cachedSize[l] * cachedSize[k - l - 1]
                }
            }
        }
    }
}

private val cacheOffset = IntArray(cacheComplexity + 2).also { cachedOffset ->
    for (i in 1..cacheComplexity + 1) cachedOffset[i] = cachedOffset[i - 1] + cacheSize[i - 1]
}

private val maxCacheIndex = cacheOffset[cacheComplexity + 1]
private val cacheOps = Operation.entries.size - 1

private val cacheIndex = IntArray((cacheComplexity - 1) * cacheOps * (cacheComplexity - 2))

private fun computeIndex(k: Int, op: Operation, l: Int): Int {
    require(k in 2..cacheComplexity)
    require(op.ordinal in 1..cacheOps)
    require(l in 1..cacheComplexity - 2)
    return ((k - 2) * cacheOps + op.ordinal - 1) * (cacheComplexity - 2) + l - 1
}

private fun baseIndex(k: Int, op: Operation, l: Int): Int = cacheIndex[computeIndex(k, op, l)]

private val formulaCache: Array<Formula> = arrayOfNulls<Formula>(maxCacheIndex).also { formulaCache ->
    var i = 0
    fun add(f: Formula) {
        f.initCacheIndex(i)
        formulaCache[i++] = f
    }
    for (i in 0..<cacheVars) add(Variable(makeVariableName(i)))
    for (k in 2..cacheComplexity) {
        check(i == cacheOffset[k])
        for (op in Operation.entries) {
            when (op.arity) {
                1 -> {
                    cacheIndex[computeIndex(k, op, 1)] = i
                    for (a in cacheOffset[k - 1]..<cacheOffset[k]) {
                        add(createNewFormula(op, formulaCache[a]!!))
                    }
                }
                2 -> for (l in 1..k - 2) {
                    cacheIndex[computeIndex(k, op, l)] = i
                    val r = k - l - 1
                    for (a in cacheOffset[l]..<cacheOffset[l + 1]) {
                        for (b in cacheOffset[r]..<cacheOffset[r + 1]) {
                            add(createNewFormula(op, formulaCache[a]!!, formulaCache[b]!!))
                        }
                    }
                }
            }
        }
    }
    check(i == maxCacheIndex)
    println("Cached $maxCacheIndex formulas (up to $cacheVars vars, up to $cacheComplexity in complexity)")
} as Array<Formula>

fun makeVariableName(i: Int): String {
    val ch = 'A' + (i % 26)
    if (i < 26) return ch.toString()
    return makeVariableName((i - 26) / 26) + ch
}

fun makeVariable(i: Int, origin: Variable? = null): Variable {
    require(i >= 0)
    if (i < cacheVars) return formulaCache[i] as Variable
    val name = makeVariableName(i)
    if (origin?.name == name) return origin
    return Variable(name)
}

fun makeVariable(name: String): Variable {
    require(name.isNotEmpty())
    return if (name.length == 1 && name[0] in 'A'..<'A' + cacheVars)
        formulaCache[name[0] - 'A'] as Variable
        else Variable(name)
}

private fun createNewFormula(op: Operation, a: Formula): Formula = when(op) {
    Operation.Negation -> Negation(a)
    else -> error("invalid op $op")
}

private fun createNewFormula(op: Operation, a: Formula, b: Formula): Formula = when(op) {
    Operation.Conjunction -> Conjunction(a, b)
    Operation.Disjunction -> Disjunction(a, b)
    Operation.Implication -> Implication(a, b)
    else -> error("invalid op $op")
}

fun makeFormula(op: Operation, a: Formula): Formula {
    require(op.arity == 1)
    return if (a.cacheIndex >= 0 && a.complexity < cacheComplexity) {
        formulaCache[baseIndex(a.complexity + 1, op, 1) + a.cacheIndex - cacheOffset[a.complexity]]
    } else
        createNewFormula(op, a)
}

fun makeFormula(op: Operation, a: Formula, b: Formula): Formula {
    require(op.arity == 2)
    return if (a.cacheIndex >= 0 && b.cacheIndex >= 0 && a.complexity + b.complexity < cacheComplexity) {
        formulaCache[baseIndex(a.complexity + b.complexity + 1, op, a.complexity) +
            (a.cacheIndex - cacheOffset[a.complexity]) * cacheSize[b.complexity] + b.cacheIndex - cacheOffset[b.complexity]]
    } else
        createNewFormula(op, a, b)
}

class VariablesBitSet(val bits: Int) : AbstractSet<Variable>() {
    override fun isEmpty(): Boolean = bits == 0
    override val size: Int get() = bits.countOneBits()
    override fun iterator(): Iterator<Variable> = object : AbstractIterator<Variable>() {
        private var i = -1
        override fun computeNext() {
            while (i < cacheVars) {
                i++
                if (((1 shl i) and bits) != 0) {
                    setNext(formulaCache[i] as Variable)
                    return
                }
            }
            done()
        }
    }
}

private val variablesSetCache = Array<VariablesBitSet>(1 shl cacheVars) { VariablesBitSet(it) }


