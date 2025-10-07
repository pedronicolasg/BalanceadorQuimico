data class Composto(val formula: String) {
    val elementos: MutableMap<String, Int> = mutableMapOf()

    init {
        parseFormula(formula.normalizarSubscritos())
    }

    private fun String.normalizarSubscritos(): String =
        replace("₀", "0").replace("₁", "1").replace("₂", "2")
            .replace("₃", "3").replace("₄", "4").replace("₅", "5")
            .replace("₆", "6").replace("₇", "7").replace("₈", "8")
            .replace("₉", "9")

    private fun parseFormula(formula: String) {
        parseGrupo(formula, 1)
    }

    private fun parseGrupo(formula: String, multiplicadorExterno: Int) {
        var i = 0

        while (i < formula.length) {
            when {
                formula[i] == '(' -> {
                    val (grupoInterno, novoIndice) = extrairGrupo(formula, i)
                    val multiplicadorGrupo = extrairNumero(formula, novoIndice)

                    parseGrupo(grupoInterno, multiplicadorExterno * multiplicadorGrupo.first)
                    i = multiplicadorGrupo.second
                }

                formula[i].isUpperCase() -> {
                    val (elemento, indiceElemento) = extrairElemento(formula, i)
                    val (quantidade, novoIndice) = extrairNumero(formula, indiceElemento)

                    val quantidadeTotal = quantidade * multiplicadorExterno
                    elementos.merge(elemento, quantidadeTotal, Int::plus)
                    i = novoIndice
                }

                else -> i++
            }
        }
    }

    private fun extrairGrupo(formula: String, inicio: Int): Pair<String, Int> {
        var i = inicio + 1
        var contador = 1

        while (i < formula.length && contador > 0) {
            when (formula[i]) {
                '(' -> contador++
                ')' -> contador--
            }
            i++
        }

        require(contador == 0) { "Parênteses não balanceados em: $formula" }

        return formula.substring(inicio + 1, i - 1) to i
    }

    private fun extrairElemento(formula: String, inicio: Int): Pair<String, Int> {
        val elemento = buildString {
            append(formula[inicio])
            var i = inicio + 1
            while (i < formula.length && formula[i].isLowerCase()) {
                append(formula[i])
                i++
            }
        }
        return elemento to (inicio + elemento.length)
    }

    private fun extrairNumero(formula: String, inicio: Int): Pair<Int, Int> {
        val numero = buildString {
            var i = inicio
            while (i < formula.length && formula[i].isDigit()) {
                append(formula[i])
                i++
            }
        }
        return (numero.toIntOrNull() ?: 1) to (inicio + numero.length)
    }
}

fun main() {
    println("=== BALANCEADOR DE EQUAÇÕES QUÍMICAS ===")
    println("Por: https://github.com/pedronicolasg")
    print("\nDigite a equação: ")

    val equacao = readln()

    try {
        balancearEquacao(equacao)
    } catch (e: Exception) {
        println("Erro: ${e.message}")
        println("Verifique se a equação foi digitada corretamente.")
        e.printStackTrace()
    }
}

fun balancearEquacao(equacao: String) {
    val (reagentesStr, produtosStr) = equacao.split("->").also { partes ->
        require(partes.size == 2) { "Formato inválido. Use: reagentes -> produtos" }
    }

    val reagentes = reagentesStr.trim().split("+").map { it.trim() }
    val produtos = produtosStr.trim().split("+").map { it.trim() }

    val todosCompostos = (reagentes + produtos).map { Composto(it) }
    val nomesCompostos = reagentes + produtos

    println("\n=== ANÁLISE DOS COMPOSTOS ===")
    reagentes.forEachIndexed { index, nome ->
        println("Reagente $nome: ${todosCompostos[index].elementos}")
    }
    produtos.forEachIndexed { index, nome ->
        println("Produto $nome: ${todosCompostos[reagentes.size + index].elementos}")
    }

    val todosElementos = todosCompostos.flatMap { it.elementos.keys }.distinct()

    println("\nElementos encontrados: $todosElementos")
    println("Número de compostos: ${todosCompostos.size}")
    println("Número de elementos: ${todosElementos.size}")

    val matriz = construirMatriz(todosElementos, todosCompostos, reagentes.size)

    val coeficientes = encontrarCoeficientes(matriz, todosCompostos.size)

    if (coeficientes != null) {
        imprimirResultado(reagentes, produtos, coeficientes, nomesCompostos)
    } else {
        println("Não foi possível balancear a equação automaticamente.")
        println("A equação pode estar incorreta ou ser muito complexa.")
        println("Tente com coeficientes menores ou verifique a fórmula.")
    }
}

