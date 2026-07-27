package ru.wertik.orcex.core

internal object CommandCatalog {
    private val symbols = mapOf(
        // Greek, lowercase
        "alpha" to makeSymbol("α"), "beta" to makeSymbol("β"), "gamma" to makeSymbol("γ"),
        "delta" to makeSymbol("δ"), "epsilon" to makeSymbol("ϵ"), "varepsilon" to makeSymbol("ε"),
        "zeta" to makeSymbol("ζ"), "eta" to makeSymbol("η"),
        "theta" to makeSymbol("θ"), "vartheta" to makeSymbol("ϑ"),
        "iota" to makeSymbol("ι"), "kappa" to makeSymbol("κ"), "varkappa" to makeSymbol("ϰ"),
        "lambda" to makeSymbol("λ"), "mu" to makeSymbol("μ"), "nu" to makeSymbol("ν"),
        "xi" to makeSymbol("ξ"), "omicron" to makeSymbol("ο"), "pi" to makeSymbol("π"),
        "varpi" to makeSymbol("ϖ"),
        "rho" to makeSymbol("ρ"), "varrho" to makeSymbol("ϱ"), "sigma" to makeSymbol("σ"),
        "varsigma" to makeSymbol("ς"), "tau" to makeSymbol("τ"), "upsilon" to makeSymbol("υ"),
        "phi" to makeSymbol("φ"), "varphi" to makeSymbol("ϕ"),
        "chi" to makeSymbol("χ"), "psi" to makeSymbol("ψ"), "omega" to makeSymbol("ω"),
        // Greek, uppercase
        "Gamma" to makeSymbol("Γ"), "Delta" to makeSymbol("Δ"),
        "Theta" to makeSymbol("Θ"), "Lambda" to makeSymbol("Λ"), "Xi" to makeSymbol("Ξ"),
        "Pi" to makeSymbol("Π"), "Sigma" to makeSymbol("Σ"), "Upsilon" to makeSymbol("Υ"),
        "Phi" to makeSymbol("Φ"), "Psi" to makeSymbol("Ψ"), "Omega" to makeSymbol("Ω"),
        "times" to makeSymbol("×", SymbolKind.BINARY),
        "cdot" to makeSymbol("·", SymbolKind.BINARY), "pm" to makeSymbol("±", SymbolKind.BINARY),
        "mp" to makeSymbol("∓", SymbolKind.BINARY), "div" to makeSymbol("÷", SymbolKind.BINARY),
        "le" to makeSymbol("≤", SymbolKind.RELATION), "leq" to makeSymbol("≤", SymbolKind.RELATION),
        "ge" to makeSymbol("≥", SymbolKind.RELATION), "geq" to makeSymbol("≥", SymbolKind.RELATION),
        "neq" to makeSymbol("≠", SymbolKind.RELATION), "approx" to makeSymbol("≈", SymbolKind.RELATION),
        "equiv" to makeSymbol("≡", SymbolKind.RELATION), "in" to makeSymbol("∈", SymbolKind.RELATION),
        "notin" to makeSymbol("∉", SymbolKind.RELATION), "subset" to makeSymbol("⊂", SymbolKind.RELATION),
        "subseteq" to makeSymbol("⊆", SymbolKind.RELATION), "supset" to makeSymbol("⊃", SymbolKind.RELATION),
        "supseteq" to makeSymbol("⊇", SymbolKind.RELATION),
        // Relations
        "sim" to makeSymbol("∼", SymbolKind.RELATION), "simeq" to makeSymbol("≃", SymbolKind.RELATION),
        "cong" to makeSymbol("≅", SymbolKind.RELATION), "propto" to makeSymbol("∝", SymbolKind.RELATION),
        "asymp" to makeSymbol("≍", SymbolKind.RELATION), "doteq" to makeSymbol("≐", SymbolKind.RELATION),
        "ne" to makeSymbol("≠", SymbolKind.RELATION),
        "ll" to makeSymbol("≪", SymbolKind.RELATION), "gg" to makeSymbol("≫", SymbolKind.RELATION),
        "leqslant" to makeSymbol("⩽", SymbolKind.RELATION), "geqslant" to makeSymbol("⩾", SymbolKind.RELATION),
        "lesssim" to makeSymbol("≲", SymbolKind.RELATION), "gtrsim" to makeSymbol("≳", SymbolKind.RELATION),
        "prec" to makeSymbol("≺", SymbolKind.RELATION), "succ" to makeSymbol("≻", SymbolKind.RELATION),
        "preceq" to makeSymbol("⪯", SymbolKind.RELATION), "succeq" to makeSymbol("⪰", SymbolKind.RELATION),
        "subsetneq" to makeSymbol("⊊", SymbolKind.RELATION), "supsetneq" to makeSymbol("⊋", SymbolKind.RELATION),
        "nsubseteq" to makeSymbol("⊈", SymbolKind.RELATION), "nsupseteq" to makeSymbol("⊉", SymbolKind.RELATION),
        "sqsubset" to makeSymbol("⊏", SymbolKind.RELATION), "sqsupset" to makeSymbol("⊐", SymbolKind.RELATION),
        "sqsubseteq" to makeSymbol("⊑", SymbolKind.RELATION), "sqsupseteq" to makeSymbol("⊒", SymbolKind.RELATION),
        "ni" to makeSymbol("∋", SymbolKind.RELATION), "models" to makeSymbol("⊨", SymbolKind.RELATION),
        "vdash" to makeSymbol("⊢", SymbolKind.RELATION), "dashv" to makeSymbol("⊣", SymbolKind.RELATION),
        "perp" to makeSymbol("⊥", SymbolKind.RELATION), "parallel" to makeSymbol("∥", SymbolKind.RELATION),
        "nparallel" to makeSymbol("∦", SymbolKind.RELATION), "mid" to makeSymbol("∣", SymbolKind.RELATION),
        "nmid" to makeSymbol("∤", SymbolKind.RELATION), "triangleq" to makeSymbol("≜", SymbolKind.RELATION),
        // Arrows
        "to" to makeSymbol("→", SymbolKind.RELATION), "rightarrow" to makeSymbol("→", SymbolKind.RELATION),
        "leftarrow" to makeSymbol("←", SymbolKind.RELATION), "leftrightarrow" to makeSymbol("↔", SymbolKind.RELATION),
        "gets" to makeSymbol("←", SymbolKind.RELATION),
        "Rightarrow" to makeSymbol("⇒", SymbolKind.RELATION), "Leftarrow" to makeSymbol("⇐", SymbolKind.RELATION),
        "Leftrightarrow" to makeSymbol("⇔", SymbolKind.RELATION),
        "implies" to makeSymbol("⟹", SymbolKind.RELATION), "impliedby" to makeSymbol("⟸", SymbolKind.RELATION),
        "iff" to makeSymbol("⟺", SymbolKind.RELATION), "mapsto" to makeSymbol("↦", SymbolKind.RELATION),
        "longrightarrow" to makeSymbol("⟶", SymbolKind.RELATION),
        "longleftarrow" to makeSymbol("⟵", SymbolKind.RELATION),
        "longleftrightarrow" to makeSymbol("⟷", SymbolKind.RELATION),
        "hookrightarrow" to makeSymbol("↪", SymbolKind.RELATION),
        "hookleftarrow" to makeSymbol("↩", SymbolKind.RELATION),
        "rightharpoonup" to makeSymbol("⇀", SymbolKind.RELATION),
        "rightharpoondown" to makeSymbol("⇁", SymbolKind.RELATION),
        "leftharpoonup" to makeSymbol("↼", SymbolKind.RELATION),
        "leftharpoondown" to makeSymbol("↽", SymbolKind.RELATION),
        "rightleftharpoons" to makeSymbol("⇌", SymbolKind.RELATION),
        "uparrow" to makeSymbol("↑", SymbolKind.RELATION), "downarrow" to makeSymbol("↓", SymbolKind.RELATION),
        "updownarrow" to makeSymbol("↕", SymbolKind.RELATION), "Uparrow" to makeSymbol("⇑", SymbolKind.RELATION),
        "Downarrow" to makeSymbol("⇓", SymbolKind.RELATION), "Updownarrow" to makeSymbol("⇕", SymbolKind.RELATION),
        "nearrow" to makeSymbol("↗", SymbolKind.RELATION), "searrow" to makeSymbol("↘", SymbolKind.RELATION),
        "swarrow" to makeSymbol("↙", SymbolKind.RELATION), "nwarrow" to makeSymbol("↖", SymbolKind.RELATION),
        // Sets, logic and other binary operators
        "cup" to makeSymbol("∪", SymbolKind.BINARY), "cap" to makeSymbol("∩", SymbolKind.BINARY),
        "setminus" to makeSymbol("∖", SymbolKind.BINARY), "oplus" to makeSymbol("⊕", SymbolKind.BINARY),
        "ominus" to makeSymbol("⊖", SymbolKind.BINARY), "otimes" to makeSymbol("⊗", SymbolKind.BINARY),
        "oslash" to makeSymbol("⊘", SymbolKind.BINARY), "odot" to makeSymbol("⊙", SymbolKind.BINARY),
        "wedge" to makeSymbol("∧", SymbolKind.BINARY), "vee" to makeSymbol("∨", SymbolKind.BINARY),
        "land" to makeSymbol("∧", SymbolKind.BINARY), "lor" to makeSymbol("∨", SymbolKind.BINARY),
        "neg" to makeSymbol("¬"), "lnot" to makeSymbol("¬"),
        "star" to makeSymbol("⋆", SymbolKind.BINARY), "ast" to makeSymbol("∗", SymbolKind.BINARY),
        "circ" to makeSymbol("∘", SymbolKind.BINARY), "bullet" to makeSymbol("∙", SymbolKind.BINARY),
        "dagger" to makeSymbol("†", SymbolKind.BINARY), "ddagger" to makeSymbol("‡", SymbolKind.BINARY),
        "amalg" to makeSymbol("⨿", SymbolKind.BINARY), "uplus" to makeSymbol("⊎", SymbolKind.BINARY),
        "sqcup" to makeSymbol("⊔", SymbolKind.BINARY), "sqcap" to makeSymbol("⊓", SymbolKind.BINARY),
        "diamond" to makeSymbol("⋄", SymbolKind.BINARY),
        "triangleleft" to makeSymbol("◁", SymbolKind.BINARY), "triangleright" to makeSymbol("▷", SymbolKind.BINARY),
        "bigtriangleup" to makeSymbol("△", SymbolKind.BINARY), "bigtriangledown" to makeSymbol("▽", SymbolKind.BINARY),
        "ltimes" to makeSymbol("⋉", SymbolKind.BINARY), "rtimes" to makeSymbol("⋊", SymbolKind.BINARY),
        // Symbols
        "infty" to makeSymbol("∞"), "partial" to makeSymbol("∂"), "nabla" to makeSymbol("∇"),
        "cdots" to makeSymbol("⋯"), "ldots" to makeSymbol("…"), "dots" to makeSymbol("…"),
        "vdots" to makeSymbol("⋮"), "ddots" to makeSymbol("⋱"),
        "emptyset" to makeSymbol("∅"), "varnothing" to makeSymbol("∅"),
        "aleph" to makeSymbol("ℵ"), "hbar" to makeSymbol("ℏ"), "ell" to makeSymbol("ℓ"),
        "Re" to makeSymbol("ℜ"), "Im" to makeSymbol("ℑ"), "wp" to makeSymbol("℘"),
        "imath" to makeSymbol("ı"), "jmath" to makeSymbol("ȷ"),
        "prime" to makeSymbol("′"), "angle" to makeSymbol("∠"), "measuredangle" to makeSymbol("∡"),
        "triangle" to makeSymbol("△"), "square" to makeSymbol("□"), "blacksquare" to makeSymbol("■"),
        "surd" to makeSymbol("√"), "top" to makeSymbol("⊤"), "bot" to makeSymbol("⊥"),
        "flat" to makeSymbol("♭"), "sharp" to makeSymbol("♯"), "natural" to makeSymbol("♮"),
        "clubsuit" to makeSymbol("♣"), "diamondsuit" to makeSymbol("♢"),
        "heartsuit" to makeSymbol("♡"), "spadesuit" to makeSymbol("♠"),
        "therefore" to makeSymbol("∴", SymbolKind.RELATION), "because" to makeSymbol("∵", SymbolKind.RELATION),
        "degree" to makeSymbol("°"), "circledR" to makeSymbol("®"), "checkmark" to makeSymbol("✓"),
        "copyright" to makeSymbol("©"), "pounds" to makeSymbol("£"), "S" to makeSymbol("§"),
        "P" to makeSymbol("¶"), "colon" to makeSymbol(":", SymbolKind.PUNCTUATION),
        "forall" to makeSymbol("∀"), "exists" to makeSymbol("∃"), "nexists" to makeSymbol("∄"),
        "sum" to makeSymbol("∑", SymbolKind.LARGE_OPERATOR),
        "prod" to makeSymbol("∏", SymbolKind.LARGE_OPERATOR), "int" to makeSymbol("∫", SymbolKind.LARGE_OPERATOR),
        "iint" to makeSymbol("∬", SymbolKind.LARGE_OPERATOR), "iiint" to makeSymbol("∭", SymbolKind.LARGE_OPERATOR),
        "iiiint" to makeSymbol("⨌", SymbolKind.LARGE_OPERATOR), "oint" to makeSymbol("∮", SymbolKind.LARGE_OPERATOR),
        "oiint" to makeSymbol("∯", SymbolKind.LARGE_OPERATOR),
        "coprod" to makeSymbol("∐", SymbolKind.LARGE_OPERATOR),
        "bigcup" to makeSymbol("⋃", SymbolKind.LARGE_OPERATOR), "bigcap" to makeSymbol("⋂", SymbolKind.LARGE_OPERATOR),
        "bigoplus" to makeSymbol("⨁", SymbolKind.LARGE_OPERATOR), "bigotimes" to makeSymbol("⨂", SymbolKind.LARGE_OPERATOR),
        "bigodot" to makeSymbol("⨀", SymbolKind.LARGE_OPERATOR), "biguplus" to makeSymbol("⨄", SymbolKind.LARGE_OPERATOR),
        "bigvee" to makeSymbol("⋁", SymbolKind.LARGE_OPERATOR), "bigwedge" to makeSymbol("⋀", SymbolKind.LARGE_OPERATOR),
        "bigsqcup" to makeSymbol("⨆", SymbolKind.LARGE_OPERATOR),
        "lim" to makeSymbol("lim", SymbolKind.OPERATOR),
        "sin" to makeSymbol("sin", SymbolKind.OPERATOR), "cos" to makeSymbol("cos", SymbolKind.OPERATOR),
        "tan" to makeSymbol("tan", SymbolKind.OPERATOR), "cot" to makeSymbol("cot", SymbolKind.OPERATOR),
        "sec" to makeSymbol("sec", SymbolKind.OPERATOR), "csc" to makeSymbol("csc", SymbolKind.OPERATOR),
        "arcsin" to makeSymbol("arcsin", SymbolKind.OPERATOR), "arccos" to makeSymbol("arccos", SymbolKind.OPERATOR),
        "arctan" to makeSymbol("arctan", SymbolKind.OPERATOR), "sinh" to makeSymbol("sinh", SymbolKind.OPERATOR),
        "cosh" to makeSymbol("cosh", SymbolKind.OPERATOR), "tanh" to makeSymbol("tanh", SymbolKind.OPERATOR),
        "arcsinh" to makeSymbol("arcsinh", SymbolKind.OPERATOR), "arccosh" to makeSymbol("arccosh", SymbolKind.OPERATOR),
        "arctanh" to makeSymbol("arctanh", SymbolKind.OPERATOR), "sech" to makeSymbol("sech", SymbolKind.OPERATOR),
        "csch" to makeSymbol("csch", SymbolKind.OPERATOR),
        "log" to makeSymbol("log", SymbolKind.OPERATOR), "ln" to makeSymbol("ln", SymbolKind.OPERATOR),
        "exp" to makeSymbol("exp", SymbolKind.OPERATOR), "det" to makeSymbol("det", SymbolKind.OPERATOR),
        "gcd" to makeSymbol("gcd", SymbolKind.OPERATOR), "min" to makeSymbol("min", SymbolKind.OPERATOR),
        "max" to makeSymbol("max", SymbolKind.OPERATOR), "sup" to makeSymbol("sup", SymbolKind.OPERATOR),
        "inf" to makeSymbol("inf", SymbolKind.OPERATOR),
        "arg" to makeSymbol("arg", SymbolKind.OPERATOR), "deg" to makeSymbol("deg", SymbolKind.OPERATOR),
        "dim" to makeSymbol("dim", SymbolKind.OPERATOR), "hom" to makeSymbol("hom", SymbolKind.OPERATOR),
        "ker" to makeSymbol("ker", SymbolKind.OPERATOR), "lg" to makeSymbol("lg", SymbolKind.OPERATOR),
        "Pr" to makeSymbol("Pr", SymbolKind.OPERATOR), "coth" to makeSymbol("coth", SymbolKind.OPERATOR),
        "liminf" to makeSymbol("lim inf", SymbolKind.OPERATOR), "limsup" to makeSymbol("lim sup", SymbolKind.OPERATOR),
        "argmin" to makeSymbol("arg min", SymbolKind.OPERATOR), "argmax" to makeSymbol("arg max", SymbolKind.OPERATOR),
        "bmod" to makeSymbol("mod", SymbolKind.BINARY), "mod" to makeSymbol("mod", SymbolKind.BINARY),
        "{" to makeSymbol("{", SymbolKind.OPEN),
        "}" to makeSymbol("}", SymbolKind.CLOSE), "|" to makeSymbol("|", SymbolKind.RELATION),
        "_" to makeSymbol("_"), "%" to makeSymbol("%"), "$" to makeSymbol("$"),
        "#" to makeSymbol("#"), "&" to makeSymbol("&"),
        "backslash" to makeSymbol("\\"),
    )

