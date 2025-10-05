fun main() {
    println("\n***** Seja bem-vindo ao Restaurante Marselha! *****\n")
    println("-> A nossa culinária é marcada por sabores do Mediterrâneo.")
    var opcao: Int
    do {
        exibirMenuPrincipal()
        opcao = readln().toIntOrNull() ?: -1 // Lida com entrada inválida

        when (opcao) {
            1 -> uiCadastrarItem()
            2 -> uiAtualizarItem()
            3 -> uiCriarPedido()
            4 -> uiAtualizarPedido()
            5 -> uiConsultarPedidos()
            0 -> println("\nObrigada por visitar o nosso Restaurante, estamos sempre à disposição para te receber!")
            else -> println("Você inseriu uma opção inválida! Por favor, tente novamente.")
        }
    } while (opcao != 0)
}

// --- FUNÇÕES DE INTERFACE (UI) ---

fun exibirMenuPrincipal() {
    println("\n***** MENU *****\n")
    println("1. Cadastrar Item")
    println("2. Atualizar Item")
    println("3. Criar Pedido")
    println("4. Atualizar Pedido")
    println("5. Consultar Pedidos")
    println("0. Sair\n")
    print("Digite a opção desejada: ")
}

fun uiCadastrarItem() {
    println("\n-> CADASTRAR ITEM\n")
    print("Insira o nome do item: ")
    val nome = readln()
    print("Insira a descrição do item: ")
    val descricao = readln()
    print("Insira o preço do item: ")
    val preco = readln().replace(',', '.').toDoubleOrNull()
    print("Insira a quantidade em estoque do item: ")
    val estoque = readln().toIntOrNull()

    if (preco == null || estoque == null) {
        println("\nPreço ou estoque inválido. O item não foi cadastrado.")
        return
    }

    cadastrarItem(nome, descricao, preco, estoque)
    println("\nNovo item cadastrado com sucesso!")
}

fun uiAtualizarItem() {
    println("\n-> ATUALIZAR ITEM\n")
    if (itensCadastrados.isEmpty()) {
        println("Não encontramos nenhum item cadastrado no momento!")
        return
    }

    itensCadastrados.forEach { println("${it.codigo} - ${it.nome}") }
    print("\nDigite o código do item que deseja atualizar: ")
    val codigo = readln().toIntOrNull()

    if (codigo == null) {
        println("Código inválido.")
        return
    }

    print("Digite o novo nome (ou ENTER para manter): ")
    val novoNome = readln()
    print("Digite a nova descrição (ou ENTER para manter): ")
    val novaDescricao = readln()
    print("Digite o novo preço (ou ENTER para manter): ")
    val novoPrecoStr = readln()
    print("Digite o novo estoque (ou ENTER para manter): ")
    val novoEstoqueStr = readln()

    val novoPreco = novoPrecoStr.replace(',', '.').toDoubleOrNull()
    val novoEstoque = novoEstoqueStr.toIntOrNull()

    val sucesso = atualizarItem(codigo, novoNome, novaDescricao, novoPreco, novoEstoque)

    if (sucesso) {
        println("\nO item foi atualizado com sucesso!")
    } else {
        println("O item com código $codigo não foi encontrado.")
    }
}

