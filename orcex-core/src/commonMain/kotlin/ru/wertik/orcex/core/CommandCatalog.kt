package ru.wertik.orcex.core

internal object CommandCatalog {
    private val symbols = mapOf(
        "alpha" to makeSymbol("α"), "beta" to makeSymbol("β"), "gamma" to makeSymbol("γ"),
        "delta" to makeSymbol("δ"), "epsilon" to makeSymbol("ϵ"), "varepsilon" to makeSymbol("ε"),
        "theta" to makeSymbol("θ"), "vartheta" to makeSymbol("ϑ"),
        "lambda" to makeSymbol("λ"), "mu" to makeSymbol("μ"), "pi" to makeSymbol("π"),
        "rho" to makeSymbol("ρ"), "varrho" to makeSymbol("ϱ"), "sigma" to makeSymbol("σ"),
        "varsigma" to makeSymbol("ς"), "phi" to makeSymbol("φ"), "varphi" to makeSymbol("ϕ"),
        "omega" to makeSymbol("ω"), "Gamma" to makeSymbol("Γ"), "Delta" to makeSymbol("Δ"),
        "Theta" to makeSymbol("Θ"), "Lambda" to makeSymbol("Λ"), "Sigma" to makeSymbol("Σ"),
        "Phi" to makeSymbol("Φ"), "Omega" to makeSymbol("Ω"), "times" to makeSymbol("×", SymbolKind.BINARY),
        "cdot" to makeSymbol("·", SymbolKind.BINARY), "pm" to makeSymbol("±", SymbolKind.BINARY),
        "mp" to makeSymbol("∓", SymbolKind.BINARY), "div" to makeSymbol("÷", SymbolKind.BINARY),
        "le" to makeSymbol("≤", SymbolKind.RELATION), "leq" to makeSymbol("≤", SymbolKind.RELATION),
        "ge" to makeSymbol("≥", SymbolKind.RELATION), "geq" to makeSymbol("≥", SymbolKind.RELATION),
        "neq" to makeSymbol("≠", SymbolKind.RELATION), "approx" to makeSymbol("≈", SymbolKind.RELATION),
        "equiv" to makeSymbol("≡", SymbolKind.RELATION), "in" to makeSymbol("∈", SymbolKind.RELATION),
        "notin" to makeSymbol("∉", SymbolKind.RELATION), "subset" to makeSymbol("⊂", SymbolKind.RELATION),
        "subseteq" to makeSymbol("⊆", SymbolKind.RELATION), "supset" to makeSymbol("⊃", SymbolKind.RELATION),
        "supseteq" to makeSymbol("⊇", SymbolKind.RELATION),
        "to" to makeSymbol("→", SymbolKind.RELATION), "rightarrow" to makeSymbol("→", SymbolKind.RELATION),
        "leftarrow" to makeSymbol("←", SymbolKind.RELATION), "leftrightarrow" to makeSymbol("↔", SymbolKind.RELATION),
        "infty" to makeSymbol("∞"), "partial" to makeSymbol("∂"), "nabla" to makeSymbol("∇"),
        "cdots" to makeSymbol("⋯"), "ldots" to makeSymbol("…"), "dots" to makeSymbol("…"),
        "forall" to makeSymbol("∀"), "exists" to makeSymbol("∃"), "sum" to makeSymbol("∑", SymbolKind.LARGE_OPERATOR),
        "prod" to makeSymbol("∏", SymbolKind.LARGE_OPERATOR), "int" to makeSymbol("∫", SymbolKind.LARGE_OPERATOR),
        "iint" to makeSymbol("∬", SymbolKind.LARGE_OPERATOR), "iiint" to makeSymbol("∭", SymbolKind.LARGE_OPERATOR),
        "iiiint" to makeSymbol("⨌", SymbolKind.LARGE_OPERATOR), "oint" to makeSymbol("∮", SymbolKind.LARGE_OPERATOR),
        "lim" to makeSymbol("lim", SymbolKind.OPERATOR),
        "sin" to makeSymbol("sin", SymbolKind.OPERATOR), "cos" to makeSymbol("cos", SymbolKind.OPERATOR),
        "tan" to makeSymbol("tan", SymbolKind.OPERATOR), "cot" to makeSymbol("cot", SymbolKind.OPERATOR),
        "sec" to makeSymbol("sec", SymbolKind.OPERATOR), "csc" to makeSymbol("csc", SymbolKind.OPERATOR),
        "arcsin" to makeSymbol("arcsin", SymbolKind.OPERATOR), "arccos" to makeSymbol("arccos", SymbolKind.OPERATOR),
        "arctan" to makeSymbol("arctan", SymbolKind.OPERATOR), "sinh" to makeSymbol("sinh", SymbolKind.OPERATOR),
        "cosh" to makeSymbol("cosh", SymbolKind.OPERATOR), "tanh" to makeSymbol("tanh", SymbolKind.OPERATOR),
        "log" to makeSymbol("log", SymbolKind.OPERATOR), "ln" to makeSymbol("ln", SymbolKind.OPERATOR),
        "exp" to makeSymbol("exp", SymbolKind.OPERATOR), "det" to makeSymbol("det", SymbolKind.OPERATOR),
        "gcd" to makeSymbol("gcd", SymbolKind.OPERATOR), "min" to makeSymbol("min", SymbolKind.OPERATOR),
        "max" to makeSymbol("max", SymbolKind.OPERATOR), "sup" to makeSymbol("sup", SymbolKind.OPERATOR),
        "inf" to makeSymbol("inf", SymbolKind.OPERATOR), "{" to makeSymbol("{", SymbolKind.OPEN),
        "}" to makeSymbol("}", SymbolKind.CLOSE), "|" to makeSymbol("|", SymbolKind.RELATION),
        "_" to makeSymbol("_"), "%" to makeSymbol("%"), "$" to makeSymbol("$"),
        "#" to makeSymbol("#"), "&" to makeSymbol("&"),
    )

