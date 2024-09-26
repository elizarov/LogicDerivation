sealed class Token {
    data class Variable(val name: String) : Token() { override fun toString(): String = name }
    data object Negation : Token() { override fun toString() = "!" }
    data object Conjunction : Token() { override fun toString() = "&" }
    data object Disjunction : Token() { override fun toString() = "|" }
    data object Implication : Token() { override fun toString() = "->" }
    data object OpenBrace : Token() { override fun toString() = "(" }
    data object ClosingBrace : Token() { override fun toString() = ")" }
}

enum class Precedence { Variable, Negation, Conjunction, Disjunction, Implication;
    fun braceAround(inner: Precedence) = this < inner || this == Implication && (inner == Conjunction || inner == Disjunction)
}

private val hashPrime = 1000003

sealed class Formula {
    abstract val token: Token
    abstract val parts: List<Formula>
    abstract val precedence: Precedence
    private var _variables: Set<Variable>? = null
    val variables: Set<Variable> get() = _variables ?: extractVariables().also { _variables = it }
    private var _complexity: Int = 0
    val complexity: Int get() = if (_complexity > 0) _complexity else (1 + parts.sumOf { it.complexity }).also { _complexity = it }
    fun toString(outer: Precedence, braceSame: Boolean = false): String =
        if (outer.braceAround(precedence) || outer == precedence && braceSame) "(${toString()})" else toString()
    protected open fun extractVariables(): Set<Variable> = parts.flatMapTo(LinkedHashSet()) { it.variables }
    abstract fun updateParts(parts: List<Formula>): Formula
    open fun substitute(map: Map<Variable, Formula>): Formula = updateParts(parts.map { it.substitute(map) })
}

data class Variable(val name: String) : Formula() {
    override val token: Token get() = Token.Variable(name)
    override val parts: List<Formula> get() = emptyList()
    override val precedence: Precedence = Precedence.Variable
    override fun toString(): String = name
    override fun extractVariables(): Set<Variable> = setOf(this)
    override fun updateParts(parts: List<Formula>): Formula = this.also { check(parts.isEmpty()) }
    override fun substitute(map: Map<Variable, Formula>) = map[this]?.takeIf { this != it }?.substitute(map) ?: this
    override fun hashCode(): Int = name.hashCode()
}

data class Negation(val a: Formula) : Formula() {
    override val token: Token get() = Token.Negation
    override val precedence: Precedence = Precedence.Negation
    override val parts: List<Formula> get() = listOf(a)
    override fun toString(): String = "!${a.toString(Precedence.Negation)}"
    override fun updateParts(parts: List<Formula>): Formula =
        if (parts == this.parts) this else Negation(parts[0]).also { check(parts.size == 1) }
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        (a.hashCode() * hashPrime + 1).also { _hash = it }
}

data class Conjunction(val a: Formula, val b: Formula) : Formula() {
    override val token: Token get() = Token.Conjunction
    override val precedence: Precedence = Precedence.Conjunction
    override val parts: List<Formula> get() = listOf(a, b)
    override fun toString(): String = "${a.toString(Precedence.Conjunction)} & ${b.toString(Precedence.Conjunction, true)}"
    override fun updateParts(parts: List<Formula>): Formula =
        if (parts == this.parts) this else Conjunction(parts[0], parts[1]).also { check(parts.size == 2) }
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        ((a.hashCode() * hashPrime + b.hashCode()) * hashPrime + 2).also { _hash = it }
}

data class Disjunction(val a: Formula, val b: Formula) : Formula() {
    override val token: Token get() = Token.Disjunction
    override val precedence: Precedence = Precedence.Disjunction
    override val parts: List<Formula> get() = listOf(a, b)
    override fun toString(): String = "${a.toString(Precedence.Disjunction)} | ${b.toString(Precedence.Disjunction, true)}"
    override fun updateParts(parts: List<Formula>): Formula =
        if (parts == this.parts) this else Disjunction(parts[0], parts[1]).also { check(parts.size == 2) }
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        ((a.hashCode() * hashPrime + b.hashCode()) * hashPrime + 3).also { _hash = it }
}

data class Implication(val a: Formula, val b: Formula) : Formula() {
    override val token: Token get() = Token.Implication
    override val precedence: Precedence = Precedence.Implication
    override val parts: List<Formula> get() = listOf(a, b)
    override fun toString(): String = "${a.toString(Precedence.Implication, true)} -> ${b.toString(Precedence.Implication)}"
    override fun updateParts(parts: List<Formula>): Formula =
        if (parts == this.parts) this else Implication(parts[0], parts[1]).also { check(parts.size == 2) }
    private var _hash = 0
    override fun hashCode(): Int = if (_hash != 0) _hash else
        ((a.hashCode() * hashPrime + b.hashCode()) * hashPrime + 4).also { _hash = it }
}