fun construirMatriz(
    elementos: List<String>,
    compostos: List<Composto>,
    numReagentes: Int
): Array<DoubleArray> {
    println("\n=== MATRIZ DE BALANCEAMENTO ===")

    return Array(elementos.size) { i ->
        val elemento = elementos[i]
        print("$elemento: ")

        DoubleArray(compostos.size + 1) { j ->
            when {
                j == compostos.size -> {
                    println("= 0")
                    0.0
                }
                else -> {
                    val coef = compostos[j].elementos[elemento] ?: 0
                    val coefAjustado = if (j >= numReagentes) -coef else coef
                    print("$coefAjustado ")
                    coefAjustado.toDouble()
                }
            }
        }
    }
}

fun encontrarCoeficientes(matriz: Array<DoubleArray>, numCompostos: Int): IntArray? {
    for (maxTentativa in 1..20) {
        val coef = IntArray(numCompostos)
        if (gerarCombinacao(coef, 0, matriz, maxTentativa)) {
            return simplificarCoeficientes(coef)
        }
    }
    return null
}

fun gerarCombinacao(
    coef: IntArray,
    pos: Int,
    matriz: Array<DoubleArray>,
    maxCoef: Int
): Boolean {
    if (pos == coef.size) {
        return verificarBalanceamento(matriz, coef)
    }

    for (i in 1..maxCoef) {
        coef[pos] = i
        if (gerarCombinacao(coef, pos + 1, matriz, maxCoef)) {
            return true
        }
    }
    return false
}

fun verificarBalanceamento(matriz: Array<DoubleArray>, coef: IntArray): Boolean =
    matriz.all { linha ->
        val soma = linha.indices.sumOf { j ->
            if (j < coef.size) linha[j] * coef[j] else 0.0
        }
        kotlin.math.abs(soma) < 1e-9
    }

fun simplificarCoeficientes(coef: IntArray): IntArray {
    val mdc = coef.reduce(::mdc)
    return if (mdc > 1) {
        coef.map { it / mdc }.toIntArray()
    } else {
        coef
    }
}

tailrec fun mdc(a: Int, b: Int): Int =
    if (b == 0) a else mdc(b, a % b)

fun imprimirResultado(
    reagentes: List<String>,
    produtos: List<String>,
    coeficientes: IntArray,
    nomesCompostos: List<String>
) {
    println("\n=== EQUAÇÃO BALANCEADA ===")

    val resultado = buildString {
        reagentes.forEachIndexed { i, reagente ->
            if (i > 0) append(" + ")
            if (coeficientes[i] > 1) append(coeficientes[i])
            append(reagente)
        }

        append(" → ")

        produtos.forEachIndexed { i, produto ->
            if (i > 0) append(" + ")
            val coefIndex = reagentes.size + i
            if (coeficientes[coefIndex] > 1) append(coeficientes[coefIndex])
            append(produto)
        }
    }

    println(resultado)

    println("\n=== COEFICIENTES ENCONTRADOS ===")
    nomesCompostos.forEachIndexed { i, nome ->
        println("%-15s: %d".format(nome, coeficientes[i]))
    }

    println("\n✓ Equação balanceada com sucesso!")
}