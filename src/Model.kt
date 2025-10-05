// --- ESTRUTURA DOS DADOS ---
enum class StatusPedido {
    ACEITO,
    FAZENDO,
    FEITO,
    ESPERANDO_ENTREGADOR,
    SAIU_PARA_ENTREGA,
    ENTREGUE
}

data class Item(
    val codigo: Int,
    var nome: String,
    var descricao: String,
    var preco: Double,
    var estoque: Int
)

data class Pedido(
    val codigo: Int,
    val itens: List<Item>, // Itens do pedido (cópia)
    var total: Double,
    var cupom: Boolean,
    var status: StatusPedido = StatusPedido.ACEITO
)

// --- ESTADO DA APLICAÇÃO ---
val itensCadastrados = mutableListOf<Item>()
val pedidosRealizados = mutableListOf<Pedido>()
var contadorItem = 1
var contadorPedido = 1

// --- FUNÇÕES DE LÓGICA DE NEGÓCIO ---

fun cadastrarItem(nome: String, descricao: String, preco: Double, estoque: Int) {
    val novoItem = Item(
        codigo = contadorItem++,
        nome = nome,
        descricao = descricao,
        preco = preco,
        estoque = estoque
    )
    itensCadastrados.add(novoItem)
}

fun atualizarItem(codigo: Int, novoNome: String?, novaDescricao: String?, novoPreco: Double?, novoEstoque: Int?): Boolean {
    val itemParaAtualizar = itensCadastrados.find { it.codigo == codigo }

    return if (itemParaAtualizar != null) {
        if (!novoNome.isNullOrBlank()) {
            itemParaAtualizar.nome = novoNome
        }
        if (!novaDescricao.isNullOrBlank()) {
            itemParaAtualizar.descricao = novaDescricao
        }
        if (novoPreco != null && novoPreco >= 0) {
            itemParaAtualizar.preco = novoPreco
        }
        if (novoEstoque != null && novoEstoque >= 0) {
            itemParaAtualizar.estoque = novoEstoque
        }
        true
    } else {
        false // Item não encontrado
    }
}

fun criarPedido(itensDoPedido: Map<Int, Int>, aplicarCupom: Boolean): Pedido? {
    val itensVerificados = mutableListOf<Item>()
    var totalPedido = 0.0

    // Validação de estoque
    for ((codigoItem, quantidade) in itensDoPedido) {
        val itemEstoque = encontrarItemPorCodigo(codigoItem)
        if (itemEstoque == null || itemEstoque.estoque < quantidade) {
            return null // Falha: item não existe ou estoque insuficiente
        }
    }

    // Se todos os itens são válidos, processa o pedido
    for ((codigoItem, quantidade) in itensDoPedido) {
        val itemEstoque = encontrarItemPorCodigo(codigoItem)!!
        itemEstoque.estoque -= quantidade // Diminui o estoque

        val itemParaPedido = itemEstoque.copy(estoque = quantidade) // Cria uma cópia com a quantidade do pedido
        itensVerificados.add(itemParaPedido)
        totalPedido += itemParaPedido.preco * quantidade
    }

    if (aplicarCupom) {
        totalPedido *= 0.90
    }

    val novoPedido = Pedido(
        codigo = contadorPedido++,
        itens = itensVerificados,
        total = totalPedido,
        cupom = aplicarCupom
    )
    pedidosRealizados.add(novoPedido)
    return novoPedido
}

fun encontrarItemPorCodigo(codigo: Int): Item? {
    return itensCadastrados.find { it.codigo == codigo }
}

fun encontrarPedidoPorCodigo(codigo: Int): Pedido? {
    return pedidosRealizados.find { it.codigo == codigo }
}

fun atualizarStatusPedido(pedido: Pedido, novoStatus: StatusPedido) {
    pedido.status = novoStatus
}

fun consultarPedidosPorStatus(status: StatusPedido?): List<Pedido> {
    return if (status == null) {
        pedidosRealizados
    } else {
        pedidosRealizados.filter { it.status == status }
    }
}