    fun symbol(command: String): MathNode.Symbol? = symbols[command]

    fun space(command: String): MathNode.Space? = when (command) {
        ",", "thinspace" -> MathNode.Space(0.17f)
        ":", "medspace" -> MathNode.Space(0.22f)
        ";", "thickspace" -> MathNode.Space(0.28f)
        "!", "negthinspace" -> MathNode.Space(-0.17f)
        "negmedspace" -> MathNode.Space(-0.22f)
        "negthickspace" -> MathNode.Space(-0.28f)
        "enspace" -> MathNode.Space(0.5f)
        " ", "nobreakspace", "~" -> MathNode.Space(0.33f)
        "quad" -> MathNode.Space(1f)
        "qquad" -> MathNode.Space(2f)
        else -> null
    }

    fun delimiter(command: String): String? = when (command) {
        "langle" -> "⟨"
        "rangle" -> "⟩"
        "lbrace" -> "{"
        "rbrace" -> "}"
        "lbrack" -> "["
        "rbrack" -> "]"
        "lfloor" -> "⌊"
        "rfloor" -> "⌋"
        "lceil" -> "⌈"
        "rceil" -> "⌉"
        "lgroup" -> "⟮"
        "rgroup" -> "⟯"
        "llbracket" -> "⟦"
        "rrbracket" -> "⟧"
        "vert", "|", "lvert", "rvert" -> "|"
        "Vert", "lVert", "rVert", "|".repeat(2) -> "‖"
        else -> symbol(command)?.value
    }