    fun symbol(command: String): MathNode.Symbol? = symbols[command]

    fun space(command: String): MathNode.Space? = when (command) {
        "," -> MathNode.Space(0.17f)
        ":" -> MathNode.Space(0.22f)
        ";" -> MathNode.Space(0.28f)
        "!" -> MathNode.Space(-0.17f)
        "quad" -> MathNode.Space(1f)
        "qquad" -> MathNode.Space(2f)
        else -> null
    }

    fun delimiter(command: String): String? = when (command) {
        "langle" -> "⟨"
        "rangle" -> "⟩"
        "lbrace" -> "{"
        "rbrace" -> "}"
        "lfloor" -> "⌊"
        "rfloor" -> "⌋"
        "lceil" -> "⌈"
        "rceil" -> "⌉"
        "vert", "|" -> "|"
        else -> symbol(command)?.value
    }

    fun accent(command: String): AccentType? = when (command) {
        "hat" -> AccentType.HAT
        "bar" -> AccentType.BAR
        "vec" -> AccentType.VEC
        "dot" -> AccentType.DOT
        "tilde" -> AccentType.TILDE
        else -> null
    }

    fun style(command: String): TextStyle? = when (command) {
        "mathrm" -> TextStyle.ROMAN
        "mathbf" -> TextStyle.BOLD
        "mathit" -> TextStyle.ITALIC
        "mathcal" -> TextStyle.CALLIGRAPHIC
        "mathbb" -> TextStyle.BLACKBOARD
        else -> null
    }

    fun environment(name: String): MatrixEnvironment? = when (name) {
        "aligned", "align", "align*", "gathered" -> MatrixEnvironment.ALIGNED
        "matrix" -> MatrixEnvironment.MATRIX
        "pmatrix" -> MatrixEnvironment.PMATRIX
        "bmatrix" -> MatrixEnvironment.BMATRIX
        "vmatrix" -> MatrixEnvironment.VMATRIX
        "cases" -> MatrixEnvironment.CASES
        else -> null
    }

    private fun makeSymbol(value: String, kind: SymbolKind = SymbolKind.ORDINARY): MathNode.Symbol =
        MathNode.Symbol(value, kind)
}
