
enum class AxiomSystem(val shortName: String, val axioms: List<Axiom>) {
    Default("", // TBD???
        "A -> B -> A",
        "(A -> B) -> (A -> B -> C) -> A -> C",
        "(A -> B) -> (A -> !B) -> !A",
        "!!A -> A"
    ),
    // https://en.wikipedia.org/wiki/List_of_axiomatic_systems_in_logic
    // Implication and negation
    Frege("F",
        "A -> (B -> A)",
        "(A -> (B -> C)) -> ((A -> B) -> (A -> C))",
        "(A -> (B -> C)) -> (B -> (A -> C))",
        "(A -> B) -> (!B -> !A)",
        "!!A -> A",
        "A -> !!A",
    ),
    Hilbert("H",
        "A -> (B -> A)",
        "(A -> (B -> C)) -> (B -> (A -> C))",
        "(B -> C) -> ((A -> B) -> (A -> C))",
        "A -> (!A -> B)",
        "(A -> B) -> ((!A -> B) -> B)"
    ),
    Lukasiewicz1("1L",
        "(A -> B) -> ((B -> C) -> (A -> C))",
        "(!A -> A) -> A",
        "A -> (!A -> B)"
    ),
    Lukasiewicz2("2L",
        "((A -> B) -> C) -> (!A -> C)",
        "((A -> B) -> C) -> (B -> C)",
        "(!A -> C) -> ((B -> C) -> ((A -> B) -> C))"
    ),
    Lukasiewicz3("3L",
        "A -> (B -> A)",
        "A -> (!A -> B)",
        "(!A -> B) -> ((B -> A) -> A)"
    ),
    Arai("A",
        "(A -> B) -> ((B -> C) -> (A -> C))",
        "A -> (!A -> B)",
        "(!A -> B) -> ((B -> A) -> A)"
    ),
    LukasiewiczTarski("LT",
        "((A -> (B -> A)) -> (((!C -> (D -> !E)) -> ((C -> (D -> F)) -> ((E -> D) -> (E -> F)))) -> G)) -> (H -> G)"
    ),
    Meredith("MR",
        "((((A -> B) -> (!C -> !D)) -> C) -> E) -> ((E -> A) -> (D -> A))"    
    ),
    Mendelson("MN",
        "A -> (B -> A)",
        "(A -> (B -> C)) -> ((A -> B) -> (A -> C))",
        "(!A -> !B) -> ((!A -> B) -> A)"
    ),
    Russel("R",
        "A -> (B -> A)",
        "(A -> B) -> ((B -> C) -> (A -> C))",
        "(A -> (B -> C)) -> (B -> (A -> C))",
        "!!A -> A",
        "(A -> !A) -> !A",
        "(A -> !B) -> (B -> !A)"
    ),
    Sobocinski1("1S",
        "!A -> (A -> B)",
        "A -> (B -> (C -> A))",
        "(!A -> C) -> ((B -> C) -> ((A -> B) -> C))"
    ),
    Sobocinski2("2S",
        "(A -> B) -> (!B -> (A -> C))",
        "A -> (B -> (C -> A))",
        "(!A -> B) -> ((A -> B) -> B)"
    ),
    // Conjunction & Disjunction
    Conjunction("C",
        "A & B -> A",
        "A & B -> B",
        "A -> B -> A & B"
    ),
    Disjunction("D",
        "A -> A | B",
        "B -> A | B",
        "(A -> C) -> (B -> C) -> (A | B -> C)"
    )
    ;

    constructor(shortName: String, vararg axioms: String):
        this(shortName, axioms.mapIndexed { i, s -> Axiom("$shortName${i + 1}", s.toFormula().normalize()) })
}

fun axiomsByName(desc: String): List<Axiom> =
    desc.trim().split(",")
        .map {
            val s = it.trim()
            (AxiomSystem.entries.singleOrNull { it.shortName.equals(s, ignoreCase = true) || it.name.equals(s, ignoreCase = true) }
                ?: AxiomSystem.entries.single { it.name.startsWith(s, ignoreCase = true) })
                .axioms
        }.flatten().distinct()