    /** Delimiter size prefixes. Sizing is left to the layout, which scales to content. */
    fun isDelimiterSize(command: String): Boolean =
        command in setOf("big", "Big", "bigg", "Bigg", "bigl", "Bigl", "biggl", "Biggl", "bigr", "Bigr", "biggr", "Biggr", "bigm", "Bigm", "biggm", "Biggm")

    fun accent(command: String): AccentType? = when (command) {
        "hat", "widehat" -> AccentType.HAT
        "bar", "overline" -> AccentType.BAR
        "underline" -> AccentType.UNDERLINE
        "vec", "overrightarrow" -> AccentType.VEC
        "overleftarrow" -> AccentType.LEFT_VEC
        "dot" -> AccentType.DOT
        "ddot" -> AccentType.DOUBLE_DOT
        "dddot" -> AccentType.TRIPLE_DOT
        "tilde", "widetilde" -> AccentType.TILDE
        "acute" -> AccentType.ACUTE
        "grave" -> AccentType.GRAVE
        "breve" -> AccentType.BREVE
        "check" -> AccentType.CHECK
        "mathring" -> AccentType.RING
        else -> null
    }

    fun style(command: String): TextStyle? = when (command) {
        "mathrm", "textrm", "mathnormal" -> TextStyle.ROMAN
        "mathbf", "textbf" -> TextStyle.BOLD
        "mathit", "textit", "emph" -> TextStyle.ITALIC
        "boldsymbol", "bm", "pmb" -> TextStyle.BOLD_ITALIC
        "mathcal", "mathscr" -> TextStyle.CALLIGRAPHIC
        "mathbb" -> TextStyle.BLACKBOARD
        "mathsf", "textsf" -> TextStyle.SANS_SERIF
        "mathtt", "texttt" -> TextStyle.MONOSPACE
        "mathfrak" -> TextStyle.FRAKTUR
        else -> null
    }

