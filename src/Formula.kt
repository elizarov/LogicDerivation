import javax.naming.OperationNotSupportedException

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

sealed class Formula {
    abstract val token: Token
    abstract val operation: Operation
    open val a: Formula? get() = null
    open val b: Formula? get() = null
    private var _variables: Set<Variable>? = null
    val variables: Set<Variable> get() = _variables ?: extractVariables().also { _variables = it }
    private var _complexity: Int = 0
    val complexity: Int get() = if (_complexity > 0) _complexity else
        (1 + (a?.complexity ?: 0) + (b?.complexity ?: 0)).also { _complexity = it }
    fun toString(outer: Operation, braceSame: Boolean = false): String =
        if (outer.braceAround(operation) || outer == operation && braceSame) "(${toString()})" else toString()
    protected open fun extractVariables(): Set<Variable> = buildSet {
        a?.let { addAll(it.variables) }
        b?.let { addAll(it.variables) }
    }
    open fun updateParts1(a: Formula): Formula = throw OperationNotSupportedException()
    open fun updateParts2(a: Formula, b: Formula): Formula = throw OperationNotSupportedException()
    open fun substitute(map: Map<Variable, Formula>): Formula = updateParts2(a!!.substitute(map), b!!.substitute(map))
}

data class Variable(val name: String) : Formula() {
    override val token: Token get() = Token.Variable(name)
    override val operation: Operation = Operation.Variable
    override fun toString(): String = name
    override fun extractVariables(): Set<Variable> = setOf(this)
    override fun substitute(map: Map<Variable, Formula>) = map[this]?.takeIf { this != it }?.substitute(map) ?: this
    override fun hashCode(): Int = name.hashCode()
}

data class Negation(override val a: Formula) : Formula() {
    override val token: Token get() = Token.Negation
    override val operation: Operation = Operation.Negation
    override fun toString(): String = "!${a.toString(Operation.Negation)}"
    override fun updateParts1(a: Formula): Formula = if (a == this.a) this else Negation(a)
    override fun substitute(map: Map<Variable, Formula>): Formula = updateParts1(a.substitute(map))
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        (a.hashCode() * hashPrime + 1).also { _hash = it }
}

data class Conjunction(override val a: Formula, override val b: Formula) : Formula() {
    override val token: Token get() = Token.Conjunction
    override val operation: Operation = Operation.Conjunction
    override fun toString(): String = "${a.toString(Operation.Conjunction)} & ${b.toString(Operation.Conjunction, true)}"
    override fun updateParts2(a: Formula, b: Formula): Formula =
        if (a == this.a && b == this.b) this else Conjunction(a, b)
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        ((a.hashCode() * hashPrime + b.hashCode()) * hashPrime + 2).also { _hash = it }
}

data class Disjunction(override val a: Formula, override val b: Formula) : Formula() {
    override val token: Token get() = Token.Disjunction
    override val operation: Operation = Operation.Disjunction
    override fun toString(): String = "${a.toString(Operation.Disjunction)} | ${b.toString(Operation.Disjunction, true)}"
    override fun updateParts2(a: Formula, b: Formula): Formula =
        if (a == this.a && b == this.b) this else Disjunction(a, b)
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        ((a.hashCode() * hashPrime + b.hashCode()) * hashPrime + 3).also { _hash = it }
}

data class Implication(override val a: Formula, override val b: Formula) : Formula() {
    override val token: Token get() = Token.Implication
    override val operation: Operation = Operation.Implication
    override fun toString(): String = "${a.toString(Operation.Implication, true)} -> ${b.toString(Operation.Implication)}"
    override fun updateParts2(a: Formula, b: Formula): Formula =
        if (a == this.a && b == this.b) this else Implication(a, b)
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        ((a.hashCode() * hashPrime + b.hashCode()) * hashPrime + 4).also { _hash = it }
}