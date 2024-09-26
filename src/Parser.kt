private val allTokens =
    listOf(Token.Negation, Token.Conjunction, Token.Disjunction, Token.Implication, Token.OpenBrace, Token.ClosingBrace)

private class Parser(var string: String) {
    private var i = 0
    private var token: Token? = null

    fun next() {
        while (i < string.length && string[i].isWhitespace()) i++
        if (i >= string.length) {
            token = null
            return
        }
        if (string[i].isLetter()) {
            var j = i + 1
            while (j < string.length && string[j].isLetter()) j++
            token = Token.Variable(string.substring(i, j))
            i = j
            return
        }
        for (t in allTokens) {
            if (string.startsWith(t.toString(), i)) {
                token = t
                i += t.toString().length
                return
            }
        }
        throw IllegalArgumentException("Invalid token $token at position $i: $string")
    }

    fun parseVariableOrNegation(): Formula = when (val cur = token) {
        is Token.Variable -> makeVariable(cur.name).also { next() }
        is Token.Negation -> {
            next()
            makeFormula(Operation.Negation, parseVariableOrNegation())
        }
        is Token.OpenBrace -> {
            next()
            parseImplication().also {
                require(token == Token.ClosingBrace) { "Expected closing brace, got $token, at position $i: $string" }
                next()
            }
        }
        else -> throw IllegalArgumentException("Unexpected $cur at position $i: $string")
    }

    fun parseConjunction(): Formula {
        var result = parseVariableOrNegation()
        while (token == Token.Conjunction) {
            next()
            result = makeFormula(Operation.Conjunction, result, parseVariableOrNegation())
        }
        return result
    }

    fun parseDisjunction(): Formula {
        var result = parseConjunction()
        while (token == Token.Disjunction) {
            next()
            result = makeFormula(Operation.Disjunction, result, parseConjunction())
        }
        return result
    }

    fun parseImplication(): Formula {
        var result = parseDisjunction()
        if (token == Token.Implication) {
            next()
            return makeFormula(Operation.Implication, result, parseImplication())
        }
        return result
    }

    fun parseFormula(): Formula {
        next()
        val result = parseImplication()
        require(token == null) { "Unexpected end of formula, got $token, at position $i: $string" }
        return result
    }
}

fun String.toFormula(): Formula = Parser(this).parseFormula()