    /** Style commands that set upright text rather than styled maths. */
    fun isTextStyleCommand(command: String): Boolean =
        command in setOf("textrm", "textbf", "textit", "texttt", "textsf", "emph")

    fun environment(name: String): MatrixEnvironment? = when (name) {
        "aligned", "align", "align*", "alignat", "alignat*", "gathered", "gather", "gather*",
        "split", "multline", "multline*", "equation", "equation*", "eqnarray", "eqnarray*",
            -> MatrixEnvironment.ALIGNED
        "matrix", "matrix*", "array" -> MatrixEnvironment.MATRIX
        "pmatrix", "pmatrix*" -> MatrixEnvironment.PMATRIX
        "bmatrix", "bmatrix*" -> MatrixEnvironment.BMATRIX
        "vmatrix", "vmatrix*" -> MatrixEnvironment.VMATRIX
        "Bmatrix", "Bmatrix*" -> MatrixEnvironment.BRACE_MATRIX
        "Vmatrix", "Vmatrix*" -> MatrixEnvironment.NORM_MATRIX
        "smallmatrix" -> MatrixEnvironment.SMALL_MATRIX
        "cases", "dcases" -> MatrixEnvironment.CASES
        else -> null
    }

    /** Environments that carry a column specification argument, e.g. `\begin{array}{cc}`. */
    fun hasColumnSpecification(name: String): Boolean = name == "array"

    /**
     * Negated forms used by `\not`. Anything not listed here gets a combining overlay,
     * which every font can compose.
     */
    private val negations = mapOf(
        "=" to "≠", "<" to "≮", ">" to "≯", "≤" to "≰", "≥" to "≱",
        "∈" to "∉", "∋" to "∌", "⊂" to "⊄", "⊃" to "⊅", "⊆" to "⊈", "⊇" to "⊉",
        "≡" to "≢", "∼" to "≁", "≃" to "≄", "≅" to "≇", "≈" to "≉",
        "∣" to "∤", "∥" to "∦", "∃" to "∄", "≺" to "⊀", "≻" to "⊁",
        "→" to "↛", "⇒" to "⇏", "⇔" to "⇎",
    )

    fun negated(value: String): String = negations[value] ?: (value + "\u0338")

    private fun makeSymbol(value: String, kind: SymbolKind = SymbolKind.ORDINARY): MathNode.Symbol =
        MathNode.Symbol(value, kind)
}
