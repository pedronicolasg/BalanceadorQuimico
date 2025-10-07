# ⚗️ Balanceador Químico

> Balanceador automático de equações químicas escrito em Kotlin

## 🎯 O que faz?

Analisa e balanceia equações químicas automaticamente, encontrando os coeficientes estequiométricos corretos para você.

## ✨ Características

- 🔢 Balanceamento automático de equações
- 🧪 Suporte para compostos complexos com parênteses
- ⚡ Algoritmo eficiente de tentativa e erro
- 📊 Análise detalhada dos elementos e coeficientes
- 🎨 Interface de terminal amigável

## 🚀 Como usar

```bash
# Clone o repositório
git clone https://github.com/pedronicolasg/balanceadorquimico.git

# Compile e execute
kotlinc main.kt -include-runtime -d balanceadorquimico.jar
java -jar balanceadorquimico.jar
```

## 💡 Exemplo

```
=== BALANCEADOR DE EQUAÇÕES QUÍMICAS ===

Digite a equação: H2 + O2 -> H2O

=== EQUAÇÃO BALANCEADA ===
2H2 + O2 → 2H2O

✓ Equação balanceada com sucesso!
```

## 📝 Formato de entrada

```
Reagentes -> Produtos
```

Exemplos válidos:
- `H2 + O2 -> H2O`
- `Fe + O2 -> Fe2O3`
- `Ca(OH)2 + HCl -> CaCl2 + H2O`

## 🛠️ Requisitos

- Kotlin 1.5+
- JVM 11+

## 📖 Como funciona

1. **Parse** - Analisa a fórmula química e extrai os elementos
2. **Matriz** - Monta uma matriz de coeficientes estequiométricos
3. **Balanceamento** - Testa combinações de coeficientes até encontrar o equilíbrio
4. **Simplificação** - Reduz os coeficientes ao MDC

## 📄 Licença

MIT License - sinta-se livre para usar e modificar!