fun uiCriarPedido() {
    println("\n-> CRIAR PEDIDO\n")
    if (itensCadastrados.isEmpty()) {
        println("Não existem itens cadastrados para criar um pedido!")
        return
    }

    val itensDoPedido = mutableMapOf<Int, Int>() // Mapa de [CodigoItem, Quantidade]

    while (true) {
        println("\nItens disponíveis:")
        itensCadastrados.forEach { println("${it.codigo} - ${it.nome} | R$ ${it.preco} | Estoque: ${it.estoque}") }
        print("\nDigite o código do item (ou 0 para finalizar): ")
        val codigoItem = readln().toIntOrNull() ?: -1
        if (codigoItem == 0) break

        val itemEscolhido = encontrarItemPorCodigo(codigoItem)
        if (itemEscolhido != null) {
            print("Quantas unidades de ${itemEscolhido.nome} deseja? ")
            val quantidade = readln().toIntOrNull()
            if (quantidade != null && quantidade > 0) {
                val quantidadeAcumulada = (itensDoPedido[codigoItem] ?: 0) + quantidade
                if (quantidadeAcumulada > itemEscolhido.estoque) {
                    println("Quantidade solicitada ($quantidadeAcumulada) maior que o estoque disponível (${itemEscolhido.estoque}).")
                } else {
                    itensDoPedido[codigoItem] = quantidadeAcumulada
                    println("Adicionado: ${quantidade}x ${itemEscolhido.nome}. Total no pedido: ${quantidadeAcumulada}")
                }
            } else {
                println("Quantidade inválida.")
            }
        } else {
            println("Item inválido!")
        }
    }

    if (itensDoPedido.isNotEmpty()) {
        print("\nDeseja aplicar cupom de desconto de 10%? (S/N): ")
        val aplicarCupom = readln().equals("S", ignoreCase = true)

        val pedidoCriado = criarPedido(itensDoPedido, aplicarCupom)

        if (pedidoCriado != null) {
            println("\nSeu pedido foi criado com sucesso! Código: ${pedidoCriado.codigo}")
            println("Total: R$ ${"%.2f".format(pedidoCriado.total)}")
        } else {
            println("\nNão foi possível criar o pedido. Verifique o estoque dos itens selecionados.")
        }
    } else {
        println("O pedido deve ter pelo menos 1 item!")
    }
}


fun uiAtualizarPedido() {
    println("\n-> ATUALIZAR PEDIDO\n")
    if (pedidosRealizados.isEmpty()) {
        println("Não encontramos nenhum pedido realizado!")
        return
    }

    pedidosRealizados.forEach { println("Código: ${it.codigo} | Status: ${it.status}") }
    print("\nDigite o código do pedido que deseja atualizar: ")
    val codigo = readln().toIntOrNull()

    if (codigo == null) {
        println("Código inválido.")
        return
    }

    val pedidoEncontrado = encontrarPedidoPorCodigo(codigo)

    if (pedidoEncontrado != null) {
        println("\nEscolha o novo status:")
        StatusPedido.values().forEachIndexed { i, status -> println("${i + 1} - $status") }
        print("Digite o número do novo status: ")
        val opcaoStatus = readln().toIntOrNull()

        if (opcaoStatus != null && opcaoStatus in 1..StatusPedido.values().size) {
            val novoStatus = StatusPedido.values()[opcaoStatus - 1]
            atualizarStatusPedido(pedidoEncontrado, novoStatus)
            println("Status atualizado com sucesso! Novo status: ${pedidoEncontrado.status}")
        } else {
            println("Opção inválida.")
        }
    } else {
        println("Pedido com código $codigo não encontrado.")
    }
}

fun uiConsultarPedidos() {
    println("\n-> CONSULTAR PEDIDOS\n")
    if (pedidosRealizados.isEmpty()) {
        println("Nenhum pedido realizado até o momento!")
        return
    }

    println("Deseja filtrar por status?")
    println("1 - Todos os pedidos")
    StatusPedido.values().forEachIndexed { i, status -> println("${i + 2} - $status") }
    print("\nDigite a opção desejada: ")
    val opcaoFiltro = readln().toIntOrNull() ?: -1

    val statusFiltrado = when (opcaoFiltro) {
        1 -> null
        in 2..StatusPedido.values().size + 1 -> StatusPedido.values()[opcaoFiltro - 2]
        else -> {
            println("Opção inválida! Mostrando todos os pedidos.")
            null
        }
    }

    val pedidosFiltrados = consultarPedidosPorStatus(statusFiltrado)

    if (pedidosFiltrados.isEmpty()) {
        println("Não encontramos nenhum pedido para este filtro.")
    } else {
        println("\nPedidos encontrados:")
        pedidosFiltrados.forEach { pedido ->
            println("\n---")
            println("Código: ${pedido.codigo} | Status: ${pedido.status}")
            println("Itens:")
            pedido.itens.forEach { item -> println("- ${item.estoque}x ${item.nome}") }
            println("Total: R$ ${"%.2f".format(pedido.total)}")
            println("---")
        }
    }
 }